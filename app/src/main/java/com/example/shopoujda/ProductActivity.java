package com.example.shopoujda;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shopoujda.database.DatabaseHelper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ProductActivity extends AppCompatActivity {
    GridView gridview;
    ArrayList<Produit> produits = new ArrayList<>();
    SQLiteDatabase db, db2;
    int idCategory;
    String nomCategory;
    ArrayBase arrayBase;
    TextView textCate,scanText;
    static final int REQUEST_IMAGE_PICK = 1;
    private Uri imageUri;
    private ImageView imageViewProduit;

    private Menu menu;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.logo);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }

        Intent intent = getIntent();
        idCategory = intent.getIntExtra("id", 0);
        nomCategory = intent.getStringExtra("nom");
        db = DatabaseHelper.getInstance(this).getReadableDatabase();
        db2 = DatabaseHelper.getInstance(this).getWritableDatabase();
        textCate = findViewById(R.id.textCate);
        textCate.setText(nomCategory);

        try (Cursor cursor = db.rawQuery("select P.*,C.nom as nomC from produits P inner join categories C on C.id=P.categorie_id" +
                " where P.categorie_id=?", new String[]{String.valueOf(idCategory)})) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String nom = cursor.getString(1);
                float prix = cursor.getFloat(2);
                String description = cursor.getString(3);
                String image = cursor.getString(4);
                int categorie_id = cursor.getInt(6);
                String scan = cursor.getString(5);
                String categorie_name = cursor.getString(7);
                Categorie categorie = new Categorie(categorie_id, categorie_name, "");
                Produit produit = new Produit(id, nom, prix, description, image, categorie,scan);
                produits.add(produit);
            }
        }

        gridview = findViewById(R.id.gridview);
        gridview.setOnItemClickListener((parent, view, position, id) -> {
            Produit produit = produits.get(position);
            Intent detailsIntent = new Intent(ProductActivity.this, DetailsActivity.class);
            detailsIntent.putExtra("produit", produit);
            startActivity(detailsIntent);
        });

        gridview.setOnItemLongClickListener((parent, view, position, id) -> {
            Produit produit = produits.get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choisir une action")
                    .setItems(new String[]{"Modifier", "Supprimer"}, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                showModifyCategoryDialog(produit, position);
                                break;
                            case 1:
                                showDeleteCategoryDialog(produit, position);
                                break;
                        }
                    })
                    .show();
            return true;
        });

        arrayBase = new ArrayBase(produits);
        gridview.setAdapter(arrayBase);
    }

    @SuppressLint("MissingInflatedId")
    private void showModifyCategoryDialog(Produit produit, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_product, null);
        builder.setView(dialogView);
        scanText = dialogView.findViewById(R.id.scanText2);

        AlertDialog dialog = builder.create();
        dialog.show();
        ImageView btnScanProduct = dialogView.findViewById(R.id.btn_scan_product2);
        btnScanProduct.setOnClickListener(v -> scanBarcode());
        EditText nomInput = dialogView.findViewById(R.id.edit_product_nom);
        EditText prixInput = dialogView.findViewById(R.id.edit_product_prix);
        EditText descriptionInput = dialogView.findViewById(R.id.edit_produit_description2);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm2);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel2);
        Button btnChangeImage = dialogView.findViewById(R.id.modifierImageProduit2);
        imageViewProduit = dialogView.findViewById(R.id.imageView7);

        if (nomInput != null) {
            nomInput.setText(produit.getNom());
        }
        prixInput.setText("" + produit.getPrix());
        scanText.setText(produit.getScan());
        descriptionInput.setText(produit.getDescription());
        String imagePath = produit.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                imageViewProduit.setImageURI(Uri.fromFile(imgFile));
            }
        }

        btnChangeImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        btnConfirm.setOnClickListener(v -> {
            String nom = nomInput.getText().toString().trim();
            String prix = prixInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String scan = scanText.getText().toString().trim();


            if (!nom.isEmpty() && !prix.isEmpty() && !description.isEmpty()) {
                String updatedImagePath = (imageUri != null) ? saveImageToFile(imageUri) : imagePath; // Utiliser la nouvelle image si elle existe
                db2.execSQL("UPDATE produits SET nom = ?, prix = ?, description = ?, image = ? ,scan=? WHERE id = ?", new String[]{
                        nom, prix, description, updatedImagePath,scan ,String.valueOf(produit.getId())
                });

                produits.get(position).setNom(nom);
                produits.get(position).setPrix(Float.parseFloat(prix));
                produits.get(position).setDescription(description);
                produits.get(position).setImage(updatedImagePath);
                produits.get(position).setScan(scan);

                arrayBase.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void showDeleteCategoryDialog(Produit produit, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete_produit, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnConfirm.setOnClickListener(v -> {
            db2.execSQL("DELETE FROM produits WHERE id = ?", new String[]{String.valueOf(produit.getId())});
            produits.remove(position);
            arrayBase.notifyDataSetChanged();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    @SuppressLint("MissingInflatedId")
    private void showAddProduitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_produit, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
        EditText inputname = dialogView.findViewById(R.id.edit_product_name);
        EditText inputPrix = dialogView.findViewById(R.id.edit_product_price);
        EditText inputDescription = dialogView.findViewById(R.id.edit_product_description);
         scanText = dialogView.findViewById(R.id.scanText);

        Button btnConfirm = dialogView.findViewById(R.id.btn_add_product);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        ImageView btnScanProduct = dialogView.findViewById(R.id.btn_scan_product);
        btnScanProduct.setOnClickListener(v -> scanBarcode());
        Button imageView = dialogView.findViewById(R.id.imageViewProduct);
        imageViewProduit = dialogView.findViewById(R.id.imageView6);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        btnConfirm.setOnClickListener(v -> {
            String nom = inputname.getText().toString().trim();
            String prix = inputPrix.getText().toString().trim();
            String description = inputDescription.getText().toString().trim();
            String scan = scanText.getText().toString().trim();

            if (!nom.isEmpty() && !prix.isEmpty() && !description.isEmpty() && imageUri != null ) {
                String imagePath = saveImageToFile(imageUri);
                db2.execSQL("INSERT INTO produits(nom, prix, description, image, categorie_id,scan) VALUES(?,?,?,?,?,?)", new String[]{
                        nom, prix, description, imagePath, String.valueOf(idCategory),scan
                });
                refreshProduits();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private String saveImageToFile(Uri imageUri) {
        try {
            File file = new File(getExternalFilesDir(null), "product_images");
            if (!file.exists()) {
                file.mkdirs();
            }

            String imagePath = file.getAbsolutePath() + File.separator + "image_" + System.currentTimeMillis() + ".jpg";
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(imagePath);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            return imagePath;
        } catch (IOException e) {
            Log.e("ImageSave", "Error saving image: " + e.getMessage());
            return null;
        }
    }

    private void refreshProduits() {
        Cursor cursor = db.rawQuery("select P.*,C.nom as nomC from produits P inner join categories C on C.id=P.categorie_id   where P.categorie_id=?", new String[]{
                String.valueOf(idCategory)
        });
        produits.clear();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String nom = cursor.getString(1);
            float prix = cursor.getFloat(2);
            String description = cursor.getString(3);
            String image = cursor.getString(4);
            int categorie_id = cursor.getInt(6);
            String scan = cursor.getString(5);
            String categorie_name = cursor.getString(7);
            Categorie categorie = new Categorie(categorie_id, categorie_name, "");
            Produit produit = new Produit(id, nom, prix, description, image, categorie,scan);
            produits.add(produit);
        }
        arrayBase.notifyDataSetChanged();
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String scannedCode = result.getContents();
            scanText.setText(scannedCode);
            return;
        }

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewProduit.setImageURI(imageUri);
        }
    }



    class ArrayBase extends BaseAdapter {
        ArrayList<Produit> produits;

        public ArrayBase(ArrayList<Produit> produits) {
            this.produits = produits;
        }

        @Override
        public int getCount() {
            return produits.size();
        }

        @Override
        public Object getItem(int i) {
            return produits.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View view1 = layoutInflater.inflate(R.layout.item_produit, null);
            TextView nom = view1.findViewById(R.id.textViewNom);
            nom.setText(produits.get(i).getNom());
            TextView prix = view1.findViewById(R.id.textViewPrix);
            prix.setText("Prix :" + produits.get(i).getPrix() + " DH");
            TextView description = view1.findViewById(R.id.textViewDescription);
            description.setText(produits.get(i).getDescription());
            ImageView imageView = view1.findViewById(R.id.imageViewProduit);
            String imagePath = produits.get(i).getImage();
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                imageView.setImageURI(Uri.fromFile(imgFile));
            }
            return view1;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        loadProfileImage();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ajouterMenu) {
            showAddProduitDialog();
            return true;
        }
        if (item.getItemId() == R.id.action_profile) {
            Intent intent = new Intent(this, UserActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_scan) {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadProfileImage() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        String imagePath = sharedPreferences.getString("profile", null);

        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                updateProfileImageInMenu(imagePath);
            }
        }
    }

    private void updateProfileImageInMenu(String imagePath) {
        MenuItem profileMenuItem = menu.findItem(R.id.action_profile);

        if (profileMenuItem != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            if (bitmap != null) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                profileMenuItem.setIcon(drawable);
            }
        }
    }

    private void scanBarcode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a barcode");
        integrator.setOrientationLocked(false);
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

}




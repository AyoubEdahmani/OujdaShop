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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shopoujda.database.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView list_categories;
    SQLiteDatabase db;
    SQLiteDatabase db2;
    ArrayBase arrayBase;
    private ImageView categoryImageView;
   private Menu menu;
    ArrayList<Categorie> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = DatabaseHelper.getInstance(this).getReadableDatabase();
        db2 = DatabaseHelper.getInstance(this).getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from categories", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String nom = cursor.getString(1);
            String image = cursor.getString(2);
            Categorie categories1 = new Categorie(id, nom, image);
            categories.add(categories1);
        }
        list_categories = findViewById(R.id.list_categories);
        arrayBase = new ArrayBase(categories);
        list_categories.setAdapter(arrayBase);
        list_categories.setOnItemClickListener((parent, view, position, id) -> {
            Categorie category = categories.get(position);
            Intent intent = new Intent(MainActivity.this, ProductActivity.class);
            intent.putExtra("id", category.getId());
            intent.putExtra("nom", category.getNom());
            startActivity(intent);
        });

        list_categories.setOnItemLongClickListener((parent, view, position, id) -> {
            Categorie category = categories.get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choisir une action")
                    .setItems(new String[]{"Modifier", "Supprimer"}, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                showModifyCategoryDialog(category, position);
                                break;
                            case 1:
                                showDeleteCategoryDialog(category, position);
                                break;
                        }
                    })
                    .show();
            return true;
        });
    }
    private static final int SCAN_REQUEST_CODE = 100;




    class ArrayBase extends BaseAdapter {
        ArrayList<Categorie> categories;

        public ArrayBase(ArrayList<Categorie> categories) {
            this.categories = categories;
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public Object getItem(int i) {
            return categories.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater layoutInflater = getLayoutInflater();
            @SuppressLint("ViewHolder")
            View view1 = layoutInflater.inflate(R.layout.categories_item, null);

            TextView textView = view1.findViewById(R.id.text_categories);
            textView.setText(categories.get(i).getNom());

            ImageView imageView = view1.findViewById(R.id.image_categories);

            String imagePath = categories.get(i).getImage();

            if (imagePath != null && !imagePath.isEmpty()) {
                File imgFile = new  File(imagePath);
                if(imgFile.exists()){
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                }
            } else {
                int defaultImage = getResources().getIdentifier("categorie", "drawable", getPackageName());
                imageView.setImageResource(defaultImage);
            }

            return view1;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int id = getIntent().getIntExtra("id", 0);
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu=menu;


            loadProfileImage();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ajouterMenu) {
            showAddCategoryDialog();
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

    private static final int PICK_IMAGE_REQUEST = 1;

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        EditText input = dialogView.findViewById(R.id.edit_produit_description);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAddImage = dialogView.findViewById(R.id.addImage);
        categoryImageView = dialogView.findViewById(R.id.imageView4);

        btnAddImage.setOnClickListener(v -> openImageChooser());

        btnConfirm.setOnClickListener(v -> {
            String category = input.getText().toString().trim();
            if (!category.isEmpty()) {
                String imagePath = saveImageToStorage();
                db2.execSQL("INSERT INTO categories(nom, image) VALUES(?, ?)", new String[]{category, imagePath});
                refreshCategories();
                dialog.dismiss();
            } else {
                Toast.makeText(MainActivity.this, "Please enter a category name.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }




    private String saveImageToStorage() {
        if (categoryImageView == null) {
            Log.e("SaveImageError", "categoryImageView is null.");
            return "";
        }

        Drawable drawable = categoryImageView.getDrawable();

        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

            File directory = new File(getFilesDir(), "category_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = "category_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return file.getAbsolutePath();
        } else {
            Log.e("SaveImageError", "The drawable is not a BitmapDrawable.");
            return "";
        }
    }
    @SuppressLint("MissingInflatedId")

    private void showModifyCategoryDialog(Categorie category, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_modify_category, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        EditText input = dialogView.findViewById(R.id.edit_produit_description);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAddImage = dialogView.findViewById(R.id.addImage2);
        categoryImageView = dialogView.findViewById(R.id.imageView5);

        input.setText(category.getNom());
        String imagePath = category.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                categoryImageView.setImageBitmap(bitmap);
            }
        }

        btnAddImage.setOnClickListener(v -> openImageChooserForModification());

        btnConfirm.setOnClickListener(v -> {
            String newCategory = input.getText().toString().trim();
            if (!newCategory.isEmpty()) {
                String updatedImagePath = saveImageToStorage();
                db2.execSQL("UPDATE categories SET nom = ?, image = ? WHERE id = ?",
                        new String[]{newCategory, updatedImagePath, String.valueOf(category.getId())});

                category.setNom(newCategory);
                category.setImage(updatedImagePath);

                categories.set(position, category);
                arrayBase.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void openImageChooserForModification() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                categoryImageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showDeleteCategoryDialog(Categorie category, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete_category, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnConfirm.setOnClickListener(v -> {
            db2.execSQL("DELETE FROM categories WHERE id = ?", new String[]{String.valueOf(category.getId())});
            categories.remove(position);
            arrayBase.notifyDataSetChanged();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void refreshCategories() {
        Cursor cursor = db.rawQuery("select * from categories", null);
        categories.clear();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String nom = cursor.getString(1);
            String image = cursor.getString(2);
            categories.add(new Categorie(id, nom, image));
        }
        arrayBase.notifyDataSetChanged();
    }

    private void loadProfileImage() {
        SharedPreferences sharedPreferences=getSharedPreferences("user",MODE_PRIVATE);
        String imagePath =sharedPreferences.getString("profile",null);

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

}

package com.example.shopoujda;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;



import java.io.File;

public class DetailsActivity extends AppCompatActivity {

    private ImageView imageViewProduit;
    private TextView textViewNom, textViewPrix, textViewDescription, textViewCategorie;
    private RadioGroup radioGroupOptions;
    private CheckBox checkBoxFavorites;

    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        imageViewProduit = findViewById(R.id.imageViewProduit);
        textViewNom = findViewById(R.id.textViewNom);
        textViewPrix = findViewById(R.id.textViewPrix);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewCategorie = findViewById(R.id.textViewCategorie);
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        checkBoxFavorites = findViewById(R.id.checkBoxFavorites);

        Intent intent = getIntent();
        Produit produit = (Produit) intent.getSerializableExtra("produit");

        textViewNom.setText(produit.getNom()+" ("+produit.getScan()+")");
        textViewPrix.setText("Prix: " + produit.getPrix() + " DH");
        textViewDescription.setText(produit.getDescription());
        textViewCategorie.setText("Catégorie: " + produit.getCategorie().getNom());

        String imagePath = produit.getImage();
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            imageViewProduit.setImageURI(Uri.fromFile(imgFile));
        }

        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioSizeSmall) {
            } else if (checkedId == R.id.radioSizeMedium) {
            } else if (checkedId == R.id.radioSizeLarge) {
            }
        });

        checkBoxFavorites.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
            } else {
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu=menu;
        loadProfileImage();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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

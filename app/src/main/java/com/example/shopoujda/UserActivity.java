package com.example.shopoujda;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.shopoujda.database.DatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_IMAGE = 1;

    private ImageView imageViewProfile;
    private EditText editTextNom, editTextPrenom, editTextEmail;
    private Switch switchDarkMode;
    private Button buttonSave, buttonChangeImage,buttonChangePassword;

    private String imagePath;
    private SQLiteDatabase db, db2;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        db = DatabaseHelper.getInstance(this).getReadableDatabase();
        db2 = DatabaseHelper.getInstance(this).getWritableDatabase();
        imageViewProfile = findViewById(R.id.imageViewProfile);
        editTextNom = findViewById(R.id.editTextNom);
        editTextPrenom = findViewById(R.id.editTextPrenom);
        editTextEmail = findViewById(R.id.editTextEmail);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        buttonSave = findViewById(R.id.buttonSave);
        buttonChangeImage = findViewById(R.id.buttonChangeImage);
        buttonChangePassword=findViewById(R.id.buttonChangePassword);
        buttonChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        switchDarkMode.setChecked(isDarkMode);

        loadUserInfo();

        buttonChangeImage.setOnClickListener(v -> openImagePicker());
        buttonSave.setOnClickListener(v -> saveUserInfo());
        Button buttonLogout = findViewById(R.id.buttonLogout);

        buttonLogout.setOnClickListener((e)->{
                SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });


    }
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        EditText inputpassOld = dialogView.findViewById(R.id.editTextOldPassword);
        EditText inputpassNew = dialogView.findViewById(R.id.editTextNewPassword);
        EditText inputpassNewCon = dialogView.findViewById(R.id.editTextConfirmPassword);
        Button btnConfirm = dialogView.findViewById(R.id.btnSaveChangePassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelChangePassword);

        btnConfirm.setOnClickListener(v -> {
            String oldPassword = inputpassOld.getText().toString();
            String newPassword = inputpassNew.getText().toString();
            String confirmPassword = inputpassNewCon.getText().toString();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Tous les champs doivent être remplis", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = db2.rawQuery("SELECT * FROM User WHERE id=?", new String[]{String.valueOf(id)});
            if (cursor != null && cursor.moveToFirst()) {
                String currentPassword = cursor.getString(4);
                        if (!oldPassword.equals(currentPassword)) {
                    Toast.makeText(this, "Ancien mot de passe incorrect", Toast.LENGTH_SHORT).show();
                    cursor.close();
                    return;
                }
            }

            db2.execSQL("UPDATE User SET password=? WHERE id=?", new String[]{newPassword, String.valueOf(id)});

            Toast.makeText(this, "Mot de passe changé avec succès", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void loadUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        id = sharedPreferences.getInt("id", 0);

        Cursor cursor = db.rawQuery("SELECT * FROM User WHERE id=?", new String[]{String.valueOf(id)});
        if (cursor != null && cursor.moveToFirst()) {
            editTextNom.setText(cursor.getString(1));
            editTextPrenom.setText(cursor.getString(2));
            editTextEmail.setText(cursor.getString(3));
            sharedPreferences.edit()
                    .putString("profile",cursor.getString(5))
                    .apply();
            Intent intent=new Intent(this,MainActivity.class);
            imagePath = cursor.getString(5);
            if (imagePath != null) {
                loadProfileImage(imagePath);
            }
        }
        cursor.close();
    }

    private void loadProfileImage(String imagePath) {
        if (imagePath != null) {
            try {
                FileInputStream inputStream = new FileInputStream(new File(imagePath));
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageViewProfile.setImageBitmap(bitmap);
            } catch (IOException e) {
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    saveProfileImage(bitmap);
                } catch (IOException e) {
                }
            }
        }
    }

    private void saveProfileImage(Bitmap bitmap) {
        File directory = new File(getFilesDir(), "profile_pictures");
        if (!directory.exists()) directory.mkdirs();

        File profileImage = new File(directory, "profile_.jpg");
        if (profileImage.exists()) profileImage.delete();

        try (FileOutputStream out = new FileOutputStream(profileImage)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

            String imagePath = profileImage.getAbsolutePath();
            db2.execSQL("UPDATE User SET image=? WHERE id=?", new String[]{imagePath, String.valueOf(id)});

            imageViewProfile.setImageBitmap(bitmap);
        } catch (IOException e) {
        }
    }

    private void saveUserInfo() {
        String nom = editTextNom.getText().toString();
        String prenom = editTextPrenom.getText().toString();
        String email = editTextEmail.getText().toString();

        db2.execSQL("UPDATE User SET nom=?, prenom=?, email=? WHERE id=?",
                new String[]{nom, prenom, email, String.valueOf(id)});
        finish();
    }

}

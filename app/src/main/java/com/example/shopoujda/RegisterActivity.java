package com.example.shopoujda;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shopoujda.database.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {
    EditText editNom,editPrenom,editEmail,editPass,editPassCon;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().hide();
        editNom=findViewById(R.id.editNom);
        editPrenom=findViewById(R.id.editPrenom);
        editEmail=findViewById(R.id.editEmail);
        editPass=findViewById(R.id.editPass);
        editPassCon=findViewById(R.id.editPassCon);
        db= DatabaseHelper.getInstance(this).getReadableDatabase();

    }

    public void inscrire(View view) {
        String nom =editNom.getText().toString();
        String prenom =editPrenom.getText().toString();
        String email =editEmail.getText().toString();
        String pass =editPass.getText().toString();
        String passCon =editPassCon.getText().toString();
        if(nom.isEmpty()|| prenom.isEmpty()||email.isEmpty() || pass.isEmpty()||passCon.isEmpty()){
            Toast.makeText(this, "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show();
        }
        else if(!pass.equals(passCon)){
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
        }
        else {
            db.execSQL("insert into User(nom,prenom,email,password,image)  values(?,?,?,?,?)",new String[]{
                    nom,prenom,email,pass,""
            });
            Intent intent=new Intent(this,LoginActivity.class);
            finish();
            startActivity(intent);
        }
    }
}
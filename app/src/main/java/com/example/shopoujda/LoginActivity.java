package com.example.shopoujda;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

public class LoginActivity extends AppCompatActivity {
    EditText emailInput,passwordInput;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().hide();

        emailInput=findViewById(R.id.emailInput);
        passwordInput=findViewById(R.id.passwordInput);
        db= DatabaseHelper.getInstance(this).getReadableDatabase();
    }


    public void connecter(View view) {
        String email=emailInput.getText().toString();
        String password=passwordInput.getText().toString();
        Cursor cursor=db.rawQuery("select * from User where email=? and password=?",new String[]{
                email,password
        });
        if(cursor.getCount()!=0){
            cursor.moveToFirst();
            SharedPreferences sharedPreferences=getSharedPreferences("user",MODE_PRIVATE);
            sharedPreferences.edit()
                    .putBoolean("islogin",true)
                    .putInt("id",cursor.getInt(0))
                    .putString("profile",cursor.getString(5))
                    .apply();
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
            ;
        }
        else{
            Toast.makeText(this,"email ou mot de pass incorrect",Toast.LENGTH_SHORT).show();
        }

    }

    public void ceerCompte(View view) {
        Intent intent=new Intent(this,RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}
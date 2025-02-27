package com.example.shopoujda;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shopoujda.database.DatabaseHelper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        scanBarcode();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scannedCode = result.getContents();
                SQLiteDatabase db = DatabaseHelper.getInstance(this).getReadableDatabase();
                try (Cursor cursor = db.rawQuery("select P.*,C.nom as nomC from produits P inner join categories C on C.id=P.categorie_id" +
                        " where P.scan=?", new String[]{String.valueOf(scannedCode)})) {
                    cursor.moveToFirst();
                    if(cursor.getCount()!=0){
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

                        Intent detailsIntent = new Intent(this, DetailsActivity.class);
                        detailsIntent.putExtra("produit", produit);
                        startActivity(detailsIntent);
                    }
                    else {
                        Toast.makeText(this, "aucun produit trouve", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
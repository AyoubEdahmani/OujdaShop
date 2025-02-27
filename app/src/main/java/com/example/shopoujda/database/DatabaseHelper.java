package com.example.shopoujda.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper instance;
    public static synchronized DatabaseHelper getInstance(Context context){
        if(instance==null){
            instance= new DatabaseHelper(context);
        }
        return instance;
    }
    public DatabaseHelper(Context context){
        super(context,"shop.db",null,1);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS User(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " nom text ," +
                "prenom text," +
                "email text," +
                "password text," +
                "image text" +
                ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS categories(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " nom text ," +
                "image text" +
                ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS produits(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " nom text ," +
                "prix INTEGER," +
                "description text," +
                "image text," +
                "scan text," +
                "categorie_id integer," +
                "FOREIGN KEY(categorie_id) REFERENCES categories(id)" +
                ")");
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE User");
        onCreate(db);

    }
}

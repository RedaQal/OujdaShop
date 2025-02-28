package com.example.oujdashop.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static DBHelper instance;
    private DBHelper(Context context){
        super(context,"oujdaShop.db",null,1);
    };
    public static synchronized DBHelper getInstance(Context context){
        if (instance == null){
            instance = new DBHelper(context);
        }
        return instance;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT, nom TEXT, prenom TEXT, email TEXT, password TEXT,image TEXT)");
    db.execSQL("CREATE TABLE IF NOT EXISTS categories(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, description TEXT, image TEXT)");
    db.execSQL("CREATE TABLE IF NOT EXISTS products(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, description TEXT, price REAL, image TEXT, category_id INTEGER, barcode TEXT ,FOREIGN KEY(category_id) REFERENCES categories(id))");
    db.execSQL("INSERT INTO categories (name, description, image) VALUES \n" +
            "('Electronics', 'Devices and gadgets', 'electro')," +
            "('Clothing', 'Men and Women fashion', 'clothes')," +
            "('Toys', 'good toys for your children', 'toys');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
    db.execSQL("DROP TABLE IF EXISTS users");
    }
}

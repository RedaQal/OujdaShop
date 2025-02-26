package com.example.oujdashop.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.oujdashop.db.DBHelper;
import com.example.oujdashop.model.User;

public class UserDAO {
    private final DBHelper dbHelper;
    public UserDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }
    public boolean createUser(User user) {
        String insertQuery = "INSERT INTO users (nom, prenom, email, password) VALUES (?, ?, ?, ?)";
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            String[] values = {user.getNom(), user.getPrenom(), user.getEmail(), user.getPassword()};
            db.execSQL(insertQuery, values);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    };
    public User connectUser(String email, String password) {
        String selectQuery = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()){
            String[] values = {email, password};
            Cursor cursor = db.rawQuery(selectQuery, values);
            if (cursor.moveToNext()){
                User user = new User(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
                cursor.close();
                return user;
            }
        }
        return null;
    };
}

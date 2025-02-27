package com.example.oujdashop.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.oujdashop.db.DBHelper;
import com.example.oujdashop.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private final DBHelper dbHelper;
    public CategoryDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }
    public Category getCategory(int id) {
        String selectQuery = "SELECT * FROM categories WHERE id = ?";
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            String[] values = {String.valueOf(id)};
            Cursor cursor = db.rawQuery(selectQuery, values);
            if (cursor.moveToNext()){
                Category category = new Category(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                cursor.close();
                return category;
            }
        }
        return null;
    }
    public boolean createCategory(Category category){
        String insertQuery = "INSERT INTO categories (name, description, image) VALUES (?, ?, ?)";
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()){
            String[] values = {category.getName(), category.getDescription(), category.getImage()};

            // Debug log
            Log.d("CategoryDAO", "Saving category with image: " + category.getImage());

            db.execSQL(insertQuery, values);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateCategory(Category category){
        String updateQuery = "UPDATE categories SET name = ?, description = ?, image = ? WHERE id = ?";
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()){
            String[] values = {category.getName(), category.getDescription(), category.getImage(), String.valueOf(category.getId())};
            db.execSQL(updateQuery, values);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteCategory(int id){
        String deleteQuery = "DELETE FROM categories WHERE id = ?";
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()){
            String[] values = {String.valueOf(id)};
            db.execSQL(deleteQuery, values);
            return true;
            }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        String selectQuery = "SELECT * FROM categories";
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()){
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor != null){
                while (cursor.moveToNext()){
                    Category category = new Category(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                    categories.add(category);
                }
                cursor.close();
            }
        }
    return categories;
    }
}

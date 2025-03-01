package com.example.oujdashop.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.oujdashop.db.DBHelper;
import com.example.oujdashop.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private final DBHelper dbHelper;

    private CategoryDAO categoryDAO;

    public ProductDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
        this.categoryDAO = new CategoryDAO(context);
    }
    public boolean createProduct(Product product) {
        String insertQuery = "INSERT INTO products (name, description, price,image ,category_id) VALUES (?, ?, ?, ?, ?)";
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            String[] values = {product.getName(), product.getDescription(), String.valueOf(product.getPrice()), product.getImage(),String.valueOf(product.getCategory().getId())};
            db.execSQL(insertQuery, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteProduct(int id) {
        String deleteQuery = "DELETE FROM products WHERE id = ?";
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            String[] values = {String.valueOf(id)};
            db.execSQL(deleteQuery, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateProduct(Product product) {
        String updateQuery = "UPDATE products SET name = ?, description = ?, price = ?,image = ? , category_id = ? ,barcode = ? WHERE id = ?";
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            String[] values = {product.getName(), product.getDescription(), String.valueOf(product.getPrice()),product.getImage(),String.valueOf(product.getCategory().getId()),product.getBarcode(), String.valueOf(product.getId())};
            db.execSQL(updateQuery, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Product> getCategoryProducts(int categoryId) {
        String selectQuery = "SELECT * FROM products WHERE category_id = ?";
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(categoryId)});

            List<Product> products = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Product product = new Product(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getFloat(3),cursor.getString(4)
                            ,categoryDAO.getCategory(cursor.getInt(5)),cursor.getString(6));
                    products.add(product);
                }
                cursor.close();
            }
            return products;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Product getProduct(int id) {
        String selectQuery = "SELECT * FROM products WHERE id = ?";
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            String[] values = {String.valueOf(id)};
            Cursor cursor = db.rawQuery(selectQuery, values);
            if (cursor.moveToNext()) {
                Product product = new Product(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                        cursor.getFloat(3),cursor.getString(4),categoryDAO.getCategory(cursor.getInt(5))
                        ,cursor.getString(6));
                cursor.close();
                return product;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public Product findProductByCodeBarre(String codeBarre) {
        String selectQuery = "SELECT * FROM products WHERE barcode = ?";
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            String[] values = {codeBarre};
            Cursor cursor = db.rawQuery(selectQuery, values);
            if (cursor.moveToNext()) {
                Product product = new Product(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                        cursor.getFloat(3),cursor.getString(4),categoryDAO.getCategory(cursor.getInt(5))
                        ,cursor.getString(6));
                cursor.close();
                return product;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

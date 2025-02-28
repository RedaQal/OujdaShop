package com.example.oujdashop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.oujdashop.adapter.ProductAdapter;
import com.example.oujdashop.dao.CategoryDAO;
import com.example.oujdashop.dao.ProductDAO;
import com.example.oujdashop.model.Category;
import com.example.oujdashop.model.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class ProductActivity extends AppCompatActivity {
    private GridView productList;
    private List<Product> products;
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private ProductAdapter productAdapter;
    private FloatingActionButton fab;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION_CODE = 100;

    private Uri selectedImageUri;
    private ImageView dialogImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        categoryDAO = new CategoryDAO(this);
        productDAO = new ProductDAO(this);
        if(getSupportActionBar() != null) {
            Category category = categoryDAO.getCategory(getIntent().getIntExtra("categoryId", 0));
            getSupportActionBar().setTitle(category.getName());
        }
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddProductDialog());
        productList = findViewById(R.id.productList);
        loadProducts();
        productList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    Intent intent = new Intent(ProductActivity.this, DetailsActivity.class);
                    intent.putExtra("productId", products.get(position).getId());
                    startActivity(intent);
                });
        registerForContextMenu(productList);
    }
    private void loadProducts() {
        products = productDAO.getCategoryProducts(getIntent().getIntExtra("categoryId", 0));
        productAdapter = new ProductAdapter(this,products);
        productList.setAdapter(productAdapter);
    }
    @SuppressLint("MissingInflatedId")
    public void showAddProductDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        dialogImageView = dialogView.findViewById(R.id.imageViewProductImage);
        EditText productNameEditText = dialogView.findViewById(R.id.editTextName);
        EditText productDescriptionEditText = dialogView.findViewById(R.id.editTextDescription);
        EditText productPriceEditText = dialogView.findViewById(R.id.editTextPrice);
        dialogImageView.setOnClickListener(v -> checkAndRequestPermissions());
        new AlertDialog.Builder(this)
                .setTitle("Ajouter produit")
                .setView(dialogView)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String productName = productNameEditText.getText().toString();
                    String productDescription = productDescriptionEditText.getText().toString();
                    float productPrice = Float.parseFloat(productPriceEditText.getText().toString());
                    if (productName.isEmpty() || productDescription.isEmpty() || productPrice <= 0) {
                        Toast.makeText(ProductActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (selectedImageUri == null) {
                        Toast.makeText(ProductActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String imagePath = saveImageToInternalStorage(selectedImageUri);
                    if (imagePath == null) {
                        Toast.makeText(ProductActivity.this, "Failed to save image", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Category category = categoryDAO.getCategory(getIntent().getIntExtra("categoryId", 0));
                    Product product = new Product(productName, productDescription, productPrice, imagePath,category);
                    productDAO.createProduct(product);
                    loadProducts();
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer produit")
                .setMessage("Etes-vous sur de vouloir supprimer " + products.get(position).getName() + "?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    // Delete the image file
                    String imagePath = products.get(position).getImage();
                    if (imagePath != null && !imagePath.isEmpty()) {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            imageFile.delete();
                        }
                    }
                    // Delete the product from the database
                    productDAO.deleteProduct(products.get(position).getId());
                    loadProducts();
                    Toast.makeText(this, "prduit supprime", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("annuler", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void showUpdateProductDialog(int position) {
        // Get the product to update
        Product productToUpdate = products.get(position);
        // Create the dialog view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        EditText productNameEditText = dialogView.findViewById(R.id.editTextName);
        EditText productDescriptionEditText = dialogView.findViewById(R.id.editTextDescription);
        EditText productPriceEditText = dialogView.findViewById(R.id.editTextPrice);
        dialogImageView = dialogView.findViewById(R.id.imageViewProductImage);
        // Pre-fill the fields with the current product details
        productNameEditText.setText(productToUpdate.getName());
        productDescriptionEditText.setText(productToUpdate.getDescription());
        productPriceEditText.setText(String.valueOf(productToUpdate.getPrice()));
        // Load the current image (if it exists)
        String imagePath = productToUpdate.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                dialogImageView.setImageURI(Uri.fromFile(imageFile));
                selectedImageUri = Uri.fromFile(imageFile);
            }
        }
        dialogImageView.setOnClickListener(v -> checkAndRequestPermissions());
        new AlertDialog.Builder(this)
                .setTitle("Modifier produit")
                .setView(dialogView)
                .setPositiveButton("Modifier", (dialog, which) -> {
                    String productName = productNameEditText.getText().toString();
                    String productDescription = productDescriptionEditText.getText().toString();
                    float productPrice = Float.parseFloat(productPriceEditText.getText().toString());
                    if (productName.isEmpty() || productDescription.isEmpty() || productPrice <= 0) {
                        Toast.makeText(ProductActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (selectedImageUri == null) {
                        Toast.makeText(ProductActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String NewimagePath = saveImageToInternalStorage(selectedImageUri);
                    if (NewimagePath == null) {
                        Toast.makeText(ProductActivity.this, "Failed to save image", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    productToUpdate.setName(productName);
                    productToUpdate.setDescription(productDescription);
                    productToUpdate.setPrice(productPrice);
                    productToUpdate.setImage(NewimagePath);
                    productDAO.updateProduct(productToUpdate);
                    loadProducts();
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            // Open an input stream from the URI
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(this, "Failed to open image stream", Toast.LENGTH_SHORT).show();
                return null;
            }

            // Create a directory for your app's images (if it doesn't exist)
            File directory = new File(getFilesDir(), "product_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate a unique file name
            String fileName = "product_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);

            // Write the image to the file
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

            // Return the file path
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST && data != null) {
            selectedImageUri = data.getData();
            if (dialogImageView != null) {
                dialogImageView.setImageURI(selectedImageUri);
            }
        }
    }
    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION_CODE);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
            } else {
                openGallery();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, "Modifier");
        menu.add(0, v.getId(), 1, "Supprimer");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        if (item.getTitle().equals("Modifier")) {
            showUpdateProductDialog(position);
        } else if (item.getTitle().equals("Supprimer")) {
            showDeleteConfirmationDialog(position);
        }
        return super.onContextItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            SharedPreferences sharedPreferences = getSharedPreferences("oujdaShop", MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
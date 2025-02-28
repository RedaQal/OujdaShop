package com.example.oujdashop;

import android.Manifest;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.oujdashop.adapter.CategoryAdapter;
import com.example.oujdashop.dao.CategoryDAO;
import com.example.oujdashop.model.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView categoryList;
    private List<Category> categories;
    private CategoryDAO categoryDAO;
    private CategoryAdapter categoryAdapter;
    private FloatingActionButton fab;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION_CODE = 100;

    private Uri selectedImageUri;
    private ImageView dialogImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Catégories");
        }

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddCategoryDialog());

        categoryDAO = new CategoryDAO(this);
        categoryList = findViewById(R.id.categoryList);
        loadCategories();
        registerForContextMenu(categoryList);
        categoryList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    Intent intent = new Intent(MainActivity.this, ProductActivity.class);
                    intent.putExtra("categoryId", categories.get(position).getId());
                    startActivity(intent);
                }
        );
    }

    private void loadCategories() {
        categories = categoryDAO.getCategories();
        categoryAdapter = new CategoryAdapter(getApplicationContext(), categories);
        categoryList.setAdapter(categoryAdapter);
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
            showUpdateCategoryDialog(position);
        } else if (item.getTitle().equals("Supprimer")) {
            showDeleteConfirmationDialog(position);
        }
        return super.onContextItemSelected(item);
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer la catégorie")
                .setMessage("Etes-vous sûr de vouloir supprimer " + categories.get(position).getName() + "?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    // Delete the image file
                    String imagePath = categories.get(position).getImage();
                    if (imagePath != null && !imagePath.isEmpty()) {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            imageFile.delete();
                        }
                    }
                    // Delete the category from the database
                    categoryDAO.deleteCategory(categories.get(position).getId());
                    loadCategories();
                    Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .show();
    }
private void showUpdateCategoryDialog(int position) {
        // Get the category to update
        Category categoryToUpdate = categories.get(position);
        // Create the dialog view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText nameEditText = dialogView.findViewById(R.id.editTextName);
        EditText descriptionEditText = dialogView.findViewById(R.id.editTextDescription);
        dialogImageView = dialogView.findViewById(R.id.imageViewCategoryImage);
        // Pre-fill the fields with the current category details
        nameEditText.setText(categoryToUpdate.getName());
        descriptionEditText.setText(categoryToUpdate.getDescription());
        // Load the current image (if it exists)
        String imagePath = categoryToUpdate.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                dialogImageView.setImageURI(Uri.fromFile(imageFile));
            }
        }
        // Handle image selection
        dialogImageView.setOnClickListener(v -> checkAndRequestPermissions());
        // Create the dialog
        new AlertDialog.Builder(this)
                .setTitle("Modifier Categorie")
                .setView(dialogView)
                .setPositiveButton("Modifier", (dialog, which) -> {
                    String updatedName = nameEditText.getText().toString();
                    String updatedDescription = descriptionEditText.getText().toString();
                    if (updatedName.isEmpty() || updatedDescription.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Save the new image (if selected) or retain the current one
                    String updatedImagePath = selectedImageUri != null ? saveImageToInternalStorage(selectedImageUri) : categoryToUpdate.getImage();
                    if (updatedImagePath == null) {
                        Toast.makeText(MainActivity.this, "Failed to save image", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Update the category object
                    categoryToUpdate.setName(updatedName);
                    categoryToUpdate.setDescription(updatedDescription);
                    categoryToUpdate.setImage(updatedImagePath);
                    // Save the updated category
                    categoryDAO.updateCategory(categoryToUpdate);
                    loadCategories();
                    Toast.makeText(MainActivity.this, "Category updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void showAddCategoryDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText nameEditText = dialogView.findViewById(R.id.editTextName);
        EditText descriptionEditText = dialogView.findViewById(R.id.editTextDescription);
        dialogImageView = dialogView.findViewById(R.id.imageViewCategoryImage);
        dialogImageView.setOnClickListener(v -> checkAndRequestPermissions());
        new AlertDialog.Builder(this)
                .setTitle("Ajouter Categorie")
                .setView(dialogView)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String categoryName = nameEditText.getText().toString();
                    String categoryDescription = descriptionEditText.getText().toString();

                    if (categoryName.isEmpty() || categoryDescription.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Save the image to internal storage and get the file path
                    String imagePath = selectedImageUri != null ? saveImageToInternalStorage(selectedImageUri) : null;
                    // Create the category with the image file path
                    Category newCategory = new Category(categoryName, categoryDescription, imagePath);
                    boolean iscreated = categoryDAO.createCategory(newCategory);

                    if (!iscreated) {
                        Toast.makeText(MainActivity.this, "Failed to add category", Toast.LENGTH_SHORT).show();
                    } else {
                        loadCategories();
                        Toast.makeText(MainActivity.this, "Category added", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(this, "Failed to open image stream", Toast.LENGTH_SHORT).show();
                return null;
            }

            File directory = new File(getFilesDir(), "category_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = "category_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION_CODE);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
            } else {
                openGallery();
            }
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
        } else if (item.getItemId() == R.id.profile) {
            startActivity(new Intent(this, UserActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

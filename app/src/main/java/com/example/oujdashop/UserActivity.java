package com.example.oujdashop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.oujdashop.dao.UserDAO;
import com.example.oujdashop.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class UserActivity extends AppCompatActivity {
    private UserDAO userDAO;
    private TextView userName, userEmail;
    private EditText mdpActual, mdpNouveau, mdpConfirmer;
    private Button modifierMdpBtn, modifierImageBtn;
    private ImageView userImage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION_CODE = 100;
    private Uri selectedImageUri;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profil");
        }

        userDAO = new UserDAO(this);
        int userId = getSharedPreferences("oujdaShop", MODE_PRIVATE).getInt("userId", 0);
        user = userDAO.getUser(userId);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userImage = findViewById(R.id.userImage);
        mdpActual = findViewById(R.id.mdpActuel);
        mdpNouveau = findViewById(R.id.mdpNouveau);
        mdpConfirmer = findViewById(R.id.mdpConfirmer);
        modifierMdpBtn = findViewById(R.id.modifierMdpBtn);
        modifierImageBtn = findViewById(R.id.modifierImageBtn);

        userName.setText(user.getPrenom() + " " + user.getNom());
        userEmail.setText(user.getEmail());

        if (user.getImage() != null) {
            userImage.setImageURI(Uri.parse(user.getImage()));
        }

        userImage.setOnClickListener(v -> checkAndRequestPermissions());

        modifierMdpBtn.setOnClickListener(v -> updatePassword());

        modifierImageBtn.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                String imagePath = saveImageToInternalStorage(selectedImageUri);
                if (imagePath != null) {
                    user.setImage(imagePath);
                    if (userDAO.updateUser(user)) {
                        Toast.makeText(this, "Image mise à jour avec succès", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erreur lors de la mise à jour de l'image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Échec de l'enregistrement de l'image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Veuillez sélectionner une image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePassword() {
        String currentPassword = mdpActual.getText().toString();
        String newPassword = mdpNouveau.getText().toString();
        String confirmPassword = mdpConfirmer.getText().toString();

        if (currentPassword.equals(user.getPassword())) {
            if (newPassword.equals(confirmPassword)) {
                user.setPassword(newPassword);
                userDAO.updateUser(user);
                Toast.makeText(this, "Mot de passe modifié avec succès", Toast.LENGTH_SHORT).show();
                mdpActual.setText("");
                mdpNouveau.setText("");
                mdpConfirmer.setText("");
            } else {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Mot de passe actuel incorrect", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }

            File directory = new File(getFilesDir(), "user_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = "user_" + System.currentTimeMillis() + ".jpg";
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
            userImage.setImageURI(selectedImageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
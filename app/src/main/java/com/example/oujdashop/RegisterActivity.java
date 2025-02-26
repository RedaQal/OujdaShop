package com.example.oujdashop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.oujdashop.dao.UserDAO;
import com.example.oujdashop.model.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText nom;
    private EditText prenom;
    private EditText email;
    private EditText password;
    private EditText confimPassword;
    private Button createAccountBtn;
    private Button loginBtn;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        userDAO = new UserDAO(this);
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confimPassword = findViewById(R.id.confimPassword);
        loginBtn = findViewById(R.id.loginBtn);
        createAccountBtn = findViewById(R.id.createAccountBtn);
        loginBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        createAccountBtn.setOnClickListener(v -> {
            if (nom.getText().toString().isEmpty() || prenom.getText().toString().isEmpty()
                    || email.getText().toString().isEmpty() || password.getText().toString().isEmpty()
                    || confimPassword.getText().toString().isEmpty()){
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.getText().toString().equals(confimPassword.getText().toString())){
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!userDAO.createUser(new User(nom.getText().toString(), prenom.getText().toString(), email.getText().toString(), password.getText().toString()))){
                Toast.makeText(this, "Erreur lors de la création du compte", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Compte créé avec succès", Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPreferences = getSharedPreferences("oujdaShop", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("userId", userDAO.connectUser(email.getText().toString(), password.getText().toString()).getId());
            editor.apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

    }
}
package com.example.oujdashop;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.oujdashop.dao.UserDAO;
import com.example.oujdashop.model.User;

public class UserActivity extends AppCompatActivity {
    private UserDAO userDAO = new UserDAO(this);
    private TextView userName, userEmail;
    private EditText mdpActual, mdpNouveau, mdpConfirmer;
    private Button modifierMdpBtn;
    private User user;
    @SuppressLint("MissingInflatedId")
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
        user = userDAO.getUser(getSharedPreferences("oujdaShop", MODE_PRIVATE).getInt("userId", 0));
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        mdpActual = findViewById(R.id.mdpActuel);
        mdpNouveau = findViewById(R.id.mdpNouveau);
        mdpConfirmer = findViewById(R.id.mdpConfirmer);
        modifierMdpBtn = findViewById(R.id.modifierMdpBtn);
        userName.setText(user.getPrenom() + " " + user.getNom());
        userEmail.setText(user.getEmail());
        modifierMdpBtn.setOnClickListener(v -> {
            if (mdpActual.getText().toString().equals(user.getPassword())) {
                if (mdpNouveau.getText().toString().equals(mdpConfirmer.getText().toString())) {
                    user.setPassword(mdpNouveau.getText().toString());
                    userDAO.updateUser(user);
                    Toast.makeText(this, "Mot de passe modifié avec succès", Toast.LENGTH_SHORT).show();
                    mdpActual.setText("");
                    mdpNouveau.setText("");
                    mdpConfirmer.setText("");
                } else {
                    Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Mot de passe actuel incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
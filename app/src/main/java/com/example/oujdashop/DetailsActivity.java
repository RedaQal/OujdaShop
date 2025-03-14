package com.example.oujdashop;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.oujdashop.dao.ProductDAO;
import com.example.oujdashop.model.Product;

public class DetailsActivity extends AppCompatActivity {
    private Product product;
    private ImageView productImage;
    private TextView productName, productPrice, productDetails, productCategory;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        product = new ProductDAO(this).getProduct(getIntent().getIntExtra("productId",0));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(product.getName());
        }
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDetails = findViewById(R.id.productDetails);
        productCategory = findViewById(R.id.productCategory);
        productImage = findViewById(R.id.productImage);
        productName.setText(product.getName());
        productPrice.setText(String.valueOf(product.getPrice())+" DH");
        productDetails.setText(product.getDescription());
        productCategory.setText(product.getCategory().getName());
        productImage.setImageURI(Uri.parse(product.getImage()));
    }
}
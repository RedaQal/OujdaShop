package com.example.oujdashop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.oujdashop.dao.ProductDAO;
import com.example.oujdashop.model.Product;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeScannerActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView barcodeResult;
    private ExecutorService cameraExecutor;
    private ImageAnalysis imageAnalysis;
    private boolean isScanning = true; // Drapeau pour contrôler la numérisation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        previewView = findViewById(R.id.previewView);
        barcodeResult = findViewById(R.id.barcodeResult);

        // Initialisation de CameraX
        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Lier la caméra au cycle de vie
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("BarcodeScanner", "Erreur d'initialisation de la caméra : " + e.getMessage());
                Toast.makeText(this, "Erreur d'initialisation de la caméra", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));

        // Initialisation de l'exécuteur de la caméra
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        // Configuration de l'aperçu de la caméra
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Configuration de l'analyse d'image (numérisation de code-barres)
        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            if (!isScanning) {
                imageProxy.close(); // Fermer le proxy d'image si la numérisation est en pause
                return;
            }

            @SuppressLint("UnsafeOptInUsageError")
            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees()
            );

            // Initialisation du lecteur de code-barres
            BarcodeScanner scanner = BarcodeScanning.getClient();
            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null) {
                                // Mettre en pause la numérisation
                                isScanning = false;

                                // Mettre à jour l'interface utilisateur avec le code-barres scanné
                                runOnUiThread(() -> barcodeResult.setText("Scanné : " + rawValue));

                                // Vérifier la base de données
                                ProductDAO productDAO = new ProductDAO(BarcodeScannerActivity.this);
                                Product product = productDAO.findProductByCodeBarre(rawValue);
                                if (product != null) {
                                    // Produit trouvé, naviguer vers l'activité DetailsActivity
                                    Intent intent = new Intent(BarcodeScannerActivity.this, DetailsActivity.class);
                                    intent.putExtra("productId", product.getId());
                                    startActivity(intent);
                                    finish(); // Fermer l'activité du lecteur de code-barres
                                } else {
                                    // Produit non trouvé, afficher un toast et reprendre la numérisation
                                    runOnUiThread(() -> Toast.makeText(BarcodeScannerActivity.this, "Produit non trouvé", Toast.LENGTH_SHORT).show());

                                    // Reprendre la numérisation après un délai
                                    new Handler().postDelayed(() -> {
                                        isScanning = true; // Reprendre la numérisation
                                    }, 2000); // Délai de 2 secondes avant de reprendre
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BarcodeScanner", "Erreur lors de la numérisation du code-barres : " + e.getMessage());
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        });

        // Sélectionner la caméra arrière
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Lier la caméra au cycle de vie
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(
                (LifecycleOwner) this, cameraSelector, preview, imageAnalysis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Arrêter l'exécuteur de la caméra
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
package com.example.oujdashop.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.oujdashop.R;
import com.example.oujdashop.model.Product;

import java.io.File;
import java.util.List;

public class ProductAdapter extends BaseAdapter {
    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.product_grid_item, parent, false);
        }

        // Get the current product
        Product product = productList.get(position);

        // Bind data to the views
        ImageView productImage = convertView.findViewById(R.id.product_image);
        TextView productName = convertView.findViewById(R.id.product_name);

        productName.setText(product.getName());

        // Load the image (you can use Glide, Picasso, or BitmapFactory)
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            // Load image from file path or URI
            File imageFile = new File(product.getImage());
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                productImage.setImageBitmap(bitmap);
            } else {
                productImage.setImageResource(R.drawable.full_logo); // Default image if file doesn't exist
            }
        } else {
            productImage.setImageResource(R.drawable.full_logo); // Default image if no path is available
        }

        return convertView;
    }
}
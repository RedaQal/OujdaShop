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
import com.example.oujdashop.model.Category;

import java.io.File;
import java.util.List;

public class CategoryAdapter extends BaseAdapter {
    private Context context;
    private List<Category> categories;
    private LayoutInflater inflater;

    public CategoryAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.category_list_item, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.category_image);
        TextView name = convertView.findViewById(R.id.category_name);

        Category category = categories.get(position);

        name.setText(category.getName());

        // Retrieve the image path (file path in internal storage)
        String imagePath = category.getImage();

        if (imagePath != null && !imagePath.isEmpty()) {
            // Check if the file exists
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                // Load the image from the file path using BitmapFactory
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                icon.setImageBitmap(bitmap);
            } else {
                // If the file doesn't exist, use the default image
                icon.setImageResource(R.drawable.full_logo);
            }
        } else {
            // If there's no image path, use the default image
            icon.setImageResource(R.drawable.full_logo);
        }

        return convertView;
    }

}

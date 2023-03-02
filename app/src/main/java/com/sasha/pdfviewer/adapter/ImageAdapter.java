package com.sasha.pdfviewer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.model.PdfModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ImageModel> imageModels;


    public ImageAdapter(Context context, ArrayList<ImageModel> imageModels) {
        this.context = context;
        this.imageModels = imageModels;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.choose_imageview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageModel imageModel = imageModels.get(position);

        Picasso.get().load(imageModel.getUri()).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
        }
    }

}

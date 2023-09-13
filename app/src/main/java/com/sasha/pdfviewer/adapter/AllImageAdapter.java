package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.view.AllImageActivity;
import com.sasha.pdfviewer.view.AllImageViewActivity;


import java.util.ArrayList;

public class AllImageAdapter extends RecyclerView.Adapter<AllImageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ImageModel> imageModels;


    public AllImageAdapter(Context context, ArrayList<ImageModel> imageModels) {
        this.context = context;
        this.imageModels = imageModels;

    }



    @NonNull
    @Override
    public AllImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_image_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllImageAdapter.ViewHolder holder, int position) {

        ImageModel imageModel = imageModels.get(position);

        holder.folderName.setText("All Images");

        int countImage = imageModels.size();
        holder.noOfImage.setText(String.valueOf(countImage)+ " Images");

        Glide.with(context).load(imageModel.getImagePath()).override(800, 800)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(holder.imageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AllImageViewActivity.class);
                intent.putExtra("images", "images");
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (imageModels.size() > 1){
            return 1;
        }
        else{
            return 0;
        }


    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView folderName, noOfImage;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            folderName = itemView.findViewById(R.id.folderName);
            noOfImage = itemView.findViewById(R.id.noOfImages);

        }

    }

    public interface OnImageClickListener{
        void onImageClick(ImageModel imageModel, int position);
    }

}

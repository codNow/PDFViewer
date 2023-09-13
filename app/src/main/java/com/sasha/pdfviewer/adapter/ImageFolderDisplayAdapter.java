package com.sasha.pdfviewer.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.ImageModel;

import com.sasha.pdfviewer.view.AllImageViewActivity;


import java.io.File;
import java.util.ArrayList;

public class ImageFolderDisplayAdapter extends RecyclerView.Adapter<ImageFolderDisplayAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> folderList;
    private ArrayList<ImageModel> imageModels;

    public ImageFolderDisplayAdapter(Context context, ArrayList<String> folderList, ArrayList<ImageModel> imageModels) {
        this.context = context;
        this.folderList = folderList;
        this.imageModels = imageModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_folder_display_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ImageModel imageModel = imageModels.get(position);
        //Picasso.get().load(imageModel.getUri()).resize(800, 800).into(holder.imageView);

        Glide.with(context).load(imageModel.getImagePath())
                .override(800,800)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);
        File file = new File(imageModel.getImagePath());
        String parentPath = file.getParentFile().getName();

        //holder.textView.setText(parentPath);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AllImageViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("myFolder", parentPath);
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        if (!imageModels.isEmpty()){
            return imageModels.size();
        }
        else {
            return folderList.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private RecyclerView recyclerView;
        private ImageView imageView;
        private TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.folderName);

        }
    }
}

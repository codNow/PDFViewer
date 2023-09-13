package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.utils.ImageUtils;



import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ImageFolderAdapter extends RecyclerView.Adapter<ImageFolderAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> folderList;
    public ArrayList<ImageModel> imageModels;


    public ImageFolderAdapter(Context context, ArrayList<String> folderList, ArrayList<ImageModel> imageModels) {
        this.context = context;
        this.folderList = folderList;
        this.imageModels = imageModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_folder_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int index = folderList.get(position).lastIndexOf("/");
        String myFolder = folderList.get(position);

        String folderName = folderList.get(position).substring(index+1);

        holder.folderName.setText(folderName);

        if (counFiles(folderList.get(position)) > 1){
            holder.noOfFile.setText(String.valueOf(counFiles(folderList.get(position))) + " Images");

        }
        else{
            holder.noOfFile.setText(String.valueOf(counFiles(folderList.get(position))) + " Image");
        }


       holder.recyclerView.setHasFixedSize(true);
       ArrayList<ImageModel> imageList = ImageUtils.allImageFile(context, folderName);
       holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
       ImageFolderDisplayAdapter imageAdapter = new ImageFolderDisplayAdapter(context, folderList, imageList);
       holder.recyclerView.setAdapter(imageAdapter);



    }


    @Override
    public int getItemCount() {
        if (!folderList.isEmpty()){
            return folderList.size();
        }
        else{
            return imageModels.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView folderName, noOfFile;
        private RecyclerView recyclerView;
        private RelativeLayout cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.folder_image);
            folderName = itemView.findViewById(R.id.folderName);
            noOfFile = itemView.findViewById(R.id.noOfImages);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            cardView = itemView.findViewById(R.id.parentRelative);

        }
    }
    int counFiles(String folders){
        int count = 0;

        for (ImageModel pdfModel : imageModels){
            if (pdfModel.getImagePath().substring(0,
                            pdfModel.getImagePath().lastIndexOf("/"))
                    .endsWith(folders)){
                count++;
            }


        }
        return count;
    }
}

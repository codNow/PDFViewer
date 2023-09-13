package com.sasha.pdfviewer.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.utils.ImageUtils;
import com.sasha.pdfviewer.view.AllImageViewActivity;

import java.io.File;
import java.util.ArrayList;

public class MyImageFolderAdapter extends RecyclerView.Adapter<MyImageFolderAdapter.ViewHolder> {

    private Context context;
    private ArrayList<File> fileArrayList;

    public MyImageFolderAdapter(Context context, ArrayList<File> fileArrayList) {
        this.context = context;
        this.fileArrayList = fileArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_folder_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        File file = fileArrayList.get(position);
        holder.folderName.setText(file.getName());
        holder.noOfFile.setText(getPDFCount(file) + " Images");
        String folder = file.getName();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AllImageViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("myFolder", folder);
                context.startActivity(intent);
            }
        });

        ArrayList<String> folderList = new ArrayList<>();
        holder.recyclerView.setHasFixedSize(true);
        ArrayList<ImageModel> imageList = ImageUtils.allImageFile(context, folder);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        ImageFolderDisplayAdapter imageAdapter = new ImageFolderDisplayAdapter(context, folderList, imageList);
        holder.recyclerView.setAdapter(imageAdapter);
    }

    @Override
    public int getItemCount() {
        return fileArrayList.size();
    }
    private int getPDFCount(File folder) {
        int count = 0;
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".jpg")) {
                count++;
            }
        }
        return count;
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
}

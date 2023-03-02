package com.sasha.pdfviewer.adapter;

import android.content.Context;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class ImagePdfAdapter extends RecyclerView.Adapter<ImagePdfAdapter.ViewHolder> {
    private ArrayList<String> imageUrls;
    private OnImageClickListener imageClickListener;

    public ImagePdfAdapter(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setImageClickListener(OnImageClickListener imageClickListener){
        this.imageClickListener = imageClickListener;
    }

    @NonNull
    @Override
    public ImagePdfAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_imageview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagePdfAdapter.ViewHolder holder, int position) {
        String chosenImages = imageUrls.get(position);

        Picasso.get().load(chosenImages).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageClickListener != null){
                    imageClickListener.onImageClick(chosenImages);
                }
            }
        });



    }
    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
    public interface OnImageClickListener{
        void onImageClick(String imageUrl);
    }
}

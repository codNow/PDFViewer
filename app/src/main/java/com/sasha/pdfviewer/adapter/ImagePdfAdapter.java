package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.ImageModel;


import java.io.File;
import java.util.ArrayList;

public class ImagePdfAdapter extends RecyclerView.Adapter<ImagePdfAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ImageModel> imageModels;
    OnImageClickListener onImageClickListener;


    public ImagePdfAdapter(Context context, ArrayList<ImageModel> imageModels,
                           OnImageClickListener onImageClickListener
                          ) {
        this.context = context;
        this.imageModels = imageModels;
        this.onImageClickListener = onImageClickListener;

    }

    @NonNull
    @Override
    public ImagePdfAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.choose_imageview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagePdfAdapter.ViewHolder holder, int position) {
        ImageModel imageModel = imageModels.get(position);


        //Picasso.get().load(imageModel.getUri()).resize(500, 500).into(holder.imageView);
        Glide.with(context).load(imageModel.getUri())
                .override(800, 800)
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                imageModel.setSelected(!imageModel.isSelected());
                holder.checkboxImage.setVisibility(View.VISIBLE);
                holder.cardView.setVisibility(View.VISIBLE);

                if (imageModel.isSelected()){
                    imageModel.setSelected(true);
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    holder.cardView.setVisibility(View.VISIBLE);

                    if (!imageModels.contains(imageModel)){
                        imageModels.add(imageModel);
                    }
                }
                else{
                    holder.checkboxImage.setVisibility(View.GONE);
                    holder.cardView.setVisibility(View.GONE);

                }
                if (onImageClickListener != null){
                    onImageClickListener.onImageClick(imageModel, holder.getAdapterPosition());
                }
            }
        });
        if (imageModel.isSelected()){
            holder.checkboxImage.setVisibility(View.VISIBLE);
            holder.cardView.setVisibility(View.VISIBLE);


        }
        else{
            holder.checkboxImage.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.GONE);

        }


    }
    @Override
    public int getItemCount() {
        return imageModels.size();
    }
    public ArrayList<ImageModel> getSelectImages() {
        ArrayList<ImageModel> selectedModels = new ArrayList<>();
        for (ImageModel model : imageModels) {
            if (model.isSelected()) {
                selectedModels.add(model);
            }
        }
        return selectedModels;
    }
    public int getSelectedPosition() {
        for (int i = 0; i < imageModels.size(); i++) {
            ImageModel imageModel = imageModels.get(i);
            if (imageModel.isSelected()) {
                return i;
            }
        }
        return -1;
    }
    public void removedSelectedImages(ArrayList<ImageModel> filesToDelete) {

        for (ImageModel imageFile : filesToDelete) {
            if (imageFile.isSelected()){
                imageModels.remove(imageFile);

                notifyDataSetChanged();
            }

        }

    }
    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
    public void updateImageModelBitmap(int position, Bitmap rotatedBitmap) {
        ImageModel imageModel = imageModels.get(position);
        imageModel.setBitmap(rotatedBitmap);
        notifyItemChanged(position);
    }


    public ImageModel getImageModel(int position) {
        if (position < 0 || position >= imageModels.size()) {
            return null;
        }
        return imageModels.get(position);
    }
    public void removedSelectedImageFiles(ArrayList<ImageModel> filesToDelete) {
        ArrayList<Integer> positionsToRemove = new ArrayList<>();

        for (int i = 0; i < imageModels.size(); i++) {
            ImageModel imageFile = imageModels.get(i);

            if (imageFile.isSelected()) {
                positionsToRemove.add(i);
            }
        }

        // Remove selected items from both lists in reverse order
        for (int i = positionsToRemove.size() - 1; i >= 0; i--) {
            int position = positionsToRemove.get(i);

            filesToDelete.remove(imageModels.get(position));
            imageModels.remove(position);

            notifyItemRemoved(position);
            //notifyItemRangeChanged(position, getItemCount() - position);
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView, checkboxImage;

        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            checkboxImage = itemView.findViewById(R.id.image_checkbox);
            cardView = itemView.findViewById(R.id.choose_card);



        }
    }
    public interface OnImageClickListener{
        void onImageClick(ImageModel imageModel, int position);
    }


}

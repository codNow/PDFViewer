package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.model.PdfModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ImageModel> imageModels;
    OnImageClickListener onImageClickListener;


    public ImageAdapter(Context context, ArrayList<ImageModel> imageModels,
                        OnImageClickListener onImageClickListener) {
        this.context = context;
        this.imageModels = imageModels;
        this.onImageClickListener = onImageClickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.choose_imageview, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageModel imageModel = imageModels.get(position);

        Picasso.get().load(imageModel.getUri()).into(holder.imageView);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                imageModel.setSelected(!imageModel.isSelected());
                holder.checkboxImage.setVisibility(View.VISIBLE);
                holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
                if (imageModel.isSelected()){
                    imageModel.setSelected(true);
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
                    if (!imageModels.contains(imageModel)){
                        imageModels.add(imageModel);
                    }
                }
                else{
                    holder.checkboxImage.setVisibility(View.GONE);
                    holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.primaryDark));
                }
                if (onImageClickListener != null){
                    onImageClickListener.onImageClick(imageModel, holder.getAdapterPosition());
                }
            }
        });
        if (imageModel.isSelected()){
            holder.checkboxImage.setVisibility(View.VISIBLE);
            holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));

        }
        else{
            holder.checkboxImage.setVisibility(View.GONE);
            holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.primaryDark));
        }

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

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView, checkboxImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            checkboxImage = itemView.findViewById(R.id.image_checkbox);
        }
    }
    public interface OnImageClickListener{
        void onImageClick(ImageModel imageModel, int position);
    }

}

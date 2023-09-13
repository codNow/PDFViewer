package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.folderList.FullScreenDisplayActivity;
import com.sasha.pdfviewer.model.ImageModel;


import java.util.ArrayList;

public class ImageExtractAdapter extends RecyclerView.Adapter<ImageExtractAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ImageModel> imageModels;
    private OnImageLongClick onImageLongClick;
    private OnImageClick onImageClick;
    public boolean selectMode = false;

    public ImageExtractAdapter(Context context, ArrayList<ImageModel> imageModels,
                               OnImageLongClick onImageLongClick, OnImageClick onImageClick) {
        this.context = context;
        this.imageModels = imageModels;
        this.onImageLongClick = onImageLongClick;
        this.onImageClick = onImageClick;
    }



    @SuppressLint("NotifyDataSetChanged")
    public void enterSelectMode(){
        selectMode = true;
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void exitSelectMode() {
        selectMode = false;
        for (ImageModel imageModel : imageModels) {
            imageModel.setSelected(false);
        }
        imageModels.clear();
        notifyDataSetChanged();
    }

    public boolean isSelectMode() {
        return selectMode;
    }
    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.choose_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        ImageModel imageView = imageModels.get(position);



        Glide.with(context).load(imageView.getUri()).into(holder.imageView);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                imageView.setSelected(!imageView.isSelected());
                holder.checkboxImage.setVisibility(View.VISIBLE);
                enterSelectMode();
                if (selectMode){
                    imageView.setSelected(!imageView.isSelected());
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    if (imageView.isSelected()){
                        imageView.setSelected(true);
                        holder.checkboxImage.setVisibility(View.VISIBLE);
                        if (!imageModels.contains(imageView)){
                            imageModels.add(imageView);
                        }

                    }
                }
                else{
                    imageView.setSelected(false);
                    holder.checkboxImage.setVisibility(View.VISIBLE);

                }
                if (onImageLongClick != null){
                    onImageLongClick.onLongClick(imageView,position);
                }
                return true;
            }
        });



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                if (selectMode){
                    imageView.setSelected(!imageView.isSelected());
                    holder.checkboxImage.setVisibility(View.VISIBLE);

                    if (imageView.isSelected()){
                        imageView.setSelected(true);
                        holder.checkboxImage.setVisibility(View.VISIBLE);
                    }
                    else{
                        holder.checkboxImage.setVisibility(View.GONE);
                    }

                    if (onImageClick != null){
                        onImageClick.onImageClick(imageView, position);

                    }
                }
                else{
                    exitSelectMode();
                    Intent intent = new Intent(context, FullScreenDisplayActivity.class);
                    intent.putParcelableArrayListExtra("imageModels", imageModels);
                    intent.putExtra("initialPosition", position);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                    onImageClick.onImageClick(imageView, position);

                }


            }
        });
        if (imageView.isSelected()){
            holder.checkboxImage.setVisibility(View.VISIBLE);

            //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.logo_background));

        }
        else{
            holder.checkboxImage.setVisibility(View.GONE);
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));

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

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView, checkboxImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            checkboxImage = itemView.findViewById(R.id.image_checkbox);


        }
    }

    public interface OnImageLongClick{
        void onLongClick(ImageModel imageModel, int position);
    }
    public interface OnImageClick{
        void onImageClick(ImageModel imageModel, int position);
    }
}

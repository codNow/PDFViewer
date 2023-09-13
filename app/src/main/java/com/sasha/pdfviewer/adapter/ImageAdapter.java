package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.ImageModel;

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
        View view = LayoutInflater.from(context).inflate(R.layout.choose_image, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageModel imageModel = imageModels.get(position);

        //Picasso.get().load(imageModel.getUri()).resize(800, 800).into(holder.imageView);
        Glide.with(context).load(imageModel.getImagePath()).override(800,800)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imageView);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                imageModel.setSelected(!imageModel.isSelected());
                holder.checkboxImage.setVisibility(View.VISIBLE);
                //holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
                //holder.imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.select_background));
                holder.cardView.setVisibility(View.VISIBLE);
                if (imageModel.isSelected()){
                    imageModel.setSelected(true);
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                   // holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
                    if (!imageModels.contains(imageModel)){
                        imageModels.add(imageModel);
                    }




                }
                else{
                    holder.checkboxImage.setVisibility(View.GONE);
                    holder.cardView.setVisibility(View.GONE);
                    //holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.primaryDark));
                }
                if (onImageClickListener != null){
                    onImageClickListener.onImageClick(imageModel, holder.getAdapterPosition());
                }
            }
        });
        if (imageModel.isSelected()){
            holder.checkboxImage.setVisibility(View.VISIBLE);
            holder.cardView.setVisibility(View.VISIBLE);
            //holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));

        }
        else{
            holder.checkboxImage.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.GONE);
            //holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.primaryDark));
        }

      /*  if (!isLoading && position >= getItemCount() - VISIBLE_THRESHOLD && onLoadMoreListener != null) {
            isLoading = true;
            onLoadMoreListener.onLoadMore();
        }

*/
;



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


    @Override
    public int getItemCount() {
        return imageModels.size();
    }
    @Override
    public int getItemViewType(int position) {
        // return the view type based on the position of the item
        return imageModels.get(position) == null ? 1 : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

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

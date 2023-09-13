package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.FolderModel;
import com.sasha.pdfviewer.view.PdfListActivity;
import com.sasha.pdfviewer.view.PdfViewActivity;

import java.util.ArrayList;

public class PdfFolderAdapter extends RecyclerView.Adapter<PdfFolderAdapter.ViewHolder> {
    private Context context;
    private ArrayList<FolderModel> pdfModels;
    OnItemClicks onItemClicks;
    OnFileLongClick onFileLongClick;
    public boolean selectMode = false;


    public PdfFolderAdapter(Context context, ArrayList<FolderModel> pdfModels,
                            OnItemClicks onItemClicks,
                            OnFileLongClick onFileLongClick) {
        this.context = context;
        this.pdfModels = pdfModels;
        this.onItemClicks = onItemClicks;
        this.onFileLongClick = onFileLongClick;

    }
    public void enterSelectMode(){
        selectMode = true;
        notifyDataSetChanged();
    }
    public void exitSelectMode() {
        selectMode = false;
        for (FolderModel folderModel : pdfModels) {
            folderModel.setSelected(false);
        }
        pdfModels.clear();
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
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_folder_layout, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        FolderModel modelPdf = pdfModels.get(position);

        holder.folderName.setText(modelPdf.getFolderName());
        holder.noOfFiles.setText(String.valueOf(modelPdf.getNumberOfPics()) + " Pdf");

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                modelPdf.setSelected(!modelPdf.isSelected());

                holder.cardView.setVisibility(View.VISIBLE);
                holder.center_image.setVisibility(View.VISIBLE);
                holder.folderImage.setColorFilter(context.getColor(R.color.choose_image));

                enterSelectMode();
                if (selectMode){
                    modelPdf.setSelected(!modelPdf.isSelected());

                    holder.cardView.setVisibility(View.VISIBLE);
                    if (modelPdf.isSelected()){
                        modelPdf.setSelected(true);
                        holder.cardView.setVisibility(View.VISIBLE);
                        holder.center_image.setVisibility(View.VISIBLE);

                        if (!pdfModels.contains(modelPdf)){
                            pdfModels.add(modelPdf);

                        }
                    }
                }
                else{
                    exitSelectMode();
                    modelPdf.setSelected(false);

                    holder.cardView.setVisibility(View.GONE);
                    holder.center_image.setVisibility(View.GONE);
                    holder.folderImage.setColorFilter(context.getColor(R.color.folder_color));
                }
                if (onFileLongClick != null){
                    onFileLongClick.onFileLongClick(modelPdf, position);
                }
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectMode){
                    modelPdf.setSelected(!modelPdf.isSelected());

                    holder.cardView.setVisibility(View.VISIBLE);
                    holder.center_image.setVisibility(View.VISIBLE);
                    holder.folderImage.setColorFilter(context.getColor(R.color.choose_image));

                    if (modelPdf.isSelected()){
                        modelPdf.setSelected(true);

                        holder.cardView.setVisibility(View.VISIBLE);
                        holder.center_image.setVisibility(View.VISIBLE);
                        holder.folderImage.setColorFilter(context.getColor(R.color.choose_image));

                        if (!pdfModels.contains(modelPdf)){
                            pdfModels.add(modelPdf);

                        }
                    }
                    else{

                        holder.cardView.setVisibility(View.GONE);
                        holder.center_image.setVisibility(View.GONE);
                        holder.folderImage.setColorFilter(context.getColor(R.color.folder_color));

                    }
                    if (onItemClicks != null){
                        onItemClicks.onItemClick(modelPdf, position);
                    }

                }
                else{
                    exitSelectMode();
                    Intent intent = new Intent(context, PdfListActivity.class);
                    intent.putExtra("folderName", modelPdf.getFolderName());
                    context.startActivity(intent);
                }
            }
        });
        if (modelPdf.isSelected()){

            holder.cardView.setVisibility(View.VISIBLE);
            holder.center_image.setVisibility(View.VISIBLE);
            holder.folderImage.setColorFilter(context.getColor(R.color.choose_image));
        }
        else{

            holder.cardView.setVisibility(View.GONE);
            holder.center_image.setVisibility(View.GONE);
            holder.folderImage.setColorFilter(context.getColor(R.color.folder_color));
        }


    }


    public ArrayList<FolderModel> getSelectedItems() {
        ArrayList<FolderModel> selectedModels = new ArrayList<>();
        for (FolderModel model : pdfModels) {
            if (model.isSelected()) {
                selectedModels.add(model);
            }
        }
        return selectedModels;
    }
    public int getSelectedPosition() {
        for (int i = 0; i < pdfModels.size(); i++) {
            FolderModel pdfModel = pdfModels.get(i);
            if (pdfModel.isSelected()) {
                return i;
            }
        }
        return -1;
    }
    @Override
    public int getItemCount() {

        return pdfModels.size();

    }



    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView folderName, noOfFiles, pdfSize, pdfPath;
        private ImageView center_image, folderImage;
        private View cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            folderImage = itemView.findViewById(R.id.imageView);
            folderName = itemView.findViewById(R.id.folderName);
            noOfFiles = itemView.findViewById(R.id.no_file);
            cardView = itemView.findViewById(R.id.choose_card);
            center_image = itemView.findViewById(R.id.center_image);

        }

    }
    public interface OnItemClicks{
        void onItemClick(FolderModel pdfModel,int Position);
    }
    public interface OnFileLongClick{
        void onFileLongClick(FolderModel folderModel, int position);
    }
}

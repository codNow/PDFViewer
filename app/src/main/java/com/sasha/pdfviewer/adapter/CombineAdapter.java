package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.utils.PdfUtils;

import java.io.File;
import java.util.ArrayList;

public class CombineAdapter extends RecyclerView.Adapter<CombineAdapter.MyViewHolder> {

    Context mContext;
    public ArrayList<PdfModel> pdfModels;
    boolean isLongClick = false;
    public boolean selectMode = false;
    OnFileLongClick onFileLongClick;
    OnItemClicks onItemClicks;

    public CombineAdapter(Context mContext, ArrayList<PdfModel> pdfModels, OnItemClicks onItemClicks) {
        this.mContext = mContext;
        this.pdfModels = pdfModels;
        this.onItemClicks = onItemClicks;
    }
    public void setOnItemClicks(OnItemClicks onItemClicks){
        this.onItemClicks = onItemClicks;
    }
    public void enterSelectMode(){
        selectMode = true;
        notifyDataSetChanged();
    }
    public void exitSelectMode() {
        selectMode = false;
        for (PdfModel recentModel : pdfModels) {
            recentModel.setSelected(false);
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
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        PdfModel modelPdf = pdfModels.get(position);
        String title = modelPdf.getTitle();
        String path = modelPdf.getPath();

        holder.pdfTitle.setText(modelPdf.getTitle());
        holder.pdfSize.setText(modelPdf.getSize());
        //holder.pdfPath.setText(modelPdf.getPdfPath());
        String filePath = modelPdf.getPath();
        File file = new File(filePath);
        String parentPath = file.getParentFile().getName();
        holder.pdfPath.setText(parentPath);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                modelPdf.setSelected(!modelPdf.isSelected());
                holder.checkboxImage.setVisibility(View.VISIBLE);
                //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.logo_background));
                if (modelPdf.isSelected()){
                    modelPdf.setSelected(true);
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    holder.option_btn.setVisibility(View.GONE);
                    holder.checkBox.setChecked(true);
                    if (!pdfModels.contains(modelPdf)){
                        pdfModels.add(modelPdf);

                    }
                }
                else{
                    holder.checkboxImage.setVisibility(View.GONE);
                    holder.checkBox.setChecked(false);
                    holder.option_btn.setVisibility(View.VISIBLE);
                    //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
                }
                if (onItemClicks != null){
                    onItemClicks.onItemClick(modelPdf, position);
                }

            }
        });
        if (modelPdf.isSelected()){
            holder.checkboxImage.setVisibility(View.VISIBLE);
            holder.option_btn.setVisibility(View.GONE);
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.logo_background));

        }
        else{
            holder.checkboxImage.setVisibility(View.GONE);
            holder.option_btn.setVisibility(View.VISIBLE);
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));

        }



    }


    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public ArrayList<PdfModel> getSelectedItems() {
        ArrayList<PdfModel> selectedModels = new ArrayList<>();
        for (PdfModel model : pdfModels) {
            if (model.isSelected()) {
                selectedModels.add(model);
            }
        }
        return selectedModels;
    }
    public void updateSearchList(ArrayList<PdfModel> searchList) {
        pdfModels = new ArrayList<>();
        pdfModels.addAll(searchList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return pdfModels.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;
        private ImageView option_btn, imageView, checkboxImage;
        private CheckBox checkBox;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.pdfPath);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            option_btn = itemView.findViewById(R.id.option_icon);
            checkBox = itemView.findViewById(R.id.recent_checkbox);
            imageView = itemView.findViewById(R.id.imageView);
            checkboxImage = itemView.findViewById(R.id.checking);


        }


    }
    public interface OnItemClicks{
        void onItemClick(PdfModel pdfModel,int Position);
    }
    public interface OnFileLongClick{
        void onFileLongClick(int position);
    }

}

package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.folderList.WaterMarkActivity;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.model.RecentModel;
import com.sasha.pdfviewer.view.AllPdfFileViewActivity;
import com.sasha.pdfviewer.view.PDFViewerActivity;

import java.io.File;
import java.util.ArrayList;

public class MultiSelectAdapter extends RecyclerView.Adapter<MultiSelectAdapter.MyViewHolder> {

    public ArrayList<RecentModel> pdfModels = new ArrayList<>();
    public ArrayList<RecentModel> selected_model = new ArrayList<>();
    Context mContext;
    WaterMarkActivity waterMarkActivity;
    OnItemSelectedListener onItemSelectedListener;

    public MultiSelectAdapter(ArrayList<RecentModel> pdfModels, Context mContext) {
        this.pdfModels = pdfModels;
        this.mContext = mContext;
        waterMarkActivity = (WaterMarkActivity) mContext;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_layout, parent, false);
        return new MyViewHolder(view, waterMarkActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        RecentModel pdfModel = pdfModels.get(position);
        String title = pdfModel.getPdfTitle();
        String path = pdfModel.getPdfPath();

        holder.pdfTitle.setText(pdfModel.getPdfTitle());
        holder.pdfSize.setText(pdfModel.getPdfSize());
        //holder.pdfPath.setText(modelPdf.getPdfPath());
        String filePath = pdfModel.getPdfPath();
        File file = new File(filePath);
        String parentPath = file.getParentFile().getName();
        holder.pdfPath.setText(parentPath);

        if (WaterMarkActivity.isSelectMode){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.option_btn.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //pdfModel.setSelected(!pdfModel.isSelected());
                 /*   if (pdfModel.isSelected()){
                        if (!pdfModels.contains(pdfModel)){
                            pdfModels.add(pdfModel);
                        }
                        if (onItemSelectedListener != null){
                            onItemSelectedListener.onItemSelected(pdfModel);
                        }
                    }
                    else{

                        //holder.checkImage.setVisibility(View.GONE);
                        onItemSelectedListener.onItemDeselected(pdfModel);
                    }*/
                }
            });
        }
        else{
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, PDFViewerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("listTitle", pdfModel.getPdfTitle());
                    intent.putExtra("listPath", pdfModel.getPdfPath().toString());
                    mContext.startActivity(intent);
                }
            });
            holder.checkBox.setVisibility(View.GONE);
            holder.option_btn.setVisibility(View.VISIBLE);
        }



    }

    @Override
    public int getItemCount() {
        return pdfModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;
        private ImageView option_btn, imageView, checkboxImage;
        private CheckBox checkBox;
        WaterMarkActivity waterMarkActivity;
        public MyViewHolder(@NonNull View itemView, WaterMarkActivity waterMarkActivity) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.pdfPath);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            option_btn = itemView.findViewById(R.id.option_icon);
            checkBox = itemView.findViewById(R.id.recent_checkbox);
            imageView = itemView.findViewById(R.id.imageView);
            checkboxImage = itemView.findViewById(R.id.checking);
            this.waterMarkActivity = waterMarkActivity;
            itemView.setOnLongClickListener(waterMarkActivity);
            checkBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            waterMarkActivity.prepareSelection(view, getAdapterPosition());
        }
    }
    public interface OnItemSelectedListener{
        void onItemSelected(PdfModel pdfModel);
        void onItemDeselected(PdfModel pdfModel);
    }

}

package com.sasha.pdfviewer.adapter;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.snackbar.Snackbar;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.utils.PdfUtils;
import com.sasha.pdfviewer.utils.SnakeBarUtils;
import com.sasha.pdfviewer.view.AllPdfFileViewActivity;
import com.sasha.pdfviewer.view.PDFViewerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MergedAdapter extends RecyclerView.Adapter<MergedAdapter.ViewHolder> {

    private Context context;
    private ArrayList<PdfModel> pdfModels;
    private ArrayList<String> pdfPathList;
    AllPdfFileViewActivity allPdfFileViewActivity;
    OnItemClicks onItemClicks;
    OnFileLongClick onFileLongClick;
    public boolean selectMode = false;
    private Intent intent;


    public MergedAdapter(Context context, ArrayList<PdfModel> pdfModels, ArrayList<String> pdfPathList,
                         OnItemClicks onItemClicks, OnFileLongClick onFileLongClick) {
        this.context = context;
        this.pdfModels = pdfModels;
        this.pdfPathList = pdfPathList;
        this.onItemClicks = onItemClicks;
        this.onFileLongClick = onFileLongClick;

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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        PdfModel modelPdf = pdfModels.get(position);
        String path = modelPdf.getPath();

        String title = modelPdf.getTitle();
        if (title != null && !title.isEmpty()) {
            String name = title.substring(0, 1).toUpperCase() + title.substring(1);
            holder.pdfTitle.setText(name);
        } else {
            holder.pdfTitle.setText(title);
            // handle the case where title is null or empty
        }
        //holder.pdfSize.setText(modelPdf.getSize());
        holder.pdfPath.setText(modelPdf.getPath());
        String filePath = modelPdf.getPath();
        File file = new File(filePath);
        String parentPath = file.getParentFile().getName();
        holder.pdfPath.setText(parentPath);
        String lastMod = modelPdf.getDate();
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(lastMod)*1000);
        String lastTime = DateFormat.format("dd-MMM-yyyy", calendar).toString();
        holder.pdfSize.setText(lastTime);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                if (PdfUtils.isFileLock(file)) {
                    modelPdf.setSelected(!modelPdf.isSelected());
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.logo_background));
                    if (modelPdf.isSelected()) {
                        modelPdf.setSelected(true);
                        holder.checkboxImage.setVisibility(View.VISIBLE);
                        holder.option_btn.setVisibility(View.GONE);
                        holder.checkBox.setChecked(true);
                        if (!pdfModels.contains(modelPdf)) {
                            pdfModels.add(modelPdf);

                        }
                    } else {
                        holder.checkboxImage.setVisibility(View.GONE);
                        holder.checkBox.setChecked(false);
                        holder.option_btn.setVisibility(View.VISIBLE);
                        //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
                    }
                    if (onItemClicks != null) {
                        onItemClicks.onItemClick(modelPdf, position);
                    }

                }
                else {
                    //Snackbar.make(view, R.string.file_not_protected, Snackbar.LENGTH_SHORT).show();
                    SnakeBarUtils.showSnackbar(view, "This File is Protected", Snackbar.LENGTH_SHORT);

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


      /*  try {
            new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (Exception e) {
            e.printStackTrace();
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.file_lock2));
        }*/

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
    public int getSelectedPosition() {
        for (int i = 0; i < pdfModels.size(); i++) {
            PdfModel pdfModel = pdfModels.get(i);
            if (pdfModel.isSelected()) {
                return i;
            }
        }
        return -1;
    }

    public int getSelectedItemCount() {
        int count = 0;
        if (pdfModels != null) {
            for (PdfModel model : pdfModels) {
                if (model.isSelected()) {
                    count++;
                }
            }
        }
        return count;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void deleteSelectedPdfFiles(ArrayList<PdfModel> filesToDelete, int position) {
        for (PdfModel pdfFile : pdfModels) {
            if (pdfFile.isSelected()) {
                filesToDelete.add(pdfFile);
            }
        }
        for (PdfModel pdfFile : filesToDelete) {
            pdfModels.remove(pdfFile);
            File file = new File(filesToDelete.get(position).getPath());
            file.delete();
            filesToDelete.remove(position);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return pdfModels.size();

    }


    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;
        private ImageView option_btn, checkboxImage;
        private CheckBox checkBox;
        View view;
        private ImageView imageView;
        private PDFView pdfView;
        private RelativeLayout pdf_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.pdfPath);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            option_btn = itemView.findViewById(R.id.option_icon);
            checkBox = itemView.findViewById(R.id.recent_checkbox);
            imageView = itemView.findViewById(R.id.imageView);
            pdfView = itemView.findViewById(R.id.pdfView);
            checkboxImage = itemView.findViewById(R.id.checking);


        }


    }

    public interface OnItemClicks{
        void onItemClick(PdfModel pdfModel, int position);
    }
    public interface OnFileLongClick{
        void onFileLongClick(int position);
    }

}

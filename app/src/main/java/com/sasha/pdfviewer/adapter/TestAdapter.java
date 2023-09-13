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
import android.os.Environment;
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
import com.sasha.pdfviewer.model.FolderModel;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.view.AllPdfFileViewActivity;
import com.sasha.pdfviewer.view.PDFViewerActivity;
import com.sasha.pdfviewer.view.PdfListActivity;
import com.sasha.pdfviewer.view.TestActivity;

import org.bouncycastle.util.test.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {

    private Context context;
    private ArrayList<FolderModel> pdfModels;
    private ArrayList<String> pdfPathList;
    public boolean selectMode = false;
    OnFileLongClick onFileLongClick;
    OnItemClicks onItemClicks;




    public TestAdapter(Context context, ArrayList<FolderModel> pdfModels, ArrayList<String> pdfPathList,
                       OnFileLongClick onFileLongClick,OnItemClicks onItemClicks) {
        this.context = context;
        this.pdfModels = pdfModels;
        this.pdfPathList = pdfPathList;
        this.onFileLongClick = onFileLongClick;
        this.onItemClicks = onItemClicks;

    }


    public void enterSelectMode(){
        selectMode = true;
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
        String path = modelPdf.getPath();

        int index = pdfPathList.get(position).lastIndexOf("/");
        String myFolder = pdfPathList.get(position);

        String folderName = pdfPathList.get(position).substring(index+1);
        holder.folderName.setText(folderName);
        holder.noOfFile.setText("");


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                enterSelectMode();
                selectMode = true;

                holder.cardView.setVisibility(View.VISIBLE);

                if (selectMode){


                }
                else {
                    holder.cardView.setVisibility(View.GONE);
                }

                if (onFileLongClick != null){
                    onFileLongClick.onFileLongClick(pdfPathList, position);
                }

                return true;
            }

        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectMode){
                    modelPdf.setSelected(!modelPdf.isSelected());
                    holder.cardView.setVisibility(View.VISIBLE);
                    //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.logo_background));
                    if (modelPdf.isSelected()){
                        modelPdf.setSelected(true);
                        holder.cardView.setVisibility(View.VISIBLE);



                    }
                    else{

                        holder.cardView.setVisibility(View.GONE);
                        //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
                    }

                    if (onItemClicks != null){
                        onItemClicks.onItemClick(modelPdf, position);
                    }


                }
                else {

                    Intent intent = new Intent(context, PdfListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("folderName", folderName);
                    context.startActivity(intent);
                }

            }
        });






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


    @Override
    public int getItemCount() {
        return  pdfPathList.size();

    }


    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView folederImage, check_image, center_image;
        private TextView folderName, noOfFile, pdf_path;
        ImageView menu_btn;
        private RecyclerView recyclerView;
        View view;
        private View cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            folederImage = itemView.findViewById(R.id.folder_image);
            folderName = itemView.findViewById(R.id.folderName);
            noOfFile = itemView.findViewById(R.id.no_file);
            menu_btn = itemView.findViewById(R.id.option_btn);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            center_image = itemView.findViewById(R.id.center_image);
            cardView = itemView.findViewById(R.id.choose_card);




        }



    }

    public interface OnItemClicks{
        void onItemClick(FolderModel pdfModel, int Position);
    }
    public interface OnFileLongClick{
        void onFileLongClick(ArrayList<String> folderModel, int position);
    }

}

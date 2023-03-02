package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.tools.AllToolsViewActivity;
import com.sasha.pdfviewer.view.AllPdfFileViewActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MergeAdapter extends RecyclerView.Adapter<MergeAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PdfModel> pdfModelArrayList;
    private OnItemSelectedListener onItemSelectedListener;


    public MergeAdapter(Context context, ArrayList<PdfModel> pdfModelArrayList) {
        this.context = context;
        this.pdfModelArrayList = pdfModelArrayList;

    }
    @NonNull
    @Override
    public MergeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MergeAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final PdfModel pdfModel = pdfModelArrayList.get(position);
        String title = pdfModel.getTitle();
        String path = pdfModel.getPath();
        String lastMod = pdfModel.getDate();
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(lastMod)*1000);
        String lastTime = DateFormat.format("dd-MMM-yyyy", calendar).toString();

        holder.pdfTitle.setText(pdfModel.getTitle());
        holder.pdfSize.setText(lastTime);
        String filePath = pdfModel.getPath();
        File file = new File(filePath);
        String parentPath = file.getParentFile().getName();
        holder.pdfPath.setText(parentPath);



        //holder.setIsRecyclable(false);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfModel.setSelected(!pdfModel.isSelected());
                holder.checkImage.setVisibility(View.VISIBLE);
                if (pdfModel.isSelected()){
                    if (!pdfModelArrayList.contains(pdfModel)){
                        pdfModelArrayList.add(pdfModel);
                    }
                    if (onItemSelectedListener != null){
                        onItemSelectedListener.onItemSelected(pdfModel);
                    }
                }
                else{

                    holder.checkImage.setVisibility(View.GONE);
                    onItemSelectedListener.onItemDeselected(pdfModel);
                }
            }
        });

/*

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pdfModel.setSelected(!pdfModel.isSelected());
                holder.checkBox.setChecked(pdfModel.isSelected());
                if (pdfModel.isSelected()){
                    pdfModelArrayList.add(pdfModel);
                    if (onItemSelectedListener != null){
                        onItemSelectedListener.onItemSelected(pdfModel);
                    }
                }
                else{
                    pdfModelArrayList.remove(pdfModel);
                    if (onItemSelectedListener != null){
                        onItemSelectedListener.onItemDeselected(pdfModel);
                    }
                }
                if (holder.checkBox.getVisibility() == View.VISIBLE){
                    holder.checkBox.setVisibility(View.INVISIBLE);
                }
                else{
                    holder.checkBox.setVisibility(View.VISIBLE);
                }

            }
        });


        holder.checkBox.setChecked(pdfModel.isSelected());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                pdfModel.setSelected(isCheck);

                if (pdfModel.isSelected()) {
                    pdfModelArrayList.add(pdfModel);
                    if (onItemSelectedListener != null) {
                        onItemSelectedListener.onItemSelected(pdfModel);
                    }
                } else {
                    pdfModelArrayList.remove(pdfModel);
                    if (onItemSelectedListener != null) {
                        onItemSelectedListener.onItemDeselected(pdfModel);
                    }
                }
            }
        });*/

    }


    public ArrayList<PdfModel> getSelectedItems() {

        ArrayList<PdfModel> selectedModels = new ArrayList<>();
        for (PdfModel model : pdfModelArrayList) {
            if (model.isSelected()) {
                selectedModels.add(model);
            }
        }
        return selectedModels;
    }
    public ArrayList<PdfModel> getSelectedModels() {
        return pdfModelArrayList;
    }

    public ArrayList<PdfModel> deSelectedModel(){
        ArrayList<PdfModel> deSelectModel = new ArrayList<>();
        for (PdfModel pdfModel : pdfModelArrayList){
            if (pdfModel.isSelected()){
                deSelectModel.removeAll(pdfModelArrayList);
            }
        }
        return deSelectModel;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener){
        this.onItemSelectedListener = onItemSelectedListener;
    }

    @Override
    public int getItemCount() {
        return pdfModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;
        private ImageView checkImage;

        AllToolsViewActivity allToolsViewActivity;
        View view;
        private ImageView imageView;
        CheckBox checkBox;
        private PDFView pdfView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.file_path);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            imageView = itemView.findViewById(R.id.pdfImage);
            pdfView = itemView.findViewById(R.id.pdfView);
            checkImage = itemView.findViewById(R.id.image_checkbox);

        }

    }

    public interface OnItemSelectedListener{
        void onItemSelected(PdfModel pdfModel);
        void onItemDeselected(PdfModel pdfModel);
    }
}

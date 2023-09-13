package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;

import java.io.File;
import java.util.ArrayList;

public class WordDocAdapter extends RecyclerView.Adapter<WordDocAdapter.ViewHolder> {

    private Context context;
    private ArrayList<PdfModel> pdfModels;

    public WordDocAdapter(Context context, ArrayList<PdfModel> pdfModels) {
        this.context = context;
        this.pdfModels = pdfModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        PdfModel pdfModel = pdfModels.get(position);

        String name = pdfModel.getTitle().substring(0, 1).toUpperCase() + pdfModel.getTitle().substring(1);


        holder.pdfTitle.setText(name);
        holder.pdfSize.setText(pdfModel.getSize());
        String filePath = pdfModel.getPath();
        File file = new File(filePath);
        String parentPath = file.getParentFile().getName();
        holder.pdfPath.setText(parentPath);

        holder.imageView.setImageDrawable(context.getDrawable(R.drawable.word_doc_icon));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWith(pdfModel, position);
            }
        });

    }

    public int counFiles(String folders){
        int count = 0;

        for (PdfModel pdfModel : pdfModels){
            if (pdfModel.getPath().substring(0,
                            pdfModel.getPath().lastIndexOf("/"))
                    .endsWith(folders)){
                count++;
            }


        }
        return count;
    }

    private void openWith(PdfModel pdfModel, int position) {


        Uri uri = Uri.parse(pdfModels.get(position).getPath());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        /*intent.setType("application/msword");
        intent.putExtra(Intent.EXTRA_STREAM, uri);*/
        intent.setDataAndType(uri, "application/msword");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent openIntent = Intent.createChooser(intent,"Open With");
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(openIntent);
    }

    @Override
    public int getItemCount() {
        return pdfModels.size();
    }

    public void updateSearchList(ArrayList<PdfModel> searchList) {
        pdfModels = new ArrayList<>();
        pdfModels.addAll(searchList);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.file_path);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            imageView = itemView.findViewById(R.id.pdfImage);

        }
    }
}

package com.sasha.pdfviewer.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.view.AllPdfFileViewActivity;
import com.sasha.pdfviewer.view.PDFViewerActivity;

import java.io.File;
import java.util.ArrayList;

public class AllPdfAdapter extends RecyclerView.Adapter<AllPdfAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PdfModel> fileArrayList;

    public AllPdfAdapter(Context context, ArrayList<PdfModel> fileArrayList) {
        this.context = context;
        this.fileArrayList = fileArrayList;
    }


    @NonNull
    @Override
    public AllPdfAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_list_layout, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull AllPdfAdapter.ViewHolder holder, int position) {

        PdfModel modelPdf = fileArrayList.get(position);
        String name = modelPdf.getTitle().substring(0, 1).toUpperCase() + modelPdf.getTitle().substring(1);

        String title = modelPdf.getTitle();
        String path = modelPdf.getPath();

        holder.pdfTitle.setText(name);
        holder.pdfSize.setText(modelPdf.getSize());
        //holder.pdfPath.setText(modelPdf.getPdfPath());
        String filePath = modelPdf.getPath();
        File file = new File(filePath);
        String parentPath = file.getParentFile().getName();
        holder.pdfPath.setText(parentPath);

        if (file.getName().endsWith(".doc")){
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.word_doc_icon));
        }
        else{
            try {
                new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
                holder.imageView.setImageDrawable(context.getDrawable(R.drawable.app_logo));
            } catch (Exception e) {
                e.printStackTrace();
                holder.imageView.setImageDrawable(context.getDrawable(R.drawable.black_lock));
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PDFViewerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("listPath", path);
                intent.putExtra("listTitle", title);
                context.startActivity(intent);
            }
        });




    }

    @Override
    public int getItemCount() {
        return fileArrayList.size();
    }

    public void updateSearchList(ArrayList<PdfModel> searchList) {
        fileArrayList = new ArrayList<>();
        fileArrayList.addAll(searchList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;
        private ImageView option_btn;
        private ImageView imageView, checkbox_image;
        private PDFView pdfView;

        private TextView title;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.file_path);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            option_btn = itemView.findViewById(R.id.option_btn);
            imageView = itemView.findViewById(R.id.pdfImage);
            pdfView = itemView.findViewById(R.id.pdfView);
            checkbox_image =  itemView.findViewById(R.id.image_checkbox);

        }

    }

    public interface  OnFileItemListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }
}

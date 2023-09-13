package com.sasha.pdfviewer.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.github.barteksc.pdfviewer.PDFView;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.utils.Constants;
import com.sasha.pdfviewer.view.PDFViewerActivity;
import java.io.File;
import java.util.ArrayList;

/* loaded from: classes2.dex */
public class AllPdfAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Context context;
    private ArrayList<PdfModel> fileArrayList;

    /* loaded from: classes2.dex */
    public interface OnFileItemListener {
        void onItemClick(int i);

        void onItemLongClick(int i);
    }

    public AllPdfAdapter(Context context, ArrayList<PdfModel> arrayList) {
        this.context = context;
        this.fileArrayList = arrayList;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(this.context).inflate(R.layout.all_list_layout, viewGroup, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        PdfModel pdfModel = this.fileArrayList.get(i);
        final String title = pdfModel.getTitle();
        final String path = pdfModel.getPath();
        viewHolder.pdfTitle.setText(pdfModel.getTitle().substring(0, 1).toUpperCase() + pdfModel.getTitle().substring(1));
        viewHolder.pdfSize.setText(pdfModel.getSize());
        File file = new File(pdfModel.getPath());
        viewHolder.pdfPath.setText(file.getParentFile().getName());
        if (!file.getName().endsWith(Constants.docExtension)) {
            try {
                new PdfRenderer(ParcelFileDescriptor.open(file, 268435456));
                viewHolder.imageView.setImageDrawable(this.context.getDrawable(R.drawable.app_logo));
            } catch (Exception e) {
                e.printStackTrace();
                viewHolder.imageView.setImageDrawable(this.context.getDrawable(R.drawable.black_lock));
            }
        } else {
            viewHolder.imageView.setImageDrawable(this.context.getDrawable(R.drawable.word_doc_icon));
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.sasha.pdfviewer.adapter.AllPdfAdapter.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Intent intent = new Intent(AllPdfAdapter.this.context, PDFViewerActivity.class);
                intent.addFlags(268468224);
                intent.putExtra("listPath", path);
                intent.putExtra("listTitle", title);
                AllPdfAdapter.this.context.startActivity(intent);
            }
        });
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.fileArrayList.size();
    }

    public void updateSearchList(ArrayList<PdfModel> arrayList) {
        ArrayList<PdfModel> arrayList2 = new ArrayList<>();
        this.fileArrayList = arrayList2;
        arrayList2.addAll(arrayList);
        notifyDataSetChanged();
    }

    /* loaded from: classes2.dex */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView checkbox_image;
        private ImageView imageView;
        private ImageView option_btn;
        private TextView pdfPath;
        private TextView pdfSize;
        private TextView pdfTitle;
        private PDFView pdfView;
        private TextView timeStamp;
        private TextView title;

        public ViewHolder(View view) {
            super(view);
            this.pdfTitle = (TextView) view.findViewById(R.id.pdfName);
            this.pdfPath = (TextView) view.findViewById(R.id.file_path);
            this.pdfSize = (TextView) view.findViewById(R.id.pdfSize);
            this.option_btn = (ImageView) view.findViewById(R.id.option_btn);
            this.imageView = (ImageView) view.findViewById(R.id.pdfImage);
            this.pdfView = (PDFView) view.findViewById(R.id.pdfView);
            this.checkbox_image = (ImageView) view.findViewById(R.id.image_checkbox);
        }
    }
}
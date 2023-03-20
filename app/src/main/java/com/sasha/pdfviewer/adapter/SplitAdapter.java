package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.EncryptDecrypt;
import com.sasha.pdfviewer.utils.SplitUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SplitAdapter extends RecyclerView.Adapter<SplitAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PdfModel> modelArrayList;

    public SplitAdapter(Context context, ArrayList<PdfModel> modelArrayList) {
        this.context = context;
        this.modelArrayList = modelArrayList;
    }

    @NonNull
    @Override
    public SplitAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SplitAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PdfModel modelPdf = modelArrayList.get(position);

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
            @Override
            public void onClick(View view) {
                if (EncryptDecrypt.isLockable(file)){
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    holder.option_btn.setVisibility(View.GONE);
                    final Dialog dialog = new Dialog(view.getRootView().getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.question_dialog);
                    Button cancelBtn, okBtn;
                    TextView question_text, textViewTitle;
                    textViewTitle = dialog.findViewById(R.id.textTitle);
                    textViewTitle.setText(R.string.split_title);
                    cancelBtn = dialog.findViewById(R.id.buttonNo);
                    okBtn = dialog.findViewById(R.id.buttonYes);
                    question_text = dialog.findViewById(R.id.question_text);
                    question_text.setText(R.string.spliting_question);
                    question_text.setPadding(0,0,0, 15);
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            holder.checkboxImage.setVisibility(View.GONE);
                            holder.option_btn.setVisibility(View.VISIBLE);
                        }
                    });
                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.checkboxImage.setVisibility(View.GONE);
                            splitPdfFiles(position, v, filePath, title);
                            dialog.dismiss();


                        }
                    });
                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setGravity(Gravity.END);
                }
                else {
                    Snackbar.make(view, R.string.file_lock_info, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void splitPdfFiles(int position, View v, String filePath, String title) {
        Dialog dialog = new Dialog(v.getRootView().getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_dialog);
        dialog.setCanceledOnTouchOutside(false);
        TextView textView = dialog.findViewById(R.id.loading_text);
        textView.setText(R.string.split_progress);
        dialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);
      /*  String destiny = Environment.getExternalStorageDirectory() +
                "/MySplitPdf/" + title;
        File dest = new File(destiny);


        if (!dest.getParentFile().exists()){
            dest.getParentFile().mkdir();
        }*/

        File destiny = new File(Environment.getExternalStorageDirectory() +
                "/MySplitPdf/" + title);
        if (!destiny.getParentFile().exists()){
            destiny.getParentFile().mkdir();
        }
        dialog.show();


        if (filePath != null){
            new CountDownTimer(2500, 1500){
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    try {

                        SplitUtil.splitWithItext(filePath, destiny  + ".pdf");
                        Toast.makeText(context, "Pdf File split successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        popupSuccessDialog(v, filePath, String.valueOf(destiny), title);
                    }
                    catch (IOException e){
                        e.printStackTrace();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            }.start();
        }

    }

    private void popupSuccessDialog(View view, String filPath, String dest, String title){
        Dialog successDialog = new Dialog(view.getRootView().getContext());
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.upload_done_layout);
        TextView textView1, textView2, textView3;
        Button noButton, yesButton;
        ImageView word_icon;

        textView1 = successDialog.findViewById(R.id.successText);
        textView2 = successDialog.findViewById(R.id.pathText);
        textView3 = successDialog.findViewById(R.id.question_text);
        noButton = successDialog.findViewById(R.id.no_btn);
        yesButton = successDialog.findViewById(R.id.yes_btn);
        word_icon = successDialog.findViewById(R.id.done_icon);

        textView1.setText(R.string.split_success);
        textView2.setText(dest+title);
        word_icon.setImageDrawable(context.getDrawable(R.drawable.s_s));
        textView3.setText(R.string.split_question);

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successDialog.dismiss();
                Intent intent = new Intent(context, ToolsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successDialog.dismiss();

            }

        });
        successDialog.show();
        successDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        successDialog.getWindow().setGravity(Gravity.TOP);
    }

    @Override
    public int getItemCount() {
        return modelArrayList.size();
    }

    public void updateSearchList(ArrayList<PdfModel> searchList) {
        modelArrayList = new ArrayList<>();
        modelArrayList.addAll(searchList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;
        private ImageView option_btn, imageView, checkboxImage;
        private CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
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
}

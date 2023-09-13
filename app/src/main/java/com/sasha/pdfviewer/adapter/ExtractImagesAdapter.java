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
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.folderList.ExtractedImagesFolderActivity;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.tools.ImageViewActivity;
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.EncryptDecrypt;
import com.sasha.pdfviewer.utils.ImageExtractUtil;
import com.sasha.pdfviewer.utils.PdfUtils;
import com.sasha.pdfviewer.utils.SuccessDialogUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;

public class ExtractImagesAdapter extends RecyclerView.Adapter<ExtractImagesAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PdfModel> pdfModelArrayList;

    public ExtractImagesAdapter(Context context, ArrayList<PdfModel> pdfModelArrayList) {
        this.context = context;
        this.pdfModelArrayList = pdfModelArrayList;
    }

    @NonNull
    @Override
    public ExtractImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExtractImagesAdapter.ViewHolder holder, int position) {
        PdfModel modelPdf = pdfModelArrayList.get(position);

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

                if (PdfUtils.isFileNotLock(file)) {
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    holder.option_btn.setVisibility(View.GONE);
                    final Dialog dialog = new Dialog(view.getRootView().getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.question_dialog);
                    Button cancelBtn, okBtn;
                    TextView question_text, textViewTitle;

                    textViewTitle = dialog.findViewById(R.id.textTitle);
                    textViewTitle.setText(R.string.extract_title);
                    cancelBtn = dialog.findViewById(R.id.buttonNo);
                    okBtn = dialog.findViewById(R.id.buttonYes);
                    okBtn.setText(R.string.extract_continue);
                    question_text = dialog.findViewById(R.id.question_text);
                    question_text.setText(R.string.extract_info);
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
                            try {
                                startExtractImages(view,title, path, file, holder.checkboxImage);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(context, R.string.file_without_image, Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();

                        }
                    });
                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setGravity(Gravity.END);
                } else {
                    Snackbar.make(view, R.string.file_lock_info, Snackbar.LENGTH_SHORT).show();
                }

            }
        });
    }

    private   void startExtractImages(View view,String title, String path, File file, ImageView checkBox) throws IOException {
        Dialog dialog = new Dialog(view.getRootView().getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_dialog);
        dialog.setCanceledOnTouchOutside(false);
        TextView textView = dialog.findViewById(R.id.loading_text);
        textView.setText(R.string.extract_progress);
        dialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);
        dialog.show();
        String dest = Environment.getExternalStorageDirectory() +
                "/ExtractedImages/"+title;
        File destinyFile = new File(dest);
        String myFile = String.valueOf(file);
        if (!destinyFile.getParentFile().exists()){
            destinyFile.getParentFile().mkdir();
        }
        if (path != null){
            new CountDownTimer(3500, 1350){

                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    try {
                        ImageExtractUtil.extractImages(String.valueOf(file), dest, context);
                        checkBox.setVisibility(View.GONE);
                        popUpDialog(view, title, dest);
                        Toast.makeText(context, R.string.extract_success, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();


                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(context, R.string.no_image_found, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                }
            }.start();
        }


    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private void popUpDialog(View view, String fileName, String dir) {
       /* SuccessDialogUtil.showSuccessDialog(view, dir, fileName, " Image Extracted Successfully",
                "Do you want to extract more ?", R.drawable.m_p);*/
        Dialog dialog = new Dialog(view.getRootView().getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.upload_done_layout);
        TextView title, messageText, questionText;
        ImageView titleIcon;
        Button positiveBtn, negativeBtn;

        title = dialog.findViewById(R.id.successText);
        messageText = dialog.findViewById(R.id.pathText);
        questionText = dialog.findViewById(R.id.question_text);
        titleIcon = dialog.findViewById(R.id.done_icon);
        positiveBtn = dialog.findViewById(R.id.yes_btn);
        negativeBtn = dialog.findViewById(R.id.no_btn);

        title.setText(R.string.extract_success);
        messageText.setText(dir + fileName);
        questionText.setText(R.string.extract_question);
        titleIcon.setImageDrawable(context.getDrawable(R.drawable.ic_round_image_24));
        titleIcon.setColorFilter(context.getColor(R.color.blue));

        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(context, ToolsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        messageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ExtractedImagesFolderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.TOP);
    }


    @Override
    public int getItemCount() {
        return pdfModelArrayList.size();
    }

    public void updateSearchList(ArrayList<PdfModel> searchList) {
        pdfModelArrayList = new ArrayList<>();
        pdfModelArrayList.addAll(searchList);
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

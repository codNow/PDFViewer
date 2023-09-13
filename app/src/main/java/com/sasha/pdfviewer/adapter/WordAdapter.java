package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
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
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.PdfUtils;
import com.sasha.pdfviewer.utils.SuccessDialogUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PdfModel> modelArrayList;
    private boolean showNote;

    public WordAdapter(Context context, ArrayList<PdfModel> modelArrayList) {
        this.context = context;
        this.modelArrayList = modelArrayList;

        SharedPreferences prefs = context.getSharedPreferences("WordPres", Context.MODE_PRIVATE);
        showNote = prefs.getBoolean("showNote", true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
                if (PdfUtils.isFileNotLock(file)) {
                    if (PdfUtils.isConvertibleToText(modelPdf.getPath())) {
                        holder.checkboxImage.setVisibility(View.VISIBLE);
                        holder.option_btn.setVisibility(View.GONE);
                        final Dialog dialog = new Dialog(view.getRootView().getContext());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.question_dialog);

                        Button cancelBtn, okBtn;
                        TextView question_text, textViewTitle;

                        textViewTitle = dialog.findViewById(R.id.textTitle);
                        textViewTitle.setText(R.string.word_title);
                        cancelBtn = dialog.findViewById(R.id.buttonNo);
                        okBtn = dialog.findViewById(R.id.buttonYes);
                        okBtn.setText(R.string.word_continue);
                        question_text = dialog.findViewById(R.id.question_text);
                        question_text.setText(R.string.word_info);
                        question_text.setPadding(0,0,0, 15);
                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                holder.checkboxImage.setVisibility(View.GONE);
                                holder.option_btn.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        });
                        okBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startCovertPdf(view, title, path, holder.checkboxImage);
                                dialog.dismiss();

                            }
                        });
                        dialog.show();
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().setGravity(Gravity.END);

                    } else {
                        Toast.makeText(context, R.string.file_not_covertible, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(view, R.string.file_lock_info, Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        try {
            new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (Exception e) {
            e.printStackTrace();
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.file_lock2));
        }
        if (showNote){
            holder.convertNote.setVisibility(View.VISIBLE);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    holder.convertNote.setVisibility(View.GONE);
                }
            }, 8000);
            SharedPreferences prefs = context.getSharedPreferences("WordPres", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("showNote", false);
            editor.apply();
            showNote = false;
        }
        else{
            holder.convertNote.setVisibility(View.GONE);
        }

    }
    private void startCovertPdf(View view, String title, String path, ImageView checkbox) {
        Dialog dialog = new Dialog(view.getRootView().getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_dialog);
        dialog.setCanceledOnTouchOutside(false);
        TextView textView = dialog.findViewById(R.id.loading_text);
        textView.setText(R.string.word_converting);
        dialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);
        /*File destiny = new File(Environment.getExternalStorageDirectory() +
                "/ConvertWord/");*/
        String dest = Environment.getExternalStorageDirectory() +
                "/Text Doc/" + title;
        File destinyFile = new File(dest);

        dialog.show();
        if (!destinyFile.getParentFile().exists()){
            destinyFile.getParentFile().mkdir();
        }

        if (path != null){
            new CountDownTimer(3500, 1500) {
                @Override
                public void onTick(long l) {

                }
                @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
                @Override
                public void onFinish() {
                    try {
                        PdfUtils.convertToText(path, dest + ".doc");

                        //Toast.makeText(context, R.string.word_success_toast, Toast.LENGTH_SHORT).show();
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

                        textView1.setText(R.string.word_convert_success);
                        textView2.setText(dest);
                        word_icon.setImageDrawable(context.getDrawable(R.drawable.word_doc_icon));
                        textView3.setText(R.string.word_question);

                        noButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                successDialog.dismiss();
                                Intent intent = new Intent(context, ToolsActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                                checkbox.setVisibility(View.GONE);
                            }
                        });
                        yesButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                successDialog.dismiss();
                                dialog.dismiss();
                                checkbox.setVisibility(View.GONE);
                            }

                        });
                        successDialog.show();
                        successDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        successDialog.getWindow().setGravity(Gravity.TOP);
                        dialog.dismiss();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d("TAG", String.valueOf(e.getStackTrace()));
                        dialog.dismiss();
                    }


                }
            }.start();
        }

    }

    private void popupSuccessDialog(View view,String path, String dest, String title) {
        SuccessDialogUtil.showSuccessDialog(view, dest, title, "Convert Successfully",
                "Do you want to convert another file", R.drawable.word_doc_icon );
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

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView pdfTitle, timeStamp, pdfSize, pdfPath, convertNote;
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
            convertNote = itemView.findViewById(R.id.convert_note);
        }
    }
}

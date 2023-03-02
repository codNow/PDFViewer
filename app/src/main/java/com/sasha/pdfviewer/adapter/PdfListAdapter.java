package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.view.AllPdfFileViewActivity;
import com.sasha.pdfviewer.view.PdfListActivity;
import com.sasha.pdfviewer.view.PdfViewActivity;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PdfListAdapter extends RecyclerView.Adapter<PdfListAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PdfModel> pdfModels;
    private ArrayList<String> pdfPathList;
    private PdfListActivity pdfListActivity;


    public PdfListAdapter(Context context, ArrayList<PdfModel> pdfModels, ArrayList<String> pdfPathList) {
        this.context = context;
        this.pdfModels = pdfModels;
        this.pdfPathList = pdfPathList;


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
        holder.pdfSize.setText(modelPdf.getDate());
        //holder.pdfPath.setText(modelPdf.getPath());

        int index = modelPdf.getPath().lastIndexOf("/");
        int index3 = pdfPathList.lastIndexOf(Environment.getExternalStorageDirectory());
        int index2 = pdfModels.get(position).getPath().lastIndexOf("/");
        String folderName = pdfModels.get(position).getPath().substring(index3 + 1);
        //holder.pdfPath.setText(modelPdf.getTitle());
        String name = folderName.substring(0, 1).toUpperCase() + folderName.substring(1);
        holder.pdfTitle.setText(name);
        File file = new File(modelPdf.getTitle());
        String parentPath = file.getParentFile().getName();
        holder.pdfPath.setText(parentPath);


       /* try {
            new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (Exception e) {
            e.printStackTrace();
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.file_lock2));
        }*/

        holder.option_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout delete_linear, rename_linear, share_linear;

                final Dialog dialog = new Dialog(v.getRootView().getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.side_popup_layout);

                delete_linear = dialog.findViewById(R.id.delete_linear);
                rename_linear = dialog.findViewById(R.id.path_linear);
                share_linear = dialog.findViewById(R.id.share_linear);

                delete_linear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteFile(position, v);
                        dialog.dismiss();
                    }
                });
                share_linear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareFile(position, v);
                        dialog.dismiss();
                    }
                });
                rename_linear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        final Dialog nameDialog = new Dialog(view.getRootView().getContext());
                        nameDialog.setContentView(R.layout.rename_file_layout);
                        final EditText editText = nameDialog.findViewById(R.id.rename_text);
                        Button cancel = nameDialog.findViewById(R.id.cancel_button);
                        Button rename_btn = nameDialog.findViewById(R.id.save_button);
                        final File renameFile = new File(pdfModels.get(position).getTitle());
                        int index3 = pdfPathList.lastIndexOf(Environment.getExternalStorageDirectory());
                        String folderName = pdfModels.get(position).getPath().substring(index3 + 1);
                        /*String nameText = renameFile.getName();
                        nameText = nameText.substring(0, nameText.lastIndexOf("."));*/
                        editText.setText(folderName);
                        editText.clearFocus();
                        dialog.getWindow().setSoftInputMode(WindowManager.
                                LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        cancel.setOnClickListener(v -> {
                            nameDialog.dismiss();
                        });
                        rename_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String filename = editText.getText().toString();
                                renameFiles(position, view);
                                nameDialog.dismiss();
                            }
                        });
                        nameDialog.show();
                        nameDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        nameDialog.getWindow().getAttributes().windowAnimations = R.style.SideMenuAnimation;
                        nameDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        nameDialog.getWindow().setGravity(Gravity.END);
                    }
                });
                dialog.show();
                //dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().getAttributes().windowAnimations = R.style.SideMenuAnimation;
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setGravity(Gravity.END);
            }
        });

    }
    private void renameFiles(int position, View view){
        final Dialog dialog = new Dialog(view.getRootView().getContext());
        dialog.setContentView(R.layout.rename_file_layout);
        final EditText editText = dialog.findViewById(R.id.rename_text);
        Button cancel = dialog.findViewById(R.id.cancel_button);
        Button rename_btn = dialog.findViewById(R.id.save_button);
        final File renameFile = new File(pdfModels.get(position).getTitle());
        String nameText = renameFile.getName();
        nameText = nameText.substring(0, nameText.lastIndexOf("."));
        editText.setText(nameText);
        editText.clearFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.
                LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        rename_btn.setOnClickListener(v1->{
            Dialog progressDialog = new Dialog(view.getRootView().getContext());
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressDialog.setContentView(R.layout.progress_dialog);
            progressDialog.setCanceledOnTouchOutside(false);
            TextView textView = progressDialog.findViewById(R.id.loading_text);
            textView.setText(R.string.rename_progress);
            progressDialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);
            progressDialog.show();
            new CountDownTimer(1000, 1000){
                @Override
                public void onTick(long l) {
                }
                @Override
                public void onFinish() {
                    progressDialog.dismiss();
                    try{
                        String newName = editText.getText().toString();
                        String dest = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RenameFiles/"+newName;
                        File dir = new File(dest);
                        if (!dir.getParentFile().exists()){
                            dir.getParentFile().mkdir();
                        }
                        String paths = pdfModels.get(position).getTitle();
                        File sourceFile = new File(paths);
                        File file = new File(String.valueOf(sourceFile));
                        String filename = file.getName();
                        FileInputStream in = new FileInputStream(sourceFile);
                        FileOutputStream out = new FileOutputStream(dest+".pdf");

                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        in.close();
                        out.flush();
                        out.close();
                        boolean removeFile = sourceFile.delete();
                        if (removeFile){
                            pdfModels.remove(position);
                            notifyItemChanged(position);
                            notifyDataSetChanged();
                            successPopup(dest, newName, view, position);
                        }

                    }
                    catch (IOException e){
                        e.printStackTrace();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }.start();

            dialog.dismiss();
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.SideMenuAnimation;
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.END);
    }
    private void successPopup(String dest, String title, View view, int position){
        Dialog successDialog = new Dialog(view.getRootView().getContext());
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.upload_done_layout);
        TextView textView1, textView2, textView3;
        Button noButton, yesButton, okButton;
        ImageView word_icon;

        textView1 = successDialog.findViewById(R.id.successText);
        textView2 = successDialog.findViewById(R.id.pathText);
        textView3 = successDialog.findViewById(R.id.question_text);
        noButton = successDialog.findViewById(R.id.no_btn);
        yesButton = successDialog.findViewById(R.id.yes_btn);
        word_icon = successDialog.findViewById(R.id.done_icon);
        okButton = successDialog.findViewById(R.id.okButton);
        okButton.setVisibility(View.VISIBLE);
        noButton.setVisibility(View.GONE);
        yesButton.setVisibility(View.GONE);
        textView1.setText(R.string.rename_success);
        textView2.setText(dest+title);
        word_icon.setImageDrawable(context.getDrawable(R.drawable.word_icon));
        textView3.setText(R.string.rename_saving_place);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successDialog.dismiss();
                notifyItemChanged(position);
                Intent intent = new Intent(context, AllPdfFileViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

        });
        successDialog.show();
        successDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        successDialog.getWindow().setGravity(Gravity.TOP);
    }
    private void shareFile(int position, View view){
        Uri uri = Uri.parse(pdfModels.get(position).getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "share"));
        Toast.makeText(context, "loading..", Toast.LENGTH_SHORT).show();
    }
    private void deleteFile(int position, View view){
        final TextView textTile, textMessage;
        final Button yesButton, noButton;
        final ImageView close_button;

        Dialog alertDialog = new Dialog(view.getRootView().getContext());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.delete_layout);

        textTile = alertDialog.findViewById(R.id.textTitle);
        textMessage = alertDialog.findViewById(R.id.textMessage);
        yesButton = alertDialog.findViewById(R.id.buttonYes);
        noButton = alertDialog.findViewById(R.id.buttonNo);
        close_button = alertDialog.findViewById(R.id.close_btn);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        Long.parseLong(pdfModels.get(position).getId()));
                File file = new File(pdfModels.get(position).getTitle());
                boolean deleted = file.delete();
                if (deleted){
                    context.getApplicationContext().getContentResolver()
                            .delete(contentUri,
                                    null, null);
                    pdfModels.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position,pdfModels.size());
                    Snackbar.make(view,"File Deleted Success",
                            Snackbar.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }else {
                    Snackbar.make(view,"File Deleted Fail",
                            Snackbar.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.SideMenuAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().setGravity(Gravity.END);

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

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;
        private ImageView option_btn, imageView;
        private PDFView pdfView;
        View view;
        private CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.pdfPath);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            option_btn = itemView.findViewById(R.id.option_icon);
            imageView = itemView.findViewById(R.id.imageView);
            checkBox = itemView.findViewById(R.id.recent_checkbox);

        }

    }
}

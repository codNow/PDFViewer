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
import android.os.Handler;
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
import android.widget.ProgressBar;
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
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.PdfUtils;
import com.sasha.pdfviewer.view.AllPdfFileViewActivity;
import com.sasha.pdfviewer.view.PDFViewerActivity;
import com.sasha.pdfviewer.view.PdfViewActivity;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.ViewHolder> {

    private Context context;
    private ArrayList<PdfModel> pdfModels;
    private ArrayList<String> pdfPathList;
    private TextView textView;
    AllPdfFileViewActivity allPdfFileViewActivity;
    private boolean isEnable = false;
    private boolean isSelectedAll = false;
    private ArrayList<PdfModel> selectList = new ArrayList<>();
    private boolean isLongClick = false;
    LoadMoreItems loadMoreItems;
    private static final int LOADING = 0;
    private static final int ITEM = 1;
    private boolean isLoadingAdded = false;



    public PdfAdapter(Context context, ArrayList<PdfModel> pdfModels, ArrayList<String> pdfPathList, AllPdfFileViewActivity allPdfFileViewActivity ) {
        this.context = context;
        this.pdfModels = pdfModels;
        this.pdfPathList = pdfPathList;
        this.allPdfFileViewActivity = allPdfFileViewActivity;

    }
    public void setLoadMoreItems(LoadMoreItems loadMoreItems){
        this.loadMoreItems = loadMoreItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, allPdfFileViewActivity);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        PdfModel modelPdf = pdfModels.get(position);
        String title = modelPdf.getTitle();
        String path = modelPdf.getPath();

        String name = modelPdf.getTitle().substring(0, 1).toUpperCase() + modelPdf.getTitle().substring(1);

        holder.pdfTitle.setText(title);
        holder.pdfSize.setText(modelPdf.getSize());
        //holder.pdfPath.setText(modelPdf.getPdfPath());
        String filePath = modelPdf.getPath();
        File file = new File(filePath);
        String parentPath = file.getParentFile().getName();
        holder.pdfPath.setText(parentPath);


        if (AllPdfFileViewActivity.isContextTualModeEnabled){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.option_btn.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
        else{
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PDFViewerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("listTitle", modelPdf.getTitle());
                    intent.putExtra("listPath", modelPdf.getPath().toString());
                    context.startActivity(intent);

                }
            });
            holder.checkBox.setVisibility(View.GONE);
            holder.option_btn.setVisibility(View.VISIBLE);
        }

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

                //share_linear.setVisibility(View.GONE);

                share_linear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        shareFile(view, position);
                    }
                });

                delete_linear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteFile(position, v);
                        dialog.dismiss();
                    }
                });
                rename_linear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        renameFiles(position, v);
                        dialog.dismiss();
                    }
                });
                dialog.show();
                //dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().getAttributes().windowAnimations = R.style.SideMenuAnimation;
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setGravity(Gravity.END);
            }
        });

        try {
            new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (Exception e) {
            e.printStackTrace();
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.file_lock2));
        }

    }

    private void shareFile(View view, int position) {
        Uri uri = Uri.parse(pdfModels.get(position).getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent shareIntent = Intent.createChooser(intent,"Share . . . ");
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(shareIntent);

    }

    @SuppressLint("SetTextI18n")
    private void deleteFile(int p, View  view){

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
                        Long.parseLong(pdfModels.get(p).getId()));
                File file = new File(pdfModels.get(p).getPath());
                boolean deleted = file.delete();
                if (deleted){
                    /*context.getApplicationContext().getContentResolver()
                            .delete(contentUri,
                                    null, null);*/
                    pdfModels.remove(p);
                    notifyItemRemoved(p);
                    notifyItemRangeChanged(p,pdfModels.size());

                    alertDialog.dismiss();
                }else {

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
   

    private void renameFiles(int position, View view){
        final Dialog dialog = new Dialog(view.getRootView().getContext());
        dialog.setContentView(R.layout.rename_file_layout);
        final EditText editText = dialog.findViewById(R.id.rename_text);
        Button cancel = dialog.findViewById(R.id.cancel_button);
        Button rename_btn = dialog.findViewById(R.id.save_button);
        final File renameFile = new File(pdfModels.get(position).getPath());
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
                        String paths = pdfModels.get(position).getPath();
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

    private void refreshPage() {
          if (pdfModels.size() > 0){
            pdfModels.clear();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            }, 1500);
        }
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;
        private ImageView option_btn;
        private CheckBox checkBox;
        AllPdfFileViewActivity allPdfFileViewActivity;
        View view;
        private ImageView imageView;
        private PDFView pdfView;

        public ViewHolder(@NonNull View itemView, AllPdfFileViewActivity allPdfFileViewActivity) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.pdfPath);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            option_btn = itemView.findViewById(R.id.option_icon);
            checkBox = itemView.findViewById(R.id.recent_checkbox);
            imageView = itemView.findViewById(R.id.imageView);
            pdfView = itemView.findViewById(R.id.pdfView);
            this.allPdfFileViewActivity = allPdfFileViewActivity;

            view = itemView;

            view.setOnLongClickListener(allPdfFileViewActivity);
            checkBox.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            allPdfFileViewActivity.makeSelection(v, getAdapterPosition());
        }

    }

    public class LoadingHolder extends RecyclerView.ViewHolder{
        ProgressBar progressBar;
        public LoadingHolder(@NonNull View itemView) {
            super(itemView);

            progressBar = itemView.findViewById(R.id.progressbar);
        }
    }
    public interface LoadMoreItems {
        void LoadItems();
    }
}

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
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
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
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.view.AllPdfFileViewActivity;
import com.sasha.pdfviewer.view.PDFViewerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class PagingAdapter extends RecyclerView.Adapter<PagingAdapter.ViewHolder> {


    private Context context;
    private ArrayList<PdfModel> pdfFiles;
    private boolean isLoading = false;
    private OnLoadMoreListener onLoadMoreListener;
    private final int VISIBLE_THRESHOLD = 5;
    OnItemClicks onItemClicks;
    OnFileLongClick onFileLongClick;
    public boolean selectMode = false;


    public PagingAdapter(Context context, ArrayList<PdfModel> pdfFiles, OnItemClicks onItemClicks,
                         OnFileLongClick onFileLongClick ) {
        this.context = context;
        this.pdfFiles = pdfFiles;
        this.onItemClicks = onItemClicks;
        this.onFileLongClick = onFileLongClick;


    }
    public void enterSelectMode(){
        selectMode = true;
        notifyDataSetChanged();
    }
    public void exitSelectMode() {
        selectMode = false;
        for (PdfModel pdfModel : pdfFiles) {
            pdfModel.setSelected(false);
        }
        pdfFiles.clear();
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        PdfModel modelPdf = pdfFiles.get(position);

        String title = modelPdf.getTitle();
        if (title != null && !title.isEmpty()) {
            String name = title.substring(0, 1).toUpperCase() + title.substring(1);
            holder.pdfTitle.setText(name);
        } else {
            holder.pdfTitle.setText(title);

        }

        String filePath = modelPdf.getPath();
        File file = new File(filePath);
        String parentPath = file.getParentFile().getName();
        holder.pdfPath.setText(parentPath);
        String lastMod = modelPdf.getDate();
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(lastMod)*1000);
        String lastTime = DateFormat.format("dd-MMM-yyyy", calendar).toString();
        holder.pdfSize.setText(lastTime);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                modelPdf.setSelected(!modelPdf.isSelected());
                holder.checkboxImage.setVisibility(View.VISIBLE);
                holder.option_btn.setVisibility(View.GONE);
                enterSelectMode();
                if (selectMode){
                    modelPdf.setSelected(!modelPdf.isSelected());
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    holder.option_btn.setVisibility(View.GONE);
                    holder.option_btn.setClickable(false);
                   /* if (modelPdf.isSelected()){
                        modelPdf.setSelected(true);
                        holder.checkboxImage.setVisibility(View.VISIBLE);
                        holder.option_btn.setVisibility(View.GONE);
                        holder.checkBox.setChecked(true);
                        if (!pdfModels.contains(modelPdf)){
                            pdfModels.add(modelPdf);

                        }
                    }*/
                }
                else{
                    modelPdf.setSelected(false);
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    holder.option_btn.setVisibility(View.GONE);
                }
                if (onFileLongClick != null){
                    onFileLongClick.onFileLongClick(modelPdf, position);
                }
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                if (selectMode){
                    modelPdf.setSelected(!modelPdf.isSelected());
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    holder.option_btn.setClickable(false);
                    //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.logo_background));
                    if (modelPdf.isSelected()){
                        modelPdf.setSelected(true);
                        holder.checkboxImage.setVisibility(View.VISIBLE);
                        holder.option_btn.setVisibility(View.GONE);
                        holder.checkBox.setChecked(true);
                        if (!pdfFiles.contains(modelPdf)){
                            pdfFiles.add(modelPdf);

                        }
                    }
                    else{
                        holder.checkboxImage.setVisibility(View.GONE);
                        holder.checkBox.setChecked(false);
                        holder.option_btn.setVisibility(View.VISIBLE);
                        //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
                    }
                    if (onItemClicks != null){
                        onItemClicks.onItemClick(modelPdf, position);
                    }

                }
                else{
                    exitSelectMode();
                    Intent intent = new Intent(context, PDFViewerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("listTitle", modelPdf.getTitle());
                    intent.putExtra("listPath", modelPdf.getPath().toString());
                    context.startActivity(intent);
                }
            }
        });
        if (modelPdf.isSelected()){
            holder.checkboxImage.setVisibility(View.VISIBLE);
            holder.option_btn.setVisibility(View.GONE);
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.logo_background));

        }
        else{
            holder.checkboxImage.setVisibility(View.GONE);
            holder.option_btn.setVisibility(View.VISIBLE);
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));

        }

        if (!isLoading && position >= getItemCount() - VISIBLE_THRESHOLD && onLoadMoreListener != null) {
            isLoading = true;
            onLoadMoreListener.onLoadMore();
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
                        dialog.dismiss();
                        final Dialog nameDialog = new Dialog(v.getRootView().getContext());
                        nameDialog.setContentView(R.layout.rename_file_layout);
                        final EditText editText = nameDialog.findViewById(R.id.rename_text);
                        Button cancel = nameDialog.findViewById(R.id.cancel_button);
                        Button rename_btn = nameDialog.findViewById(R.id.save_button);
                        final File renameFile = new File(pdfFiles.get(position).getPath());
                        String nameText = renameFile.getName();
                        nameText = nameText.substring(0, nameText.lastIndexOf("."));
                        String oldName = pdfFiles.get(position).getTitle();
                        editText.setText(modelPdf.getTitle());
                        editText.clearFocus();
                        dialog.getWindow().setSoftInputMode(WindowManager.
                                LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        cancel.setOnClickListener(view -> {
                            nameDialog.dismiss();
                        });
                        rename_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String filename = editText.getText().toString();
                                String filePath = modelPdf.getPath();
                                renameFiles(position, view, filename, filePath);
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


    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
    public ArrayList<PdfModel> getSelectedItems() {
        ArrayList<PdfModel> selectedModels = new ArrayList<>();
        for (PdfModel model : pdfFiles) {
            if (model.isSelected()) {
                selectedModels.add(model);
            }
        }
        return selectedModels;
    }
    public int getSelectedPosition() {
        for (int i = 0; i < pdfFiles.size(); i++) {
            PdfModel pdfModel = pdfFiles.get(i);
            if (pdfModel.isSelected()) {
                return i;
            }
        }
        return -1;
    }

    private void shareFile(View view, int position) {
        Uri uri = Uri.parse(pdfFiles.get(position).getPath());
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
                        MediaStore.Files.getContentUri("external"),
                        Long.parseLong(pdfFiles.get(p).getId()));
                File file = new File(pdfFiles.get(p).getPath());
                boolean deleted = file.delete();
                if (deleted){
                    context.getApplicationContext().getContentResolver()
                            .delete(contentUri,
                                    null, null);
                    pdfFiles.remove(p);
                    notifyItemRemoved(p);
                    notifyItemRangeChanged(p,pdfFiles.size());
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

    private void renameFiles(int position, View view, String filePath, String fileName){
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

                    String paths = pdfFiles.get(position).getPath();
                    File sourceFile = new File(paths);
                    File parentDir = sourceFile.getParentFile(); // get the directory of the current file
                    String fileExtension = ".pdf"; // change this to the file extension you want
                    String newFileName = fileName + fileExtension;
                    String newPath = parentDir.getAbsolutePath() + File.separator + newFileName;
                    File file = new File(String.valueOf(sourceFile));
                    String filename = file.getName();
                    FileInputStream in = new FileInputStream(sourceFile);
                    FileOutputStream out = new FileOutputStream(newPath);

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
                        pdfFiles.remove(position);
                        notifyItemChanged(position);
                        notifyDataSetChanged();
                        successPopup(newPath, fileName, view, position);
                    }
                    Log.d("Path", newPath);

                }
                catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
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
        word_icon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_edit_note_24));
        word_icon.setColorFilter(context.getColor(R.color.blue));
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
    @Override
    public int getItemCount() {
        return pdfFiles.size();
    }



    @Override
    public int getItemViewType(int position) {
        // return the view type based on the position of the item
        return pdfFiles.get(position) == null ? 1 : 0;
    }
    public void updateSearchList(ArrayList<PdfModel> searchList) {
        pdfFiles = new ArrayList<>();
        pdfFiles.addAll(searchList);
        notifyDataSetChanged();
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
    public void addMoreItems(ArrayList<PdfModel> newItems) {
        pdfFiles.addAll(newItems);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;
        private ImageView option_btn, checkboxImage, check_circle;
        private CheckBox checkBox;
        View view;
        private ImageView imageView;
        private PDFView pdfView;
        public ViewHolder(View itemView) {
            super(itemView);
            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.pdfPath);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            option_btn = itemView.findViewById(R.id.option_icon);
            checkBox = itemView.findViewById(R.id.recent_checkbox);
            imageView = itemView.findViewById(R.id.imageView);
            pdfView = itemView.findViewById(R.id.pdfView);
            checkboxImage = itemView.findViewById(R.id.checking);
            view = itemView;

            /*view.setOnLongClickListener(pagingActivity);*/
        }


    }
    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressbar);
        }
    }
    public interface OnItemClicks{
        void onItemClick(PdfModel pdfModel, int position);
    }
    public interface OnFileLongClick {
        void onFileLongClick(PdfModel pdfModel, int position);
    }
}

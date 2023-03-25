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
import android.os.Environment;
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
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.view.AllPdfFileViewActivity;
import com.sasha.pdfviewer.view.PDFViewerActivity;
import com.sasha.pdfviewer.view.PagingActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PagingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;
    private Context context;
    private ArrayList<PdfModel> pdfFiles;
    private ArrayList<PdfModel> selected = new ArrayList<>();
    private boolean isLoading = false;
    private OnLoadMoreListener onLoadMoreListener;
    private final int VISIBLE_THRESHOLD = 5;
    OnItemClicks onItemClicks;
    OnFileLongClick onFileLongClick;
    public boolean selectMode = false;


    PagingActivity pagingActivity;

    public PagingAdapter(Context context, ArrayList<PdfModel> pdfFiles, OnItemClicks onItemClicks,
                         OnFileLongClick onFileLongClick ) {
        this.context = context;
        this.pdfFiles = pdfFiles;
        this.pagingActivity = (PagingActivity) context;
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_layout, parent, false);
            return new ViewHolder(view, pagingActivity);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_layout, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder instanceof ViewHolder) {
            PdfModel modelPdf = pdfFiles.get(position);
            ((ViewHolder) holder).pdfTitle.setText(modelPdf.getTitle());
            ((ViewHolder) holder).pdfPath.setText(modelPdf.getPath());
            ((ViewHolder) holder).pdfSize.setText(modelPdf.getSize());
            ((ViewHolder) holder).itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    modelPdf.setSelected(!modelPdf.isSelected());
                    ((ViewHolder) holder).checkboxImage.setVisibility(View.VISIBLE);
                    ((ViewHolder) holder).option_btn.setVisibility(View.GONE);
                    enterSelectMode();
                    if (selectMode){
                        modelPdf.setSelected(!modelPdf.isSelected());
                        ((ViewHolder) holder).checkboxImage.setVisibility(View.VISIBLE);
                        ((ViewHolder) holder).option_btn.setVisibility(View.GONE);
                    if (modelPdf.isSelected()){
                        modelPdf.setSelected(true);
                        ((ViewHolder) holder).checkboxImage.setVisibility(View.VISIBLE);
                        ((ViewHolder) holder).option_btn.setVisibility(View.GONE);
                        ((ViewHolder) holder).checkBox.setChecked(true);
                        if (!pdfFiles.contains(modelPdf)){
                            pdfFiles.add(modelPdf);

                        }
                    }
                    }
                    else{
                        modelPdf.setSelected(false);
                        ((ViewHolder) holder).checkboxImage.setVisibility(View.VISIBLE);
                        ((ViewHolder) holder).option_btn.setVisibility(View.GONE);
                    }
                    if (onFileLongClick != null){
                        onFileLongClick.onFileLongClick(modelPdf, position);
                    }
                    return true;
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectMode){
                        modelPdf.setSelected(!modelPdf.isSelected());
                        ((ViewHolder) holder).checkboxImage.setVisibility(View.VISIBLE);
                        if (modelPdf.isSelected()){
                            modelPdf.setSelected(true);
                            ((ViewHolder) holder).checkboxImage.setVisibility(View.VISIBLE);
                            ((ViewHolder) holder).option_btn.setVisibility(View.GONE);
                            if (!pdfFiles.contains(modelPdf)){
                                pdfFiles.add(modelPdf);

                            }
                        }
                        else{
                            ((ViewHolder) holder).checkboxImage.setVisibility(View.GONE);
                            ((ViewHolder) holder).checkBox.setChecked(false);
                            ((ViewHolder) holder).option_btn.setVisibility(View.VISIBLE);
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
                ((ViewHolder) holder).checkboxImage.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).option_btn.setVisibility(View.GONE);
            }
            else{
                ((ViewHolder) holder).checkboxImage.setVisibility(View.GONE);
                ((ViewHolder) holder).option_btn.setVisibility(View.VISIBLE);
            }

            ((ViewHolder) holder).option_btn.setOnClickListener(new View.OnClickListener() {
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
                            shareFile(v, position);
                            dialog.dismiss();
                        }
                    });
                    rename_linear.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ArrayList<String> pdfPathList = new ArrayList<>();
                            dialog.dismiss();
                            final Dialog nameDialog = new Dialog(view.getRootView().getContext());
                            nameDialog.setContentView(R.layout.rename_file_layout);
                            final EditText editText = nameDialog.findViewById(R.id.rename_text);
                            Button cancel = nameDialog.findViewById(R.id.cancel_button);
                            Button rename_btn = nameDialog.findViewById(R.id.save_button);
                            final File renameFile = new File(pdfFiles.get(position).getTitle());
                            int index3 = pdfPathList.lastIndexOf(Environment.getExternalStorageDirectory());
                            String folderName = pdfFiles.get(position).getPath().substring(index3 + 1);
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

        } else {
            // bind the progress bar
            ((LoadingViewHolder) holder).progressBar.setIndeterminate(true);
        }
        if (!isLoading && position >= getItemCount() - VISIBLE_THRESHOLD && onLoadMoreListener != null) {
            isLoading = true;
            onLoadMoreListener.onLoadMore();
        }
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
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
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

    private void renameFiles(int position, View view){
        final Dialog dialog = new Dialog(view.getRootView().getContext());
        dialog.setContentView(R.layout.rename_file_layout);
        final EditText editText = dialog.findViewById(R.id.rename_text);
        Button cancel = dialog.findViewById(R.id.cancel_button);
        Button rename_btn = dialog.findViewById(R.id.save_button);
        final File renameFile = new File(pdfFiles.get(position).getPath());
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
                        String paths = pdfFiles.get(position).getPath();
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
                            pdfFiles.remove(position);
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
        PagingActivity pagingActivity;
        public ViewHolder(View itemView, PagingActivity pagingActivity) {
            super(itemView);
            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.pdfPath);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            option_btn = itemView.findViewById(R.id.option_icon);
            checkBox = itemView.findViewById(R.id.recent_checkbox);
            imageView = itemView.findViewById(R.id.imageView);
            pdfView = itemView.findViewById(R.id.pdfView);
            checkboxImage = itemView.findViewById(R.id.checking);
            this.pagingActivity = pagingActivity;
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

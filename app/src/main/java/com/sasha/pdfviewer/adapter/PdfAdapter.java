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
import android.text.format.DateFormat;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.model.RecentModel;
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
import java.util.Calendar;
import java.util.Locale;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.ViewHolder> {

    private Context context;
    private ArrayList<PdfModel> pdfModels;
    private ArrayList<String> pdfPathList;
    AllPdfFileViewActivity allPdfFileViewActivity;
    OnItemClicks onItemClicks;
    OnFileLongClick onFileLongClick;
    public boolean selectMode = false;
    private Intent intent;


    public PdfAdapter(Context context, ArrayList<PdfModel> pdfModels, ArrayList<String> pdfPathList,
                      OnItemClicks onItemClicks, OnFileLongClick onFileLongClick) {
        this.context = context;
        this.pdfModels = pdfModels;
        this.pdfPathList = pdfPathList;
        this.onItemClicks = onItemClicks;
        this.onFileLongClick = onFileLongClick;

    }

    public void enterSelectMode(){
        selectMode = true;
        notifyDataSetChanged();
    }
    public void exitSelectMode() {
        selectMode = false;
        for (PdfModel recentModel : pdfModels) {
            recentModel.setSelected(false);
        }
        pdfModels.clear();
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
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        PdfModel modelPdf = pdfModels.get(position);
        String title = modelPdf.getTitle();
        String path = modelPdf.getPath();

        String name = modelPdf.getTitle().substring(0, 1).toUpperCase() + modelPdf.getTitle().substring(1);

        holder.pdfTitle.setText(name);
        //holder.pdfSize.setText(modelPdf.getSize());
        //holder.pdfPath.setText(modelPdf.getPdfPath());
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
                    onFileLongClick.onFileLongClick(position);
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
                    //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.logo_background));
                    if (modelPdf.isSelected()){
                        modelPdf.setSelected(true);
                        holder.checkboxImage.setVisibility(View.VISIBLE);
                        holder.option_btn.setVisibility(View.GONE);
                        holder.checkBox.setChecked(true);
                        if (!pdfModels.contains(modelPdf)){
                            pdfModels.add(modelPdf);

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

        /*holder.checkBox.setTag(modelPdf);*/
        //holder.checkBox.setChecked(modelPdf.isSelected());
       /* if (AllPdfFileViewActivity.isContextTualModeEnabled){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    modelPdf.setSelected(!modelPdf.isSelected());
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    if (modelPdf.isSelected()){
                        modelPdf.setSelected(true);
                        holder.checkboxImage.setVisibility(View.VISIBLE);
                        holder.option_btn.setVisibility(View.GONE);
                        holder.checkBox.setChecked(true);
                        if (!pdfModels.contains(modelPdf)){
                            pdfModels.add(modelPdf);

                        }

                    }
                    else{
                        holder.checkboxImage.setVisibility(View.GONE);
                        holder.checkBox.setChecked(false);
                        holder.option_btn.setVisibility(View.VISIBLE);
                    }
                    if (onItemClicks != null){
                        onItemClicks.onItemClick(modelPdf, position);
                    }

                }
            });
            if (modelPdf.isSelected()){
                holder.checkboxImage.setVisibility(View.VISIBLE);
                holder.option_btn.setVisibility(View.GONE);
            }
            else{
                holder.checkboxImage.setVisibility(View.GONE);
                holder.option_btn.setVisibility(View.VISIBLE);
            }

            *//*holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.checkBox.isChecked()){
                        modelPdf.setSelected(true);
                        holder.checkBox.setChecked(true);
                        holder.checkboxImage.setVisibility(View.VISIBLE);

                    }
                    else{
                        holder.checkBox.setChecked(false);
                        holder.checkboxImage.setVisibility(View.GONE);
                        modelPdf.setSelected(false);
                    }
                }
            });



            }*//*


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
            holder.checkboxImage.setVisibility(View.GONE);
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

      /*  try {
            new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (Exception e) {
            e.printStackTrace();
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.file_lock2));
        }*/

    }
    public ArrayList<PdfModel> getSelectedItems() {
        ArrayList<PdfModel> selectedModels = new ArrayList<>();
        for (PdfModel model : pdfModels) {
            if (model.isSelected()) {
                selectedModels.add(model);
            }
        }
        return selectedModels;
    }
    public int getSelectedPosition() {
        for (int i = 0; i < pdfModels.size(); i++) {
            PdfModel pdfModel = pdfModels.get(i);
            if (pdfModel.isSelected()) {
                return i;
            }
        }
        return -1;
    }
    public void addLoadingView() {
        //add loading item
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                pdfModels.add(null);
                notifyItemInserted(pdfModels.size() - 1);
            }
        });
    }

    public void removeLoadingView() {
        //Remove loading item
        pdfModels.remove(pdfModels.size() - 1);
        notifyItemRemoved(pdfModels.size());
    }
    // set isLoading to false when the data has been loaded
    public void setLoaded() {
        boolean isLoading = false;
    }
    // remove the progress bar item when there are no more items to load
    public void setEnd() {
        pdfModels.remove(getItemCount() - 1);
        notifyDataSetChanged();
    }
    public int getSelectedItemCount() {
        int count = 0;
        if (pdfModels != null) {
            for (PdfModel model : pdfModels) {
                if (model.isSelected()) {
                    count++;
                }
            }
        }
        return count;
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
    private void deleteFile(int position, View  view){
        Dialog alertDialog = new Dialog(view.getRootView().getContext());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.delete_layout);

        final TextView textTile, textMessage;
        final Button yesButton, noButton;
        final ImageView close_button;

        textTile = alertDialog.findViewById(R.id.textTitle);
        textMessage = alertDialog.findViewById(R.id.textMessage);
        textMessage.setText(R.string.are_you_sure);
        yesButton = alertDialog.findViewById(R.id.buttonYes);
        noButton = alertDialog.findViewById(R.id.buttonNo);
        close_button = alertDialog.findViewById(R.id.close_btn);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"),
                        Long.parseLong(pdfModels.get(position).getId()));
                File file = new File(pdfModels.get(position).getPath());
                boolean deleted = file.delete();
                if (deleted){
                    context.getApplicationContext().getContentResolver()
                            .delete(contentUri,
                                    null, null);
                    pdfModels.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position,pdfModels.size());
                    Snackbar.make(view, R.string.snake_bar_success,
                            Snackbar.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }else {
                    Snackbar.make(view, R.string.snake_bar_fail,
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
    @SuppressLint("NotifyDataSetChanged")
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
                       /* String dest = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RenameFiles/"+newName;
                        File dir = new File(dest);
                        if (!dir.getParentFile().exists()){
                            dir.getParentFile().mkdir();
                        }*/
                        String paths = pdfModels.get(position).getPath();
                        File sourceFile = new File(paths);
                        File parentDir = sourceFile.getParentFile(); // get the directory of the current file
                        String fileExtension = ".pdf"; // change this to the file extension you want
                        String newFileName = newName + fileExtension;
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
                            pdfModels.remove(position);
                            notifyItemChanged(position);
                            notifyDataSetChanged();
                            successPopup(newFileName, newName, view, position);
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
                notifyDataSetChanged();
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

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;
        private ImageView option_btn, checkboxImage;
        private CheckBox checkBox;
        View view;
        private ImageView imageView;
        private PDFView pdfView;
        private RelativeLayout pdf_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.pdfPath);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            option_btn = itemView.findViewById(R.id.option_icon);
            checkBox = itemView.findViewById(R.id.recent_checkbox);
            imageView = itemView.findViewById(R.id.imageView);
            pdfView = itemView.findViewById(R.id.pdfView);
            checkboxImage = itemView.findViewById(R.id.checking);
            pdf_layout = itemView.findViewById(R.id.deltaRelative);


            view = itemView;

        /*    view.setOnLongClickListener(allPdfFileViewActivity);*/


        }


      /*  @Override
        public void onClick(View view) {
            allPdfFileViewActivity.countSelectedFile(view, getAdapterPosition());
            if (view instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) view;
                PdfModel pdfModel = (PdfModel) checkBox.getTag();
                pdfModel.setSelected(checkBox.isChecked());
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkBox.setChecked(true);
                    }
                });


            }
        }*/
    }


    public interface OnItemClicks{
        void onItemClick(PdfModel pdfModel, int position);
    }
    public interface OnFileLongClick{
        void onFileLongClick(int position);
    }

}

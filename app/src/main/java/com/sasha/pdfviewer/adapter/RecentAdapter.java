package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.ParcelFileDescriptor;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.google.android.material.snackbar.Snackbar;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.view.MainActivity;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.RecentModel;
import com.sasha.pdfviewer.view.PDFViewerActivity;
import com.sasha.pdfviewer.view.RecentViewActivity;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {

    Context context;
    ArrayList<RecentModel> recentModels;
    MainActivity homeFragment;
    OnItemClicks onItemClicks;
    OnFileLongClick onFileLongClick;
    public  boolean selectMode = false;

    public  RecentAdapter(Context context, ArrayList<RecentModel> recentModels,
                          OnItemClicks onItemClicks, OnFileLongClick onFileLongClick
                        ) {
        this.context = context;
        this.recentModels = recentModels;
        this.onItemClicks = onItemClicks;
        this.onFileLongClick = onFileLongClick;

    }
    public void enterSelectMode(){
        selectMode = true;
        notifyDataSetChanged();
    }
    public void exitSelectMode() {
        selectMode = false;
        for (RecentModel recentModel : recentModels) {
            recentModel.setSelected(false);
        }
        recentModels.clear();
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
        View view = LayoutInflater.from(context).inflate(R.layout.recent_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        RecentModel recentModel = recentModels.get(position);
        String name = recentModel.getPdfTitle().substring(0, 1).toUpperCase() + recentModel.getPdfTitle().substring(1);

        holder.pdfTitle.setText(name);
        holder.pdfSize.setText(recentModel.getPdfSize());
        String lastMod = recentModel.getPdfDate();
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(lastMod));
        String lastTime = DateFormat.format("dd-MMM-yyyy", calendar).toString();
        holder.timeStamp.setText(lastTime);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                recentModel.setSelected(!recentModel.isSelected());
                holder.checkboxImage.setVisibility(View.VISIBLE);
                holder.cardView.setVisibility(View.VISIBLE);
               // holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.logo_background));
                holder.option_btn.setVisibility(View.GONE);
                selectMode = true;
                enterSelectMode();
                if (selectMode){
                    recentModel.setSelected(!recentModel.isSelected());
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    holder.cardView.setVisibility(View.VISIBLE);
                    holder.option_btn.setVisibility(View.GONE);

                }

                if (onFileLongClick != null){
                    onFileLongClick.onFileLongClick(position);
                }
                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectMode){
                    recentModel.setSelected(!recentModel.isSelected());
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    holder.cardView.setVisibility(View.VISIBLE);
                    enterSelectMode();
                    //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.logo_background));
                    if (recentModel.isSelected()){
                        recentModel.setSelected(true);
                        holder.checkboxImage.setVisibility(View.VISIBLE);
                        holder.cardView.setVisibility(View.VISIBLE);
                        holder.option_btn.setVisibility(View.GONE);
                        holder.checkBox.setChecked(true);
                        if (!recentModels.contains(recentModel)){
                            recentModels.add(recentModel);

                        }
                    }
                    else{
                        holder.checkboxImage.setVisibility(View.GONE);
                        holder.checkBox.setChecked(false);
                        holder.option_btn.setVisibility(View.VISIBLE);
                        holder.cardView.setVisibility(View.GONE);
                        //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
                    }
                    if (onItemClicks != null){
                        onItemClicks.onItemClick(recentModel, position);
                    }
                }
                else if (!selectMode){
                    exitSelectMode();
                    Intent intent = new Intent(context, RecentViewActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("fileName", recentModel.getPdfTitle());
                    intent.putExtra("filePath", recentModel.getPdfPath().toString());
                    intent.putExtra("fileSize", recentModel.getPdfSize());
                    intent.putExtra("fileId", recentModel.getPdfId());
                    intent.putExtra("date", recentModel.getPdfDate());
                    context.startActivity(intent);


                }
            }
        });
        if (recentModel.isSelected()){
            holder.checkboxImage.setVisibility(View.VISIBLE);
            holder.option_btn.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.VISIBLE);
           // holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.logo_background));
        }
        else{
            holder.checkboxImage.setVisibility(View.GONE);
            holder.option_btn.setVisibility(View.VISIBLE);
            holder.cardView.setVisibility(View.GONE);
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
        }

        if (selectMode){
            holder.image_icon.setVisibility(View.VISIBLE);

        }
        else if (!selectMode){
            holder.image_icon.setVisibility(View.GONE);
            File file = new File(recentModel.getPdfPath());

            try{
                if (file != null){
                    //new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
                    holder.pdfView.fromFile(new File(recentModel.getPdfPath()))
                            .onError(new OnErrorListener() {
                                @Override
                                public void onError(Throwable t) {
                                    holder.pdfView.setVisibility(View.GONE);
                                    holder.image_icon.setVisibility(View.VISIBLE);
                                    holder.image_icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.file_lock2));
                                }
                            })
                            .pages(0).load();
                }

            }
            catch (Exception e){
                e.printStackTrace();
                holder.pdfView.setVisibility(View.GONE);
                holder.image_icon.setVisibility(View.VISIBLE);
                holder.image_icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.file_lock2));
            }
        }





        holder.option_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ImageView delete_icon, flag_icon, edit_icon;
                final TextView delete_text, flag_text, edit_text;
                LinearLayout delete_linear, path_linear, share_button;

                final Dialog dialog = new Dialog(view.getRootView().getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.clear_recent_layout);

                delete_linear = dialog.findViewById(R.id.delete_linear);
                path_linear = dialog.findViewById(R.id.path_linear);
                share_button = dialog.findViewById(R.id.share_linear);
                path_linear.setVisibility(View.GONE);
                share_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareFile(position);
                        dialog.dismiss();
                    }
                });
                delete_linear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearFile(position, v);
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




    }
    public ArrayList<RecentModel> getSelectedItems() {
        ArrayList<RecentModel> selectedModels = new ArrayList<>();
        for (RecentModel model : recentModels) {
            if (model.isSelected()) {
                selectedModels.add(model);
            }
        }
        return selectedModels;
    }
    public int getSelectedPosition() {
        for (int i = 0; i < recentModels.size(); i++) {
            RecentModel pdfModel = recentModels.get(i);
            Uri uriArrayList = Uri.parse(recentModels.get(i).getPdfPath());
            if (pdfModel.isSelected()) {
                return i;
            }
        }
        return -1;
    }

    public void removedSelectedImages(ArrayList<RecentModel> filesToDelete) {

        for (RecentModel recentModel : filesToDelete) {
            if (recentModel.isSelected()){
                recentModels.remove(recentModel);

                notifyDataSetChanged();
            }

        }

    }

    @SuppressLint("SetTextI18n")
    private void clearFile(int p, View view){

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
                File file = new File(recentModels.get(p).getPdfPath());
                file.delete();
                notifyDataSetChanged();
                recentModels.remove(p);
                notifyItemRemoved(p);
                notifyItemRangeChanged(p, recentModels.size());
                alertDialog.dismiss();
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

    private void shareFile(int p){
        Uri uri = Uri.parse(recentModels.get(p).getPdfPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent shareIntent = Intent.createChooser(intent,"Share Pdf. . . ");
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(shareIntent);

    }


    @Override
    public int getItemCount() {
        return recentModels.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;

        private ImageView option_btn, image_icon, checkboxImage;
        private CheckBox checkBox;
        private PDFView pdfView;
        private ProgressBar progressBar;
        private View cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            timeStamp = itemView.findViewById(R.id.file_date);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            pdfView = itemView.findViewById(R.id.pdfView);
            option_btn = itemView.findViewById(R.id.option_icon);
            checkBox = itemView.findViewById(R.id.recent_checkbox);
            image_icon = itemView.findViewById(R.id.image_icon);
            progressBar = itemView.findViewById(R.id.progressbar);
            checkboxImage = itemView.findViewById(R.id.checking);
            cardView = itemView.findViewById(R.id.choose_card);
        }

    }
    public interface OnItemClicks{
        void onItemClick(RecentModel recentModel, int Position);
    }
    public interface OnFileLongClick{
        void onFileLongClick(int position);
    }

}

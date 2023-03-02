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
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.snackbar.Snackbar;
import com.sasha.pdfviewer.view.MainActivity;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.RecentModel;
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

    public  RecentAdapter(Context context, ArrayList<RecentModel> recentModels, MainActivity homeFragment) {
        this.context = context;
        this.recentModels = recentModels;
        this.homeFragment = homeFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, homeFragment);
        return viewHolder;
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




        if (MainActivity.isContextTualModeEnabled){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.option_btn.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!holder.checkBox.isChecked()){
                        holder.checkBox.setChecked(true);
                    }
                    else{
                        holder.checkBox.setChecked(false);
                    }
                }
            });
        }
        else{
            holder.checkBox.setVisibility(View.GONE);
            holder.option_btn.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, RecentViewActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("fileName", recentModel.getPdfTitle());
                    intent.putExtra("filePath", recentModel.getPdfPath().toString());
                    intent.putExtra("fileSize", recentModel.getPdfSize());
                    intent.putExtra("fileId", recentModel.getPdfId());
                    intent.putExtra("date", recentModel.getPdfDate());
                    context.startActivity(intent);

                }
            });
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


        File file = new File(recentModel.getPdfPath());

        try{
            if (file != null){
                new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
                holder.pdfView.fromFile(new File(recentModel.getPdfPath())).defaultPage(0).load();
            }

        }
        catch (Exception e){
            e.printStackTrace();
            holder.pdfView.setVisibility(View.GONE);
            holder.image_icon.setVisibility(View.VISIBLE);
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


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;

        private ImageView option_btn, image_icon;
        MainActivity mainActivity;
        private CheckBox checkBox;
        private PDFView pdfView;
        private ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView, MainActivity mainActivity) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            timeStamp = itemView.findViewById(R.id.file_date);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            pdfView = itemView.findViewById(R.id.pdfView);
            option_btn = itemView.findViewById(R.id.option_icon);
            checkBox = itemView.findViewById(R.id.recent_checkbox);
            image_icon = itemView.findViewById(R.id.image_icon);
            progressBar = itemView.findViewById(R.id.progressbar);
            this.mainActivity = mainActivity;

            itemView.setOnLongClickListener(mainActivity);
            checkBox.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            homeFragment.MakeSelection(v, getAdapterPosition());
        }
    }

}

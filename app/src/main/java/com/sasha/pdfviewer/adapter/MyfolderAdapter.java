package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.view.PdfListActivity;

import java.io.File;
import java.util.ArrayList;

public class MyfolderAdapter extends RecyclerView.Adapter<MyfolderAdapter.ViewHolder> {

    private Context context;
    private ArrayList<File> fileArrayList;

    public MyfolderAdapter(Context context, ArrayList<File> fileArrayList) {
        this.context = context;
        this.fileArrayList = fileArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_folder_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        File file = fileArrayList.get(position);
        holder.folderName.setText(file.getName());
        holder.noOfFile.setText(getPDFCount(file) + " PDF");
        String folderName = file.getName();

        holder.menu_btn.setVisibility(View.VISIBLE);
        

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("folderName", folderName);
                context.startActivity(intent);
            }
        });

        /*if (folderName.equals("New PDF")){
            holder.center_image.setVisibility(View.VISIBLE);
            holder.center_image.setImageDrawable(context.getDrawable(R.drawable.ic_outline_insert_drive_file_24));
        }
        else if (folderName.equals("Converted PDF")){
            holder.center_image.setVisibility(View.VISIBLE);
            holder.center_image.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_arrow_outward_24));
        }
        else if (folderName.equals("WhatsApp Documents")){
            holder.center_image.setVisibility(View.VISIBLE);
            holder.center_image.setImageDrawable(context.getDrawable(R.drawable.ic_outline_message_24));
        }
        else if (folderName.equals("Split PDF")){
            holder.center_image.setVisibility(View.VISIBLE);
            holder.center_image.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_call_split_24));
        }
        else if (folderName.equals("Combined PDF")){
            holder.center_image.setVisibility(View.VISIBLE);
            holder.center_image.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_merge_24));
        }
        else if (folderName.equals("Locked PDF")){
            holder.center_image.setVisibility(View.VISIBLE);
            holder.center_image.setImageDrawable(context.getDrawable(R.drawable.ic_outline_lock_24));
        }
        else if (folderName.equals("Unlocked PDF")){
            holder.center_image.setVisibility(View.VISIBLE);
            holder.center_image.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_lock_open_24));
        }
        else if (folderName.equals("Download")){
            holder.center_image.setVisibility(View.VISIBLE);
            holder.center_image.setImageDrawable(context.getDrawable(R.drawable.ic_outline_download_for_offline_24));
        }
        else if (folderName.equals("Document")){
            holder.center_image.setVisibility(View.VISIBLE);
            holder.center_image.setImageDrawable(context.getDrawable(R.drawable.ic_outline_insert_drive_file_24));
        }
        else if (folderName.equals("Marked PDF")){
            holder.center_image.setVisibility(View.VISIBLE);
            holder.center_image.setImageDrawable(context.getDrawable(R.drawable.ic_outline_bookmarks_24));
        }
        else if (folderName.equals("documents")){
            holder.center_image.setVisibility(View.VISIBLE);
            holder.center_image.setImageDrawable(context.getDrawable(R.drawable.ic_outline_insert_drive_file_24));
        }*/
        holder.menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(view.getRootView().getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.side_popup_layout);
                LinearLayout path_linear, delete_linear, folder_path;
                ImageView flat_icon;
                TextView flat_text;

                path_linear = dialog.findViewById(R.id.path_linear);
                flat_icon = dialog.findViewById(R.id.flag_icon);
                flat_text = dialog.findViewById(R.id.flat_text);
                delete_linear = dialog.findViewById(R.id.delete_linear);
                folder_path = dialog.findViewById(R.id.share_linear);
                folder_path.setVisibility(View.GONE);
                flat_text.setText("Folder");
                flat_icon.setImageDrawable(context.getDrawable(R.drawable.new_folder2));

                delete_linear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String path = String.valueOf(fileArrayList.get(position));
                        deleteFolder(path, position);
                        dialog.dismiss();
                    }
                });
                path_linear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, PdfListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("folderName", folderName);
                        context.startActivity(intent);
                        dialog.dismiss();
                    }
                });



                dialog.show();
                //dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                //dialog.getWindow().getAttributes().windowAnimations = R.style.PopupOptionAnimation;
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setGravity(Gravity.END);
            }
        });
    }
    private int getPDFCount(File folder) {
        int count = 0;
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".pdf")) {
                count++;
            }
        }
        return count;
    }
    private void deleteFolder(String path, int position) {
        boolean isExist = isDirectoryExit(path);
        if (isExist){
            boolean isDeleted = deleteDirectory(path);
            if (isDeleted){
                Toast.makeText(context, "Folder Deleted", Toast.LENGTH_SHORT).show();
                fileArrayList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, fileArrayList.size());

            }
            else{
                Toast.makeText(context, "Delete fail", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(context, "folder already deleted", Toast.LENGTH_SHORT).show();
            fileArrayList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, fileArrayList.size());
        }
    }

    private boolean deleteDirectory(String path) {
        return deleteDirectoryImp(path);
    }

    private boolean deleteDirectoryImp(String path) {
        File directory = new File(path);
        if (directory.exists()){
            File[] files = directory.listFiles();
            if (files == null){
                return true;
            }
            for (int i = 0; i < files.length; i++){
                if (files[i].isDirectory()){
                    deleteDirectoryImp(files[i].getAbsolutePath());
                }
                else{
                    files[i].delete();

                }
            }
        }
        return directory.delete();
    }

    private boolean isDirectoryExit(String path) {
        return new File(path).exists();
    }

    @Override
    public int getItemCount() {
        return fileArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView folderName, noOfFile, pdf_path;
        private ImageView menu_btn, center_image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            folderName = itemView.findViewById(R.id.folderName);
            noOfFile = itemView.findViewById(R.id.no_file);
            menu_btn = itemView.findViewById(R.id.option_btn);
            center_image = itemView.findViewById(R.id.center_image);

        }
    }
}

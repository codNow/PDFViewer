package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.folderList.WordFolderActivity;
import com.sasha.pdfviewer.model.FolderModel;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.utils.FileUtils;
import com.sasha.pdfviewer.view.AllPdfFileViewActivity;
import com.sasha.pdfviewer.view.PdfListActivity;
import com.sasha.pdfviewer.view.SearchActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> folderList;
    public ArrayList<PdfModel> fileList;
    public boolean selectMode = false;
    public boolean isSelected = false;
    OnFileLongClick onFileLongClick;
    OnItemClicks onItemClicks;

    public FolderAdapter(Context context, ArrayList<String> folderList,
                         ArrayList<PdfModel> fileList, OnFileLongClick onFileLongClick, OnItemClicks onItemClicks){
        this.context = context;
        this.folderList = folderList;
        this.fileList = fileList;
        this.onFileLongClick = onFileLongClick;
        this.onItemClicks = onItemClicks;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_folder_layout, parent, false);
        return new ViewHolder(view);
    }

    public void enterSelectMode(){
        selectMode = true;
        notifyDataSetChanged();
    }

    public boolean isSelectMode() {
        return selectMode;
    }
    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        int index = folderList.get(position).lastIndexOf("/");
        String myFolder = folderList.get(position);

        String folderName = folderList.get(position).substring(index+1);
        holder.folderName.setText(folderName);
        holder.noOfFile.setText(String.valueOf(counFiles(folderList.get(position))) + " Pdf");

        PdfModel pdfModel = fileList.get(position);
        
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

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                enterSelectMode();
                selectMode = true;

                holder.cardView.setVisibility(View.VISIBLE);

                if (selectMode){
                    isSelected = true;

                }
                else {
                    holder.cardView.setVisibility(View.GONE);
                }

                if (onFileLongClick != null){
                    onFileLongClick.onFileLongClick(folderList, position);
                }

                return true;
            }

        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectMode){
                    isSelected = true;
                    holder.cardView.setVisibility(View.VISIBLE);
                    //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.logo_background));
                    if (folderList.get(position).contains("Folders")){
                        pdfModel.setSelected(true);
                        holder.cardView.setVisibility(View.VISIBLE);



                    }
                    else{

                        holder.cardView.setVisibility(View.GONE);
                        //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
                    }

                    if (onItemClicks != null){
                        onItemClicks.onItemClick(pdfModel, position);
                    }


                }
                else {

                    Intent intent = new Intent(context, PdfListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("folderName", folderName);
                    context.startActivity(intent);
                }

            }
        });
        String filePath = pdfModel.getPath();
        File file = new File(filePath);
        String parentPath = file.getParentFile().getName();



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
                        String path = folderList.get(position);
                        deleteFolder(path, position);
                        dialog.dismiss();
                    }
                });
                path_linear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, PdfListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("folderName", myFolder);
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

    public ArrayList<PdfModel> getSelectedItems() {
        ArrayList<PdfModel> selectedModels = new ArrayList<>();
        for (PdfModel model : fileList) {
            if (model.isSelected()) {
                selectedModels.add(model);
            }
        }
        return selectedModels;
    }

    public ArrayList<String> getFolderSelected(){
        ArrayList<String> selectedFolders = new ArrayList<>();
        for (int i = 0; i < folderList.size(); i++){
            String selectPath = folderList.get(i);
            selectedFolders.add(selectPath);
        }

        return selectedFolders;
    }

    private void deleteFolder(String path, int position) {
        boolean isExist = isDirectoryExit(path);
        if (isExist){
            boolean isDeleted = deleteDirectory(path);
            if (isDeleted){
                Toast.makeText(context, "Folder Deleted", Toast.LENGTH_SHORT).show();
                folderList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, folderList.size());

            }
            else{
                Toast.makeText(context, "Delete fail", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(context, "folder already deleted", Toast.LENGTH_SHORT).show();
            folderList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, folderList.size());
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
        return folderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView folederImage, check_image, center_image;
        private TextView folderName, noOfFile, pdf_path;
        ImageView menu_btn;
        private RecyclerView recyclerView;
        View view;
        private View cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            folederImage = itemView.findViewById(R.id.folder_image);
            folderName = itemView.findViewById(R.id.folderName);
            noOfFile = itemView.findViewById(R.id.no_file);
            menu_btn = itemView.findViewById(R.id.option_btn);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            center_image = itemView.findViewById(R.id.center_image);
            cardView = itemView.findViewById(R.id.choose_card);


        }


    }
    int counFiles(String folders){
        int count = 0;

        for (PdfModel pdfModel : fileList){

            if (pdfModel.getPath().substring(0,
                    pdfModel.getPath().lastIndexOf("/"))
                    .endsWith(folders)){
                count++;
            }

        }
        return count;
    }

    public interface OnItemClicks{
        void onItemClick(PdfModel pdfModel, int Position);
    }
    public interface OnFileLongClick{
        void onFileLongClick(ArrayList<String> folderModel, int position);
    }
}

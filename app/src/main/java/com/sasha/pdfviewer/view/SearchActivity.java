package com.sasha.pdfviewer.view;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.FolderAdapter;
import com.sasha.pdfviewer.adapter.FolderListAdapter;
import com.sasha.pdfviewer.adapter.MyfolderAdapter;
import com.sasha.pdfviewer.adapter.PdfAdapter;
import com.sasha.pdfviewer.adapter.PdfFolderAdapter;
import com.sasha.pdfviewer.model.FolderModel;
import com.sasha.pdfviewer.model.PdfModel;


import com.sasha.pdfviewer.tools.AllToolsViewActivity;
import com.sasha.pdfviewer.tools.MergedPdfActivity;
import com.sasha.pdfviewer.tools.NewPdfFileActivity;
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SearchActivity extends AppCompatActivity
        implements
        PdfFolderAdapter.OnFileLongClick, PdfFolderAdapter.OnItemClicks{

    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView, listRecylcer;
    private ArrayList<PdfModel> mediaList = new ArrayList<>();
    private ArrayList<String> folderList =new ArrayList<>();
    private FolderAdapter folderAdapter;
    private LinearLayout all_file, sub_linear, delete_linear;
    private ImageView new_file, option_btn, clearButton;
    private TextView selectedText, all_folder_text, folders;
    private MyfolderAdapter myfolderAdapter;
    private AnimationDrawable animationDrawable;
    private LinearLayout downloadLinear, documentLinear, search_linear,
            all_linear, folder_linear, folder_list, list_view, download_list, document_list;
    private PdfAdapter pdfAdapter;
    private ArrayList<String> strings = new ArrayList<>();
    private boolean isSelectMode;
    private boolean isSearchMode = false;
    private ScrollView scrollView;
    private FolderListAdapter folderListAdapter;
    private ArrayList<FolderModel> picFolders = new ArrayList<>();
    private PdfFolderAdapter pdfFolderAdapter;
    private ImageView delete_button;
    private String downloadDir, documentDir;
    private int count = 0;





    @SuppressLint({"NotifyDataSetChanged", "UseCompatLoadingForDrawables", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerView = findViewById(R.id.recyclerView);
        all_file = findViewById(R.id.all_file_linear);
        new_file = findViewById(R.id.new_file);
        downloadLinear = findViewById(R.id.download_linear);
        documentLinear = findViewById(R.id.document_linear);
        scrollView = findViewById(R.id.scrollView);
        all_linear = findViewById(R.id.all_linear);
        clearButton = findViewById(R.id.clear_button);
        folder_list = findViewById(R.id.folder_list);
        folder_linear = findViewById(R.id.folder_linear);
        option_btn = findViewById(R.id.option_btn);
        listRecylcer = findViewById(R.id.listRecycler);
        list_view = findViewById(R.id.list_view);
        download_list = findViewById(R.id.download_list);
        document_list = findViewById(R.id.document_list);
        folders = findViewById(R.id.sub_text);
        delete_button = findViewById(R.id.delete_icon);
        sub_linear = findViewById(R.id.sub_linear);
        delete_linear = findViewById(R.id.delete_linear);
        search_linear = findViewById(R.id.linear_search);




        downloadDir = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        documentDir = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));

        ButtonListener();



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

            try {
                mediaList = fetchAllFile(this);

            } catch (IOException e) {
                e.printStackTrace();
            }
           /* try {
                mediaList = fetchAllFile(this);

            } catch (IOException e) {
                e.printStackTrace();
            }
            folderAdapter = new FolderAdapter(SearchActivity.this, folderList, mediaList);
            recyclerView.setAdapter(folderAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(SearchActivity.this, 3));
            folderAdapter.notifyDataSetChanged();*/

            picFolders = getFileFolderPath();
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            pdfFolderAdapter = new PdfFolderAdapter(this, picFolders, this, this);
            recyclerView.setAdapter(pdfFolderAdapter);

        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
             Set<File> pdfFolders = new HashSet<>();
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            findPdfFolders(Environment.getExternalStorageDirectory(), pdfFolders);
            myfolderAdapter = new MyfolderAdapter(this, new ArrayList<>(pdfFolders));
            recyclerView.setAdapter(myfolderAdapter);
            option_btn.setVisibility(View.GONE);

        }



        showBottom();


/*
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(SearchActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);*/


    }

    private void ButtonListener() {

        search_linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, AllPdfFileViewActivity.class);
                startActivity(intent);
            }
        });

        /*option_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSearchMode){
                    isSearchMode = true;
                    folder_list.setVisibility(View.VISIBLE);
                    folder_linear.setVisibility(View.GONE);
                    folders.setVisibility(View.GONE);
                    option_btn.setImageDrawable(getDrawable(R.drawable.ic_baseline_grid_view_24));

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaList = fetchAllFile(SearchActivity.this);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    folderListAdapter = new FolderListAdapter(SearchActivity.this, folderList, mediaList);
                    recyclerView.setAdapter(folderListAdapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                    recyclerView.setPadding(75, 15, 55, 15);
                    folderListAdapter.notifyDataSetChanged();

                }
                else{

                    folder_list.setVisibility(View.GONE);
                    folder_linear.setVisibility(View.VISIBLE);
                    folders.setVisibility(View.VISIBLE);
                    option_btn.setImageDrawable(getDrawable(R.drawable.ic_baseline_sort_24));

                    folderAdapter = new FolderAdapter(SearchActivity.this, folderList, mediaList);
                    recyclerView.setAdapter(folderAdapter);
                    recyclerView.setPadding(0,0,0,0);
                    recyclerView.setLayoutManager(new GridLayoutManager(SearchActivity.this, 3));
                    folderAdapter.notifyDataSetChanged();

                }
            }
        });*/


        all_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SearchActivity.this, AllPdfFileViewActivity.class));
            }
        });

        list_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SearchActivity.this, AllPdfFileViewActivity.class));
            }
        });

        new_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getBottomToolsLayout();
                //startActivity(new Intent(SearchActivity.this, AllImageActivity.class));
            }
        });

        downloadLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, PdfListActivity.class);
                intent.putExtra("downloadDir", downloadDir);
                startActivity(intent);
            }
        });
        documentLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, PdfListActivity.class);
                intent.putExtra("documentDir", documentDir);
                startActivity(intent);
            }
        });
        download_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, PdfListActivity.class);
                intent.putExtra("downloadDir", downloadDir);
                startActivity(intent);
            }
        });
        document_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, PdfListActivity.class);
                intent.putExtra("documentDir", documentDir);
                startActivity(intent);
            }
        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<FolderModel> selectedModel = pdfFolderAdapter.getSelectedItems();
                int position = pdfFolderAdapter.getSelectedPosition();
                if (!selectedModel.isEmpty()){
                    for (FolderModel pdfModel : selectedModel) {
                        String filePath = pdfModel.getPath();
                        File file = new File(filePath);
                        if (file.exists()) {
                            deleteFolder(filePath, position);


                        }

                        Toast.makeText(SearchActivity.this, filePath, Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
    }

    private void getBottomToolsLayout() {

        Dialog alertDialog = new Dialog(this);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.bottom_tools_layout);

        LinearLayout pdfToWord, newPdf, imageToPdf, pdfToImage, unlockPdf, lockPdf,
                waterMarkPdf, compressPdf, splitPdf, extracted_images;
        ImageView back_btn = alertDialog.findViewById(R.id.back_button);

        pdfToWord = alertDialog.findViewById(R.id.pdfToWord);
        newPdf = alertDialog.findViewById(R.id.newPdf);
        imageToPdf = alertDialog.findViewById(R.id.imagetoPdf);
        pdfToImage = alertDialog.findViewById(R.id.pdftoImage);
        unlockPdf = alertDialog.findViewById(R.id.unlockPdf);
        lockPdf = alertDialog.findViewById(R.id.encryptPdf);
        compressPdf = alertDialog.findViewById(R.id.compressPdf);
        splitPdf = alertDialog.findViewById(R.id.splitPdf);
        waterMarkPdf = alertDialog.findViewById(R.id.waterMark);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        unlockPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, AllToolsViewActivity.class);
                intent.putExtra("unlockBtn", "unlockBtn");
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
        newPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SearchActivity.this, NewPdfFileActivity.class));
                alertDialog.dismiss();
            }
        });
        splitPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, AllToolsViewActivity.class);
                intent.putExtra("splitBtn", "splitBtn");
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
        waterMarkPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, AllImageActivity.class);
                intent.putExtra("waterBtn", "waterBtn");
                startActivity(intent);
                alertDialog.dismiss();
            }
        });

        compressPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, MergedPdfActivity.class);
                intent.putExtra("mergeBtn", "mergeBtn");
                startActivity(intent);
                alertDialog.dismiss();
            }
        });

        lockPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, AllToolsViewActivity.class);
                intent.putExtra("lockBtn", "lockBtn");
                startActivity(intent);
                alertDialog.dismiss();
            }
        });

        pdfToWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, AllToolsViewActivity.class);
                intent.putExtra("wordBtn", "wordBtn");
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
        imageToPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, AllImageActivity.class);
                intent.putExtra("addButton", "addButton");
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
        pdfToImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, AllToolsViewActivity.class);
                intent.putExtra("toImageBtn", "toImageBtn");
                startActivity(intent);
                alertDialog.dismiss();
            }
        });


        alertDialog.show();
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.BottomSlideUpAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private ArrayList<PdfModel> fetchAllFile(Context context) throws IOException {


        ArrayList<PdfModel> fileModel = new ArrayList<>();
        String recentFolder = "RecentFile";


        Uri uri = MediaStore.Files.getContentUri("external");
        String [] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
        };

        String mimeType = "application/pdf";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String sortOrder = MediaStore.Files.FileColumns.DISPLAY_NAME + " DESC";
        String[] selectionArgs = new String[] {"%" + recentFolder + "%"};

        Cursor cursor = context.getContentResolver().query(uri, projection,
                whereClause, null, sortOrder);

        int ids = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);

        if (cursor != null){
            while (cursor.moveToNext()){
                FolderModel folderModel = new FolderModel();
                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                String name = cursor.getString(3);
                String size = cursor.getString(4);
                String date = cursor.getString(5);
                String folderName = cursor.getString(6);
                boolean isSelected = false;
                Uri fileUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), ids);
                PdfModel pdfModel = new PdfModel(id, title, path, size, date, isSelected);

                int slashFirstIndex = path.lastIndexOf("/");
                String subString = path.substring(0, slashFirstIndex);


                if (!folderList.contains(subString) &&
                        !subString.contains("RecentFile") &&
                        !subString.endsWith("storage/emulated/0")){
                    folderList.add(subString);
                }

                if (subString.endsWith("storage/emulated/0")){
                    File file = new File(pdfModel.getPath());
                    Toast.makeText(context, pdfModel.getPath(), Toast.LENGTH_SHORT).show();
                    if (file.getName().endsWith(".pdf") && file.exists()){

                        String fileName = file.getName();
                        File destiny = new File(Environment.
                                getExternalStorageDirectory().getAbsolutePath()
                                + "/New PDF/"+ fileName);


                        if (!destiny.getParentFile().exists()){
                            destiny.getParentFile().mkdir();
                        }


                        FileChannel source = null;
                        FileChannel destination = null;

                        try{
                            source = new FileInputStream(file).getChannel();
                            destination = new FileOutputStream(destiny).getChannel();
                            destination.transferFrom(source, 0, source.size());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (source != null){
                                source.close();
                            }
                            if (destination != null){
                                destination.close();
                            }
                            boolean delete = file.delete();

                            if (delete){
                                context.getApplicationContext().getContentResolver()
                                        .delete(fileUri,
                                                null, null);
                                fileModel.remove(file);
                            }
                        }


                    }
                }






                fileModel.add(pdfModel);


            }
            cursor.close();
        }
        return fileModel;
    }


    private void showBottom() {

        bottomNavigationView.setSelectedItemId(R.id.search_menu);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id){
                    case R.id.home_menu:
                        startActivity(new Intent(SearchActivity.this, MainActivity.class));
                        overridePendingTransition(0, 0);
                        break;

                    case R.id.tools_menu:
                        startActivity(new Intent(SearchActivity.this, ToolsActivity.class));
                        overridePendingTransition(0, 0);
                        break;

                    case R.id.search_menu:
                         return true;

                    /*case R.id.profile_menu:
                        startActivity(new Intent(SearchActivity.this, ProfileActivity.class));
                        overridePendingTransition(0, 0);
                        break;*/

                }

                return false;
            }
        });
    }

    private void findPdfFolders(File dir, Set<File> pdfFolders){
       boolean foundPdf = false;
        for (File file : dir.listFiles()){
            if (file.isDirectory() && !file.getName().equals("RecentFile")){
                findPdfFolders(file, pdfFolders);
            }
            else{
                String fileName = file.getName();
                if (fileName.endsWith(".pdf")){
                    if (!foundPdf){
                        pdfFolders.add(file.getParentFile());
                        foundPdf = true;
                    }
                }
            }

        }
    }

    private ArrayList<FolderModel> getFileFolderPath(){

        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = MediaStore.Files.getContentUri("external");
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA ,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Files.FileColumns.BUCKET_ID,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED
        };

        String mimeType = "application/pdf";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String sortOrder = MediaStore.Files.FileColumns.DISPLAY_NAME + " DESC";

        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, whereClause, null, sortOrder);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }
            do{
                FolderModel folds = new FolderModel();
                int ids = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                Uri fileUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), ids);
                String size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED));

                //String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder+"/"));
                folderpaths = folderpaths+folder+"/";

                boolean isSelected = false;
                PdfModel pdfModel = new PdfModel(id, name, datapath, size, date, isSelected);

                int slashFirstIndex = datapath.lastIndexOf("/");
                String subString = datapath.substring(0, slashFirstIndex);
                if (!picPaths.contains(folderpaths) &&
                        !subString.contains("RecentFile") &&
                        !subString.endsWith("storage/emulated/0")) {
                    picPaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setFolderName(folder);
                    folds.setFirstPic(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.addpics();
                    folds.setSelected(false);


                    picFolders.add(folds);


                }else{
                    for(int i = 0;i<picFolders.size();i++){
                        if(picFolders.get(i).getPath().equals(folderpaths)){
                            picFolders.get(i).setFirstPic(datapath);
                            picFolders.get(i).addpics();
                        }
                    }
                }
            }while(cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(int i = 0;i < picFolders.size();i++){
            Log.d("picture folders",picFolders.get(i).getFolderName()+" and path = "+picFolders.get(i).getPath()+" "+picFolders.get(i).getNumberOfPics());
        }


        return picFolders;
    }

    private void RemoveContextualActionMode() {
        isSelectMode = false;
        pdfFolderAdapter.notifyDataSetChanged();
        sub_linear.setVisibility(View.VISIBLE);
        delete_linear.setVisibility(View.GONE);
        new_file.setVisibility(View.VISIBLE);
        deselectAll();
        //startActivity(getIntent());

    }

    private void deleteFolder(String path, int position) {
        boolean isExist = isDirectoryExit(path);
        if (isExist){
            boolean isDeleted = deleteDirectory(path);
            if (isDeleted){
                Toast.makeText(this, "Folder Deleted", Toast.LENGTH_SHORT).show();
                picFolders.remove(position);
                pdfFolderAdapter.notifyItemRemoved(position);
                pdfFolderAdapter.notifyItemRangeChanged(position, picFolders.size());
                RemoveContextualActionMode();

            }
            else{
                Toast.makeText(this, "Delete fail", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "folder already deleted", Toast.LENGTH_SHORT).show();
            picFolders.remove(position);

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
    private void deselectAll() {
        for (FolderModel item : picFolders) {
            item.setSelected(false);
            if (!item.isSelected()){

            }
        }
        pdfFolderAdapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(FolderModel folderModel, int Position) {

        isSelectMode = true;
        if (folderModel.isSelected()){
            count = count +1;
            updateCount(count);

        }
        else if (!folderModel.isSelected()){
                count = count -1;
                updateCount(count);
        }
        else {
                count = 0;
                updateCount(count);
        }

    }

    @Override
    public void onFileLongClick(FolderModel folderModel1, int position) {

        isSelectMode = true;
        if (isSelectMode){
            delete_button.setVisibility(View.VISIBLE);
            delete_linear.setVisibility(View.VISIBLE);
            new_file.setVisibility(View.GONE);
            sub_linear.setVisibility(View.GONE);
            FolderModel folderModel = picFolders.get(position);
            folderModel.setSelected(!folderModel.isSelected());
            if (folderModel.isSelected()){
                count = count +1;
                updateCount(count);

            }

        }
        else{

            delete_button.setVisibility(View.GONE);
            isSelectMode = false;
        }

    }

    private void updateCount(int count) {

        if (count == 0){
            sub_linear.setVisibility(View.VISIBLE);
            delete_linear.setVisibility(View.GONE);
            new_file.setVisibility(View.VISIBLE);
            isSelectMode = false;
            pdfFolderAdapter.setSelectMode(false);
        }
        else {
            sub_linear.setVisibility(View.GONE);
            delete_linear.setVisibility(View.VISIBLE);
            new_file.setVisibility(View.GONE);
            isSelectMode = true;
        }
    }

    @Override
    public void onBackPressed() {

        if (pdfFolderAdapter.selectMode){
            pdfFolderAdapter.setSelectMode(false);
            RemoveContextualActionMode();
            isSelectMode = false;

        }
        else{
            startActivity(new Intent(SearchActivity.this, MainActivity.class));
            overridePendingTransition(0, 0);
        }

    }
}
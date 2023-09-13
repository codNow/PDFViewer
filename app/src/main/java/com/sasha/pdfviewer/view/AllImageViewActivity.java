package com.sasha.pdfviewer.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.ImageAdapter;
import com.sasha.pdfviewer.model.ImageModel;

import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.tools.ImageViewActivity;
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.ImageUtils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class AllImageViewActivity extends AppCompatActivity
        implements ImageAdapter.OnImageClickListener {

    private ImageAdapter imageAdapter;
    private ArrayList<ImageModel> imageModels = new ArrayList<>();
    private RecyclerView recyclerView;
    private String folderName, allImages;
    private int limitFile = 54;
    private int offset = 0;
    private int count = 0;
    private ProgressBar progressBar, continue_progress;
    private boolean isLoading = false;
    private LinearLayout continue_linear, continue_button;
    private TextView selectText;
    private ImageView continue_btn, left_icon, right_icon, crop_icon, close_btn, delete_icon;
    String folder = Environment.getExternalStorageDirectory()+
            "/CustomFolder/";
    private Dialog compressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_images);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressbar);
        continue_btn = findViewById(R.id.continue_button);
        continue_linear = findViewById(R.id.continue_linear);
        selectText = findViewById(R.id.selectedText);
        continue_progress = findViewById(R.id.continue_progress);
        close_btn = findViewById(R.id.chose_backBtn);
        continue_button = findViewById(R.id.continue_btn);


        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        compressDialog = new Dialog(this);
        compressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        compressDialog.setContentView(R.layout.progress_dialog);
        TextView titleText = compressDialog.findViewById(R.id.loading_text);
        titleText.setText("Please wait........");
        compressDialog.setCancelable(false);
        compressDialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);

        buttonListener();


        if (getIntent().hasExtra("allImages")){
            allImages = getIntent().getStringExtra("allImages");
            recyclerView.setHasFixedSize(true);
            loadImages();
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            imageAdapter = new ImageAdapter(this, imageModels, this);
            recyclerView.setAdapter(imageAdapter);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (!recyclerView.canScrollVertically(1)
                            && newState == RecyclerView.SCROLL_STATE_IDLE
                            && !isLoading) {
                        // Load more items
                        loadImages();

                        progressBar.setVisibility(View.VISIBLE);

                    }
                }
            });
        }

        else if (getIntent().hasExtra("myFolder")){
            folderName = getIntent().getStringExtra("myFolder");
            recyclerView.setHasFixedSize(true);
            imageModels = ImageUtils.allImageFile(this, folderName);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            imageAdapter = new ImageAdapter(this, imageModels, this);
            recyclerView.setAdapter(imageAdapter);
           /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                recyclerView.setHasFixedSize(true);
                imageModels = ImageUtils.allImageFile(this, folderName);
                recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                imageAdapter = new ImageAdapter(this, imageModels, this);
                recyclerView.setAdapter(imageAdapter);



            }
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
                *//*recyclerView.setHasFixedSize(true);
                imageModels = getPdfFileForBelowB(folderName);
                recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                imageAdapter = new ImageAdapter(this, imageModels, this);
                recyclerView.setAdapter(imageAdapter);*//*
                getImageForBelowB(folderName);

            }*/

        }else if (getIntent().hasExtra("images")){
            imageModels = ImageUtils.allPhotosFromDevice(this);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            imageAdapter = new ImageAdapter(this, imageModels, this);
            recyclerView.setAdapter(imageAdapter);

        }

    }

    private void buttonListener() {

        continue_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<ImageModel> selectList = imageAdapter.getSelectImages();
                int position = imageAdapter.getSelectedPosition();

                if (selectList.size() > 20){
                    Toast.makeText(AllImageViewActivity.this, "Please choose upto 20 images", Toast.LENGTH_SHORT).show();
                }
                else if (selectList.isEmpty()){
                    Toast.makeText(AllImageViewActivity.this, "please choose image", Toast.LENGTH_SHORT).show();
                }
                else{
                    new SaveInBackground().execute(selectList.toArray(new ImageModel[selectList.size()]));
                }

            }
        });


    }

    private class SaveInBackground extends AsyncTask<ImageModel, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            compressDialog.show();
        }

        @Override
        protected Boolean doInBackground(ImageModel... imageModels) {
            boolean isDeleted = false;
            int position = imageAdapter.getSelectedPosition();
            for (ImageModel imageModel : imageModels){
                Uri imageUri = imageModel.getUri();
                String uriPath = imageUri.getPath();
                String imageName = imageModel.getImageTitle();
                String folderName = folder + imageName;

                File folder = new File(folderName);
                if (!folder.getParentFile().exists()) {
                    folder.getParentFile().mkdir();
                }

                FileChannel source = null;
                FileChannel destination = null;

                try{
                    source = new FileInputStream(imageModel.getImagePath()).getChannel();
                    destination = new FileOutputStream(folderName + ".jpg").getChannel();
                    destination.transferFrom(source, 0, source.size());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (source != null){
                        try {
                            source.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (destination != null){
                        try {
                            destination.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }
            return isDeleted;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            compressDialog.dismiss();
            Intent intent = new Intent(AllImageViewActivity.this, ImageViewActivity.class);
            startActivity(intent);
        }
    }



    private void loadImages() {
        new Handler( ).postDelayed(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                String[] projection = {
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.TITLE,
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media.DATE_ADDED
                };
                String selection = MediaStore.Images.Media.MIME_TYPE + "=?";
                String[] selectionArgs = new String[]{"image/*"};

                String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
                String limit = limitFile + " OFFSET " + offset;
                //String limit = "10";
                //String offset = String.valueOf(items.size());
                int totalFile = imageModels.size();
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Cursor cursor
                        = createCursor(getContentResolver(),
                        uri,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder,
                        limit,
                        offset);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                        String date = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
                        boolean isSelected = false;
                        int slashFirstIndex = path.lastIndexOf("/");
                        String subString = path.substring(0, slashFirstIndex);


                        ImageModel imageModel = new ImageModel(imageUri, id, title,path, date, isSelected, null);
                        imageModels.add(imageModel);
                        progressBar.setVisibility(View.GONE);

                        int totalCount = cursor.getCount();
                        int remaining = totalCount - limitFile;
                    } while (cursor.moveToNext());
                    cursor.close();
                    imageAdapter.notifyDataSetChanged();
                    if (imageModels.size() <= offset + limitFile && limitFile == 0) {
                        // all items loaded
                        progressBar.setVisibility(View.GONE);
                    }

                    offset += limitFile;

                }
                else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AllImageViewActivity.this, "That's all the data ..", Toast.LENGTH_SHORT).show();
                }
            }
        }, 500);


    }
    private Cursor createCursor(ContentResolver contentResolver,
                                Uri uri, String[] projection,
                                String selection,
                                String[] selectionArgs,
                                String sortOrder,
                                String limit, int offset) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Bundle bundleSelection = createSelectionBundle(projection, selection, selectionArgs, sortOrder);
            return contentResolver.query(uri, projection, bundleSelection, null);

        }
        return contentResolver.query(uri, projection, selection, selectionArgs,
                sortOrder + " LIMIT " + limit );

    }

    private Bundle createSelectionBundle(String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Bundle bundle = new Bundle();
        bundle.putInt(ContentResolver.QUERY_ARG_LIMIT, limitFile);
        bundle.putInt(ContentResolver.QUERY_ARG_OFFSET, offset);
        bundle.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder);
        /*if (sortOrder.equals("ALPHABET")) {
            bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, new String[]{MediaStore.Images.Media.DATE_ADDED});
        } else {
            bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, new String[]{MediaStore.Images.Media.DATE_ADDED});
        }*/


        return bundle;
    }


    private void getImageForBelowB(String folderName){
        recyclerView.setHasFixedSize(true);
        ArrayList<String> pdfPathList = new ArrayList<>();
        imageAdapter = new ImageAdapter(getApplicationContext(), imageModels, this);
        recyclerView.setAdapter(imageAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        ImageModel item ;
        String file_ext = ".jpg";


        try{
            String folderPath =
                    Environment.getExternalStorageDirectory()
                            .getAbsolutePath() +"/"+ folderName;

            File dir = new File(folderPath);

            File listPdf[] = dir.listFiles();

            if (listPdf != null){
                for (int i = 0; i < listPdf.length; i++){
                    File pdf_file = listPdf[i];

                    if (pdf_file.getName().endsWith(file_ext)){
                        item = new ImageModel();
                        item.setUri(Uri.fromFile(pdf_file));
                        item.setImagePath(pdf_file.getPath());
                        item.setImageDate(String.valueOf(pdf_file.lastModified()));

                        imageModels.add(item);
                    }
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }


    }





    private void updateCount(int counts) {
        if (counts == 0){
            selectText.setText("Select Files");
        }
        else{
            selectText.setText(counts + " Selected");
        }

    }


    @Override
    public void onImageClick(ImageModel imageModel, int position) {

        if (imageModel.isSelected()){
            continue_linear.setVisibility(View.VISIBLE);
            continue_btn.setVisibility(View.VISIBLE);

            count = count +1;
            updateCount(count);

            if (count > 0){
                continue_btn.setColorFilter(getColor(R.color.Red_color));

            }
            else if (count < 0)
            {
                continue_btn.setColorFilter(getColor(R.color.blue));
            }


        }
        else if (!imageModel.isSelected()){
            count = count - 1;
            updateCount(count);
        }
        else {
            count = 0;
            updateCount(count);
        }
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, AllImageActivity.class));
        overridePendingTransition(0, 0);
    }


}
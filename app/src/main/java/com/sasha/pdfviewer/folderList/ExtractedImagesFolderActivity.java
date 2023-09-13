package com.sasha.pdfviewer.folderList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.ImageAdapter;
import com.sasha.pdfviewer.adapter.ImageExtractAdapter;
import com.sasha.pdfviewer.adapter.RecentAdapter;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.model.RecentModel;
import com.sasha.pdfviewer.tools.ImageViewActivity;
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.ImageUtils;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ExtractedImagesFolderActivity extends AppCompatActivity
        implements ImageExtractAdapter.OnImageLongClick, ImageExtractAdapter.OnImageClick {

    private ArrayList<ImageModel> imageList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ImageExtractAdapter imageAdapter;
    private String folderName = "ExtractedImages";
    private Uri imageUri;
    private TextView selectedText;
    public boolean isSelectMode = false;
    private ImageView delete_items, share_items, choose_backBtn, selectButton;
    private int count = 0;
    private boolean isSelectAll = false;
    private LinearLayout chooseLayout;
    private ImageView imageView;
    private int limitFile = 24;
    private int offset = 0;
    private boolean isLoading = false;
    private Button fragment_button;
    private boolean doubleBackPressed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extracted_images_folder);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        selectedText = findViewById(R.id.selectedText);
        delete_items = findViewById(R.id.delete_items);
        share_items = findViewById(R.id.share_items);
        choose_backBtn = findViewById(R.id.chose_backBtn);
        selectButton = findViewById(R.id.select_items);
        chooseLayout = findViewById(R.id.chose_linear);
        imageView = findViewById(R.id.imageView);



        toolbar.setTitle("Extracted Images");
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleTextAppearance);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageUri = getIntent().getParcelableExtra("imageUri");

        FragmentManager fragmentManager = getSupportFragmentManager();



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            getImagesFromFolder();

        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            getPdfFileForBelowB();
        }

        buttonListener();
    }
    private void getImagesFromFolder() {
        recyclerView.setHasFixedSize(true);
        imageList = ImageUtils.allImageFile(this, folderName);
        imageAdapter = new ImageExtractAdapter(getApplicationContext(),
                imageList, this, this);
        recyclerView.setAdapter(imageAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(
                ExtractedImagesFolderActivity.this, 3));

    }

    private void getPdfFileForBelowB(){
        recyclerView.setHasFixedSize(true);
        ArrayList<String> pdfPathList = new ArrayList<>();
        imageAdapter = new ImageExtractAdapter(getApplicationContext(), imageList, this, this);
        recyclerView.setAdapter(imageAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        ImageModel item ;
        String file_ext = ".jpg";
        String folderName = "ExtractedImages";

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

                        imageList.add(item);
                    }
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    private void buttonListener() {

        choose_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        selectButton.setOnClickListener(view -> {
            if (!isSelectAll) {
                //ArrayList<PdfModel> selectedFile = multiSelectAdapter.getSelectedItems();
                selectAll();
                //After checking all items change button text
                selectButton.setImageDrawable(getDrawable(R.drawable.deselect_all_icon));
                isSelectAll = true;
                imageAdapter.notifyDataSetChanged();

            } else {
                //If button text is Deselect All remove check from all items
                //multiSelectAdapter.removeSelection();
                deselectAll();
                isSelectAll = false;
                //After checking all items change button text
                selectButton.setImageDrawable(getDrawable(R.drawable.select_all_icon));
                imageAdapter.notifyDataSetChanged();
            }
        });

        share_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<ImageModel> selectedFilePaths = imageAdapter.getSelectImages();
                for (ImageModel imageModel : selectedFilePaths) {
                    String filePath = imageModel.getImagePath();
                    String id = imageModel.getImageId();
                    File file = new File(filePath);
                    ArrayList<Uri> uris = new ArrayList<>();
                    uris.add(Uri.parse(filePath));
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, "Share"));
                }

            }
        });

        delete_items.setOnClickListener(view -> {
            ArrayList<ImageModel> selectedFilePaths = imageAdapter.getSelectImages();
            if (!selectedFilePaths.isEmpty()) {

                Dialog alertDialog = new Dialog(view.getRootView().getContext());
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertDialog.setContentView(R.layout.delete_layout);
                final TextView textTile, textMessage;
                final Button yesButton, noButton;
                final ImageView close_button;

                textTile = alertDialog.findViewById(R.id.textTitle);
                textMessage = alertDialog.findViewById(R.id.textMessage);
                yesButton = alertDialog.findViewById(R.id.buttonYes);
                noButton = alertDialog.findViewById(R.id.buttonNo);
                close_button = alertDialog.findViewById(R.id.close_btn);

                textTile.setText(R.string.delete_file_title);
                textMessage.setText("Are you sure to remove");
                yesButton.setText(R.string.yes);
                noButton.setText(R.string.no);
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onClick(View v) {
                        int position = imageAdapter.getSelectedPosition();
                        imageAdapter.removedSelectedImages(selectedFilePaths);

                        if (selectedFilePaths != null) {
                            for (ImageModel pdfModel : selectedFilePaths) {
                                String filePath = pdfModel.getImagePath();
                                String id = pdfModel.getImageId();

                                File file = new File(pdfModel.getImagePath());
                                if (file.exists()){
                                    file.delete();

                                    imageAdapter.notifyItemRemoved(position);
                                    recyclerView.getAdapter().notifyItemRemoved(position);
                                    count = 0;
                                    updateCount(count);
                                }


                            }
                        } else {
                            Toast.makeText(ExtractedImagesFolderActivity.this, "Please select atleast one", Toast.LENGTH_SHORT).show();
                        }
                        alertDialog.dismiss();
                    }
                });
                noButton.setOnClickListener(v -> alertDialog.dismiss());

                close_button.setOnClickListener(v -> alertDialog.dismiss());

                alertDialog.show();
                alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                alertDialog.getWindow().getAttributes().windowAnimations
                        = R.style.SideMenuAnimation;
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.getWindow().setGravity(Gravity.END);


            }
            else{
                Toast.makeText(ExtractedImagesFolderActivity.this, R.string.select_one, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateCount(int counts) {
        if (counts == 0){
            selectedText.setText("Select Files");
        }
        else{
            selectedText.setText(counts + " Selected");
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private void selectAll() {
        for (ImageModel item : imageList) {
            item.setSelected(true);
            if (item.isSelected()){
                count = count + 1;
                updateCount(count);
            }
        }
        imageAdapter.notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void deselectAll() {
        for (ImageModel item : imageList) {
            item.setSelected(false);
            if (!item.isSelected()){
                count = 0;
                updateCount(count);
            }
        }
        imageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLongClick(ImageModel imageModel, int position) {


        isSelectMode = true;
        if (isSelectMode){
            imageModel = imageList.get(position);
            imageModel.setSelected(!imageModel.isSelected());
            chooseLayout.setVisibility(View.VISIBLE);
            if (imageModel.isSelected()){
                count = count +1;
                updateCount(count);
            }
        }
        else{
            isSelectMode = false;
            chooseLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onImageClick(ImageModel imageModel, int position) {

        if (isSelectMode){
            isSelectMode = true;
            if (imageModel.isSelected()){
                count ++;
                updateCount(count);


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




    }
    @SuppressLint("NotifyDataSetChanged")
    private void RemoveContextualActionMode() {
        isSelectMode = false;
        count = 0;
        chooseLayout.setVisibility(View.GONE);
        selectedText.setText("Select File");
        deselectAll();
        //startActivity(getIntent());
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (imageAdapter.selectMode){
            imageAdapter.setSelectMode(false);
            count = 0;
            isSelectMode = false;
            RemoveContextualActionMode();

        }
        else if (!isSelectMode){
            startActivity(new Intent(ExtractedImagesFolderActivity.this, ToolsActivity.class));
            overridePendingTransition(0, 0);

           /* if (doubleBackPressed){
                //super.onBackPressed();

                startActivity(new Intent(ExtractedImagesFolderActivity.this, ToolsActivity.class));

            }
            else {


                doubleBackPressed = true;

                startActivity(new Intent(ExtractedImagesFolderActivity.this, ExtractedImagesFolderActivity.class));


            }
            new Handler(Looper.getMainLooper())
                    .postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            doubleBackPressed = false;

                        }
                    }, 2000);*/

        }

    }
}
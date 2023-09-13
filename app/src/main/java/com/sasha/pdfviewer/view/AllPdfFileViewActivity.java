package com.sasha.pdfviewer.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.PdfAdapter;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class AllPdfFileViewActivity extends AppCompatActivity implements PdfAdapter.OnFileLongClick, PdfAdapter.OnItemClicks {

    private RecyclerView recyclerView, wordRecyclerView;
    private ArrayList<PdfModel> mediaList = new ArrayList<>();
    private ArrayList<String> folderList =new ArrayList<>();
    private ArrayList<File> fileList =new ArrayList<>();
    private PdfAdapter pdfAdapter;
    private Toolbar toolbar;
    private EditText search_text;
    private ArrayList<PdfModel> selectList = new ArrayList<>();
    private ArrayList<Uri> uriList = new ArrayList<>();
    private int count = 0;
    private TextView selectedText;
    public static boolean isContextTualModeEnabled = false;
    private ImageView voice_icon, back_button, clear_button, search_logo;
    private ArrayList<String> lockList = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout search_layout, choose_layout, search_linear;
    private ArrayList<Uri> uris = new ArrayList<>();
    private NestedScrollView nestedScrollView;
    private ProgressBar loadingBar;
    private ImageView delete_items, share_items, choose_backBtn, selectButton;
    private boolean isSelectAll = false;
    private String defaultText = "Select File";
    boolean isSelectMode;
    ArrayList<String> strings = new ArrayList<>();
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_pdf_file_view);

        selectedText = findViewById(R.id.selectedText);


        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressbar);
        search_linear = findViewById(R.id.linear_search);
        search_layout = findViewById(R.id.search_layout);
        choose_layout = findViewById(R.id.chose_linear);
        search_text = findViewById(R.id.search_editText);
        back_button = findViewById(R.id.back_button);
        clear_button = findViewById(R.id.clear_button);
        search_logo = findViewById(R.id.search_logo);
        delete_items = findViewById(R.id.delete_items);
        share_items = findViewById(R.id.share_items);
        choose_backBtn = findViewById(R.id.chose_backBtn);
        selectButton = findViewById(R.id.select_items);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        buttonListener();

        loadAllPdf();


        progressBar.setVisibility(View.VISIBLE);
        search_layout.setVisibility(View.VISIBLE);
        new CountDownTimer(700, 500){

            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {

                progressBar.setVisibility(View.GONE);

            }
        }.start();
        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence inputText, int i, int i1, int i2) {
                searchFiles(inputText.toString());
                clear_button.setVisibility(View.VISIBLE);

            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


    }

    private void buttonListener() {
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSelectAll) {
                    //ArrayList<PdfModel> selectedFile = multiSelectAdapter.getSelectedItems();
                    selectAll();
                    //After checking all items change button text
                    selectButton.setImageDrawable(getDrawable(R.drawable.deselect_all_icon));
                    isSelectAll = true;
                    delete_items.setColorFilter(getColor(R.color.Red_color));
                    share_items.setColorFilter(getColor(R.color.Red_color));
                    pdfAdapter.notifyDataSetChanged();

                } else {
                    //If button text is Deselect All remove check from all items
                    //multiSelectAdapter.removeSelection();
                    deselectAll();
                    isSelectAll = false;
                    //After checking all items change button text
                    delete_items.setColorFilter(getColor(R.color.image_color));
                    share_items.setColorFilter(getColor(R.color.image_color));
                    selectButton.setImageDrawable(getDrawable(R.drawable.select_all_icon));
                    pdfAdapter.notifyDataSetChanged();
                }
            }
        });
        share_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<PdfModel> selectedFilePaths = pdfAdapter.getSelectedItems();

                for (PdfModel pdfModel : selectedFilePaths) {
                    String filePath = pdfModel.getPath();
                    File file = new File(filePath);
                    Uri uri = FileProvider.getUriForFile(AllPdfFileViewActivity.this, "com.sasha.pdfviewer.fileprovider", file);

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant read permission
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(Intent.createChooser(intent, "Share"));

                    Log.d("paths", uri.getPath());

                }

            }
        });

        delete_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<PdfModel> selectedFilePaths = pdfAdapter.getSelectedItems();

                //new DeleteFilesTask().execute(selectedFilePaths.toArray(new PdfModel[selectedFilePaths.size()]));

                if (count > 0) {
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
                    textMessage.setText(R.string.delete_question);
                    yesButton.setText(R.string.yes);
                    noButton.setText(R.string.no);
                    yesButton.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onClick(View v) {
                            int position = pdfAdapter.getSelectedPosition();
                            ArrayList<PdfModel> selectedFilePaths = pdfAdapter.getSelectedItems();
                            if (selectedFilePaths != null) {
                                for (PdfModel pdfModel : selectedFilePaths) {
                                    String filePath = pdfModel.getPath();
                                    String id = pdfModel.getId();
                                    File file = new File(filePath);
                                    if (file.exists()) {
                                        file.delete();
                                        selectedFilePaths.remove(file);
                                        Uri contentUris = ContentUris.withAppendedId(
                                                MediaStore.Files.getContentUri("external"),
                                                Long.parseLong(id));
                                        getApplicationContext().getContentResolver().delete(
                                                contentUris, null, null
                                        );
                                        //startActivity(getIntent());
                                        mediaList.remove(position);
                                        recyclerView.getAdapter().notifyItemRemoved(position);
                                        count = 0;
                                        updateCount(count);
                                        deselectAll();
                                        isSelectMode = false;
                                        RemoveContextualActionMode();
                                        pdfAdapter.setSelectMode(false);

                                    }

                                }
                            } else {
                                Toast.makeText(AllPdfFileViewActivity.this, "Please select atleast one", Toast.LENGTH_SHORT).show();
                            }
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
                    alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    alertDialog.getWindow().getAttributes().windowAnimations
                            = R.style.SideMenuAnimation;
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.getWindow().setGravity(Gravity.END);


                }
                else{
                    Toast.makeText(AllPdfFileViewActivity.this, "please select atleast one", Toast.LENGTH_SHORT).show();
                }
            }
        });

        choose_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_text.setText("");
                clear_button.setVisibility(View.GONE);

            }
        });
    }


    private void updateCount(int counts) {
        if (counts == 0){
            selectedText.setText("Select Files");
            delete_items.setColorFilter(getColor(R.color.image_color));
            share_items.setColorFilter(getColor(R.color.image_color));

        }
        else{
            selectedText.setText(counts + " Selected");
            delete_items.setColorFilter(getColor(R.color.Red_color));
            share_items.setColorFilter(getColor(R.color.Red_color));
        }

    }
    private void searchFiles(String newText) {
        String inputText = newText.toLowerCase();
        ArrayList<PdfModel> searchList = new ArrayList<>();
        for (PdfModel model : mediaList){
            if (model.getTitle().toLowerCase().contains(inputText)){
                searchList.add(model);
            }
        }
        pdfAdapter.updateSearchList(searchList);
    }
    private void loadAllPdf() {
        mediaList = FileUtils.allPdfFile(this);
        recyclerView.setHasFixedSize(true);
        pdfAdapter = new PdfAdapter(getApplicationContext(), mediaList, strings,  this, this);
        recyclerView.setAdapter(pdfAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        isSelectMode = pdfAdapter.isSelectMode();
        ViewCompat.setNestedScrollingEnabled(recyclerView, true);



    }
/*
  *//*  @Override
    public boolean onLongClick(View v) {
        isContextTualModeEnabled = true;
        search_layout.setVisibility(View.GONE);
        choose_layout.setVisibility(View.VISIBLE);
        pdfAdapter.notifyDataSetChanged();
        return true;
    }*/
    private void refresh() {
        startActivity(new Intent(getIntent()));
        RemoveContextualActionMode();
    }
    private void selectAll() {
        for (PdfModel item : mediaList) {
            item.setSelected(true);
            if (item.isSelected()){
                count = mediaList.size();
                updateCount(count);
            }
        }
        pdfAdapter.notifyDataSetChanged();
    }
    private void deselectAll() {
        for (PdfModel item : mediaList) {
            item.setSelected(false);
            if (!item.isSelected()){
                count = 0;
                updateCount(count);
            }
        }
        pdfAdapter.notifyDataSetChanged();
    }
    private void RemoveContextualActionMode() {
        isSelectMode = false;
        count = 0;
        selectList.clear();
        pdfAdapter.notifyDataSetChanged();
        search_layout.setVisibility(View.VISIBLE);
        choose_layout.setVisibility(View.GONE);
        selectedText.setText("Select File");
        deselectAll();
        //startActivity(getIntent());
        progressBar.setVisibility(View.GONE);
    }
/*
    public void makeSelection(View v, int position) {
        if (((CheckBox)v).isChecked()){
            selectList.add(mediaList.get(position));
            isSelectAll = true;
            count = count + 1;
            updateCount(count);
        }
        else{
            selectList.remove(mediaList.get(position));
            count = count - 1;
            updateCount(count);
        }
        delete_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DeleteFilesTask().execute(selectList.toArray(new PdfModel[selectList.size()]));

            }
        });

        *//*delete_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectList.size() > 0){
                    try {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean isDeleted = selectedFile(selectList, true);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isDeleted){
                                            refresh();
                                            pdfAdapter.notifyItemRemoved(position);
                                            RemoveContextualActionMode();
                                            pdfAdapter.notifyDataSetChanged();
                                            startActivity(getIntent());
                                            //Toast.makeText(getApplicationContext(), selectList.size() + "Deleted success", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                        thread.start();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });*//*

        share_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < selectList.size(); i++){
                    String fileId = selectList.get(i).getId();
                    String filePath = selectList.get(i).getPath();
                    uris.add(Uri.parse(filePath));
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, "Share"));

                }
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

    private class DeleteFilesTask extends AsyncTask<PdfModel, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show progress dialog or perform any setup before deleting files
        }

        @Override
        protected Boolean doInBackground(PdfModel... pdfModels) {
            boolean isDeleted = false;
            for (PdfModel pdfModel : pdfModels) {
                String id = pdfModel.getId();
                String path = pdfModel.getPath();
                uriList.add(Uri.parse(path));
                File files = new File(path);
                if (files.exists()) {
                    isDeleted = files.delete();
                }
                if (isDeleted) {
                    Uri contentUris = ContentUris.withAppendedId(
                            MediaStore.Files.getContentUri("external"),
                            Long.parseLong(id)
                    );
                    getApplicationContext().getContentResolver().delete(
                            contentUris, null, null
                    );
                }
            }
            return isDeleted;
        }

        @Override
        protected void onPostExecute(Boolean isDeleted) {
            super.onPostExecute(isDeleted);
            // Hide progress dialog or perform any UI updates after deleting files
            if (isDeleted) {
                //refresh();
                RemoveContextualActionMode();
                pdfAdapter.notifyDataSetChanged();
                startActivity(getIntent());
                //Toast.makeText(getApplicationContext(), selectList.size() + "Deleted success", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    @Override
    public void onItemClick(PdfModel pdfModel, int position) {
        isSelectMode = true;
        if (pdfModel.isSelected()){
            selectList.add(mediaList.get(position));
            count = count + 1;
            updateCount(count);

            if (count > 0){
                delete_items.setColorFilter(getColor(R.color.Red_color));
                share_items.setColorFilter(getColor(R.color.Red_color));
            }

            Log.d("List", String.valueOf(selectList.size()));



        }
        else if (!pdfModel.isSelected()){
            count = count - 1;
            updateCount(count);
        }
        else {
            selectList.remove(mediaList.get(position));
            count = 0;
            updateCount(count);
            delete_items.setColorFilter(getColor(R.color.image_color));
        }

    }

    @Override
    public void onFileLongClick(int position) {
        isSelectMode = true;
        if (isSelectMode){
            search_layout.setVisibility(View.GONE);
            choose_layout.setVisibility(View.VISIBLE);
            PdfModel pdfModel = mediaList.get(position);
            pdfModel.setSelected(!pdfModel.isSelected());
            if (pdfModel.isSelected()){
                count = count +1;
                updateCount(count);

            }


        }
        else{
            search_layout.setVisibility(View.VISIBLE);
            choose_layout.setVisibility(View.GONE);
            isSelectMode = false;
        }
    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (pdfAdapter.selectMode){
            pdfAdapter.setSelectMode(false);
            count = 0;
            isSelectMode = false;
            RemoveContextualActionMode();
        }
        else if (!isSelectMode){
            startActivity(new Intent(AllPdfFileViewActivity.this, SearchActivity.class));
            overridePendingTransition(0, 0);
        }
    }
}
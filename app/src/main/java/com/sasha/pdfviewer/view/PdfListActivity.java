package com.sasha.pdfviewer.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.sasha.pdfviewer.adapter.PdfListAdapter;
import com.sasha.pdfviewer.model.PdfModel;

import java.io.File;
import java.util.ArrayList;

public class PdfListActivity extends AppCompatActivity implements PdfListAdapter.OnItemClicks, PdfListAdapter.OnFileLongClick {

    private RecyclerView recyclerView;
    private PdfListAdapter pdfAdapter;
    private ArrayList<PdfModel> mediaList = new ArrayList<>();
    private ArrayList<String> folderList = new ArrayList<>();
    private String name;
    private ProgressBar progressBar;
    private ArrayList<PdfModel> selectList = new ArrayList<>();
    private ArrayList<Uri> uriList = new ArrayList<>();
    private int count = 0;
    private TextView selectedText;
    public static boolean isContextTualModeEnabled = false;
    private Toolbar toolbar;
    private ArrayList<Uri> uris = new ArrayList<>();
    private ImageView voice_icon, back_button, clear_button, search_logo;
    private LinearLayout search_layout, choose_layout, search_linear;
    private NestedScrollView nestedScrollView;
    private ImageView delete_items, share_items, choose_backBtn, selectButton;
    private EditText search_text;
    private boolean isSelectAll = false;
    private boolean isSelectMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_list);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressbar);
        name = getIntent().getStringExtra("folderName");
      /*  int index = name.lastIndexOf("/");
        String displayFolderName = name.substring(index + 1);*/
        selectedText = findViewById(R.id.selectedText);
        progressBar.setVisibility(View.VISIBLE);

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

        buttonListener();

        search_layout.setVisibility(View.GONE);


        new CountDownTimer(1500, 1500){

            @Override
            public void onTick(long l) {

            }
            @Override
            public void onFinish() {
                loadFile(name);
                //loadAllPdf();
                progressBar.setVisibility(View.GONE);
                search_layout.setVisibility(View.VISIBLE);
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
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_text.setText("");
                clear_button.setVisibility(View.GONE);

            }
        });

    }



    private void loadFile(String name) {

        mediaList = fetchAllFile(this, name);

        if (name != null && mediaList.size() > 0){
            recyclerView.hasNestedScrollingParent();
            recyclerView.setHasFixedSize(true);
            pdfAdapter = new PdfListAdapter(this, mediaList, folderList, this, this );
            recyclerView.setAdapter(pdfAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,
                    RecyclerView.VERTICAL, false));
        }
        else{
            Toast.makeText(this, "sorry no files", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<PdfModel> fetchAllFile(Context context, String name) {
        progressBar.setVisibility(View.VISIBLE);

        ArrayList<PdfModel> fileModel = new ArrayList<>();

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

        Cursor cursor = context.getContentResolver().query(uri, projection,
                whereClause, null, sortOrder);

        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
        int idPath = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
        int idTitle = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
        int idSize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
        int idFile = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
        int date_added = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);
        int bucket = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String pdfId = cursor.getString(0);
                String path = cursor.getString(1);
                String pdfTitle = cursor.getString(2);
                Long pdfSize = cursor.getLong(3);
                String pdfName = cursor.getString(4);
                String date = cursor.getString(5);
                String bucket_display = cursor.getString(6);

                boolean isSelected = false;
                PdfModel pdfModel = new PdfModel(pdfId, path, pdfTitle, date, convertSize(pdfSize), isSelected);

                if (name.endsWith(bucket_display)) {
                    fileModel.add(pdfModel);
                    progressBar.setVisibility(View.GONE);
                }

            }
            cursor.close();
        }
        return fileModel;
    }

    private String convertSize(Long size) {
        String s = "";
        long kilo = 1024;
        long mega = kilo * kilo;
        long giga = mega * kilo;
        long tera = giga * kilo;
        double kb = (double) size / kilo;
        double mb = kb / kilo;
        double gb = mb / kilo;
        double tb = gb / kilo;
        if (size < kilo) {
            s = size + " Bytes";
        } else if (size >= kilo && size < mega) {
            s = String.format("%.2f", kb) + " KB";
        } else if (size >= mega && size < giga) {
            s = String.format("%.2f", mb) + " MB";
        } else if (size >= giga && size < tera) {
            s = String.format("%.2f", gb) + " GB";
        } else if (size >= tera) {
            s = String.format("%.2f", tb) + " TB";
        }
        return s;
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
                    pdfAdapter.notifyDataSetChanged();

                } else {
                    //If button text is Deselect All remove check from all items
                    //multiSelectAdapter.removeSelection();
                    deselectAll();
                    isSelectAll = false;
                    //After checking all items change button text
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
                    String id = pdfModel.getId();
                    File file = new File(filePath);
                    uris.add(Uri.parse(filePath));
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, "Share"));
                }

            }
        });

        delete_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                            for (PdfModel pdfModel : selectedFilePaths) {
                                String filePath = pdfModel.getTitle();
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
                                    mediaList.remove(position);
                                    recyclerView.getAdapter().notifyItemRemoved(position);
                                    count = 0;
                                    updateCount(count);
                                }
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

                }else
                {
                    Toast.makeText(PdfListActivity.this, R.string.select_request, Toast.LENGTH_SHORT).show();
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
   /* private void loadAllPdf() {

        mediaList = FileUtils.allPdfFile(this);
        recyclerView.setHasFixedSize(true);
        pdfAdapter = new PdfListAdapter(getApplicationContext(), mediaList, folderList, this, this);
        recyclerView.setAdapter(pdfAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        isSelectMode = pdfAdapter.isSelectMode();
        ViewCompat.setNestedScrollingEnabled(recyclerView, true);
    }*/

    private void refresh() {
        startActivity(new Intent(getIntent()));
        RemoveContextualActionMode();
    }
    private void selectAll() {
        for (PdfModel item : mediaList) {
            item.setSelected(true);
            if (item.isSelected()){
                count = count + 1;
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

    }

    @Override
    public void onItemClick(PdfModel pdfModel, int position) {
        isSelectMode = true;
        if (pdfModel.isSelected()){
            count = count + 1;
            updateCount(count);
        }
        else if (!pdfModel.isSelected()){
            count = count - 1;
            updateCount(count);
        }
        else {
            count = 0;
            updateCount(count);
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
            startActivity(new Intent(PdfListActivity.this, SearchActivity.class));
            overridePendingTransition(0, 0);
        }
    }

}

package com.sasha.pdfviewer.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
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

public class PdfListActivity extends AppCompatActivity {

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
    private ImageView delete_items, share_items, choose_backBtn;
    private EditText search_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_list);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressbar);
        name = getIntent().getStringExtra("folderName");
       /* int index = name.lastIndexOf("/");
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


        new CountDownTimer(100, 100){

            @Override
            public void onTick(long l) {

            }
            @Override
            public void onFinish() {
                loadFile(name);
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
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_text.setText("");
                clear_button.setVisibility(View.GONE);

            }
        });

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
    private void loadFile(String name) {

        mediaList = fetchAllFile(this, name);

        if (name != null && mediaList.size() > 0){
            recyclerView.hasNestedScrollingParent();
            recyclerView.setHasFixedSize(true);
            pdfAdapter = new PdfListAdapter(this, mediaList, folderList );
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

    @Override
    protected void onResume() {
        super.onResume();
    }


  /*  @Override
    public boolean onLongClick(View v) {
        isContextTualModeEnabled = true;
        search_layout.setVisibility(View.GONE);
        choose_layout.setVisibility(View.VISIBLE);
        pdfAdapter.notifyDataSetChanged();
        return true;
    }

    public void makeSelection(View v, int adapterPosition) {
        if (((CheckBox)v).isChecked()){
            selectList.add(mediaList.get(adapterPosition));
            count = count + 1;
            updateCount(count);
        }
        else{
            selectList.remove(mediaList.get(adapterPosition));
            count = count - 1;
            updateCount(count);
        }
        delete_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < selectList.size(); i++){
                    String fileId = selectList.get(i).getId();
                    String filePath = selectList.get(i).getTitle();
                    File deleteFile = new File(filePath);
                    deleteFile.delete();
                    selectList.remove(deleteFile);
                    pdfAdapter.notifyItemRemoved(adapterPosition);
                    pdfAdapter.notifyDataSetChanged();
                    startActivity(getIntent());

                }
            }
        });

        share_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < selectList.size(); i++){
                    String fileId = selectList.get(i).getId();
                    String filePath = selectList.get(i).getTitle();
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
            selectedText.setText("Select File");
        }
        else{
            selectedText.setText(counts + " File Selected");
        }

    }


    private void refresh() {
        startActivity(new Intent(getIntent()));
        RemoveContextualActionMode();
    }

    private void RemoveContextualActionMode() {
        isContextTualModeEnabled = false;
        count = 0;
        selectList.clear();
        selectedText.setVisibility(View.GONE);
        pdfAdapter.notifyDataSetChanged();
        toolbar.getMenu().clear();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }*/

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PdfListActivity.this, SearchActivity.class));
        overridePendingTransition(0,0);
    }

}

package com.sasha.pdfviewer.folderList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.AllPdfAdapter;
import com.sasha.pdfviewer.model.PdfModel;

import java.util.ArrayList;

public class MergeFolderActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ArrayList<PdfModel> mediaList = new ArrayList<>();
    private AllPdfAdapter pdfAdapter;
    private Toolbar toolbar;
    private EditText search_text;
    private ProgressBar progressBar;
    private ImageView empty_icon;
    private ArrayList<String> stringArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge_folder);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressbar);
        empty_icon = findViewById(R.id.empty_icon);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Combined PDF");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set the OnClickListener for the back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        progressBar.setVisibility(View.VISIBLE);

        new CountDownTimer(1000, 1000){
            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {
                loadAllPdf();
                progressBar.setVisibility(View.GONE);
            }
        }.start();


    }
    private void loadAllPdf() {
        ArrayList<String> strings = new ArrayList<>();
        mediaList = loadFiles(this);
        recyclerView.setHasFixedSize(true);
        pdfAdapter = new AllPdfAdapter(getApplicationContext(), mediaList);
        recyclerView.setAdapter(pdfAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
    }

    private ArrayList<PdfModel> loadFiles(Context context){

        ArrayList<PdfModel> arrayList = new ArrayList<>();
        mediaList.clear();

        Uri uri = MediaStore.Files.getContentUri("external");
        String [] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED
        };

        String mimeType = "application/pdf";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
        String[] selectionArgs = new String[] { "application/pdf", "/storage/emulated/0/RecentFile" };

        Cursor cursor = context.getContentResolver().query(uri, projection,
                whereClause, null, sortOrder);

        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
        int idPath = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
        int idTitle = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
        int idSize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
        int date_added = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);

        if (cursor != null){
            while (cursor.moveToNext()){
                String pdfId = cursor.getString(idColumn);
                String path = cursor.getString(idPath);
                String title = cursor.getString(idTitle);
                long size = cursor.getLong(idSize);
                String date = cursor.getString(date_added);
                int slashFirstIndex = path.lastIndexOf("/");
                String subString = path.substring(0, slashFirstIndex);

                boolean isSelected = false;
                PdfModel modelPdf = new PdfModel(pdfId, title, path, convertSize(size), date, isSelected);
                if (subString.contains("MergedPdf")) {
                    arrayList.add(modelPdf);
                    progressBar.setVisibility(View.GONE);

                }
            }
        }
        return arrayList;
    }
    private String convertSize(long size) {

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        //menuItem.expandActionView();
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Files");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String input = newText.toLowerCase();
                ArrayList<PdfModel> searchList = new ArrayList<>();
                for (PdfModel model : mediaList){
                    if (model.getTitle().toLowerCase().contains(input)){
                        searchList.add(model);
                    }
                }
                pdfAdapter.updateSearchList(searchList);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
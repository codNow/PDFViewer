package com.sasha.pdfviewer.tools;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.DecryptAdapter;
import com.sasha.pdfviewer.adapter.ExtractImagesAdapter;
import com.sasha.pdfviewer.adapter.ImagePdfAdapter;
import com.sasha.pdfviewer.adapter.LockAdapter;
import com.sasha.pdfviewer.adapter.SplitAdapter;
import com.sasha.pdfviewer.adapter.WaterMarkAdapter;
import com.sasha.pdfviewer.adapter.WordAdapter;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class AllToolsViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<PdfModel> modelArrayList = new ArrayList<>();
    private LockAdapter lockAdapter;
    private WordAdapter wordAdapter;
    private WaterMarkAdapter waterMarkAdapter;
    private SplitAdapter splitAdapter;
    private ExtractImagesAdapter extractImagesAdapter;
    private DecryptAdapter decryptAdapter;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    ArrayList<PdfModel> searchList = new ArrayList<>();
    Intent intent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tools_view);

        recyclerView = findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressbar);
        toolbar.setTitle("");
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleTextAppearance);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        intent = getIntent();

        progressBar.setVisibility(View.VISIBLE);

        new CountDownTimer(1000, 1000){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {

                if (intent.hasExtra("lockBtn")){
                    toolbar.setTitle("Choose file to lock");
                    recyclerView.setHasFixedSize(true);
                    modelArrayList = loadFiles(AllToolsViewActivity.this);
                    recyclerView.setLayoutManager(new LinearLayoutManager(AllToolsViewActivity.this));
                    lockAdapter = new LockAdapter(getApplicationContext(), modelArrayList );
                    recyclerView.setAdapter(lockAdapter);
                    progressBar.setVisibility(View.GONE);

                }
                else if (intent.hasExtra("wordBtn")){
                    toolbar.setTitle("Choose file to convert");
                    recyclerView.setHasFixedSize(true);
                    modelArrayList = FileUtils.allPdfFile(AllToolsViewActivity.this);
                    recyclerView.setLayoutManager(new LinearLayoutManager(AllToolsViewActivity.this));
                    wordAdapter = new WordAdapter(getApplicationContext(), modelArrayList );
                    recyclerView.setAdapter(wordAdapter);
                    progressBar.setVisibility(View.GONE);

                }
                else if (intent.hasExtra("toImageBtn")){
                    toolbar.setTitle("Choose file to extract images");
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(AllToolsViewActivity.this));
                    modelArrayList = loadFiles(AllToolsViewActivity.this);
                    extractImagesAdapter = new ExtractImagesAdapter(getApplicationContext(),modelArrayList);
                    recyclerView.setAdapter(extractImagesAdapter);
                    progressBar.setVisibility(View.GONE);

                }
                else if (intent.hasExtra("splitBtn")){
                    toolbar.setTitle("Choose file to Split");
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(AllToolsViewActivity.this));
                    modelArrayList = loadFiles(AllToolsViewActivity.this);
                    splitAdapter = new SplitAdapter(getApplicationContext(),modelArrayList);
                    recyclerView.setAdapter(splitAdapter);
                    progressBar.setVisibility(View.GONE);

                }
                else if (intent.hasExtra("waterBtn")){
                    toolbar.setTitle("Choose file to mark");
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(AllToolsViewActivity.this));
                    modelArrayList = loadFiles(AllToolsViewActivity.this);
                    waterMarkAdapter = new WaterMarkAdapter(getApplicationContext(),modelArrayList);
                    recyclerView.setAdapter(waterMarkAdapter);
                    progressBar.setVisibility(View.GONE);

                }
                else if (intent.hasExtra("unlockBtn")){
                    toolbar.setTitle("Choose lock file to unlock");
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(AllToolsViewActivity.this));
                    modelArrayList = loadFiles(AllToolsViewActivity.this);
                    decryptAdapter = new DecryptAdapter(getApplicationContext(),modelArrayList);
                    recyclerView.setAdapter(decryptAdapter);
                    progressBar.setVisibility(View.GONE);

                }

            }
        }.start();

    }


    private ArrayList<PdfModel> loadFiles(Context context) {

        ArrayList<PdfModel> arrayList = new ArrayList<>();


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
                if (!subString.contains("RecentFile")) {
                    arrayList.add(modelPdf);

                }

            }
        }
        cursor.close();

        return arrayList;
    }


    private Date convertDate(String date) {

        Date newDate = new Date();
        return newDate;
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
                for (PdfModel model : modelArrayList){
                    if (model.getTitle().toLowerCase().contains(input)){
                        searchList.add(model);
                    }
                }

                if (intent.hasExtra("lockBtn")){
                    lockAdapter.updateSearchList(searchList);
                }
                else if (intent.hasExtra("wordBtn")){
                    wordAdapter.updateSearchList(searchList);
                }
                else if (intent.hasExtra("toImageBtn")){
                    extractImagesAdapter.updateSearchList(searchList);
                }
                else if (intent.hasExtra("splitBtn")){
                    splitAdapter.updateSearchList(searchList);
                }
                else if (intent.hasExtra("waterBtn")){
                    waterMarkAdapter.updateSearchList(searchList);
                }
                else if (intent.hasExtra("unlockBtn")){
                    decryptAdapter.updateSearchList(searchList);
                }

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AllToolsViewActivity.this, ToolsActivity.class));
        overridePendingTransition(0, 0);
    }

}
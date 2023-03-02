package com.sasha.pdfviewer.folderList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.AllPdfAdapter;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.model.RecentModel;
import com.sasha.pdfviewer.view.SearchActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WordFolderActivity extends AppCompatActivity {
    private RecyclerView recyclerView, wordRecyclerView;
    private ArrayList<PdfModel> mediaList = new ArrayList<>();
    private ArrayList<String> folderList =new ArrayList<>();
    private ArrayList<File> fileList =new ArrayList<>();
    private AllPdfAdapter pdfAdapter;
    private Toolbar toolbar;
    private EditText search_text;
    private ArrayList<RecentModel> selectList = new ArrayList<>();
    private ArrayList<Uri> uriList = new ArrayList<>();
    private int count = 0;
    private TextView selectedText;
    public static boolean isContext = false;
    private ImageView voice_icon, back_button, clear_button, search_logo;
    private ArrayList<String> lockList = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout linear_search;
    private ArrayList<File> pdfList;
    private ArrayList<PdfModel> modelPdfs = new ArrayList<>();
    private String name, displayName;
    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int pageSize = 5;
    private int currentPage = PAGE_START;
    int itemCount = 0;
    int limit = 2;
    SwipeRefreshLayout swipeRefresh;
    String pdfId, path, title, date, subString;
    int slashFirstIndex;
    long size;
    boolean isSelected = false;
    ArrayList<PdfModel> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_folder);

        selectedText = findViewById(R.id.selected_text);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressbar);
        linear_search = findViewById(R.id.linear_search);
        search_text = findViewById(R.id.search_editText);
        toolbar = findViewById(R.id.toolbar);
        back_button = findViewById(R.id.back_button);
        search_logo = findViewById(R.id.search_logo);

        progressBar.setVisibility(View.VISIBLE);

        new CountDownTimer(1500, 1500){
            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {
                getAllPdf();
                progressBar.setVisibility(View.GONE);


            }
        }.start();

    }

   /* private ArrayList<PdfModel> fetchData(int currentPage, int pageSize) {
        ArrayList<PdfModel> dataList = new ArrayList<>();
        // Define the projection (the columns to return)
        String [] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED
        };

        // Define the selection criteria
        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String[] selectionArgs = {"application/pdf"};

        // Query the content provider to retrieve the PDF files
        Cursor cursor = getContentResolver()
                .query(MediaStore.Files.getContentUri("external"),
                        projection, selection, selectionArgs, null);

        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
        int idPath = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
        int idTitle = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
        int idSize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
        int date_added = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);
        // Add the PDF files to the dataList
        if (cursor != null) {
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
                dataList.add(modelPdf);


            }
            int totalItems = dataList.size();
            int TOTAL_PAGES = (int) Math.ceil((double) totalItems / pageSize);
            if (currentPage != TOTAL_PAGES) {
                pdfAdapter.addLoadingFooter();
            } else {
                isLastPage = true;
                pdfAdapter.removeLoadingFooter();
            }
            pdfAdapter.addAll(dataList);

        }
        return dataList;

        // Add the data to the adapter

    }*/
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


    private void getAllPdf(){

        ArrayList<String> pdfPathList = new ArrayList<>();
        modelPdfs.clear();

        pdfAdapter = new AllPdfAdapter(getApplicationContext(), modelPdfs);
        recyclerView.setAdapter(pdfAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        pdfAdapter.notifyDataSetChanged();

        PdfModel item ;
        String file_ext = ".doc";
        String name = "ConvertWord";

        try{
            String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+name;
            File dir = new File(folderPath);

            File listPdf[] = dir.listFiles();

            if (listPdf != null){
                for (int i = 0; i < listPdf.length; i++){
                    File pdf_file = listPdf[i];

                    if (pdf_file.getName().endsWith(file_ext)){
                        item = new PdfModel();
                        item.setTitle(pdf_file.getName());
                        item.setPath(pdf_file.getAbsolutePath());
                        item.setDate(String.valueOf(pdf_file.lastModified()));
                        item.setSize(convertSize(pdf_file.length()));

                        modelPdfs.add(item);
                        pdfAdapter.notifyDataSetChanged();

                    }
                }
            }

            modelPdfs.sort(new Comparator<PdfModel>() {
                @Override
                public int compare(PdfModel pdfModel, PdfModel t1) {
                    if (modelPdfs != null) {
                        return pdfModel.getDate().compareTo(t1.getDate());
                    }
                    else{
                        return 0;
                    }
                }
            });
            Collections.reverse(modelPdfs);

            if (modelPdfs.isEmpty()){
                Toast.makeText(this, "No Word Document", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(WordFolderActivity.this, SearchActivity.class));
    }


}
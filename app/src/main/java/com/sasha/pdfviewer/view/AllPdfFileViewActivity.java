package com.sasha.pdfviewer.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.Date;

public class AllPdfFileViewActivity extends AppCompatActivity implements View.OnLongClickListener {

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
    private ImageView delete_items, share_items, choose_backBtn;



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
      /*  back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });*/
        choose_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        progressBar.setVisibility(View.VISIBLE);
        new CountDownTimer(50, 50){

            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {
                loadAllPdf();
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
    private void loadAllPdf() {
        ArrayList<String> strings = new ArrayList<>();
        mediaList = FileUtils.allPdfFile(this);
        recyclerView.setHasFixedSize(true);
        pdfAdapter = new PdfAdapter(getApplicationContext(), mediaList, strings, this);
        recyclerView.setAdapter(pdfAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ViewCompat.setNestedScrollingEnabled(recyclerView, true);
    }
/*
    private ArrayList<PdfModel> loadFiles(Context context){

        ArrayList<PdfModel> arrayList = new ArrayList<>();
        mediaList.clear();
        String recentPath = Environment.getExternalStorageDirectory() +"/RecentFile/";
        File recentFile = new File(recentPath);

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
                if (!subString.contains("RecentFile")) {
                    arrayList.add(modelPdf);
                    progressBar.setVisibility(View.GONE);

                }

            }
        }
        return arrayList;
    }*/

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
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onLongClick(View v) {
        isContextTualModeEnabled = true;
        search_layout.setVisibility(View.GONE);
        choose_layout.setVisibility(View.VISIBLE);
        pdfAdapter.notifyDataSetChanged();
        return true;
    }
    public void makeSelection(View v, int position) {
        if (((CheckBox)v).isChecked()){
            selectList.add(mediaList.get(position));
            count = count + 1;
            updateCount(count);
        }
        else{
            selectList.remove(mediaList.get(position));
            count = count - 1;
            updateCount(count);
        }

        if (count > 0){
            delete_items.setClickable(false);
        }
        else{
            delete_items.setClickable(true);
        }
        delete_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectList.size() > 0){
                    new DeleteFilesTask().execute(selectList.toArray(new PdfModel[selectList.size()]));
                }

            }
        });

        /*delete_items.setOnClickListener(new View.OnClickListener() {
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
        });*/

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
  /*  private boolean selectedFile(ArrayList<PdfModel> list, boolean canDelete){
        for (int i = 0; i < list.size(); i++){
            String id = list.get(i).getId();
            String path = list.get(i).getPath();
            uriList.add(Uri.parse(path));
            File files = new File(path);
            files.delete();
            list.remove(files);
            if (canDelete){
                Uri contentUris = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        Long.parseLong(id)
                );
                File file = new File(path);
                boolean isDeleted = file.delete();
                list.remove(file);
                if (isDeleted){
                    getApplicationContext().getContentResolver().delete(
                            contentUris, null, null
                    );
                    list.remove(i);
                    list.clear();
                    pdfAdapter.notifyItemRemoved(i);
                    pdfAdapter.notifyItemRangeChanged(i, list.size());
                }
            }


        }
        return true;
    }*/
    private void refresh() {
        startActivity(new Intent(getIntent()));
        RemoveContextualActionMode();
    }
    private void RemoveContextualActionMode() {
        isContextTualModeEnabled = false;
        count = 0;
        selectList.clear();
        pdfAdapter.notifyDataSetChanged();
        search_layout.setVisibility(View.VISIBLE);
        choose_layout.setVisibility(View.GONE);
    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (isContextTualModeEnabled){
            RemoveContextualActionMode();
        }
        else if (!isContextTualModeEnabled){
            startActivity(new Intent(AllPdfFileViewActivity.this, SearchActivity.class));
            overridePendingTransition(0, 0);
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
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
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
    }
    private void selectAll() {
        for (PdfModel item : selectList) {
            item.setSelected(true);
        }
        pdfAdapter.notifyDataSetChanged();
    }

    private void deselectAll() {
        for (PdfModel item : selectList) {
            item.setSelected(false);
        }
        pdfAdapter.notifyDataSetChanged();
    }
}
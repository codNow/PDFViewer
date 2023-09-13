package com.sasha.pdfviewer.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
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
import com.sasha.pdfviewer.adapter.PdfListAdapter;
import com.sasha.pdfviewer.adapter.PdfViewAdapter;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class PdfListActivity extends AppCompatActivity implements
        PdfListAdapter.OnItemClicks, PdfListAdapter.OnFileLongClick,
        PdfViewAdapter.OnItemClicks, PdfViewAdapter.OnFileLongClick {

    private RecyclerView recyclerView;
    private PdfListAdapter pdfAdapter;
    private ArrayList<String> folderList = new ArrayList<>();
    private String name;
    private ProgressBar progressBar;
    private ArrayList<PdfModel> selectList = new ArrayList<>();
    private int count = 0;
    private TextView selectedText;
    private Toolbar toolbar;
    private ArrayList<Uri> uris = new ArrayList<>();
    private LinearLayout choose_layout;
    private ImageView delete_items, share_items, choose_backBtn, selectButton, empty_icon;
    private boolean isSelectAll = false;
    private boolean isSelectMode;
    private ArrayList<PdfModel> pdfModels = new ArrayList<>();
    private PdfViewAdapter pdfViewAdapter;
    private TextView noOfFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_list);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressbar);
        toolbar = findViewById(R.id.toolbar);
        selectedText = findViewById(R.id.selectedText);
        progressBar.setVisibility(View.VISIBLE);
        choose_layout = findViewById(R.id.chose_linear);
        delete_items = findViewById(R.id.delete_items);
        share_items = findViewById(R.id.share_items);
        choose_backBtn = findViewById(R.id.chose_backBtn);
        selectButton = findViewById(R.id.select_items);
        empty_icon = findViewById(R.id.empty_icon);



        

        if (getIntent().hasExtra("downloadDir")){
            name = getIntent().getStringExtra("downloadDir");
            toolbar.setTitle("Downloads");
            toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleTextAppearance);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            getPdfFileFolders(name);

        }
        else if (getIntent().hasExtra("documentDir")){
            name = getIntent().getStringExtra("documentDir");
            toolbar.setTitle("Documents");
            toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleTextAppearance);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            getPdfFileFolders(name);

        }
        else if (getIntent().hasExtra("folderName")){

            name = getIntent().getStringExtra("folderName");
            int index = name.lastIndexOf("/");
            String displayFolderName = name.substring(index + 1);
            toolbar.setTitle(displayFolderName);
            toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleTextAppearance);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            getPdfFileFolders(name);



        }

        buttonListener();

        choose_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });



        new CountDownTimer(1000, 500){

            @Override
            public void onTick(long l) {

            }
            @Override
            public void onFinish() {

                progressBar.setVisibility(View.GONE);
                //getPdfFileFolders(name);

            }
        }.start();


    }

    private void getPdfFileFolders(String name) {


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
            loadFile(name);

        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            getPdfFileForBelowB(name);

        }
    }


    private void loadFile(String name) {

        if (name != null) {
            pdfModels = FileUtils.fetchPdfFromFolder(this, name);
            recyclerView.hasNestedScrollingParent();
            recyclerView.setHasFixedSize(true);
            pdfAdapter = new PdfListAdapter(this, pdfModels, folderList, this, this);
            recyclerView.setAdapter(pdfAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,
                    RecyclerView.VERTICAL, false));



        }
        if (pdfModels.isEmpty()){
            empty_icon.setVisibility(View.VISIBLE);
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
        String selection = MediaStore.Files.FileColumns.DATA + " like ? ";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String[] selectionArgs = new String[] {"%" + name + "%"};
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        Cursor cursor = context.getContentResolver().query(uri, projection,
                selection, selectionArgs, sortOrder);

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

                fileModel.add(pdfModel);



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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
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
                    /*for (PdfModel pdfModel : selectedFilePaths) {
                        String filePath = pdfModel.getPath();
                        String id = pdfModel.getId();
                        File file = new File(filePath);
                        uris.add(Uri.parse(filePath));
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("application/pdf");
                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(Intent.createChooser(intent, "Share"));
                    }*/
                    for (PdfModel pdfModel : selectedFilePaths) {
                        String filePath = pdfModel.getPath();
                        File file = new File(filePath);
                        Uri uri = FileProvider.getUriForFile(PdfListActivity.this, "com.sasha.pdfviewer.fileprovider", file);

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
                                        pdfModels.remove(position);
                                        recyclerView.getAdapter().notifyItemRemoved(position);
                                        count = 0;
                                        updateCount(count);
                                        isSelectMode = false;
                                        RemoveContextualActionMode();
                                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
                                            pdfAdapter.setSelectMode(false);
                                        }
                                        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
                                            pdfViewAdapter.setSelectMode(false);
                                        }
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
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isSelectAll) {
                        //ArrayList<PdfModel> selectedFile = multiSelectAdapter.getSelectedItems();
                        selectAll();
                        //After checking all items change button text
                        selectButton.setImageDrawable(getDrawable(R.drawable.deselect_all_icon));
                        isSelectAll = true;
                        pdfViewAdapter.notifyDataSetChanged();

                    } else {
                        //If button text is Deselect All remove check from all items
                        //multiSelectAdapter.removeSelection();
                        deselectAll();
                        isSelectAll = false;
                        //After checking all items change button text
                        selectButton.setImageDrawable(getDrawable(R.drawable.select_all_icon));
                        pdfViewAdapter.notifyDataSetChanged();
                    }
                }
            });
            share_items.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<PdfModel> selectedFilePaths = pdfViewAdapter.getSelectedItems();
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
                                int position = pdfViewAdapter.getSelectedPosition();
                                ArrayList<PdfModel> selectedFilePaths = pdfViewAdapter.getSelectedItems();
                                for (PdfModel pdfModel : selectedFilePaths) {
                                    String filePath = pdfModel.getPath();
                                    String id = pdfModel.getId();
                                    File file = new File(filePath);
                                    if (file.exists()) {
                                        file.delete();
                                        selectedFilePaths.remove(file);
                                   /* Uri contentUris = ContentUris.withAppendedId(
                                            MediaStore.Files.getContentUri("external"),
                                            Long.parseLong(id));
                                    getApplicationContext().getContentResolver().delete(
                                            contentUris, null, null
                                    );*/
                                        pdfModels.remove(position);
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



    }
    private void getPdfFileForBelowB(String name){
        recyclerView.setHasFixedSize(true);
        pdfViewAdapter = new PdfViewAdapter(getApplicationContext(), pdfModels, this, this);
        recyclerView.setAdapter(pdfViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        PdfModel item ;
        String file_ext = ".pdf";

        try{
            String folderPath =
                    Environment.getExternalStorageDirectory()
                            .getAbsolutePath()+"/"+name;
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

                        pdfModels.add(item);
                    }
                }
            }

            if (pdfModels.isEmpty()){
                empty_icon.setVisibility(View.VISIBLE);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
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
        for (PdfModel model : pdfModels){
            if (model.getTitle().toLowerCase().contains(inputText)){
                searchList.add(model);
            }
        }
        pdfViewAdapter.updateSearchList(searchList);
    }

    private void selectAll() {
        for (PdfModel item : pdfModels) {
            item.setSelected(true);
            if (item.isSelected()){
                count = pdfModels.size();
                updateCount(count);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            pdfAdapter.notifyDataSetChanged();

        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            pdfViewAdapter.notifyDataSetChanged();

        }
    }
    private void deselectAll() {
        for (PdfModel item : pdfModels) {
            item.setSelected(false);
            if (!item.isSelected()){
                count = 0;
                updateCount(count);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            pdfAdapter.notifyDataSetChanged();

        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            pdfViewAdapter.notifyDataSetChanged();

        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private void RemoveContextualActionMode() {
        isSelectMode = false;
        count = 0;
        selectList.clear();


        choose_layout.setVisibility(View.GONE);
        selectedText.setText("Select File");
        deselectAll();
        //startActivity(getIntent());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            pdfAdapter.notifyDataSetChanged();

        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            pdfViewAdapter.notifyDataSetChanged();

        }

    }

    @Override
    public void onItemClick(PdfModel pdfModel, int position) {
        isSelectMode = true;
        if (pdfModel.isSelected()){
            count = count + 1;
            updateCount(count);
            delete_items.setColorFilter(getColor(R.color.Red_color));
            share_items.setColorFilter(getColor(R.color.Red_color));
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
            choose_layout.setVisibility(View.VISIBLE);
            PdfModel pdfModel = pdfModels.get(position);
            pdfModel.setSelected(!pdfModel.isSelected());
            if (pdfModel.isSelected()){
                count = count +1;
                updateCount(count);
            }
        }
        else{
            choose_layout.setVisibility(View.GONE);
            isSelectMode = false;
        }
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
                for (PdfModel model : pdfModels){
                    if (model.getTitle().toLowerCase().contains(input)){
                        searchList.add(model);
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    pdfAdapter.updateSearchList(searchList);

                }
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
                    pdfViewAdapter.updateSearchList(searchList);

                }


                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            if (pdfAdapter.selectMode) {
                pdfAdapter.setSelectMode(false);
                count = 0;
                isSelectMode = false;
                RemoveContextualActionMode();
            } else if (!isSelectMode) {
                startActivity(new Intent(PdfListActivity.this, SearchActivity.class));
                overridePendingTransition(0, 0);
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (pdfViewAdapter.selectMode) {
                pdfViewAdapter.setSelectMode(false);
                count = 0;
                isSelectMode = false;
                RemoveContextualActionMode();
            } else if (!isSelectMode) {
                startActivity(new Intent(PdfListActivity.this, SearchActivity.class));
                overridePendingTransition(0, 0);
            }

        }


    }

}

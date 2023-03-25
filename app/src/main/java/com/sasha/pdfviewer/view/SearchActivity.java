package com.sasha.pdfviewer.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.FolderAdapter;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.model.RecentModel;
import com.sasha.pdfviewer.tools.CameraFolderActivity;
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.RealPathUtil;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity{

    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    private ArrayList<PdfModel> mediaList = new ArrayList<>();
    private ArrayList<RecentModel> recentModels = new ArrayList<>();
    private ArrayList<String> folderList =new ArrayList<>();
    private FolderAdapter folderAdapter;
    private LinearLayout all_file, drive_file;
    private ProgressBar progressBar;
    private ActivityResultLauncher<Intent> resultLauncher;
    private ImageView new_file;
    public static boolean isContextModeEnabled = false;
    private TextView selectedText, all_folder_text;
    private Toolbar toolbar;
    private ArrayList<String> selectList = new ArrayList<>();
    private int count = 0;
    private static final int STORAGE_PERMISSION_CODE = 111;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerView = findViewById(R.id.recyclerView);
        all_file = findViewById(R.id.all_file_linear);
        new_file = findViewById(R.id.new_file);
        all_folder_text = findViewById(R.id.all_folder_text);
        progressBar = findViewById(R.id.progressbar);



        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        all_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, AllPdfFileViewActivity.class));
            }
        });

        new_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SearchActivity.this, CameraFolderActivity.class));
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaList = fetchAllFile(this);
        }
        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            {
                mediaList = fetchAllFile(this);
            }
        }
        if (folderList != null && folderList.size() > 0 && mediaList != null){
            folderAdapter = new FolderAdapter(this, folderList, mediaList);
            recyclerView.setAdapter(folderAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,
                    RecyclerView.VERTICAL, false));
            folderAdapter.notifyDataSetChanged();
        }
        else{
            Toast.makeText(this, "Sorry no files", Toast.LENGTH_SHORT).show();
        }

        showBottom();

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Uri uri = result.getData().getData();

                            Context context = SearchActivity.this;
                            String realPath = RealPathUtil.getRealPath(context, uri);
                            Cursor returnCursor = getApplicationContext().getContentResolver().query(
                                    uri, null, null, null, null);
                            int nameIndex = returnCursor.getColumnIndexOrThrow(
                                    MediaStore.Files.FileColumns.DISPLAY_NAME);
                            returnCursor.moveToFirst();
                            String file_name = returnCursor.getString(nameIndex);
                            Intent intent = new Intent(getApplicationContext(), PdfViewActivity.class);
                            intent.setData(uri);
                            intent.putExtra("uriFile", uri);
                            intent.putExtra("uriPath", realPath);
                            intent.putExtra("uriName", file_name);
                            startActivity(intent);

                        }
                    }
                });

    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private ArrayList<PdfModel> fetchAllFile(Context context){


        ArrayList<PdfModel> fileModel = new ArrayList<>();
        String recentFolder = "RecentFile";


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
        String[] selectionArgs = new String[] {"%" + recentFolder + "%"};

        Cursor cursor = context.getContentResolver().query(uri, projection,
                whereClause, null, sortOrder);

        if (cursor != null){
            while (cursor.moveToNext()){
                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                String name = cursor.getString(3);
                String size = cursor.getString(4);
                String date = cursor.getString(5);
                String folderName = cursor.getString(6);
                boolean isSelected = false;
                PdfModel pdfModel = new PdfModel(id, title, path, size, date, isSelected);

                int slashFirstIndex = path.lastIndexOf("/");
                String subString = path.substring(0, slashFirstIndex);

                if (!folderList.contains(subString) && !subString.contains("RecentFile")){
                    folderList.add(subString);
                }

                fileModel.add(pdfModel);


            }
            cursor.close();
        }
        return fileModel;
    }

    private void getPdfFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        resultLauncher.launch(intent);
    }
    private void showBottom() {

        bottomNavigationView.setSelectedItemId(R.id.search_menu);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id){
                    case R.id.home_menu:
                        startActivity(new Intent(SearchActivity.this, MainActivity.class));
                        overridePendingTransition(0, 0);
                        break;

                    case R.id.tools_menu:
                        startActivity(new Intent(SearchActivity.this, ToolsActivity.class));
                        overridePendingTransition(0, 0);
                        break;

                    case R.id.search_menu:
                         return true;

                    /*case R.id.profile_menu:
                        startActivity(new Intent(SearchActivity.this, ProfileActivity.class));
                        overridePendingTransition(0, 0);
                        break;*/

                }

                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SearchActivity.this, MainActivity.class));
    }
}
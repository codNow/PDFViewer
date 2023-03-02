package com.sasha.pdfviewer.view;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.RecentAdapter;
import com.sasha.pdfviewer.folderList.WaterMarkActivity;
import com.sasha.pdfviewer.folderList.WordFolderActivity;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.model.RecentModel;
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.RealPathUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener {

    private BottomNavigationView bottomNavigationView;

    private ImageView new_file, close_btn;
    private ActivityResultLauncher<Intent> resultLauncher;
    private LinearLayout floating_window;
    private ArrayList<RecentModel> modelPdfs = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView no_recent, clear_button;
    private ProgressBar progressBar;
    private RecentAdapter pdfAdapter;
    private Toolbar toolbar;
    int count = 0;
    private TextView app_name, selectedText, recent_first;
    public static boolean isContextTualModeEnabled = false;
    private ImageView notification, app_logo, delete_items, shareItems;
    ArrayList<RecentModel> selectList = new ArrayList<>();
    LinearLayout recent_linear, chose_linear;
    TextView selectTextView;
    ArrayList<Uri> uris = new ArrayList<>();
    private boolean doubleBackPressed = false;
    private final static String READ_EXTERNAL_STORAGE =
            "android.permission.READ_EXTERNAL_STORAGE";
    private int STORAGE_PERMISSION_CODE = 101;
    private ArrayList<RecentModel> recentList = new ArrayList<>();
    private RelativeLayout main_page;
    private CoordinatorLayout coordinatorLayout;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSION_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private ImageView app_logos, chose_backBtn;
    static boolean stackAndTrackingEnabled = false;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        bottomNavigationView = findViewById(R.id.bottom_navigation);
        new_file = findViewById(R.id.new_file);
        close_btn = findViewById(R.id.close_btn);
        floating_window = findViewById(R.id.floating_window);
        recyclerView = findViewById(R.id.recyclerView);
        no_recent = findViewById(R.id.no_recent_file);
        progressBar = findViewById(R.id.progressbar);
        clear_button = findViewById(R.id.clear_button);
        coordinatorLayout = findViewById(R.id.ordinate_display);
        recent_linear = findViewById(R.id.recent_linear);
        chose_linear = findViewById(R.id.chose_linear);
        delete_items = findViewById(R.id.delete_items);
        selectedText = findViewById(R.id.text_select);
        selectTextView = findViewById(R.id.selectedText);
        shareItems = findViewById(R.id.share_items);
        recent_first = findViewById(R.id.first_recent);
        app_logo = findViewById(R.id.app_logo);
        app_name = findViewById(R.id.app_name);
        toolbar = findViewById(R.id.toolbar);
        main_page = findViewById(R.id.home_page);
        chose_backBtn = findViewById(R.id.chose_backBtn);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        verifyStoragePermission(this);
        isStoragePermissionGranted();

        new_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getPdfFile();
                startActivity(new Intent(MainActivity.this, WaterMarkActivity.class));
            }
        });

        showBottom();
        recentNote();
        chose_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFile(view);
            }
        });

        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Uri uri = result.getData().getData();

                            Context context = MainActivity.this;
                            String realPath = RealPathUtil.getRealPathFromURI_API19(context, uri);
                            Cursor returnCursor = getApplicationContext().getContentResolver().query(
                                    uri, null, null, null, null);
                            int nameIndex = returnCursor.getColumnIndexOrThrow(
                                    MediaStore.Files.FileColumns.DISPLAY_NAME);
                            returnCursor.moveToFirst();
                            String file_name = returnCursor.getString(nameIndex);
                            Intent intent = new Intent(getApplicationContext(), PdfViewActivity.class);
                            intent.setData(uri);
                            intent.putExtra("file", uri);
                            intent.putExtra("uriPath", realPath);
                            intent.putExtra("uriName", file_name);
                            startActivity(intent);

                        }
                    }
                });
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floating_window.setVisibility(View.GONE);
            }
        });
    }
    public  boolean isStoragePermissionGranted() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

            return true;
        } else {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }

    }

    private void recentNote() {
        SharedPreferences prefs = getSharedPreferences("MyPres", Context.MODE_PRIVATE);
        boolean showNote = prefs.getBoolean("showNote", true);
        if (showNote){
            recent_first.setVisibility(View.VISIBLE);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recent_first.setVisibility(View.GONE);
                }
            }, 4000);
            SharedPreferences preferences = getSharedPreferences("MyPres", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("showNote", false);
            editor.apply();
            showNote = false;
        }
        else{
            recent_first.setVisibility(View.GONE);
        }
    }


    //Permissions Check
    public void verifyStoragePermission(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE);

       /* if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]
                    {READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            ActivityCompat.requestPermissions(this, new String[]
                    {WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

        }*/

        // Surrounded with if statement for Android R to get access of complete file.
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager() && permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSION_STORAGE,
                        REQUEST_EXTERNAL_STORAGE);

                // Abruptly we will ask for permission once the application is launched for sake demo.
                Intent intent = new Intent();
                intent.setAction(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }


    public  void clearFile(View view) {

        final TextView textTile, textMessage;
        final Button yesButton, noButton;
        final ImageView close_button;

        Dialog alertDialog = new Dialog(view.getRootView().getContext());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.delete_layout);

        textTile = alertDialog.findViewById(R.id.textTitle);
        textMessage = alertDialog.findViewById(R.id.textMessage);
        yesButton = alertDialog.findViewById(R.id.buttonYes);
        noButton = alertDialog.findViewById(R.id.buttonNo);
        close_button = alertDialog.findViewById(R.id.close_btn);

        textTile.setText("Clear Recent");
        textMessage.setText("Are You Sure To Clear Recent List?");
        yesButton.setText("Yes");
        noButton.setText("No");
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Environment.getExternalStorageDirectory()+"/RecentFile/";
                File deleteFile = new File(path);
                if (deleteFile.exists()){
                    String deleteCmd = "rm -r " + path;
                    Runtime runtime = Runtime.getRuntime();
                    try{
                        runtime.exec(deleteCmd);
                        alertDialog.dismiss();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                    pdfAdapter.notifyDataSetChanged();
                    modelPdfs.clear();
                    alertDialog.dismiss();

                }
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
        alertDialog.getWindow().setGravity(Gravity.END);

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

    private void getPdfFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        resultLauncher.launch(intent);
    }



    private void showBottom() {

        bottomNavigationView.setSelectedItemId(R.id.home_menu);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id){
                    case R.id.home_menu:
                        return true;

                    case R.id.tools_menu:
                        startActivity(new Intent(MainActivity.this, ToolsActivity.class));
                        overridePendingTransition(0, 0);
                        break;

                    case R.id.search_menu:
                        startActivity(new Intent(MainActivity.this, SearchActivity.class));
                        overridePendingTransition(0, 0);
                        break;

                    /*case R.id.profile_menu:
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                        overridePendingTransition(0, 0);
                        break;*/

                }

                return false;
            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public boolean onLongClick(View v) {
        isContextTualModeEnabled = true;
        pdfAdapter.notifyDataSetChanged();
        new_file.setVisibility(View.GONE);
        if (isContextTualModeEnabled){
            recent_linear.setVisibility(View.GONE);
            chose_linear.setVisibility(View.VISIBLE);
        }
        else{
            recent_linear.setVisibility(View.VISIBLE);
            chose_linear.setVisibility(View.GONE);
        }
        return true;
    }

    public void MakeSelection(View v, int position) {
        if (((CheckBox)v).isChecked()){
            selectList.add(modelPdfs.get(position));
            count = count +1;
            updateCount(count);
        }
        else{
            selectList.remove(modelPdfs.get(position));
            count = count - 1;
            updateCount(count);
        }

        delete_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                textTile.setText("Clear Recent");
                textMessage.setText("Are You Sure To Clear?");
                yesButton.setText("Yes");
                noButton.setText("No");
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectList.size() > 0){
                            try {
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean isDeleted = selectedFile(selectList, true);
                                        runOnUiThread(new Runnable() {
                                            @SuppressLint("NotifyDataSetChanged")
                                            @Override
                                            public void run() {
                                                if (isDeleted){
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
                alertDialog.getWindow().setGravity(Gravity.END);

            }
        });

        shareItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < selectList.size(); i++){
                    String id = selectList.get(i).getPdfId();
                    String path = selectList.get(i).getPdfPath();
                    uris.add(Uri.parse(path));
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, "Share"));

                }
            }
        });
    }
    private boolean selectedFile(ArrayList<RecentModel> list, boolean canDelete){
        for (int i = 0; i < list.size(); i++){
            String id = list.get(i).getPdfId();
            String path = list.get(i).getPdfPath();
            uris.add(Uri.parse(path));
            File files = new File(path);
            files.delete();
            list.remove(files);
            if (canDelete){
                /*Uri contentUris = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        Long.parseLong(id)
                );*/
                File file = new File(path);
                boolean isDeleted = file.delete();
                list.remove(file);
                /*if (isDeleted){
                    getApplicationContext().getContentResolver().delete(
                            contentUris, null, null
                    );
                }*/
            }
        }
        return true;
    }
    private void updateCount(int counts) {
        if (counts == 0){
            selectTextView.setText("Select");
        }
        else {
            selectTextView.setText(counts + " Selected");
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.profile_menu:
                startActivity(new Intent(this, ProfileActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<String> pdfPathList = new ArrayList<>();
        modelPdfs.clear();

        pdfAdapter = new RecentAdapter(getApplicationContext(), modelPdfs, MainActivity.this);
        recyclerView.setAdapter(pdfAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                RecyclerView.VERTICAL, false));
        pdfAdapter.notifyDataSetChanged();

        RecentModel item ;
        String file_ext = ".pdf";
        String name = "RecentFile";

        try{
            String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+name;
            File dir = new File(folderPath);

            File listPdf[] = dir.listFiles();

            if (listPdf != null){
                for (int i = 0; i < listPdf.length; i++){
                    File pdf_file = listPdf[i];

                    if (pdf_file.getName().endsWith(file_ext)){
                        item = new RecentModel();
                        item.setPdfTitle(pdf_file.getName());
                        item.setPdfPath(pdf_file.getAbsolutePath());
                        item.setPdfDate(String.valueOf(pdf_file.lastModified()));
                        item.setPdfSize(convertSize(pdf_file.length()));

                        modelPdfs.add(item);
                        pdfAdapter.notifyDataSetChanged();

                    }
                }
            }
           modelPdfs.sort(new Comparator<RecentModel>() {
               @Override
               public int compare(RecentModel recentModel, RecentModel t1) {
                   if (modelPdfs != null) {
                       return recentModel.getPdfDate().compareTo(t1.getPdfDate());
                   }
                   else{
                       return 0;
                   }
               }
           });
            Collections.reverse(modelPdfs);

            if (modelPdfs.isEmpty()){
                no_recent.setVisibility(View.VISIBLE);
                clear_button.setVisibility(View.GONE);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void RemoveContextualActionMode() {
        isContextTualModeEnabled = false;
        count = 0;
        selectList.clear();
        pdfAdapter.notifyDataSetChanged();
        new_file.setVisibility(View.VISIBLE);
        chose_linear.setVisibility(View.GONE);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    public static boolean isEnabled() {
        return stackAndTrackingEnabled;

    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if (doubleBackPressed){
            //super.onBackPressed();
          /*  Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();*/
            finishAffinity();


        }
        else {
            this.doubleBackPressed = true;
            RemoveContextualActionMode();
            new_file.setVisibility(View.VISIBLE);
            chose_linear.setVisibility(View.GONE);


        }
        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackPressed = false;

            }
        }, 2000);

    }
}
package com.sasha.pdfviewer.view;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.FolderAdapter;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.model.RecentModel;
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.Constants;
import com.sasha.pdfviewer.utils.RealPathUtil;
import java.util.ArrayList;

/* loaded from: classes2.dex */
public class SearchActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 111;
    public static boolean isContextModeEnabled = false;
    private LinearLayout all_file;
    private TextView all_folder_text;
    private BottomNavigationView bottomNavigationView;
    private LinearLayout drive_file;
    private FolderAdapter folderAdapter;
    private ImageView new_file;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ActivityResultLauncher<Intent> resultLauncher;
    private TextView selectedText;
    private Toolbar toolbar;
    private ArrayList<PdfModel> mediaList = new ArrayList<>();
    private ArrayList<RecentModel> recentModels = new ArrayList<>();
    private ArrayList<String> folderList = new ArrayList<>();
    private ArrayList<String> selectList = new ArrayList<>();
    private int count = 0;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_search);
        this.bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        this.recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        this.all_file = (LinearLayout) findViewById(R.id.all_file_linear);
        this.new_file = (ImageView) findViewById(R.id.new_file);
        this.all_folder_text = (TextView) findViewById(R.id.all_folder_text);
        this.progressBar = (ProgressBar) findViewById(R.id.progressbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.toolbar = toolbar;
        toolbar.setTitle("");
        setSupportActionBar(this.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.toolbar.setNavigationOnClickListener(new View.OnClickListener() { // from class: com.sasha.pdfviewer.view.SearchActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SearchActivity.this.onBackPressed();
            }
        });
        this.all_file.setOnClickListener(new View.OnClickListener() { // from class: com.sasha.pdfviewer.view.SearchActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SearchActivity.this.startActivity(new Intent(SearchActivity.this, AllPdfFileViewActivity.class));
            }
        });
        this.new_file.setOnClickListener(new View.OnClickListener() { // from class: com.sasha.pdfviewer.view.SearchActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SearchActivity.this.startActivity(new Intent(SearchActivity.this, ToolsActivity.class));
            }
        });
        if (Build.VERSION.SDK_INT >= 29) {
            this.mediaList = fetchAllFile(this);
        } else if (Build.VERSION.SDK_INT <= 29) {
            this.mediaList = fetchAllFile(this);
        }
        ArrayList<String> arrayList = this.folderList;
        if (arrayList != null && arrayList.size() > 0 && this.mediaList != null) {
            FolderAdapter folderAdapter = new FolderAdapter(this, this.folderList, this.mediaList);
            this.folderAdapter = folderAdapter;
            this.recyclerView.setAdapter(folderAdapter);
            this.recyclerView.setLayoutManager(new LinearLayoutManager(this, 1, false));
            this.folderAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Sorry no files", 0).show();
        }
        showBottom();
        this.resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() { // from class: com.sasha.pdfviewer.view.SearchActivity.4
            @Override // androidx.activity.result.ActivityResultCallback
            public void onActivityResult(ActivityResult activityResult) {
                if (activityResult.getResultCode() == -1) {
                    Uri data = activityResult.getData().getData();
                    String realPath = RealPathUtil.getRealPath(SearchActivity.this, data);
                    Cursor query = SearchActivity.this.getApplicationContext().getContentResolver().query(data, null, null, null, null);
                    int columnIndexOrThrow = query.getColumnIndexOrThrow("_display_name");
                    query.moveToFirst();
                    String string = query.getString(columnIndexOrThrow);
                    Intent intent = new Intent(SearchActivity.this.getApplicationContext(), PdfViewActivity.class);
                    intent.setData(data);
                    intent.putExtra("uriFile", data);
                    intent.putExtra("uriPath", realPath);
                    intent.putExtra("uriName", string);
                    SearchActivity.this.startActivity(intent);
                }
            }
        });
    }

    private ArrayList<PdfModel> fetchAllFile(Context context) {
        ArrayList<PdfModel> arrayList = new ArrayList<>();
        String str = Environment.getExternalStorageState() + "/RecentFile/";
        Cursor query = context.getContentResolver().query(MediaStore.Files.getContentUri("external"), new String[]{"_id", "_data", "title", "_size", "mime_type", "date_added", "bucket_display_name"}, "mime_type IN ('application/pdf')", null, "_display_name DESC");
        if (query != null) {
            while (query.moveToNext()) {
                String string = query.getString(0);
                String string2 = query.getString(1);
                String string3 = query.getString(2);
                query.getString(3);
                String string4 = query.getString(4);
                String string5 = query.getString(5);
                query.getString(6);
                PdfModel pdfModel = new PdfModel(string, string3, string2, string4, string5, false);
                String substring = string2.substring(0, string2.lastIndexOf(Constants.PATH_SEPERATOR));
                if (!this.folderList.contains(substring)) {
                    this.folderList.add(substring);
                }
                if (this.folderList.equals("/Downloads")) {
                    this.folderList.add(substring);
                }
                arrayList.add(pdfModel);
            }
            query.close();
        }
        return arrayList;
    }

    private void getPdfFile() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("application/pdf");
        this.resultLauncher.launch(intent);
    }

    private void showBottom() {
        this.bottomNavigationView.setSelectedItemId(R.id.search_menu);
        this.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() { // from class: com.sasha.pdfviewer.view.SearchActivity.5
            @Override // com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.home_menu) {
                    SearchActivity.this.startActivity(new Intent(SearchActivity.this, MainActivity.class));
                    SearchActivity.this.overridePendingTransition(0, 0);
                } else if (itemId == R.id.search_menu) {
                    SearchActivity searchActivity = SearchActivity.this;
                    searchActivity.startActivity(searchActivity.getIntent());
                } else if (itemId == R.id.tools_menu) {
                    SearchActivity.this.startActivity(new Intent(SearchActivity.this, ToolsActivity.class));
                    SearchActivity.this.overridePendingTransition(0, 0);
                }
                return false;
            }
        });
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
    }
}
package com.sasha.pdfviewer.tools;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sasha.pdfviewer.folderList.ExtractedImagesFolderActivity;
import com.sasha.pdfviewer.folderList.MergeFolderActivity;
import com.sasha.pdfviewer.view.MainActivity;
import com.sasha.pdfviewer.R;

import com.sasha.pdfviewer.folderList.DecryptedFolderActivity;
import com.sasha.pdfviewer.folderList.EncryptedFolderActivity;
import com.sasha.pdfviewer.folderList.ImageToPdfFolderActivity;
import com.sasha.pdfviewer.folderList.NewFileFolderActivity;
import com.sasha.pdfviewer.folderList.SplitFolder;
import com.sasha.pdfviewer.folderList.WordFolderActivity;
import com.sasha.pdfviewer.view.SearchActivity;

import java.util.ArrayList;

public class ToolsActivity extends AppCompatActivity {

    private CardView encryptBtn;
    private LinearLayout pdfToWord, newPdf, imageToPdf, pdfToImage, unlockPdf, lockPdf,
            waterMarkPdf, compressPdf, splitPdf, extracted_images;
    private LinearLayout lock_folder, unlock_folder, combine_folder,
            split_folder, convert_folder,word_folder, new_folder;
    private BottomNavigationView bottomNavigationView;
    private ActivityResultLauncher<Intent> resultLauncher, newResultLauncher;
    private ArrayList<String> inputPdfs;
    private String paths;
    private Dialog dialog;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

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

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        pdfToWord = findViewById(R.id.pdfToWord);
        newPdf = findViewById(R.id.newPdf);
        imageToPdf = findViewById(R.id.imagetoPdf);
        pdfToImage = findViewById(R.id.pdftoImage);
        unlockPdf = findViewById(R.id.unlockPdf);
        lockPdf = findViewById(R.id.encryptPdf);
        compressPdf = findViewById(R.id.compressPdf);
        splitPdf = findViewById(R.id.splitPdf);
        waterMarkPdf = findViewById(R.id.waterMark);
        lock_folder = findViewById(R.id.lock_folder);
        unlock_folder = findViewById(R.id.unlock_folder);
        combine_folder = findViewById(R.id.combine_folder);
        split_folder = findViewById(R.id.split_folder);
        word_folder = findViewById(R.id.word_folder);
        convert_folder = findViewById(R.id.convertPdf_folder);
        new_folder = findViewById(R.id.new_folder);
        progressBar = findViewById(R.id.progressbar);
        scrollView = findViewById(R.id.scrollView);
        extracted_images = findViewById(R.id.extracted_image);
        Context context = ToolsActivity.this;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_popup_layout);


        new CountDownTimer(500, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
            }
        }.start();

        listener();
        showBottom();
    }

    private void listener() {
        unlockPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ToolsActivity.this, AllToolsViewActivity.class);
                intent.putExtra("unlockBtn", "unlockBtn");
                startActivity(intent);
            }
        });
        newPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, NewPdfFileActivity.class));
            }
        });
        extracted_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, ExtractedImagesFolderActivity.class));
            }
        });
        splitPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ToolsActivity.this, AllToolsViewActivity.class);
                intent.putExtra("splitBtn", "splitBtn");
                startActivity(intent);
            }
        });
        waterMarkPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ToolsActivity.this, AllToolsViewActivity.class);
                intent.putExtra("waterBtn", "waterBtn");
                startActivity(intent);
            }
        });

        compressPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ToolsActivity.this, AllToolsViewActivity.class);
                intent.putExtra("mergeBtn", "mergeBtn");
                startActivity(intent);
            }
        });

        lockPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ToolsActivity.this, AllToolsViewActivity.class);
                intent.putExtra("lockBtn", "lockBtn");
                startActivity(intent);
            }
        });

        pdfToWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ToolsActivity.this, AllToolsViewActivity.class);
                intent.putExtra("wordBtn", "wordBtn");
                startActivity(intent);
            }
        });
        imageToPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, ImageViewActivity.class));
            }
        });
        pdfToImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ToolsActivity.this, AllToolsViewActivity.class);
                intent.putExtra("toImageBtn", "toImageBtn");
                startActivity(intent);
            }
        });

        /*Folder Intent here..*/
        lock_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, EncryptedFolderActivity.class));
            }
        });
        unlock_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, DecryptedFolderActivity.class));
            }
        });
        combine_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, MergeFolderActivity.class));
            }
        });
        split_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, SplitFolder.class));
            }
        });
        convert_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, ImageToPdfFolderActivity.class));
            }
        });
        word_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, WordFolderActivity.class));
            }
        });
        new_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, NewFileFolderActivity.class));
            }
        });
    }



    private void showBottom() {

        bottomNavigationView.setSelectedItemId(R.id.tools_menu);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id){
                    case R.id.home_menu:
                        startActivity(new Intent(ToolsActivity.this, MainActivity.class));
                        overridePendingTransition(0, 0);
                        break;

                    case R.id.tools_menu:
                        return true;

                    case R.id.search_menu:
                        startActivity(new Intent(ToolsActivity.this, SearchActivity.class));
                        overridePendingTransition(0, 0);
                        break;

                   /* case R.id.profile_menu:
                        startActivity(new Intent(ToolsActivity.this, ProfileActivity.class));
                        overridePendingTransition(0, 0);
                        break;*/

                }

                return false;
            }
        });
    }



}
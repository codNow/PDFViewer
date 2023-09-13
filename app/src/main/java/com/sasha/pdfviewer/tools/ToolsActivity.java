package com.sasha.pdfviewer.tools;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sasha.pdfviewer.adapter.ConvertPdfAdapter;
import com.sasha.pdfviewer.folderList.ExtractedImagesFolderActivity;
import com.sasha.pdfviewer.folderList.MergeFolderActivity;
import com.sasha.pdfviewer.folderList.WatermarkFolderActivity;
import com.sasha.pdfviewer.utils.PdfUtils;
import com.sasha.pdfviewer.view.MainActivity;
import com.sasha.pdfviewer.R;

import com.sasha.pdfviewer.folderList.DecryptedFolderActivity;
import com.sasha.pdfviewer.folderList.EncryptedFolderActivity;
import com.sasha.pdfviewer.folderList.ImageToPdfFolderActivity;
import com.sasha.pdfviewer.folderList.SplitFolder;
import com.sasha.pdfviewer.folderList.WordFolderActivity;
import com.sasha.pdfviewer.view.AllImageActivity;
import com.sasha.pdfviewer.view.ProfileActivity;
import com.sasha.pdfviewer.view.SearchActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ToolsActivity extends AppCompatActivity {

    private LinearLayout profile_card;
    private LinearLayout pdfToWord, newPdf, imageToPdf,
            pdfToImage, unlockPdf, lockPdf,
            waterMarkPdf, compressPdf, splitPdf;
    private BottomNavigationView bottomNavigationView;
    private ActivityResultLauncher<Intent> resultLauncher, newResultLauncher;
    private ArrayList<String> inputPdfs;
    private String paths;
    private Dialog dialog;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private Toolbar toolbar;
    private CardView wordCard, convertCard,lockCard, combineCard,
            unlockCard, splitCard, extractCard, waterMarkCard;
    String folder = Environment.getExternalStorageDirectory()+
            "/CustomFolder/";
    private Uri imageUri, singleUri;
    private String addButton;
    private TextView noOfText, noOfConvert, noOfLock, noOfUnlock,
            noOfSplit, noOfMark, noOfCombine, noOfImage;

    private ConvertPdfAdapter convertPdfAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

        toolbar = findViewById(R.id.toolbar);


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
        lockCard = findViewById(R.id.lock_cardView);
        unlockCard = findViewById(R.id.unlock_cardView);
        combineCard = findViewById(R.id.combine_cardView);
        splitCard = findViewById(R.id.split_cardView);
        wordCard = findViewById(R.id.word_cardView);
        convertCard = findViewById(R.id.convert_cardView);
        waterMarkCard = findViewById(R.id.waterMark_cardView);
        progressBar = findViewById(R.id.progressbar);
        scrollView = findViewById(R.id.scrollView);
        extractCard = findViewById(R.id.extracted_cardView);
        noOfSplit = findViewById(R.id.noOfSplit);
        noOfText = findViewById(R.id.noOfText);
        noOfCombine = findViewById(R.id.noOfCombine);
        noOfConvert = findViewById(R.id.noOfConvert);
        noOfImage = findViewById(R.id.noOfImage);
        noOfLock = findViewById(R.id.noOfLock);
        noOfUnlock = findViewById(R.id.noOfUnlock);
        noOfMark = findViewById(R.id.noOfMark);
        profile_card = findViewById(R.id.profile_card);


        Context context = ToolsActivity.this;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_popup_layout);

        addButton = getIntent().getStringExtra("addButton");



        profile_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, ProfileActivity.class));
            }
        });


        getAllTheNumbers();

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

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result != null) {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData().getClipData() != null) {
                            int countOfImages = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < countOfImages; i++) {
                                progressBar.setVisibility(View.VISIBLE);
                                imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                Cursor returnCursor = getApplicationContext().getContentResolver().query(
                                        imageUri, null, null, null, null);
                                int nameIndex = returnCursor.getColumnIndexOrThrow(
                                        MediaStore.Files.FileColumns.DISPLAY_NAME);
                                returnCursor.moveToFirst();
                                String file_name = returnCursor.getString(nameIndex);
                                String folderName = folder+ file_name;

                                File folder = new File(folderName);
                                    if (!folder.getParentFile().exists()) {
                                        folder.getParentFile().mkdir();
                                    }
                                    Bitmap bitmap = null;
                                    try {
                                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }




                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
                                    byte[] compressedData = outputStream.toByteArray();
                                    FileOutputStream fos = null;
                                    try {
                                        fos = new FileOutputStream(new File(folderName+".jpg"));
                                        fos.write(compressedData);
                                        fos.flush();
                                        fos.close();

                                        Log.d("FileName", file_name);

                                        Intent intent = new Intent(ToolsActivity.this, ImageViewActivity.class);
                                        startActivity(intent);
                                        progressBar.setVisibility(View.GONE);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                //imageModel.setBitmap(bitmap);

                                }

                            }
                        else {
                            singleUri = result.getData().getData();
                            Cursor returnCursor = getApplicationContext().getContentResolver().query(
                                    singleUri, null, null, null, null);
                            int nameIndex = returnCursor.getColumnIndexOrThrow(
                                    MediaStore.Files.FileColumns.DISPLAY_NAME);
                            returnCursor.moveToFirst();
                            String filePath = returnCursor.getString(nameIndex);
                            String file_name = returnCursor.getString(nameIndex);
                            String folderName = folder+ file_name;
                            File folder = new File(folderName);
                            if (!folder.getParentFile().exists()) {
                                folder.getParentFile().mkdir();
                            }
                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), singleUri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
                            byte[] compressedData = outputStream.toByteArray();
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(new File(folderName+".jpg"));
                                fos.write(compressedData);
                                fos.flush();
                                fos.close();

                                Log.d("SingleFile", file_name);

                                Intent intent = new Intent(ToolsActivity.this, ImageViewActivity.class);
                                startActivity(intent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                        }
                    }
                }

        });

        if (addButton != null){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            resultLauncher.launch(intent);
        }


    }



    private void getAllTheNumbers() {
        String splitFolder = Environment.getExternalStorageDirectory()+
                "/Split PDF/";
        String imageFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"ExtractedImages";
        String wordFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"Text Doc";
        String markFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"Marked PDF";
        String unlock = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"Unlocked PDF";
        String lockFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"Locked PDF";
        String combineFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"Combined PDF";
        String convertFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"Converted PDF";

        noOfSplit.setText(String.valueOf(PdfUtils.getFileNumber(splitFolder)));
        noOfImage.setText(String.valueOf(PdfUtils.getImageNumber(imageFolder)));
        noOfText.setText(String.valueOf(PdfUtils.getWordNumber(wordFolder)));
        noOfMark.setText(String.valueOf(PdfUtils.getFileNumber(markFolder)));
        noOfUnlock.setText(String.valueOf(PdfUtils.getFileNumber(unlock)));
        noOfLock.setText(String.valueOf(PdfUtils.getFileNumber(lockFolder)));
        noOfCombine.setText(String.valueOf(PdfUtils.getFileNumber(combineFolder)));
        noOfConvert.setText(String.valueOf(PdfUtils.getFileNumber(convertFolder)));

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
        extractCard.setOnClickListener(new View.OnClickListener() {
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
                Intent intent = new Intent(ToolsActivity.this, MergedPdfActivity.class);
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
                startActivity(new Intent(ToolsActivity.this, AllImageActivity.class));
                //getImagesFromDevice();
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
        lockCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, EncryptedFolderActivity.class));
            }
        });
        unlockCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, DecryptedFolderActivity.class));
            }
        });
        combineCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, MergeFolderActivity.class));
            }
        });
        splitCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, SplitFolder.class));
            }
        });
        convertCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, ImageToPdfFolderActivity.class));
            }
        });
        wordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, WordFolderActivity.class));
            }
        });
        waterMarkCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ToolsActivity.this, WatermarkFolderActivity.class));
            }
        });
    }

    private void getImagesFromDevice() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        resultLauncher.launch(intent);
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

  /*  @Override
    public void onBackPressed() {

        *//*startActivity(new Intent(ToolsActivity.this, SearchActivity.class));
        overridePendingTransition(0, 0);*//*
    }*/
}
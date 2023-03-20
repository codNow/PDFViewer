package com.sasha.pdfviewer.tools;

import androidx.appcompat.app.AppCompatActivity;
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

    private RecyclerView recyclerView, wordView, compressView;
    private ArrayList<PdfModel> modelArrayList = new ArrayList<>();
    private LockAdapter lockAdapter;
    private WordAdapter wordAdapter;
    private WaterMarkAdapter waterMarkAdapter;
    private SplitAdapter splitAdapter;
    private ImagePdfAdapter imagePdfAdapter;
    private ExtractImagesAdapter extractImagesAdapter;
    private DecryptAdapter decryptAdapter;
    private ArrayList<PdfModel> pdfModelArrayList = new ArrayList<>();
    private Toolbar toolbar;
    private boolean isContentViewEnabled = false;
    private Button continueBtn;
    ArrayList<PdfModel> selectedModels;
    private ProgressBar progressBar;
    private int limitFile = 20;
    private int offset = 0;
    private boolean isLoading = false;
    ArrayList<PdfModel> searchList = new ArrayList<>();
    private TextView first_display;


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
        Intent intent = getIntent();

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

  /*  private void getPopupMenuFileName() {

        ArrayList<PdfModel> selectedFilePaths = mergeAdapter.getSelectedItems();
        ArrayList<String> selectedFilePathStrings = new ArrayList<>();

        if (selectedFilePaths != null) {
            for (PdfModel pdfModel : selectedFilePaths) {
                selectedFilePathStrings.add(pdfModel.getPath());
            }
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_popup_layout);
        selectedModels = mergeAdapter.getSelectedModels();

        EditText editText;
        Button cancelBtn, okBtn;
        TextView textTitle, textMessage;
        editText = dialog.findViewById(R.id.editText);
        cancelBtn = dialog.findViewById(R.id.buttonNo);
        okBtn = dialog.findViewById(R.id.buttonYes);
        textTitle = dialog.findViewById(R.id.textTitle);
        textTitle.setText("Pdf Merge");
        textMessage = dialog.findViewById(R.id.textMessage);
        textMessage.setText("Please enter your file name");
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = editText.getText().toString();
                startMergePdf(selectedFilePathStrings, fileName );
                dialog.dismiss();

            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.END);
    }

    private void startMergePdf(ArrayList<String> selectedPaths, String fileName) {
        Dialog mdialog = new Dialog(this);
        mdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mdialog.setContentView(R.layout.progress_dialog);
        mdialog.setCanceledOnTouchOutside(false);
        TextView textView = mdialog.findViewById(R.id.loading_text);
        textView.setText("Combining Pdf....");
        mdialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);
        String dest = Environment.getExternalStorageDirectory()+"/MergedPdf/"+fileName;
        File file = new File(dest);
        mdialog.show();
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdir();
            if (!TextUtils.isEmpty(fileName)){
                startMergingPdfFile(selectedPaths, fileName, dest, mdialog);
            }
            else{
                Toast.makeText(this, "Please enter file name", Toast.LENGTH_SHORT).show();
                mdialog.dismiss();
            }
        }
        else{
        }
        if (!TextUtils.isEmpty(fileName)){
            startMergingPdfFile(selectedPaths, fileName, dest, mdialog);

        }else{
            Toast.makeText(this, "Please enter your file name", Toast.LENGTH_SHORT).show();
            mdialog.dismiss();
        }
    }
    private void startMergingPdfFile(ArrayList<String> inputPdfs, String fileName, String dest, Dialog mdialog) {
        mdialog.show();
        new CountDownTimer(3500, 1500){
            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {
                try {
                    PdfWriter writer = writer = new PdfWriter(dest+".pdf");
                    PdfDocument outputPdf = new PdfDocument(writer);
                    // Create a PDF merger
                    PdfMerger merger = new PdfMerger(outputPdf);
                    // Add each input PDF to the merger
                    for (String inputPdf : inputPdfs) {
                        PdfDocument pdf = pdf = new PdfDocument(new PdfReader(inputPdf));
                        merger.merge(pdf, 1, pdf.getNumberOfPages());
                        pdf.close();

                    }
                    // Close the output PDF
                    outputPdf.close();
                    Toast.makeText(getApplicationContext(), "Merged Successfully", Toast.LENGTH_SHORT).show();
                    popupDoneDialog(dest, fileName);
                    mdialog.dismiss();
                }
                catch (IOException e){
                    e.printStackTrace();
                    mdialog.dismiss();
                    Toast.makeText(AllToolsViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }.start();
    }

    private void popupDoneDialog(String destiny, String title) {
        Dialog successDialog = new Dialog(this);
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.upload_done_layout);
        TextView textView1, textView2, textView3;
        Button noButton, yesButton;
        ImageView word_icon;
        textView1 = successDialog.findViewById(R.id.successText);
        textView2 = successDialog.findViewById(R.id.pathText);
        textView3 = successDialog.findViewById(R.id.question_text);
        noButton = successDialog.findViewById(R.id.no_btn);
        yesButton = successDialog.findViewById(R.id.yes_btn);
        word_icon = successDialog.findViewById(R.id.done_icon);

        textView1.setText("Pdf Combined Successfully !!");
        textView2.setText(destiny+title);
        word_icon.setImageDrawable(getApplicationContext().getDrawable(R.drawable.c_c));
        textView3.setText("Do you want to combine more ?");

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successDialog.dismiss();
                startActivity(new Intent(AllToolsViewActivity.this, ToolsActivity.class));

            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successDialog.dismiss();
                startActivity(getIntent());
            }
        });
        successDialog.show();
        successDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        successDialog.getWindow().setGravity(Gravity.TOP);
    }*/


    private ArrayList<PdfModel> loadFiles(Context context) {

        ArrayList<PdfModel> arrayList = new ArrayList<>();

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
    public void onBackPressed() {
        super.onBackPressed();
    }

}
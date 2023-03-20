package com.sasha.pdfviewer.tools;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.MergedAdapter;
import com.sasha.pdfviewer.adapter.PdfAdapter;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.utils.FileUtils;
import com.sasha.pdfviewer.view.SearchActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MergedPdfActivity extends AppCompatActivity implements MergedAdapter.OnFileLongClick, MergedAdapter.OnItemClicks {

    private RecyclerView recyclerView, wordRecyclerView;
    private ArrayList<PdfModel> mediaList = new ArrayList<>();
    private ArrayList<String> folderList =new ArrayList<>();
    private ArrayList<File> fileList =new ArrayList<>();
    private MergedAdapter pdfAdapter;
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
    private ImageView delete_items, share_items, choose_backBtn, selectButton;
    private boolean isSelectAll = false;
    private String defaultText = "Select File";
    boolean isSelectMode;
    ArrayList<String> strings = new ArrayList<>();
    private Button mergedButton;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merged_pdf_activity);

        selectedText = findViewById(R.id.selectedText);


        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressbar);
        choose_layout = findViewById(R.id.chose_linear);
        choose_backBtn = findViewById(R.id.chose_backBtn);
        selectButton = findViewById(R.id.select_items);
        mergedButton = findViewById(R.id.continueButton);
        share_items = findViewById(R.id.share_items);

        buttonListener();


        progressBar.setVisibility(View.VISIBLE);

        new CountDownTimer(1500, 1000){

            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {
                loadAllPdf();
                progressBar.setVisibility(View.GONE);

            }
        }.start();

        mergedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPopupCombine();
            }
        });

    }

    private void buttonListener() {
        selectButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"NotifyDataSetChanged", "UseCompatLoadingForDrawables"})
            @Override
            public void onClick(View view) {
                if (!isSelectAll) {
                    //ArrayList<PdfModel> selectedFile = multiSelectAdapter.getSelectedItems();
                    selectAll();
                    //After checking all items change button text
                    selectButton.setImageDrawable(getDrawable(R.drawable.deselect_all_icon));
                    isSelectAll = true;
                    pdfAdapter.notifyDataSetChanged();

                } else {
                    //If button text is Deselect All remove check from all items
                    //multiSelectAdapter.removeSelection();
                    deselectAll();
                    isSelectAll = false;
                    //After checking all items change button text
                    selectButton.setImageDrawable(getDrawable(R.drawable.select_all_icon));
                    pdfAdapter.notifyDataSetChanged();
                }
            }
        });
        share_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count > 0){
                    ArrayList<PdfModel> selectedFilePaths = pdfAdapter.getSelectedItems();
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
                else{
                    Toast.makeText(MergedPdfActivity.this, R.string.choose_atliest, Toast.LENGTH_SHORT).show();
                }

            }
        });

        choose_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }


    private void updateCount(int counts) {
        if (counts == 0){
            selectedText.setText("Select Files");
        }
        else{
            selectedText.setText(counts + " File Selected");
        }

    }

    private void loadAllPdf() {

        mediaList = FileUtils.allPdfFile(this);
        recyclerView.setHasFixedSize(true);
        pdfAdapter = new MergedAdapter(getApplicationContext(), mediaList, strings,  this, this);
        recyclerView.setAdapter(pdfAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        isSelectMode = pdfAdapter.isSelectMode();
        ViewCompat.setNestedScrollingEnabled(recyclerView, true);
    }
    private void getPopupCombine() {
        ArrayList<PdfModel> selectedFilePaths = pdfAdapter.getSelectedItems();
        ArrayList<String> selectedFilePathStrings = new ArrayList<>();

        if (selectedFilePaths != null) {
            for (PdfModel pdfModel : selectedFilePaths) {
                selectedFilePathStrings.add(pdfModel.getPath());
            }
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_popup_layout);


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
                    ArrayList<FileObserver> observers = new ArrayList<>();
                    PdfWriter writer = writer = new PdfWriter(dest+".pdf");
                    File outputFile = new File(dest + ".pdf");
                    FileObserver observer = new FileObserver(outputFile.getAbsolutePath()) {
                        @Override
                        public void onEvent(int event, @Nullable String path) {
                            // Check if the file has been fully written to disk
                            if (event == FileObserver.CLOSE_WRITE) {
                                // Get the size of the merged PDF file
                                long fileSize = outputFile.length();

                                // Update the UI with the file size
                                runOnUiThread(() -> {

                                });

                                // Stop observing file events
                                stopWatching();
                            }
                        }
                    };

                    // Start observing file events
                    observer.startWatching();
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
                    observers.add(observer);
                    Toast.makeText(getApplicationContext(), "Merged Successfully", Toast.LENGTH_SHORT).show();
                    popupDoneDialog(dest, fileName);
                    mdialog.dismiss();
                }
                catch (IOException e){
                    e.printStackTrace();
                    mdialog.dismiss();
                    Toast.makeText(MergedPdfActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

        textView1.setText(R.string.combine_success);
        textView2.setText(destiny+title);
        word_icon.setImageDrawable(getApplicationContext().getDrawable(R.drawable.c_c));
        textView3.setText(R.string.combine_question);

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successDialog.dismiss();
                startActivity(new Intent(MergedPdfActivity.this, ToolsActivity.class));

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
    }


    private void selectAll() {
        for (PdfModel item : mediaList) {
            item.setSelected(true);
            if (item.isSelected()){
                count = count + 1;
                updateCount(count);
            }
        }
        pdfAdapter.notifyDataSetChanged();
    }
    private void deselectAll() {
        for (PdfModel item : mediaList) {
            item.setSelected(false);
            if (!item.isSelected()){
                count = 0;
                updateCount(count);
            }
        }
        pdfAdapter.notifyDataSetChanged();
    }



    @Override
    public void onItemClick(PdfModel pdfModel, int position) {

        if (pdfModel.isSelected()){
            count = count + 1;
            updateCount(count);
            mergedButton.setVisibility(View.VISIBLE);
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

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
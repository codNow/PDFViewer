package com.sasha.pdfviewer.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;

import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.itextpdf.kernel.exceptions.BadPasswordException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.sasha.pdfviewer.R;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;


public class PDFViewerActivity extends AppCompatActivity implements OnLoadCompleteListener, OnPageChangeListener, OnPageErrorListener {

    private String list_path, list_title, path, title;
    private PDFView pdfView;
    private File listFile, allFile;
    private TextView pdfName, pdfPath;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private ImageView share_button, back_button, star_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfviewer);


        pdfName = findViewById(R.id.pdfTitle);
        pdfView = findViewById(R.id.pdfView);
        pdfPath = findViewById(R.id.pdfPath);
        progressBar = findViewById(R.id.progressbar);
        toolbar = findViewById(R.id.toolbar);
        back_button = findViewById(R.id.back_button);
        share_button = findViewById(R.id.share_button);
        star_button = findViewById(R.id.star_outline);

        star_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star_button.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.teal_700));
            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });



        listFile = new File(getIntent().getStringExtra("listPath"));
        list_title = getIntent().getStringExtra("listTitle");
        list_path = getIntent().getStringExtra("listPath");

        pdfName.setText(list_title);
        pdfPath.setText(list_path);

        try {
            loadFile(listFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(list_path);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, "Share Pdf"));
            }
        });

    }

    private void loadFile(File listFile) throws IOException {

        progressBar.setVisibility(View.VISIBLE);

        new CountDownTimer(1500, 1500){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {

                try {
                    pdfView.fromFile(listFile)
                            .onError(errorListener)
                            .onPageError(PDFViewerActivity.this)
                            .onPageChange(PDFViewerActivity.this)
                            .scrollHandle(new DefaultScrollHandle(PDFViewerActivity.this))
                            .defaultPage(0)
                            .load();
                    progressBar.setVisibility(View.GONE);
                    copyListFile(listFile);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private final OnErrorListener errorListener = t ->{
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_layout);
        Button yesBtn, noBtn;
        TextView msg_text;
        EditText password_text;
        noBtn = dialog.findViewById(R.id.buttonNo);
        yesBtn = dialog.findViewById(R.id.buttonYes);
        password_text = dialog.findViewById(R.id.pwdText);
        msg_text = dialog.findViewById(R.id.msg_text);

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(PDFViewerActivity.this, AllPdfFileViewActivity.class));
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfView.recycle();
                msg_text.setVisibility(View.VISIBLE);
                pdfView.fromFile(listFile)
                        .password(password_text.getText().toString())
                        .onError(finalError)
                        .onPageError(PDFViewerActivity.this)
                        .onPageChange(PDFViewerActivity.this)
                        .scrollHandle(new DefaultScrollHandle(PDFViewerActivity.this))
                        .defaultPage(0)
                        .load();

                dialog.dismiss();

            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.END);
        Toast.makeText(this, R.string.file_is_protect, Toast.LENGTH_SHORT).show();
    };

    private final OnErrorListener finalError = t ->{
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_layout);
        Button yesBtn, noBtn;
        TextView msg_text;
        EditText password_text;
        noBtn = dialog.findViewById(R.id.buttonNo);
        yesBtn = dialog.findViewById(R.id.buttonYes);
        password_text = dialog.findViewById(R.id.pwdText);
        msg_text = dialog.findViewById(R.id.msg_text);
        msg_text.setVisibility(View.VISIBLE);
        msg_text.setPadding(0, 5, 0, 0);
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(PDFViewerActivity.this, AllPdfFileViewActivity.class));
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfView.recycle();

                pdfView.fromFile(listFile).password(password_text.getText().toString())
                        .onError(finalError)
                        .onPageError(PDFViewerActivity.this)
                        .onPageChange(PDFViewerActivity.this)
                        .scrollHandle(new DefaultScrollHandle(PDFViewerActivity.this))
                        .defaultPage(0)
                        .load();

                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.END);
    };
    private void copyListFile(File listFile) throws IOException {

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("RECENT_FILE", list_path);
        editor.apply();

        File sourceFile = new File(String.valueOf(listFile));
        String fileName = sourceFile.getName();
        File destiny = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecentFile/ "+fileName);


        if (!destiny.getParentFile().exists()){
            destiny.getParentFile().mkdir();
        }

        if (!destiny.exists()){
            destiny.createNewFile();
        }
        else {
            Toast.makeText(this, "This file already added to recent file", Toast.LENGTH_SHORT).show();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try{
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destiny).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (source != null){
                source.close();
            }
            if (destination != null){
                destination.close();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //refresh();
        startActivity(new Intent(PDFViewerActivity.this, AllPdfFileViewActivity.class));
    }

    private void refresh() {
        startActivity(new Intent(getIntent()));
    }


    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }

    @Override
    public void onPageError(int page, Throwable t) {

    }
}
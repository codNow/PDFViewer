package com.sasha.pdfviewer.view;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnLongPressListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.sasha.pdfviewer.R;
import com.shockwave.pdfium.PdfDocument;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class PdfViewActivity extends AppCompatActivity implements
        OnLoadCompleteListener, OnPageChangeListener, OnPageErrorListener {

    private String title, pdfPath, uriPath, uriTitle;
    private TextView pdfName, pdf_path;
    private PDFView pdfView;
    private ProgressBar progressBar;
    private File file_path;
    private Uri pdfUri;
    private ImageView share_button, back_button, star_button;
    public static File destiny;
    private int pageCount = 0;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);

        pdfName = findViewById(R.id.pdfTitle);
        pdfView = findViewById(R.id.pdfView);
        pdf_path = findViewById(R.id.pdfPath);
        progressBar = findViewById(R.id.progressbar);
        share_button = findViewById(R.id.share_button);
        back_button = findViewById(R.id.back_button);
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

        if (getIntent().hasExtra("file")){
            pdfUri = getIntent().getParcelableExtra("file");
            uriPath = getIntent().getStringExtra("uriPath");
            uriTitle = getIntent().getStringExtra("uriName");
            File uriFile = new File(String.valueOf(pdfUri));
            String realPath = uriFile.getPath();
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
                pdf_path.setText(realPath);
            }
            else{
                pdf_path.setText(uriPath);
            }
            pdfName.setText(uriTitle);
            File parentFile = uriFile.getParentFile();
            String filePath = parentFile.getAbsolutePath();
            pdf_path.setText(filePath);

            try {
                loadUriFile(pdfUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if (getIntent().hasExtra("title")){
            file_path = new File(getIntent().getStringExtra("path"));
            title = getIntent().getStringExtra("title");
            pdfPath = getIntent().getStringExtra("path");
            String parentFile = file_path.getParentFile().getName();

            pdfName.setText(title);
            pdf_path.setText(parentFile);
            try {
                loadFile(file_path);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getIntent().hasExtra("title")){
                    Uri uri = Uri.parse(title);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, "Share Pdf"));
                }
                else if (getIntent().hasExtra("file")){
                    Uri uri = Uri.parse(uriPath);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, "Share Pdf"));
                }
            }
        });

    }

    private void loadUriFile(Uri pdfUri) throws IOException {

        progressBar.setVisibility(View.VISIBLE);
        new CountDownTimer(1500, 1500){
            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {
                pdfView.fromUri(pdfUri)
                        .onError(errorListener)
                        .scrollHandle(new DefaultScrollHandle(PdfViewActivity.this))
                        .onPageChange(PdfViewActivity.this)
                        .onPageError(PdfViewActivity.this)
                        .swipeHorizontal(false)
                        .spacing(0)
                        .onLongPress(new OnLongPressListener() {
                            @Override
                            public void onLongPress(MotionEvent e) {

                            }
                        })
                        .defaultPage(0)
                        .load();

                pdfView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        float startX = pdfView.getCurrentXOffset();
                        float startY = pdfView.getCurrentYOffset();
                        float endX = startX + pdfView.getWidth();
                        float endY = startY + pdfView.getHeight();




                        String selectedText = "";

                        if (selectedText != null && !selectedText.isEmpty()) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Copied Text", selectedText);
                            clipboard.setPrimaryClip(clip);

                        }
                        return false;
                    }
                });
                progressBar.setVisibility(View.GONE);


                try {
                    copyUri(uriPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }



    private void loadFile(File file_path) throws IOException {
        progressBar.setVisibility(View.VISIBLE);
        new CountDownTimer(1500, 1500){
            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {
                pdfView.fromFile(file_path)
                        .onError(fileError)
                        .scrollHandle(new DefaultScrollHandle(PdfViewActivity.this))
                        .onPageChange(PdfViewActivity.this)
                        .onPageError(PdfViewActivity.this)
                        .swipeHorizontal(false)
                        .spacing(0)
                        .defaultPage(0)
                        .load();
                progressBar.setVisibility(View.GONE);
                try {
                    copyListFile(file_path);
                } catch (IOException e) {
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
        TextView msg_text, blank_text;
        EditText password_text;
        noBtn = dialog.findViewById(R.id.buttonNo);
        yesBtn = dialog.findViewById(R.id.buttonYes);
        password_text = dialog.findViewById(R.id.pwdText);
        msg_text = dialog.findViewById(R.id.msg_text);
        blank_text = dialog.findViewById(R.id.blank_text);

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(PdfViewActivity.this, MainActivity.class));
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfView.recycle();
                msg_text.setVisibility(View.VISIBLE);
                if (password_text != null){
                    pdfView.fromUri(pdfUri).password(password_text.getText().toString())
                            .onError(finalError)
                            .load();

                    dialog.dismiss();
                }
                else {
                    blank_text.setVisibility(View.VISIBLE);
                }


            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.END);
        Toast.makeText(this, "File is protected", Toast.LENGTH_SHORT).show();
    };

    private final OnErrorListener finalError = t ->{
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_layout);
        Button yesBtn, noBtn;
        TextView msg_text, blank_text;
        EditText password_text;
        noBtn = dialog.findViewById(R.id.buttonNo);
        yesBtn = dialog.findViewById(R.id.buttonYes);
        password_text = dialog.findViewById(R.id.pwdText);
        msg_text = dialog.findViewById(R.id.msg_text);
        blank_text = dialog.findViewById(R.id.blank_text);
        msg_text.setVisibility(View.VISIBLE);
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(PdfViewActivity.this, MainActivity.class));
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfView.recycle();
                if (password_text != null) {
                    pdfView.fromUri(pdfUri).password(password_text.getText().toString())
                            .onError(finalError)
                            .load();

                    dialog.dismiss();
                }
                else {
                    blank_text.setVisibility(View.VISIBLE);
                }
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.END);
    };

    private final OnErrorListener fileError = t ->{
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_layout);
        Button yesBtn, noBtn;
        TextView msg_text, blank_text;
        EditText password_text;
        noBtn = dialog.findViewById(R.id.buttonNo);
        yesBtn = dialog.findViewById(R.id.buttonYes);
        password_text = dialog.findViewById(R.id.pwdText);
        blank_text = dialog.findViewById(R.id.blank_text);
        msg_text = dialog.findViewById(R.id.msg_text);

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(PdfViewActivity.this, SearchActivity.class));
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfView.recycle();
                msg_text.setVisibility(View.VISIBLE);
                if (password_text != null) {
                    pdfView.fromFile(file_path)
                            .password(password_text.getText().toString())
                            .onError(fileErrorFinal)
                            .load();

                    dialog.dismiss();
                }
                else{
                    blank_text.setVisibility(View.VISIBLE);
                }

            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.END);
        Toast.makeText(this, "File is protected", Toast.LENGTH_SHORT).show();
    };

    private final OnErrorListener fileErrorFinal = t ->{
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_layout);
        Button yesBtn, noBtn;
        TextView msg_text, blank_text;
        EditText password_text;
        noBtn = dialog.findViewById(R.id.buttonNo);
        yesBtn = dialog.findViewById(R.id.buttonYes);
        password_text = dialog.findViewById(R.id.pwdText);
        msg_text = dialog.findViewById(R.id.msg_text);
        blank_text = dialog.findViewById(R.id.blank_text);
        msg_text.setVisibility(View.VISIBLE);
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(PdfViewActivity.this, SearchActivity.class));
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfView.recycle();
                if (password_text != null) {
                    pdfView.fromFile(file_path).password(password_text.getText().toString())
                            .onError(fileErrorFinal)
                            .load();

                    dialog.dismiss();
                }
                else{
                    blank_text.setVisibility(View.VISIBLE);
                }
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.END);
    };

    private void copyUri(String uriPath) throws IOException {

        File sourceFile = new File(uriPath);
        String fileName = sourceFile.getName();
        File destiny = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecentFile/"+ fileName);


        if (!destiny.getParentFile().exists()){
            destiny.getParentFile().mkdir();
        }

        if (!destiny.getParentFile().exists()){
            destiny.getParentFile().createNewFile();
        }
        else {
            Log.d("TAG", "Already in Recent");
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

    private void copyListFile(File listFile) throws IOException {

        File sourceFile = new File(String.valueOf(listFile));
        String fileName = sourceFile.getName();
        File destiny = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecentFile/"+fileName);


        if (!destiny.getParentFile().exists()){
            destiny.getParentFile().mkdir();
        }

        if (!destiny.exists()){
            destiny.createNewFile();
        }
        else {
            Log.d("TAG", "Already in Rencent");
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

        startActivity(new Intent(PdfViewActivity.this, SearchActivity.class));
        overridePendingTransition(0, 0);
    }

    @Override
    public void loadComplete(int nbPages) {
        pageCount = nbPages;
        PdfDocument.Meta meta = pdfView.getDocumentMeta();

    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }

    @Override
    public void onPageError(int page, Throwable t) {

    }
}
package com.sasha.pdfviewer.view;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.sasha.pdfviewer.R;


import java.io.File;

public class RecentViewActivity extends AppCompatActivity implements OnLoadCompleteListener, OnPageChangeListener, OnPageErrorListener {

    private String title, pdfPath, recentDate;
    private TextView pdfName, pdf_path;
    private PDFView pdfView;
    private ProgressBar progressBar;
    private File file_path;
    private Toolbar toolbar;
    private ImageView back_button, star_button, share_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_view);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        back_button = findViewById(R.id.back_button);
        star_button = findViewById(R.id.star_button);
        share_button = findViewById(R.id.share_button);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        star_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star_button.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.teal_700));
            }
        });



        title = getIntent().getStringExtra("fileName");
        pdfPath = getIntent().getStringExtra("filePath");
        file_path = new File(getIntent().getStringExtra("filePath"));

        pdfName = findViewById(R.id.pdfTitle);
        pdfView = findViewById(R.id.pdfView);
        pdf_path = findViewById(R.id.pdfPath);
        progressBar = findViewById(R.id.progressbar);

        pdfName.setText(title);
        pdf_path.setText(pdfPath);

        loadFile(pdfPath);
        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(pdfPath);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, "Share Pdf"));
            }
        });
    }

    private void loadFile(String pdfPath) {

        progressBar.setVisibility(View.VISIBLE);

        new CountDownTimer(1500, 1500){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                pdfView.fromFile(file_path)
                        .defaultPage(0)
                        .enableSwipe(true)
                        .enableAnnotationRendering(true)
                        .onLoad(RecentViewActivity.this)
                        .onPageChange(RecentViewActivity.this)
                        .scrollHandle(new DefaultScrollHandle(RecentViewActivity.this))
                        .enableDoubletap(true)
                        .onPageError(RecentViewActivity.this)
                        .swipeHorizontal(false)
                        .onError(errorListener)
                        .spacing(0)
                        .load();
                progressBar.setVisibility(View.GONE);
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
                startActivity(new Intent(RecentViewActivity.this, MainActivity.class));
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfView.recycle();
                msg_text.setVisibility(View.VISIBLE);
                pdfView.fromFile(file_path).password(password_text.getText().toString())
                        .onError(finalError)
                        .onPageError(RecentViewActivity.this)
                        .onPageChange(RecentViewActivity.this)
                        .scrollHandle(new DefaultScrollHandle(RecentViewActivity.this))
                        .defaultPage(0)
                        .load();

                dialog.dismiss();

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
        TextView msg_text;
        EditText password_text;
        noBtn = dialog.findViewById(R.id.buttonNo);
        yesBtn = dialog.findViewById(R.id.buttonYes);
        password_text = dialog.findViewById(R.id.pwdText);
        msg_text = dialog.findViewById(R.id.msg_text);
        msg_text.setVisibility(View.VISIBLE);
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(RecentViewActivity.this, MainActivity.class));
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfView.recycle();

                pdfView.fromFile(file_path).password(password_text.getText().toString())
                        .onError(finalError)
                        .onPageError(RecentViewActivity.this)
                        .onPageChange(RecentViewActivity.this)
                        .scrollHandle(new DefaultScrollHandle(RecentViewActivity.this))
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



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RecentViewActivity.this, MainActivity.class));
        overridePendingTransition(0,0);
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
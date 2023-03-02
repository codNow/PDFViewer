package com.sasha.pdfviewer.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
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

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.sasha.pdfviewer.R;

import java.io.File;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class PdfFileViewActivity extends AppCompatActivity implements OnLoadCompleteListener, OnPageChangeListener, OnPageErrorListener {

    private PDFView pdfView;
    private TextView pdfTitle, pdf_path;
    private ProgressBar progressBar;
    private Uri uri;
    private ImageView starImage, share_file;
    private String filePath;
    private File file;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_file_view);

        pdf_path = findViewById(R.id.pdfPath);
        pdfTitle = findViewById(R.id.pdfTitle);
        pdfView = findViewById(R.id.pdfView);
        progressBar = findViewById(R.id.progressbar);
        starImage = findViewById(R.id.star_outline);
        share_file = findViewById(R.id.share_button);
        toolbar = findViewById(R.id.toolbar);

        toolbar.setVisibility(View.INVISIBLE);


        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        progressBar.setVisibility(View.VISIBLE);

        new CountDownTimer(1500, 1500){

            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {
                if (Intent.ACTION_VIEW.equals(action) && type != null){
                    uri = intent.getData();
                    if (uri != null){
                        filePath = uri.getPath();
                        file = new File(filePath);
                        String fileName = file.getName();
                        pdfTitle.setText(fileName);
                        pdf_path.setText(filePath);
                        loadUriFile(uri);
                        progressBar.setVisibility(View.GONE);
                        toolbar.setVisibility(View.VISIBLE);

                    }

                }
            }
        }.start();

        starImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                starImage.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.teal_700));
            }
        });

        share_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, "Share Pdf"));
            }
        });

    }

    private void loadUriFile(Uri uri) {

        pdfView.fromUri(uri)
                .onError(errorListener)
                .scrollHandle(new DefaultScrollHandle(PdfFileViewActivity.this))
                .onPageChange(PdfFileViewActivity.this)
                .onPageError(PdfFileViewActivity.this)
                .swipeHorizontal(false)
                .spacing(0)
                .defaultPage(0)
                .load();
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
                startActivity(new Intent(PdfFileViewActivity.this, MainActivity.class));
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfView.recycle();
                msg_text.setVisibility(View.VISIBLE);
                if (password_text != null){
                    pdfView.fromUri(uri).password(password_text.getText().toString())
                            .onError(finalError)
                            .scrollHandle(new DefaultScrollHandle(PdfFileViewActivity.this))
                            .onPageChange(PdfFileViewActivity.this)
                            .onPageError(PdfFileViewActivity.this)
                            .swipeHorizontal(false)
                            .spacing(0)
                            .defaultPage(0)
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
                startActivity(new Intent(PdfFileViewActivity.this, MainActivity.class));
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfView.recycle();
                if (password_text != null) {
                    pdfView.fromUri(uri).password(password_text.getText().toString())
                            .onError(finalError)
                            .scrollHandle(new DefaultScrollHandle(PdfFileViewActivity.this))
                            .onPageChange(PdfFileViewActivity.this)
                            .onPageError(PdfFileViewActivity.this)
                            .swipeHorizontal(false)
                            .spacing(0)
                            .defaultPage(0)
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
    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }

    @Override
    public void onPageError(int page, Throwable t) {

    }
    private String getTopPackageName() {
        String packageName = "";
        long currentTime = System.currentTimeMillis();

        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, currentTime - 1000 * 10, currentTime);

        if (usageStatsList != null && !usageStatsList.isEmpty()) {
            UsageStats recentStats = null;
            for (UsageStats usageStats : usageStatsList) {
                if (recentStats == null || recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                    recentStats = usageStats;
                }
            }
            if (recentStats != null) {
                packageName = recentStats.getPackageName();
            }
        }

        return packageName;
    }
    @Override
    public void onBackPressed() {

       /* ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ActivityManager.RunningTaskInfo task = tasks.get(0);
            ComponentName componentName = task.topActivity;
            // Check if the previous app is still in the foreground
            if (!componentName.getPackageName().equals(getPackageName())) {
                // If the previous app is still in the foreground, go back to it
                onBackPressed();

            } else {
                // If the previous app is not in the foreground, exit the app
                moveTaskToBack(true);

            }
        }*/
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
        if (!tasks.isEmpty()){
            for (ActivityManager.AppTask task : tasks){
                ComponentName componentName = task.getTaskInfo().baseActivity;
                if (!componentName.getPackageName().equals(getPackageName())){
                    onBackPressed();
                }
                else{
                    moveTaskToBack(true);
                    finish();
                }
            }
        }


    }

}
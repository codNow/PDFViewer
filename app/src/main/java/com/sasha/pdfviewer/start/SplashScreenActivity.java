package com.sasha.pdfviewer.start;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.github.barteksc.pdfviewer.PDFView;
import com.sasha.pdfviewer.view.MainActivity;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.view.PdfFileViewActivity;
import com.sasha.pdfviewer.view.PdfViewActivity;

import java.io.File;

public class
SplashScreenActivity extends AppCompatActivity {

    private ImageView app_logo;
    private Animation animation;
    String filePath;
    private static final  int READ_REQUEST_CODE = 42;
    private Toolbar toolbar;
    private PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        app_logo = findViewById(R.id.splash_icon);
        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.splash_screen_animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));

            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        app_logo.startAnimation(animation);



    }


}
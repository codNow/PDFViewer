package com.sasha.pdfviewer.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.folderList.ExtractedImagesFolderActivity;
import com.sasha.pdfviewer.model.ImageModel;

import java.util.ArrayList;

public class TestActivity extends AppCompatActivity{


    private Button fragmentButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);





    }



    @Override
    public void onBackPressed() {
        startActivity(new Intent(TestActivity.this, ExtractedImagesFolderActivity.class));
    }
}
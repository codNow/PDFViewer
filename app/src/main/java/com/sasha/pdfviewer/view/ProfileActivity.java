package com.sasha.pdfviewer.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.tools.ToolsActivity;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ImageView floatingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        floatingBtn = findViewById(R.id.floatingBtn);

        floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, ToolsActivity.class));
            }
        });

        showBottom();

    }


    private void showBottom() {

       /* bottomNavigationView.setSelectedItemId(R.id.profile_menu);*/

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id){
                    case R.id.home_menu:
                        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.tools_menu:
                        startActivity(new Intent(ProfileActivity.this, ToolsActivity.class));
                        overridePendingTransition(0, 0);
                        break;

                    case R.id.search_menu:
                        startActivity(new Intent(ProfileActivity.this, SearchActivity.class));
                        overridePendingTransition(0, 0);

                     /*   break;
                    case R.id.profile_menu:
                        return true;*/

                }

                return false;
            }
        });
    }
}
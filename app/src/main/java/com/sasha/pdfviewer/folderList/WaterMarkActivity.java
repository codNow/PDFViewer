package com.sasha.pdfviewer.folderList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.ActionMode;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.MultiSelectAdapter;
import com.sasha.pdfviewer.adapter.RecentAdapter;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.model.RecentModel;
import com.sasha.pdfviewer.utils.FileUtils;
import com.sasha.pdfviewer.view.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WaterMarkActivity extends AppCompatActivity implements View.OnLongClickListener {

    private RecyclerView recyclerView;
    private ArrayList<RecentModel> mediaList = new ArrayList<>();
    private ArrayList<RecentModel> fileList = new ArrayList<>();
    private MultiSelectAdapter multiSelectAdapter;
    ActionMode mActionMode;
    public static boolean isSelectMode = false;
    private int count = 0;
    private LinearLayout search_layout, choose_layout, search_linear;
    private EditText search_text;
    private TextView selectedText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_mark);

        selectedText = findViewById(R.id.selectedText);

        recyclerView = findViewById(R.id.recyclerView);
        search_linear = findViewById(R.id.linear_search);
        search_layout = findViewById(R.id.search_layout);
        choose_layout = findViewById(R.id.chose_linear);
        search_text = findViewById(R.id.search_editText);

        loadAllPdf();


    }
    private void loadAllPdf() {

        recyclerView.setHasFixedSize(true);
        multiSelectAdapter = new MultiSelectAdapter( mediaList, this);
        recyclerView.setAdapter(multiSelectAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ViewCompat.setNestedScrollingEnabled(recyclerView, true);

        ArrayList<String> pdfPathList = new ArrayList<>();
        mediaList.clear();

        multiSelectAdapter = new MultiSelectAdapter(mediaList, this);
        recyclerView.setAdapter(multiSelectAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        multiSelectAdapter.notifyDataSetChanged();

        RecentModel item ;
        String file_ext = ".pdf";
        String name = "RecentFile";

        try{
            String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File directory = null;
            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q) {
                directory = Environment.getExternalStorageDirectory();
            }
            File dir = new File(folderPath);

            File listPdf[] = directory.listFiles();

            if (listPdf != null){
                for (int i = 0; i < listPdf.length; i++){
                    File pdf_file = listPdf[i];

                    if (pdf_file.getName().endsWith(file_ext)){
                        item = new RecentModel();
                        item.setPdfTitle(pdf_file.getName());
                        item.setPdfPath(pdf_file.getAbsolutePath());
                        item.setPdfDate(String.valueOf(pdf_file.lastModified()));
                        item.setPdfSize(convertSize(pdf_file.length()));

                        mediaList.add(item);
                        multiSelectAdapter.notifyDataSetChanged();

                    }
                }
            }
            mediaList.sort(new Comparator<RecentModel>() {
                @Override
                public int compare(RecentModel recentModel, RecentModel t1) {
                    if (mediaList != null) {
                        return recentModel.getPdfDate().compareTo(t1.getPdfDate());
                    }
                    else{
                        return 0;
                    }
                }
            });
            Collections.reverse(mediaList);

            if (mediaList.isEmpty()){

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }
    private String convertSize(long size) {

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
    public boolean onLongClick(View view) {
        isSelectMode = true;
        search_layout.setVisibility(View.GONE);
        choose_layout.setVisibility(View.VISIBLE);
        multiSelectAdapter.notifyDataSetChanged();
        return true;
    }

    public void prepareSelection(View view, int position) {
        if(  ((CheckBox) view).isChecked() ){
            fileList.add(mediaList.get(position));
            count = count + 1;
            updateCount(count);
        }else {
            fileList.remove(mediaList.get(position));
            count = count - 1;
            updateCount(count);
        }
    }
    private void updateCount(int counts) {
        if (counts == 0){
            selectedText.setText("Select File");
        }
        else{
            selectedText.setText(counts + " Selected");
        }

    }
}
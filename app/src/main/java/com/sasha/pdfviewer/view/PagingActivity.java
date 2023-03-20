package com.sasha.pdfviewer.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.system.ErrnoException;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.PagingAdapter;
import com.sasha.pdfviewer.adapter.PdfAdapter;
import com.sasha.pdfviewer.model.ModelPdf;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.model.RecentModel;
import com.sasha.pdfviewer.tools.PaginationScrollListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

public class PagingActivity extends AppCompatActivity implements View.OnLongClickListener, PagingAdapter.OnItemClicks, PagingAdapter.OnFileLongClick {

    private RecyclerView recyclerView;
    private PagingAdapter pagingAdapter;
    private ArrayList<PdfModel> mediaList = new ArrayList<>();
    private int limitFile = 20;
    private int offset = 0;
    private boolean isLoading = false;
    private boolean loadAll = false;
    private EditText search_text;
    private ArrayList<PdfModel> selectList = new ArrayList<>();
    private ArrayList<Uri> uriList = new ArrayList<>();
    private int count = 0;
    private TextView selectedText;
    public static boolean isContextTualModeEnabled = false;
    private ImageView voice_icon, back_button, clear_button, search_logo;
    private ProgressBar progressBar, mProgress;
    private LinearLayout search_layout, choose_layout, search_linear;
    private ArrayList<Uri> uris = new ArrayList<>();
    private NestedScrollView nestedScrollView;
    private ProgressBar loadingBar;
    private ImageView delete_items, share_items, choose_backBtn;
    private ImageView select_id;
    private boolean isSelectAll = false;
    private String defaultText = "Select File";
    private boolean isSelectMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paging);

        selectedText = findViewById(R.id.selectedText);
        progressBar = findViewById(R.id.progressbar);
        search_linear = findViewById(R.id.linear_search);
        search_layout = findViewById(R.id.search_layout);
        choose_layout = findViewById(R.id.chose_linear);
        search_text = findViewById(R.id.search_editText);
        back_button = findViewById(R.id.back_button);
        clear_button = findViewById(R.id.clear_button);
        search_logo = findViewById(R.id.search_logo);
        delete_items = findViewById(R.id.delete_items);
        share_items = findViewById(R.id.share_items);
        choose_backBtn = findViewById(R.id.chose_backBtn);
        mProgress = findViewById(R.id.startProgressbar);
        select_id = findViewById(R.id.select_id);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        pagingAdapter = new PagingAdapter(this,mediaList, this, this);
        recyclerView.setAdapter(pagingAdapter);
        isSelectMode = pagingAdapter.isSelectMode();

        buttonListener();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(-1)
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && !isLoading) {
                    // Load more items
                    loadItems();
                    progressBar.setVisibility(View.VISIBLE);

                }
            }
        });
        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence,
                                          int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence inputText,
                                      int i, int i1, int i2) {
                searchFiles(inputText.toString());
                clear_button.setVisibility(View.VISIBLE);

            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_text.setText("");
                clear_button.setVisibility(View.GONE);

            }
        });

        mProgress.setVisibility(View.VISIBLE);
        search_layout.setVisibility(View.GONE);

        new CountDownTimer(1000, 1000){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                loadItems();
                mProgress.setVisibility(View.GONE);
                search_layout.setVisibility(View.VISIBLE);
            }
        }.start();

    }

    private void buttonListener() {
        select_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSelectAll) {
                    ArrayList<PdfModel> selectedFile = pagingAdapter.getSelectedItems();
                    selectAll();
                    //After checking all items change button text
                    select_id.setImageDrawable(getDrawable(R.drawable.deselect_all_icon));
                    isSelectAll = true;
                    pagingAdapter.notifyDataSetChanged();
                    if (isSelectAll){

                    }

                } else {
                    //If button text is Deselect All remove check from all items
                    //multiSelectAdapter.removeSelection();
                    deselectAll();
                    isSelectAll = false;
                    //After checking all items change button text
                    select_id.setImageDrawable(getDrawable(R.drawable.select_all_icon));
                    pagingAdapter.notifyDataSetChanged();
                }
            }
        });
        share_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<PdfModel> selectedFilePaths = pagingAdapter.getSelectedItems();
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
        });

        delete_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count > 0) {
                    Dialog alertDialog = new Dialog(view.getRootView().getContext());
                    alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    alertDialog.setContentView(R.layout.delete_layout);
                    final TextView textTile, textMessage;
                    final Button yesButton, noButton;
                    final ImageView close_button;

                    textTile = alertDialog.findViewById(R.id.textTitle);
                    textMessage = alertDialog.findViewById(R.id.textMessage);
                    yesButton = alertDialog.findViewById(R.id.buttonYes);
                    noButton = alertDialog.findViewById(R.id.buttonNo);
                    close_button = alertDialog.findViewById(R.id.close_btn);

                    textTile.setText(R.string.delete_file_title);
                    textMessage.setText(R.string.delete_question);
                    yesButton.setText(R.string.yes);
                    noButton.setText(R.string.no);
                    yesButton.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onClick(View v) {
                            int position = pagingAdapter.getSelectedPosition();
                            ArrayList<PdfModel> selectedFilePaths = pagingAdapter.getSelectedItems();
                            for (PdfModel pdfModel : selectedFilePaths) {
                                String filePath = pdfModel.getPath();
                                String id = pdfModel.getId();
                                File file = new File(filePath);
                                if (file.exists()) {
                                    file.delete();
                                    selectedFilePaths.remove(file);

                                    Uri contentUris = ContentUris.withAppendedId(
                                            MediaStore.Files.getContentUri("external"),
                                            Long.parseLong(id));
                                    getApplicationContext().getContentResolver().delete(
                                            contentUris, null, null
                                    );
                                    mediaList.remove(position);
                                    recyclerView.getAdapter().notifyItemRemoved(position);
                                    count = 0;
                                    updateCount(count);

                                }

                            }
                            alertDialog.dismiss();
                        }
                    });
                    noButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    close_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
                    alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    alertDialog.getWindow().getAttributes().windowAnimations
                            = R.style.SideMenuAnimation;
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.getWindow().setGravity(Gravity.END);
                }
                else{
                    Toast.makeText(PagingActivity.this, R.string.select_request, Toast.LENGTH_SHORT).show();
                }
            }
        });

        choose_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_text.setText("");
                clear_button.setVisibility(View.GONE);

            }
        });
    }

    private void searchFiles(String newText) {
        String inputText = newText.toLowerCase();
        ArrayList<PdfModel> searchList = new ArrayList<>();
        for (PdfModel model : mediaList){
            if (model.getTitle().toLowerCase().contains(inputText)){
                searchList.add(model);
            }
        }
        pagingAdapter.updateSearchList(searchList);
    }

    private void loadItems() {
        new Handler( ).postDelayed(new Runnable() {
            @Override
            public void run() {
                String[] projection = {
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.Files.FileColumns.TITLE,
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.SIZE,
                        MediaStore.Files.FileColumns.MIME_TYPE,
                        MediaStore.Files.FileColumns.DATE_ADDED
                };
                String selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
                String[] selectionArgs = new String[]{"application/pdf"};

                String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
                String limit = limitFile + " OFFSET " + offset;
                //String limit = "10";
                //String offset = String.valueOf(items.size());
                int totalFile = mediaList.size();
                Uri uri = MediaStore.Files.getContentUri("external");
                Cursor cursor
                        = createCursor(getContentResolver(),
                        uri,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder,
                        limit,
                        offset);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE));
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                        long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE));
                        String date = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED));
                        boolean isSelected = false;
                        int slashFirstIndex = path.lastIndexOf("/");
                        String subString = path.substring(0, slashFirstIndex);
                        PdfModel pdfModel = new PdfModel(id, title, path, convertSize(size), date, isSelected);
                        if (!subString.contains("RecentFile")){
                            mediaList.add(pdfModel);
                        }
                        progressBar.setVisibility(View.GONE);

                        int totalCount = cursor.getCount();
                        int remaining = totalCount - limitFile;
                    } while (cursor.moveToNext());
                    cursor.close();
                    pagingAdapter.notifyDataSetChanged();
                    if (mediaList.size() <= offset + limitFile && limitFile == 0) {
                        // all items loaded
                        progressBar.setVisibility(View.GONE);
                    }

                    offset += limitFile;

                }
                else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PagingActivity.this, "That's all the data ..", Toast.LENGTH_SHORT).show();
                }
            }
        }, 500);


    }
    private Cursor createCursor(ContentResolver contentResolver,
                                Uri uri, String[] projection,
                                String selection,
                                String[] selectionArgs,
                                String sortOrder,
                                String limit, int offset) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Bundle bundleSelection = createSelectionBundle(projection, selection, selectionArgs, sortOrder);
            return contentResolver.query(uri, projection, bundleSelection, null);

        }
        return contentResolver.query(uri, projection, selection, selectionArgs,
                sortOrder + " LIMIT " + limit);

    }

    private Bundle createSelectionBundle(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int limit = 11; // set the limit value here
        int offset = mediaList.size();
        Bundle bundle = new Bundle();
        bundle.putInt(ContentResolver.QUERY_ARG_LIMIT, limitFile);
        bundle.putInt(ContentResolver.QUERY_ARG_OFFSET, offset);
        bundle.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder);
        /*if (sortOrder.equals("ALPHABET")) {
            bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, new String[]{MediaStore.Files.FileColumns.DATE_ADDED});
        } else {
            bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, new String[]{MediaStore.Files.FileColumns.DATE_ADDED});
        }*/

        bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
        bundle.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs);
        return bundle;
    }
    private static String convertSize(Long size) {
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
        isContextTualModeEnabled = true;
        search_layout.setVisibility(View.GONE);
        choose_layout.setVisibility(View.VISIBLE);
        pagingAdapter.notifyDataSetChanged();
        return true;
    }

    private void updateCount(int counts) {
        if (counts == 0){
            selectedText.setText("Select Files");
        }
        else{
            selectedText.setText(counts + " Selected");
        }

    }

    @Override
    public void onItemClick(PdfModel pdfModel, int position) {
        if (pdfModel.isSelected()){
            count = count + 1;
            updateCount(count);
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

    private void selectAll() {
        for (PdfModel item : mediaList) {
            item.setSelected(true);
            if (item.isSelected()){
                count = count+1;
                updateCount(count);
            }
        }
        pagingAdapter.notifyDataSetChanged();
    }
    private void deselectAll() {
        for (PdfModel item : mediaList) {
            item.setSelected(false);
            if (!item.isSelected()){
                count = 0;
                updateCount(count);
            }
        }
        pagingAdapter.notifyDataSetChanged();
    }

    private void refresh() {
        startActivity(new Intent(getIntent()));
        RemoveContextualActionMode();
    }

    private void RemoveContextualActionMode() {
        isSelectMode = false;
        count = 0;
        selectList.clear();
        pagingAdapter.notifyDataSetChanged();
        search_layout.setVisibility(View.VISIBLE);
        choose_layout.setVisibility(View.GONE);
        selectedText.setText(defaultText);
        deselectAll();
    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (pagingAdapter.isSelectMode()){
            pagingAdapter.setSelectMode(false);
            RemoveContextualActionMode();
            count = 0;
            updateCount(count);
        }
        else{
            startActivity(new Intent(PagingActivity.this, SearchActivity.class));
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onFileLongClick(PdfModel pdfModel, int position) {
        isSelectMode = true;
        if (isSelectMode){
            search_layout.setVisibility(View.GONE);
            choose_layout.setVisibility(View.VISIBLE);
            pdfModel = mediaList.get(position);
            pdfModel.setSelected(!pdfModel.isSelected());
            if (pdfModel.isSelected()){
                count = count +1;
                updateCount(count);
            }
        }
        else{
            search_layout.setVisibility(View.VISIBLE);
            choose_layout.setVisibility(View.GONE);
            isSelectMode = false;
        }

    }
}




package com.sasha.pdfviewer.view;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.AllImageAdapter;
import com.sasha.pdfviewer.adapter.ImageAdapter;
import com.sasha.pdfviewer.adapter.ImageFolderAdapter;
import com.sasha.pdfviewer.adapter.MyImageFolderAdapter;
import com.sasha.pdfviewer.model.FolderModel;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.tools.CameraActivity;
import com.sasha.pdfviewer.tools.CameraFolderActivity;
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class AllImageActivity extends AppCompatActivity implements ImageAdapter.OnImageClickListener{
    private RecyclerView recyclerView, allRecyclerView;
    private boolean isLoading = false;
    private ProgressBar progressBar;
    private int limitFile = 1;
    private int offset = 0;
    private ArrayList<ImageModel> imageList = new ArrayList<>();
    private ImageFolderAdapter imageFolderAdapter;
    private ArrayList<Uri> uriArrayList = new ArrayList<>();
    private ArrayList<String> folderList =new ArrayList<>();
    private MyImageFolderAdapter myImageFolderAdapter;
    private CardView cardView;
    private TextView imageCount;
    private int count = 0;
    private ImageView imageView, back_button;
    private LinearLayout allImages;
    private AllImageAdapter allImageAdapter;
    private LinearLayout oldLinear;
    private RelativeLayout camera_button;







    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_folder);



        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressbar);
        allRecyclerView = findViewById(R.id.allRecyclerView);
        oldLinear = findViewById(R.id.old_linear);
        camera_button = findViewById(R.id.camera_button);
        back_button = findViewById(R.id.back_button);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AllImageActivity.this, CameraActivity.class));
            }
        });




        //loadImages();


        if (Build.VERSION.SDK_INT >  Build.VERSION_CODES.Q){
            imageList = fetchAllFile(this);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            imageFolderAdapter = new ImageFolderAdapter(this, folderList, imageList);
            recyclerView.setAdapter(imageFolderAdapter);
            imageFolderAdapter.notifyDataSetChanged();



        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            recyclerView.setHasFixedSize(true);
            Set<File> pdfFolders = new HashSet<>();
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            findPdfFolders(Environment.getExternalStorageDirectory(), pdfFolders);
            myImageFolderAdapter = new MyImageFolderAdapter(this, new ArrayList<>(pdfFolders));
            recyclerView.setAdapter(myImageFolderAdapter);

            /*allRecyclerView.setVisibility(View.GONE);
            oldLinear.setVisibility(View.VISIBLE);
            oldLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(AllImageActivity.this, AllImageViewActivity.class);
                    intent.putExtra("images", "images");
                    startActivity(intent);
                }
            });*/

        }

        allRecyclerView.setHasFixedSize(true);
        imageList = ImageUtils.allPhotosFromDevice(this);
        allRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allImageAdapter = new AllImageAdapter(this, imageList);
        allRecyclerView.setAdapter(allImageAdapter);




    }

    int counFiles(int count){

        ArrayList<File> fileArrayLis = new ArrayList<>();

        for (File file : fileArrayLis){

            if (file.getName().endsWith(".jpg")){
                count++;
            }

        }
        return count;
    }

    private void findPdfFolders(File dir, Set<File> pdfFolders){
        boolean foundPdf = false;
        for (File file : dir.listFiles()){
            if (file.isDirectory() && !file.getName().equals(".thumbnails")){
                findPdfFolders(file, pdfFolders);
            }
            else{
                String fileName = file.getName();
                if (fileName.endsWith(".jpg")){
                    if (!foundPdf){
                        pdfFolders.add(file.getParentFile());
                        foundPdf = true;
                        count++;
                    }
                }
                if (fileName.endsWith(".png")){
                    if (!foundPdf){
                        pdfFolders.add(file.getParentFile());
                        foundPdf = true;
                    }
                }
            }
        }


    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private ArrayList<ImageModel> fetchAllFile(Context context){


        ArrayList<ImageModel> imageList = new ArrayList<>();
        String folderName = Environment.getExternalStorageDirectory()+"/CameraXPhotos/";
        ContentResolver resolver = context.getContentResolver();

        // Get all the images from the external storage
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };
        String mimeType = "image/*";
        String selection = MediaStore.Images.Media.DATA + " like ? ";
        String[] selectionArgs = new String[]{"%" + folderName + "%"};
        String sortOrder = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC";
        Cursor cursor = resolver.query(uri, projection, null, null, sortOrder);
        int idPath = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        int ids = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(idPath);
                int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String id = cursor.getString(ids);
                String imageTitle = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String imageDate = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
                boolean isSelected = false;
                int slashFirstIndex = imagePath.lastIndexOf("/");
                String subString = imagePath.substring(0, slashFirstIndex);

                ImageModel imageData = new ImageModel(imageUri, id, imageTitle, imagePath, imageDate, isSelected, null);
                if (!folderList.contains(subString) && !subString.contains(".thumbnails")){
                    folderList.add(subString);
                }
                if (subString.isEmpty()){
                    Toast.makeText(AllImageActivity.this, "it is present", Toast.LENGTH_SHORT).show();
                }


                imageList.add(imageData);

            }
            cursor.close();
        }

        return imageList;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private ArrayList<FolderModel> fetchFolders(Context context) throws IOException {


        ArrayList<FolderModel> fileModel = new ArrayList<>();
        String recentFolder = "RecentFile";


        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String [] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,

        };

        String mimeType = "application/pdf";
        String whereClause = MediaStore.Images.Media.MIME_TYPE + " IN ('" + mimeType + "')";
        String sortOrder = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC";
        String[] selectionArgs = new String[] {"%" + recentFolder + "%"};

        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, sortOrder);

        int ids = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

        if (cursor != null){
            while (cursor.moveToNext()){
                FolderModel folderModel = new FolderModel();
                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                String name = cursor.getString(3);
                String size = cursor.getString(4);
                String date = cursor.getString(5);
                String folderName = cursor.getString(6);
                boolean isSelected = false;
                Uri fileUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ids);
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                int slashFirstIndex = path.lastIndexOf("/");
                String subString = path.substring(0, slashFirstIndex);


                FolderModel folds = new FolderModel();
                String new_name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String new_folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String new_datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                String folderpaths = new_datapath.substring(0, path.lastIndexOf(new_folder+"/"));
                folderpaths = folderpaths+folderName+"/";

                if (!folderList.contains(folderpaths)
                        ){
                    folderList.add(folderpaths);

                    folds.setPath(new_datapath);
                    folds.setFolderName(new_folder);
                    folds.setFirstPic(new_datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.addpics();


                    fileModel.add(folds);

                }
                else {
                    for (int i = 0; i < fileModel.size(); i++){

                        if(fileModel.get(i).getPath().equals(folderpaths)){
                            fileModel.get(i).setFirstPic(new_datapath);
                            fileModel.get(i).addpics();
                        }
                    }
                }



            }
            cursor.close();
        }
        return fileModel;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AllImageActivity.this, ToolsActivity.class));
        overridePendingTransition(0, 0);
    }

    @Override
    public void onImageClick(ImageModel imageModel, int position) {

    }
}





package com.sasha.pdfviewer.tools;

import static androidx.core.view.ViewCompat.getRotation;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.common.util.concurrent.ListenableFuture;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.ImagePdfAdapter;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.utils.ConvertImageToPdf;
import com.sasha.pdfviewer.utils.RealPath;
import com.sasha.pdfviewer.utils.RealPathUtil;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ImageViewActivity extends AppCompatActivity{

    TextView titleText, select_display;

    private RecyclerView recyclerView;
    private Button galleryBtn, cameraBtn, convertBtn;
    private ActivityResultLauncher<Intent> resultLauncher, cameraLauncher;
    private Uri imageUri, singleImage;
    private ImagePdfAdapter imagePdfAdapter;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayList<Uri> urlList = new ArrayList<>();
    private Bitmap bitmap, imageBit;
    private Bitmap[] bitmaps;
    private Bitmap[] bitmapList;
    private String name, path, cameraPath;
    private ImageView imageView;
    private EditText editText;
    private Dialog convertDialog;
    private File directoryPath = new File(Environment.getExternalStorageDirectory() + "/ConvertedPdf/");
    private Toolbar toolbar;
    private RelativeLayout first_layout;
    private ImageView zoomImage;
    private boolean isZoomed = false;
    private String imagePath;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);



        recyclerView = findViewById(R.id.recyclerView);
        galleryBtn = findViewById(R.id.galleryButton);
        convertBtn = findViewById(R.id.convertButton);
        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.fileName);
        toolbar = findViewById(R.id.toolbar);
        select_display = findViewById(R.id.selected_display);
        zoomImage = findViewById(R.id.zoomImage);
        first_layout = findViewById(R.id.first_layout);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        convertDialog = new Dialog(this);
        convertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        convertDialog.setContentView(R.layout.progress_dialog);
        titleText = convertDialog.findViewById(R.id.loading_text);
        titleText.setText(R.string.images_progress);
        convertDialog.setCancelable(false);
        convertDialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imagePdfAdapter = new ImagePdfAdapter(arrayList);
        recyclerView.setAdapter(imagePdfAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
                int swipeFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
                return  makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                Collections.swap(urlList, from, to);
                imagePdfAdapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                urlList.remove(position);
                imagePdfAdapter.notifyItemRemoved(position);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result != null) {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData().getClipData() != null) {
                            int countOfImages = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < countOfImages; i++) {
                                imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                urlList.add(imageUri);
                                imagePath = RealPathUtil.getRealPath(ImageViewActivity.this, imageUri);
                                arrayList.add(String.valueOf(imageUri));
                                imagePdfAdapter.notifyDataSetChanged();

                                if (imageUri != null){
                                    convertBtn.setVisibility(View.VISIBLE);
                                    galleryBtn.setVisibility(View.GONE);
                                    editText.setVisibility(View.VISIBLE);
                                    select_display.setVisibility(View.GONE);
                                    //galleryBtn.setText("Convert");
                                }

                            }

                        } else {
                            singleImage = result.getData().getData();
                            urlList.add(singleImage);
                            //arrayList.add(String.valueOf(singleImage));
                            imagePdfAdapter.notifyDataSetChanged();
                            String[] filePathColumn = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME};
                            Cursor cursor = getContentResolver().query(singleImage, filePathColumn,
                                    null, null);
                            cursor.moveToFirst();
                            int filePath = cursor.getColumnIndex(filePathColumn[0]);
                            int fileName = cursor.getColumnIndex(filePathColumn[1]);
                            path = cursor.getString(filePath);
                            name = cursor.getString(fileName);
                            imagePath = RealPathUtil.getRealPath(ImageViewActivity.this, singleImage);
                            cursor.close();
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), singleImage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (bitmap != null){
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setImageBitmap(bitmap);
                                convertBtn.setVisibility(View.VISIBLE);
                                galleryBtn.setVisibility(View.GONE);
                                editText.setVisibility(View.VISIBLE);
                                select_display.setVisibility(View.GONE);
                                //galleryBtn.setText("Convert");
                            }
                        }
                    }
                }
            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        convertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (singleImage != null) {
                    if (TextUtils.isEmpty(editText.getText().toString())){
                        Toast.makeText(ImageViewActivity.this, R.string.please_enter_file_name, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        convertSingleImage(path, name, bitmap);
                    }
                } else if (imageUri != null){
                    editText.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(editText.getText().toString())){
                        //convertImagesToPdf(bitmapList);
                        compressAndConvert(urlList);
                    }
                    else {
                        Toast.makeText(ImageViewActivity.this, R.string.please_enter_file_name, Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


    }

    private void compressAndConvert(ArrayList<Uri> urlList) {
        String filename = editText.getText().toString();
        String dir = Environment.getExternalStorageDirectory() + "/Converted Pdf/"+filename;
        File file = new File(dir);
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdir();
        }
        convertDialog.show();
        new CountDownTimer(3500, 1500) {
            @Override
            public void onTick(long l) {

            }
            @Override
            public void onFinish() {
                try {
                    ConvertImageToPdf.compressAndConvertToPdf(urlList, filename, ImageViewActivity.this);
                    Toast.makeText(ImageViewActivity.this, "success", Toast.LENGTH_SHORT).show();
                    convertDialog.dismiss();
                    imageView.setVisibility(View.GONE);
                    convertBtn.setVisibility(View.GONE);
                    galleryBtn.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.GONE);
                    arrayList.clear();
                    popUpDialog(filename, dir);
                }
                catch (IOException e){
                    e.printStackTrace();
                    convertDialog.dismiss();
                    imageView.setVisibility(View.GONE);
                    convertBtn.setVisibility(View.GONE);
                    galleryBtn.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.GONE);
                    arrayList.clear();
                    Toast.makeText(ImageViewActivity.this, "An Error Occurred"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    private void convertSingleImage(String path, String name, Bitmap bitmap) {
        String file_name = editText.getText().toString();
        String directory = Environment.getExternalStorageDirectory() + "/Converted Pdf/"+file_name;
        final Dialog convertDialog = new Dialog(this);
        convertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        convertDialog.setContentView(R.layout.progress_dialog);
        TextView titleText = convertDialog.findViewById(R.id.loading_text);
        titleText.setText(R.string.images_progress);
        convertDialog.setCancelable(false);
        convertDialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);
        convertDialog.show();
        File file = new File(directory);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }
        convertImageSingle(file_name, directory);
    }

    private void convertImageSingle(String file_name, String directory) {
        new CountDownTimer(5500, 3000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                try{
                    Bitmap bitmap1 = BitmapFactory.decodeFile(path);
                    PdfDocument pdfDocument = new PdfDocument();
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                            1920, 1080, 0).create();
                    PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                    page.getCanvas().drawBitmap(bitmap, 0, 0, null);
                    pdfDocument.finishPage(page);
                    //String changeName = getWithoutExtension(name);
                    String newPath = directoryPath  + ".pdf";
                    File newFile = new File(newPath);
                    pdfDocument.writeTo(new FileOutputStream(newFile));
                    MediaScannerConnection.scanFile(ImageViewActivity.this,
                            new String[]{directoryPath.toString()}, null, null);
                    Toast.makeText(ImageViewActivity.this, "converted to pdf", Toast.LENGTH_SHORT).show();
                    convertDialog.dismiss();

                    pdfDocument.close();
                    popUpDialog(file_name, directory);
                    imageView.setVisibility(View.GONE);
                    convertBtn.setVisibility(View.GONE);
                    galleryBtn.setVisibility(View. VISIBLE);
                    editText.setVisibility(View.GONE);
                    editText.setText("");
                }
                catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(ImageViewActivity.this, "somethings wrong, try again!", Toast.LENGTH_SHORT).show();
                    convertDialog.dismiss();
                    imageView.setVisibility(View.GONE);
                    convertBtn.setVisibility(View.GONE);
                    galleryBtn.setVisibility(View. VISIBLE);
                    editText.setVisibility(View.GONE);
                    editText.setText("");
                }
            }
        }
                .start();
    }

    private String getWithoutExtension(String name) {
        return name.substring(0, name.lastIndexOf("."));
    }


    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        resultLauncher.launch(intent);
    }

    private void popUpDialog(String fileName, String dir) {
        Context context = ImageViewActivity.this;
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.upload_done_layout);
        TextView title, messageText, questionText;
        ImageView titleIcon;
        Button positiveBtn, negativeBtn;

        title = dialog.findViewById(R.id.successText);
        messageText = dialog.findViewById(R.id.pathText);
        questionText = dialog.findViewById(R.id.question_text);
        titleIcon = dialog.findViewById(R.id.done_icon);
        positiveBtn = dialog.findViewById(R.id.yes_btn);
        negativeBtn = dialog.findViewById(R.id.no_btn);

        title.setText(R.string.images_success);
        messageText.setText(dir + fileName);
        questionText.setText(R.string.images_question);
        titleIcon.setImageDrawable(context.getDrawable(R.drawable.m_p));

        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(new Intent(ImageViewActivity.this, ToolsActivity.class));
            }
        });
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                arrayList.clear();
                select_display.setVisibility(View.VISIBLE);
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.TOP);
    }



}
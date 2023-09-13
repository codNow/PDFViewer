package com.sasha.pdfviewer.tools;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;

import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
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

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.ImageAdapter;
import com.sasha.pdfviewer.cropper.CropImage;
import com.sasha.pdfviewer.model.ImageModel;

import com.sasha.pdfviewer.utils.ConvertImageToPdf;
import com.sasha.pdfviewer.utils.ImageUtils;

import com.sasha.pdfviewer.utils.RealPathUtil;
import com.sasha.pdfviewer.utils.SuccessDialogUtil;
import com.sasha.pdfviewer.view.MainActivity;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;

public class CameraFolderActivity extends AppCompatActivity
        implements ImageAdapter.OnImageClickListener {

    private RecyclerView recyclerView;
    private ArrayList<ImageModel> imageList = new ArrayList<>();
    private ArrayList<ImageModel> selectList = new ArrayList<>();
    private int count = 0;
    private LinearLayout choose_layout;
    private TextView selectedText;
    private ImageView selectButton, shareButton, deleteButton, addButton, imageView, chose_btn;
    boolean isSelectAll = false;
    private boolean isSelectMode;
    private Button combineBtn;
    private ArrayList<ImageModel> selectedModels;
    private ProgressBar progressBar;
    private ImageAdapter imageAdapter;
    private LinearLayout convertBtn, delete_button, crop_button;
    private ArrayList<Uri> uris = new ArrayList<>();
    private final Handler handler = new Handler();
    private Runnable deletionTask;
    private String folderName = Environment.getExternalStorageDirectory() + "/AppImages/";



    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_photo);

        selectedText = findViewById(R.id.selectedText);

        recyclerView = findViewById(R.id.recyclerView);
        choose_layout = findViewById(R.id.chose_linear);
        progressBar = findViewById(R.id.progressbar);
        selectButton = findViewById(R.id.select_button);
        delete_button = findViewById(R.id.delete_button);
        convertBtn = findViewById(R.id.convert_btn);
        addButton = findViewById(R.id.add_button);
        crop_button = findViewById(R.id.crop_button);
        chose_btn = findViewById(R.id.chose_backBtn);

        progressBar.setVisibility(View.VISIBLE);
        buttonListener();

        chose_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        new CountDownTimer(1500, 1500){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(View.GONE);
            }
        }.start();

        convertBtn.setOnClickListener(view -> {

            ArrayList<ImageModel> selectList = imageAdapter.getSelectImages();

            if (selectList.isEmpty()) {
                popupEnterFileName();
            }
            else{
                Toast.makeText(CameraFolderActivity.this, R.string.select_file, Toast.LENGTH_SHORT).show();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

            recyclerView.setHasFixedSize(true);
            imageList = ImageUtils.allImageFile(this, folderName);
            imageAdapter = new ImageAdapter(getApplicationContext(), imageList, this);
            recyclerView.setAdapter(imageAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            getPdfFileForBelowB();
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CameraFolderActivity.this, CameraActivity.class));
            }
        });


    }

   /* @Override
    protected void onResume() {
        super.onResume();
        deletionTask = new Runnable() {
            @Override
            public void run() {
                deleteImagesOlderThanOneHour();
                handler.postDelayed(this, 60000); // 1 hour in milliseconds
            }
        };
        handler.postDelayed(deletionTask, 60000); // 1 hour in milliseconds
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove the deletion task when the activity is paused
        handler.removeCallbacks(deletionTask);
    }*/

    private void deleteImagesOlderThanOneHour() {
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.DATA};
        String selection = MediaStore.Images.Media.DATE_ADDED + " <= ?";
        String[] selectionArgs = {String.valueOf((System.currentTimeMillis() / 1000) - 3600)}; // 1 hour in seconds

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            }
            cursor.close();
        }
    }

    @SuppressLint("SetTextI18n")
    private void popupEnterFileName() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_popup_layout);

        EditText editText;
        Button cancelBtn, okBtn;
        TextView textTitle, textMessage;
        editText = dialog.findViewById(R.id.editText);
        cancelBtn = dialog.findViewById(R.id.buttonNo);
        okBtn = dialog.findViewById(R.id.buttonYes);
        textTitle = dialog.findViewById(R.id.textTitle);
        textTitle.setText(R.string.pdf_merge);
        textMessage = dialog.findViewById(R.id.textMessage);
        textMessage.setText(R.string.enter_name);
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        okBtn.setOnClickListener(v -> {
            String fileName = editText.getText().toString();
            convertImageToPdf(fileName);
            dialog.dismiss();
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.END);
    }

    private void convertImageToPdf(String fileName) {

        final Dialog convertDialog = new Dialog(this);
        convertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        convertDialog.setContentView(R.layout.progress_dialog);
        TextView titleText = convertDialog.findViewById(R.id.loading_text);
        titleText.setText(R.string.images_progress);
        convertDialog.setCancelable(false);
        convertDialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);
        convertDialog.show();
        new CountDownTimer(1500, 1500){

            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                ArrayList<ImageModel> selectedImages = imageAdapter.getSelectImages();
                try {
                    startConvertFile(selectedImages, fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                convertDialog.dismiss();
            }
        }.start();
    }

    private void startConvertFile(ArrayList<ImageModel> selectedImages,
                                  String fileName) throws IOException {
        ConvertImageToPdf.convertImageToPdf( selectedImages, fileName, CameraFolderActivity.this);
        popupSuccessDialog(fileName);

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void popupSuccessDialog(String fileName) {

        Context context = CameraFolderActivity.this;
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
        messageText.setText(fileName);
        questionText.setText(R.string.images_question);
        titleIcon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_insert_drive_file_24));

        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(new Intent(CameraFolderActivity.this, MainActivity.class));
            }
        });
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(getIntent());
                dialog.dismiss();
                deselectAll();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.TOP);
    }


    private void getPdfFileForBelowB(){
        recyclerView.setHasFixedSize(true);
        ArrayList<String> pdfPathList = new ArrayList<>();
        imageAdapter = new ImageAdapter(getApplicationContext(), imageList, this);
        recyclerView.setAdapter(imageAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        ImageModel item ;
        String file_ext = ".jpg";
        String folderName = "AppImages";

        try{
            String folderPath =
                    Environment.getExternalStorageDirectory()
                            .getAbsolutePath() +"/"+ folderName;

            File dir = new File(folderPath);

            File listPdf[] = dir.listFiles();

            if (listPdf != null){
                for (int i = 0; i < listPdf.length; i++){
                    File pdf_file = listPdf[i];

                    if (pdf_file.getName().endsWith(file_ext)){
                        item = new ImageModel();
                        item.setUri(Uri.fromFile(pdf_file));
                        item.setImagePath(pdf_file.getPath());
                        item.setImageDate(String.valueOf(pdf_file.lastModified()));

                        imageList.add(item);
                    }
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }


    }
    private void buttonListener() {
        selectButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"NotifyDataSetChanged", "UseCompatLoadingForDrawables"})
            @Override
            public void onClick(View view) {
                if (!isSelectAll) {
                    //ArrayList<PdfModel> selectedFile = multiSelectAdapter.getSelectedItems();
                    selectAll();
                    //After checking all items change button text
                    selectButton.setImageDrawable(getDrawable(R.drawable.deselect_all_icon));
                    isSelectAll = true;
                    convertBtn.setVisibility(View.VISIBLE);
                    imageAdapter.notifyDataSetChanged();

                } else {
                    //If button text is Deselect All remove check from all items
                    //multiSelectAdapter.removeSelection();
                    deselectAll();
                    isSelectAll = false;
                    convertBtn.setVisibility(View.GONE);
                    //After checking all items change button text
                    selectButton.setImageDrawable(getDrawable(R.drawable.select_all_icon));
                    imageAdapter.notifyDataSetChanged();
                }
            }
        });

        delete_button.setOnClickListener(view -> {

            if (count > 0) {
                int position = imageAdapter.getSelectedPosition();
                ArrayList<ImageModel> selectedFilePaths = imageAdapter.getSelectImages();
                imageAdapter.removedSelectedImages(selectedFilePaths);
                for (ImageModel pdfModel : selectedFilePaths) {
                    String filePath = pdfModel.getImagePath();
                    //String id = pdfModel.getId();
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                        selectedFilePaths.remove(file);

                        //selectedFilePaths.remove(position);
                        recyclerView.getAdapter().notifyItemRemoved(position);
                        count = 0;
                        updateCount(count);

                    }

                }

            }
            else{
                Toast.makeText(CameraFolderActivity.this, R.string.select_one, Toast.LENGTH_SHORT).show();
            }
        });

        crop_button.setOnClickListener(view -> {
            int position = imageAdapter.getSelectedPosition();
            ArrayList<ImageModel> selectList = imageAdapter.getSelectImages();

            for (ImageModel imageModel : selectList) {
                if (selectList.size() > 1) {
                    Toast.makeText(CameraFolderActivity.this, R.string.select_only_one, Toast.LENGTH_SHORT).show();
                }
                else{
                    imageView = recyclerView.findViewHolderForAdapterPosition(position)
                            .itemView.findViewById(R.id.imageView);
                    cropImage();
                }
            }
        });

    }

    private void cropImage() {

        ArrayList<ImageModel> selectList = imageAdapter.getSelectImages();
        if (selectList != null){
            for (ImageModel imageModel : selectList){
                Uri imageUri = imageModel.getUri();



                CropImage.activity(imageUri).start(this);

            }
        }

    }
    @SuppressLint("NotifyDataSetChanged")
    private void selectAll() {
        for (ImageModel item : imageList) {
            item.setSelected(true);
            if (item.isSelected()){
                count = count + 1;
                updateCount(count);
            }
        }
        imageAdapter.notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void deselectAll() {
        for (ImageModel item : imageList) {
            item.setSelected(false);
            if (!item.isSelected()){
                count = 0;
                updateCount(count);
            }
        }
        imageAdapter.notifyDataSetChanged();
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
    public void onImageClick(ImageModel imageModel, int position) {
        if (imageModel.isSelected()){
            convertBtn.setVisibility(View.VISIBLE);
            count = count +1;
            updateCount(count);
        }
        else if (!imageModel.isSelected()){
            count = count - 1;
            updateCount(count);
        }
        else {
            count = 0;
            updateCount(count);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // get the cropped image Uri
                Uri resultUri = CropImage.getActivityResult(data).getUri();
                String fileName = String.valueOf(System.currentTimeMillis());
                String realPath = RealPathUtil.getRealPathFromURI_API19(this, resultUri);
                String folderNames = resultUri.getPath().toString()+ fileName;

                ArrayList<ImageModel> selectList = imageAdapter.getSelectImages();
                int position = imageAdapter.getSelectedPosition();
                for (ImageModel imageModel : selectList){
                    String imageName = String.valueOf(System.currentTimeMillis());
                    String nameFile = imageModel.getImageTitle();
                    String imagePath = imageModel.getImagePath();

                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (bitmap != null){

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                        byte[] compressedData = outputStream.toByteArray();
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(new File(folderName + nameFile + imageName+".jpg"));
                            fos.write(compressedData);
                            fos.flush();
                            fos.close();
                            imageView.setImageURI(resultUri);

                            selectList.clear();

                            File oldFile = new File(imageModel.getImagePath());
                            String ids = imageModel.getImageId();
                            if (oldFile.exists()){
                                oldFile.delete();
                                /*boolean delete = oldFile.delete();
                                Uri contentUri = ContentUris.withAppendedId(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        Long.parseLong(imageList.get(position).getImageId()));
                                if (delete){
                                    getContentResolver()
                                            .delete(contentUri,
                                                    null, null);
                                    imageList.remove(imageModel);

                                }*/

                            }
                            deselectAll();
                            startActivity(getIntent());
                            overridePendingTransition(0,0);


                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    Log.d("ImagePath", imagePath);
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                // handle cropping error
                Exception error = CropImage.getActivityResult(data).getError();
                Log.e("TAG", "Error cropping image", error);
            }
        }

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CameraFolderActivity.this, CameraActivity.class));
        overridePendingTransition(0, 0);
    }
}
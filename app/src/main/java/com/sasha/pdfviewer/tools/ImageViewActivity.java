package com.sasha.pdfviewer.tools;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


import android.annotation.SuppressLint;
import android.app.Dialog;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
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

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.ImagePdfAdapter;
import com.sasha.pdfviewer.cropper.CropImage;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.utils.ConvertImageToPdf;
import com.sasha.pdfviewer.utils.ImageUtils;
import com.sasha.pdfviewer.utils.RealPathUtil;
import com.sasha.pdfviewer.view.AllImageActivity;
import com.sasha.pdfviewer.view.AllImageViewActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;



public class ImageViewActivity extends AppCompatActivity
        implements ImagePdfAdapter.OnImageClickListener{


    private RecyclerView recyclerView;
    private ImagePdfAdapter imagePdfAdapter;
    private ArrayList<ImageModel> imageList = new ArrayList<>();
    private ArrayList<Uri> urlList = new ArrayList<>();
    private ImageView imageView, addButton;
    private Toolbar toolbar;
    private int count = 0;
    private ActivityResultLauncher<Intent> launcher;
    private File cropImageFile;
    private ImageView delete_btn, select_button, left_icon, right_icon, crop_icon;
    private boolean isSelectAll = false;
    private LinearLayout rotate_right, rotate_left, crop_image, convertBtn, delete_linear;
    private String folder = "CustomFolder";
    private String myFolder = Environment.getExternalStorageDirectory() + "/CustomFolder/";
    final int[] oldPos = new int[1];
    final int[] newPos = new int[1];
    private EditText editText;
    private ProgressBar progressBar;
    private Dialog convertDialog, dialog;
    private TextView imageNumber, delete_text, crop_text;
    private String fileName;
    private String dir = Environment.getExternalStorageDirectory() + "/Converted PDF/";





    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);


        /*Intent intent = getIntent();
        selectedImage = (ArrayList<ImageModel>) intent.getSerializableExtra("selectedImage");
        don't delete this
*/
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressbar);
        delete_text = findViewById(R.id.delete_text);
        crop_text = findViewById(R.id.crop_text);


        toolbar = findViewById(R.id.toolbar);
        delete_btn = findViewById(R.id.delete_icon);
        select_button = findViewById(R.id.select_button);
        convertBtn = findViewById(R.id.convert_btn);
        rotate_left = findViewById(R.id.rotate_left);
        crop_image = findViewById(R.id.crop_image);
        rotate_right = findViewById(R.id.rotate_right);

        addButton = findViewById(R.id.add_button);
        left_icon = findViewById(R.id.left_icon);
        right_icon = findViewById(R.id.right_icon);
        crop_icon = findViewById(R.id.crop_icon);
        delete_linear = findViewById(R.id.delete_linear);
        imageNumber = findViewById(R.id.image_number);

        progressBar.setVisibility(View.VISIBLE);


        toolbar.setTitle("");

        buttonListener();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            loadImages(folder);

        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R){
            getPdfFileForBelowB();
        }

        updateImageList(count);


        convertDialog = new Dialog(this);
        convertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        convertDialog.setContentView(R.layout.progress_dialog);
        TextView titleText = convertDialog.findViewById(R.id.loading_text);
        ImageView imageView = convertDialog.findViewById(R.id.imageView);
        titleText.setText(R.string.please_wait_dialog);
        imageView.setImageDrawable(getDrawable(R.drawable.converter_icon));
        convertDialog.setCancelable(false);
        convertDialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);


        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_dialog);
        TextView textView = dialog.findViewById(R.id.loading_text);
        textView.setText(R.string.images_progress);
        dialog.setCancelable(false);
        dialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);



        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ImageViewActivity.this, AllImageViewActivity.class);
                intent.putExtra("images", "images");
                startActivity(intent);
            }
        });




        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(

                ItemTouchHelper.UP |
                        ItemTouchHelper.DOWN |
                        ItemTouchHelper.LEFT |
                        ItemTouchHelper.RIGHT,
                0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                oldPos[0] = viewHolder.getAdapterPosition();
                newPos[0] = target.getAdapterPosition();
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                 int direction) {

            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                moveImages(oldPos[0], newPos[0]);
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);



    }


    private void updateImageList(int counting){
        if (!imageList.isEmpty()){

            imageNumber.setText(String.valueOf(imageList.size()));
        }
        else {
            imageNumber.setText("0");
            delete_text.setTextColor(getColor(R.color.image_color));
            crop_text.setTextColor(getColor(R.color.image_color));
            delete_btn.setColorFilter(getColor(R.color.image_color));
            left_icon.setColorFilter(getColor(R.color.image_color));
            right_icon.setColorFilter(getColor(R.color.image_color));
            crop_icon.setColorFilter(getColor(R.color.image_color));
        }
    }

    private void moveImages(int oldPo, int newPo) {
        ImageModel temp = imageList.get(oldPo);
        imageList.set(oldPo, imageList.get(newPo));
        imageList.set(newPo, temp);
        imagePdfAdapter.notifyItemChanged(oldPo);
        imagePdfAdapter.notifyItemChanged(newPo);
    }

    private void loadImages(String folder) {
        recyclerView.setHasFixedSize(true);
        imageList = ImageUtils.imageFromFolderWithoutSort(this, folder);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        imagePdfAdapter = new ImagePdfAdapter(this,imageList, this::onImageClick);
        recyclerView.setAdapter(imagePdfAdapter);
        progressBar.setVisibility(View.GONE);

    }

    private void cropImage() {

        ArrayList<ImageModel> selectList = imagePdfAdapter.getSelectImages();
        if (selectList != null){
            for (ImageModel imageModel : selectList){
                Uri imageUri = imageModel.getUri();

                CropImage.activity(imageUri)
                        .start(this);

            }
        }

    }

    @SuppressLint({"NotifyDataSetChanged", "UseCompatLoadingForDrawables", "SetTextI18n"})
    private void buttonListener() {
        crop_image.setOnClickListener(view -> {
            int position = imagePdfAdapter.getSelectedPosition();
            ArrayList<ImageModel> selectList = imagePdfAdapter.getSelectImages();

            for (ImageModel imageModel : selectList) {
                if (selectList.size() > 1) {
                    Toast.makeText(ImageViewActivity.this, R.string.select_only_one, Toast.LENGTH_SHORT).show();
                }
                else{
                    imageView = recyclerView.findViewHolderForAdapterPosition(position)
                            .itemView.findViewById(R.id.imageView);
                    cropImage();

                }
            }
        });
        rotate_left.setOnClickListener(view -> {
            ArrayList<ImageModel> selectList = imagePdfAdapter.getSelectImages();
            int position  = imagePdfAdapter.getSelectedPosition();
            if (!selectList.isEmpty()){
                convertDialog.show();

                new CountDownTimer(1000, 1000){

                    @Override
                    public void onTick(long l) {
                        if (position != RecyclerView.NO_POSITION) {
                            // Rotate the selected image in the RecyclerView
                            View itemView = recyclerView.getLayoutManager().findViewByPosition(position);
                            if (itemView != null) {
                                //itemView.setRotation(itemView.getRotation() - 90);

                            }
                            for (ImageModel imageModel : selectList){
                                ImageView imageView = recyclerView.findViewHolderForAdapterPosition(position)
                                        .itemView.findViewById(R.id.imageView);
                                Uri imageUri = imageModel.getUri();
                                String fileName = String.valueOf(System.currentTimeMillis());
                                String nameFile = imageModel.getImageTitle();
                                String foldeNames = imageModel.getImagePath() + nameFile;
                                Bitmap bitmap = null;
                                try {
                                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                //Bitmap imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                                Matrix matrix = new Matrix();
                                matrix.postRotate(-90);
                                if (bitmap != null){
                                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                                            bitmap.getHeight(), matrix, true);
                                    imageView.setImageBitmap(rotatedBitmap);
                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                                    byte[] compressedData = outputStream.toByteArray();
                                    FileOutputStream fos = null;
                                    try {
                                        fos = new FileOutputStream(new File(myFolder+ nameFile + fileName+".jpg"));
                                        fos.write(compressedData);
                                        fos.flush();
                                        fos.close();
                                        String imageDate = imageModel.getImageDate();

                                        // Set last modified date of new file to be the same as the original file

                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTimeInMillis(Long.parseLong(imageDate)*1000);
                                        String lastTime = DateFormat.format("dd-MMM-yyyy hh:mm:ss", calendar).toString();


                                        File oldFile = new File(imageModel.getImagePath());
                                        if (oldFile.exists()){
                                            oldFile.delete();
                                        }

                                        startActivity(getIntent());
                                        overridePendingTransition(0,0);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Toast.makeText(ImageViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    imageModel.setBitmap(rotatedBitmap);


                                }


                            }

                        }
                    }

                    @Override
                    public void onFinish() {
                        convertDialog.dismiss();
                    }
                }.start();
            }
            else{
                Toast.makeText(this, "Select First", Toast.LENGTH_SHORT).show();
            }
        });
        rotate_right.setOnClickListener(view -> {
            ArrayList<ImageModel> selectList = imagePdfAdapter.getSelectImages();
            int position  = imagePdfAdapter.getSelectedPosition();

            if (!selectList.isEmpty()){
                convertDialog.show();

                new CountDownTimer(1000, 1000){

                    @Override
                    public void onTick(long l) {
                        if (position != RecyclerView.NO_POSITION) {
                            // Rotate the selected image in the RecyclerView
                            View itemView = recyclerView.getLayoutManager().findViewByPosition(position);
                            if (itemView != null) {
                                {
                                    //itemView.setRotation(itemView.getRotation() + 90);

                                }
                                for (ImageModel imageModel : selectList) {
                                    ImageView imageView = recyclerView.findViewHolderForAdapterPosition(position)
                                            .itemView.findViewById(R.id.imageView);
                                    Uri imageUri = imageModel.getUri();
                                    String fileName = String.valueOf(System.currentTimeMillis());
                                    String nameFile = imageModel.getImageTitle();
                                    String foldeNames = imageModel.getImagePath() + fileName;


                                    Bitmap bitmap = null;
                                    try {
                                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    //Bitmap imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(90);
                                    if (bitmap != null) {
                                        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                                                bitmap.getHeight(), matrix, true);
                                        imageView.setImageBitmap(rotatedBitmap);
                                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                                        byte[] compressedData = outputStream.toByteArray();
                                        FileOutputStream fos = null;
                                        try {
                                            fos = new FileOutputStream(new File(myFolder + nameFile + fileName + ".jpg"));
                                            fos.write(compressedData);
                                            fos.flush();
                                            fos.close();
                                            File oldFile = new File(imageModel.getImagePath());
                                            if (oldFile.exists()) {
                                                oldFile.delete();

                                            }

                                            startActivity(getIntent());
                                            overridePendingTransition(0, 0);

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        imageModel.setBitmap(rotatedBitmap);


                                    }

                                }

                            }
                        }
                    }

                    @Override
                    public void onFinish() {
                        convertDialog.dismiss();
                    }
                }.start();
            }
            else {
                Toast.makeText(this, "Select First", Toast.LENGTH_SHORT).show();
            }


        });

        select_button.setOnClickListener(view -> {
            if (!isSelectAll) {
                //ArrayList<PdfModel> selectedFile = multiSelectAdapter.getSelectedItems();
                selectAll();
                //After checking all items change button text
                select_button.setImageDrawable(getDrawable(R.drawable.deselect_all_icon));
                isSelectAll = true;
                delete_btn.setColorFilter(getColor(R.color.Red_color));
                left_icon.setColorFilter(getColor(R.color.Red_color));
                right_icon.setColorFilter(getColor(R.color.Red_color));
                crop_icon.setColorFilter(getColor(R.color.Red_color));
                delete_text.setTextColor(getColor(R.color.Red_color));
                crop_text.setTextColor(getColor(R.color.Red_color));
                imagePdfAdapter.notifyDataSetChanged();

            } else {
                //If button text is Deselect All remove check from all items
                //multiSelectAdapter.removeSelection();
                deselectAll();
                isSelectAll = false;
                //After checking all items change button text
                delete_btn.setColorFilter(getColor(R.color.image_color));
                left_icon.setColorFilter(getColor(R.color.image_color));
                right_icon.setColorFilter(getColor(R.color.image_color));
                crop_icon.setColorFilter(getColor(R.color.image_color));
                delete_text.setTextColor(getColor(R.color.image_color));
                crop_text.setTextColor(getColor(R.color.image_color));
                select_button.setImageDrawable(getDrawable(R.drawable.select_all_icon));
                imagePdfAdapter.notifyDataSetChanged();
            }
        });

        delete_linear.setOnClickListener(view -> {
            ArrayList<ImageModel> selectedFilePaths = imagePdfAdapter.getSelectImages();
            if (!selectedFilePaths.isEmpty()) {

                int position = imagePdfAdapter.getSelectedPosition();
                imagePdfAdapter.removedSelectedImages(selectedFilePaths);

                for (ImageModel pdfModel : selectedFilePaths) {
                    String filePath = pdfModel.getImagePath();
                    String id = pdfModel.getImageId();

                    File file = new File(pdfModel.getImagePath());
                    if (file.exists()){
                        file.delete();

                        imagePdfAdapter.notifyItemRemoved(position);
                        recyclerView.getAdapter().notifyItemRemoved(position);
                        count = count -1;
                        updateImageList(count);

                    }

                }


            }
            else{
                Toast.makeText(ImageViewActivity.this,
                        R.string.select_one, Toast.LENGTH_SHORT).show();
            }
        });

        convertBtn.setOnClickListener(view -> {

            if (imageList.size() > 20) {

                Toast.makeText(ImageViewActivity.this,
                        "20 is the limit to convert images", Toast.LENGTH_SHORT).show();

            }
            else if (!imageList.isEmpty()){
                final Dialog dialog = new Dialog(ImageViewActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_popup_layout);

                EditText editText;
                Button cancelBtn, okBtn;
                TextView question_text, textViewTitle;
                textViewTitle = dialog.findViewById(R.id.textTitle);
                textViewTitle.setText(R.string.converter);
                editText = dialog.findViewById(R.id.editText);
                cancelBtn = dialog.findViewById(R.id.buttonNo);
                okBtn = dialog.findViewById(R.id.buttonYes);
                question_text = dialog.findViewById(R.id.textMessage);
                question_text.setText(R.string.enter_file_name);
                cancelBtn.setOnClickListener(v -> dialog.dismiss());
                okBtn.setOnClickListener(v -> {

                    fileName = editText.getText().toString();
                    if (!TextUtils.isEmpty(fileName)){
                        //convertImage(fileName);

                        new convertInBackground().execute(imageList.toArray(new ImageModel[imageList.size()]));

                    }
                    else{
                        Toast.makeText(ImageViewActivity.this,
                                R.string.request_filename, Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();

                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setGravity(Gravity.END);
            }
            else{
                Toast.makeText(this, "Please choose images", Toast.LENGTH_SHORT).show();
            }

        });
    }
    private void getPdfFileForBelowB(){
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        imagePdfAdapter = new ImagePdfAdapter(getApplicationContext(), imageList, this::onImageClick);
        recyclerView.setAdapter(imagePdfAdapter);
        ImageModel item ;
        String file_ext = ".jpg";
        String folderName = "CustomFolder";

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
                        progressBar.setVisibility(View.GONE);
                    }
                }

               /* Collections.sort(imageList, new Comparator<ImageModel>() {
                    @Override
                    public int compare(ImageModel o1, ImageModel o2) {
                        return o1.getImageTitle().compareToIgnoreCase(o2.getImageTitle());
                    }
                });*/
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }


    }


    private void convertImage(String fileName) {

        File file = new File(dir);
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdir();
        }

        dialog.show();
        new CountDownTimer(1500, 1500){

            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                try {
                    startConvertFile(imageList, fileName, dir);
                    Log.d("SelectImage", String.valueOf(imageList.size()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                convertDialog.dismiss();
            }
        }.start();
    }

    private void startConvertFile(ArrayList<ImageModel> imageList,
                                  String fileName, String directory) throws IOException {

        ConvertImageToPdf.convertImageToPdf( imageList, fileName, ImageViewActivity.this);
        for (ImageModel imageModel : imageList) {
            File file = new File(imageModel.getImagePath());
            if (file.exists()) {
                file.delete();
            }
        }

        imageList.clear();
        imagePdfAdapter.notifyDataSetChanged();

        popUpDialog(fileName, directory);

    }

    private class convertInBackground extends AsyncTask<ImageModel, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(ImageModel... imageModels) {

            File file = new File( dir +fileName+".pdf");

            if (!file.getParentFile().exists()){
                file.getParentFile().mkdir();
            }

            PdfDocument pdfDocument = null;
            try {
                pdfDocument = new PdfDocument(new PdfWriter(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Document document = new Document(pdfDocument);
            for (ImageModel imageModel : imageModels){
                Uri imageUri = imageModel.getUri();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                byte[] compressImage = byteArrayOutputStream.toByteArray();
                Image image = new Image(ImageDataFactory.create(compressImage));

                document.add(image);

                File oldFile = new File(imageModel.getImagePath());
                if (oldFile.exists()) {
                    oldFile.delete();
                }

            }
            document.close();


            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            dialog.dismiss();
            imageList.clear();
            imagePdfAdapter.notifyDataSetChanged();

            popUpDialog(fileName, dir);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
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
        titleIcon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_insert_drive_file_24));
        titleIcon.setColorFilter(getColor(R.color.blue));
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
                startActivity(new Intent(ImageViewActivity.this, AllImageActivity.class));

            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.TOP);
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
        imagePdfAdapter.notifyDataSetChanged();
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
        imagePdfAdapter.notifyDataSetChanged();
    }
    private void updateCount(int counts) {
        if (counts == 0){
            delete_btn.setColorFilter(getColor(R.color.image_color));
            left_icon.setColorFilter(getColor(R.color.image_color));
            right_icon.setColorFilter(getColor(R.color.image_color));
            crop_icon.setColorFilter(getColor(R.color.image_color));
            delete_text.setTextColor(getColor(R.color.image_color));
            crop_text.setTextColor(getColor(R.color.image_color));
            //selectedText.setText("Select Files");

        }
        else{
            //selectedText.setText(counts + " Selected");

        }


    }


    @Override
    public void onImageClick(ImageModel imageModel, int position) {

        if (imageModel.isSelected()){
            count ++;
            updateCount(count);

            if (count > 0){
                delete_btn.setColorFilter(getColor(R.color.Red_color));
                left_icon.setColorFilter(getColor(R.color.Red_color));
                right_icon.setColorFilter(getColor(R.color.Red_color));
                crop_icon.setColorFilter(getColor(R.color.Red_color));
                delete_text.setTextColor(getColor(R.color.Red_color));
                crop_text.setTextColor(getColor(R.color.Red_color));

            }
        }
        else if (!imageModel.isSelected()){
            count = count - 1;
            updateCount(count);
            delete_text.setTextColor(getColor(R.color.image_color));
            crop_text.setTextColor(getColor(R.color.image_color));
        }
        else {
            count = 0;
            updateCount(count);
            delete_text.setTextColor(getColor(R.color.image_color));
            crop_text.setTextColor(getColor(R.color.image_color));
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

                ArrayList<ImageModel> selectList = imagePdfAdapter.getSelectImages();
                int position = imagePdfAdapter.getSelectedPosition();
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
                            fos = new FileOutputStream(new File(myFolder + nameFile + imageName+".jpg"));
                            fos.write(compressedData);
                            fos.flush();
                            fos.close();
                            imageView.setImageURI(resultUri);

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
        super.onBackPressed();
        startActivity(new Intent(ImageViewActivity.this, AllImageActivity.class));
        overridePendingTransition(0, 0);
    }
}
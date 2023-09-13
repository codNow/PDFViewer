package com.sasha.pdfviewer.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Adapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.ImageAdapter;
import com.sasha.pdfviewer.model.ImageModel;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

public class ImageUtils {

    public static ArrayList<ImageModel> allImageFile(Context context, String folderName){
        ArrayList<ImageModel> imageList = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        String folderPath = Environment.getExternalStorageDirectory()+ folderName;

        // Get all the images from the external storage
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
        };
        String mimeType = "image/*";
        String selection = MediaStore.Images.Media.DATA + " like ? ";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String[] selectionArgs = new String[]{"%" + folderName + "%"};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " ASC";
        String sorting = MediaStore.Images.Media.TITLE;
        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        int ids = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String id = cursor.getString(ids);
                String imageTitle = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String imageDate = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
                boolean isSelected = false;
                int slashFirstIndex = imagePath.lastIndexOf("/");
                String subString = imagePath.substring(0, slashFirstIndex);
                Bitmap bitmap = null;


                ImageModel imageData = new ImageModel(imageUri, id, imageTitle,imagePath, imageDate, isSelected, null);
                    imageList.add(imageData);


            }
            cursor.close();


            ArrayList<ImageModel> reSelection = new ArrayList<>();
            for(int i = imageList.size()-1;i > -1;i--){
                reSelection.add(imageList.get(i));
            }
            imageList = reSelection;
        }

        return imageList;
    }

    public static ArrayList<ImageModel> imageFromFolderWithoutSort(Context context, String folderName){
        ArrayList<ImageModel> imageList = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        String folderPath = Environment.getExternalStorageDirectory()+ folderName;

        // Get all the images from the external storage
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
        };
        String mimeType = "image/*";
        String selection = MediaStore.Images.Media.DATA + " like ? ";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String[] selectionArgs = new String[]{"%" + folderName + "%"};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
        String sorting = MediaStore.Images.Media.TITLE;
        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);
        int ids = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String id = cursor.getString(ids);
                String imageTitle = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String imageDate = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
                boolean isSelected = false;
                int slashFirstIndex = imagePath.lastIndexOf("/");
                String subString = imagePath.substring(0, slashFirstIndex);
                Bitmap bitmap = null;


                ImageModel imageData = new ImageModel(imageUri, id, imageTitle,imagePath, imageDate, isSelected, null);
                imageList.add(imageData);


            }
            cursor.close();

            ArrayList<ImageModel> reSelection = new ArrayList<>();
            for(int i = imageList.size()-1;i > -1;i--){
                reSelection.add(imageList.get(i));
            }
            imageList = reSelection;
        }

        return imageList;
    }

    public static ArrayList<ImageModel> allPhotosFromDevice(Context context){

        ArrayList<ImageModel> imageList = new ArrayList<>();
        String folderName = Environment.getExternalStorageDirectory()+"/CameraXPhotos/";
        ContentResolver resolver = context.getContentResolver();

        // Get all the images from the external storage
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
        };
        String mimeType = "image/*";
        String selection = MediaStore.Images.Media.DATA + " like ? ";
        String[] selectionArgs = new String[]{"%" + folderName + "%"};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " ASC";
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
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
                boolean isSelected = false;
                int slashFirstIndex = imagePath.lastIndexOf("/");
                String subString = imagePath.substring(0, slashFirstIndex);
                Bitmap bitmap = null;
                ImageModel imageData = new ImageModel(imageUri, id, imageTitle, imagePath, imageDate, isSelected, null);
                imageList.add(imageData);

            }
            cursor.close();


            ArrayList<ImageModel> reSelection = new ArrayList<>();
            for(int i = imageList.size()-1;i > -1;i--){
                reSelection.add(imageList.get(i));
            }
            imageList = reSelection;
        }

        return imageList;
    }

    public static ArrayList<ImageModel> getPdfFileForBelowB(){

        ArrayList<ImageModel> imageList = new ArrayList<>();

        ImageModel item ;
        String file_ext = ".jpg";
        String folderName = "CameraXPhotos";

        try{
            String folderPath =
                    Environment.getExternalStorageDirectory()
                            .getAbsolutePath() +"/"+ folderName;
            File allDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

            File dir = new File(folderPath);

            File listPdf[] = allDir.listFiles();

            if (listPdf != null){
                for (int i = 0; i < listPdf.length; i++){
                    File pdf_file = listPdf[i];

                    if (pdf_file.getName().endsWith(file_ext)){
                        item = new ImageModel();
                        item.setUri(Uri.fromFile(pdf_file));
                        item.setImageTitle(pdf_file.getName());
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

        return imageList;


    }

    public static ArrayList<ImageModel> ImageFromFolderWithLimit(
            Context context, String folderName){
        int limitFile = 1;
        int offset = 0;
        ArrayList<ImageModel> imageList = new ArrayList<>();


        // Get all the images from the external storage
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
        };
        String mimeType = "image/*";
        String selection = MediaStore.Images.Media.DATA + " like ? ";
        String[] selectionArgs = new String[]{"%" + folderName + "%"};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
        String sorting = MediaStore.Images.Media.TITLE + " ASC";
        Cursor cursor
                = createCursor(context.getContentResolver(),
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder,
                limitFile,
                offset);
        int ids = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String id = cursor.getString(ids);
                String imageTitle = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String imageDate = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
                boolean isSelected = false;
                int slashFirstIndex = imagePath.lastIndexOf("/");
                String subString = imagePath.substring(0, slashFirstIndex);
                Bitmap bitmap = null;
                ImageModel imageData = new ImageModel(imageUri, id, imageTitle,imagePath, imageDate, isSelected, null);
                imageList.add(imageData);


            }
            cursor.close();
        }


        return imageList;
    }

    private static Cursor createCursor(ContentResolver contentResolver,
                                       Uri uri, String[] projection, String selection, String[]
                                               selectionArgs, String sortOrder,
                                       int limitFile, int offset) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Bundle bundleSelection = createSelectionBundle(projection, selection, selectionArgs, sortOrder, limitFile, offset);
            return contentResolver.query(uri, projection, bundleSelection, null);

        }

        return contentResolver.query(uri, projection, selection, selectionArgs,
                sortOrder + " LIMIT " + limitFile);
    }

    private static Bundle createSelectionBundle(String[] projection, String selection, String[] selectionArgs, String sortOrder, int limitFile, int offaet) {

        Bundle bundle = new Bundle();
        bundle.putInt(ContentResolver.QUERY_ARG_LIMIT, limitFile);
        bundle.putInt(ContentResolver.QUERY_ARG_OFFSET, offaet);
        bundle.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder);
        /*if (sortOrder.equals("ALPHABET")) {
            bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, new String[]{MediaStore.Images.Media.DATE_ADDED});
        } else {
            bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, new String[]{MediaStore.Images.Media.DATE_ADDED});
        }*/
        bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
        bundle.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs);

        return bundle;
    }

    public static ArrayList<Bitmap> resizeImages(Context context) throws IOException {
        ArrayList<Bitmap> fileList = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String [] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
        };

        String mimeType = "image/*";
        String selection = MediaStore.Images.Media.DATA + " like ? ";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " ASC";
        String sorting = MediaStore.Images.Media.TITLE;
        @SuppressLint("Recycle") Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);
        int ids = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

        if (cursor != null){
            while (cursor.moveToNext()){
                int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String id = cursor.getString(ids);
                String imageTitle = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String imageDate = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);

                Bitmap image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                byte[] byteArray = stream.toByteArray();

                Image image1 = new Image(ImageDataFactory.create(byteArray));


                fileList.add(image);


            }
        }


        return fileList;
    }


}

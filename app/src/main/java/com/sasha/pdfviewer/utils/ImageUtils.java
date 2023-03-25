package com.sasha.pdfviewer.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.model.PdfModel;

import java.util.ArrayList;

public class ImageUtils {

    public static ArrayList<ImageModel> allImageFile(Context context, String folderName){
        ArrayList<ImageModel> imageList = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();

        // Get all the images from the external storage
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
        };
        String selection = MediaStore.Images.Media.DATA + " like ? ";
        String[] selectionArgs = new String[]{"%" + folderName + "%"};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String imageDate = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
                boolean isSelected = false;
                ImageModel imageData = new ImageModel(imageUri, imagePath, imageDate, isSelected);
                imageList.add(imageData);
            }
            cursor.close();
        }
        else{
            Toast.makeText(context, "no image found", Toast.LENGTH_SHORT).show();
        }

        return imageList;
    }

    public static ArrayList<ImageModel> allPhotosFromDevice(Context context){
        ArrayList<ImageModel> imageList = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();

        // Get all the images from the external storage
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
        };
        String mimeType = "image/*";
        //String selection = MediaStore.Images.Media.DATA + " like ? ";
        //String[] selectionArgs = new String[]{"%" + folderName + "%"};
        String selection = MediaStore.Images.Media.MIME_TYPE + "=?";
        String[] selectionArgs = new String[]{mimeType};
        String whereClause = MediaStore.Images.Media.MIME_TYPE + " IN ('" + mimeType + "')";
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
        Cursor cursor = resolver.query(uri, projection, null, null, sortOrder);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String imageDate = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
                boolean isSelected = false;
                ImageModel imageData = new ImageModel(imageUri, imagePath, imageDate, isSelected);
                imageList.add(imageData);
            }
            cursor.close();
        }

        return imageList;
    }

}

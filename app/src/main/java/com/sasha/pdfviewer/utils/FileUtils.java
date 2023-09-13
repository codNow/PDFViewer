package com.sasha.pdfviewer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.model.RecentModel;

import java.io.File;
import java.util.ArrayList;

public class FileUtils {


    public static ArrayList<PdfModel> allPdfFile(Context context){
        ArrayList<PdfModel> fileList = new ArrayList<>();
        String recentFolder = Environment.getExternalStorageState().toString();
            Uri uri = MediaStore.Files.getContentUri("external");
            String [] projection = {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.TITLE,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.DATE_ADDED
            };

            String mimeType = "application/pdf";
            String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
            String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
            String[] selectionArgs = new String[] { "application/pdf", "/storage/emulated/0/RecentFile" };
            String[] selectionArgss = new String[] {"%" + recentFolder + "%"};

            Cursor cursor = context.getContentResolver().query(uri, projection,
                    whereClause, null, sortOrder);

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
            int idPath = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            int idTitle = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
            int idSize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
            int date_added = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);

            if (cursor != null){
                while (cursor.moveToNext()){
                    String pdfId = cursor.getString(idColumn);
                    String path = cursor.getString(idPath);
                    String title = cursor.getString(idTitle);
                    long size = cursor.getLong(idSize);
                    String date = cursor.getString(date_added);
                    int slashFirstIndex = path.lastIndexOf("/");
                    String subString = path.substring(0, slashFirstIndex);

                    boolean isSelected = false;
                    PdfModel modelPdf = new PdfModel(pdfId, title, path, convertSize(size), date, isSelected);
                    if (!subString.contains("RecentFile")) {
                        fileList.add(modelPdf);
                    }

                }
            }


        return fileList;
    }

    public static ArrayList<PdfModel> allFileForCombine(Context context){
        ArrayList<PdfModel> fileList = new ArrayList<>();
        String recentFolder = Environment.getExternalStorageState().toString();
        Uri uri = MediaStore.Files.getContentUri("external");
        String [] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED
        };

        String mimeType = "application/pdf";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
        String[] selectionArgs = new String[] { "application/pdf", "/storage/emulated/0/RecentFile" };
        String[] selectionArgss = new String[] {"%" + recentFolder + "%"};

        Cursor cursor = context.getContentResolver().query(uri, projection,
                whereClause, null, sortOrder);

        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
        int idPath = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
        int idTitle = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
        int idSize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
        int date_added = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);

        if (cursor != null){
            while (cursor.moveToNext()){
                String pdfId = cursor.getString(idColumn);
                String path = cursor.getString(idPath);
                String title = cursor.getString(idTitle);
                long size = cursor.getLong(idSize);
                String date = cursor.getString(date_added);
                int slashFirstIndex = path.lastIndexOf("/");
                String subString = path.substring(0, slashFirstIndex);

                boolean isSelected = false;
                PdfModel modelPdf = new PdfModel(pdfId, title, path, convertSize(size), date, isSelected);
                if (!subString.contains("RecentFile") && !subString.contains("Locked PDF")) {
                    fileList.add(modelPdf);
                }

            }
        }


        return fileList;
    }
    public static ArrayList<RecentModel> recentFolder(Context context){
        ArrayList<RecentModel> fileList = new ArrayList<>();
        String recentFolder = Environment.getExternalStorageState().toString();
        Uri uri = MediaStore.Files.getContentUri("external");
        String [] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED
        };

        String mimeType = "application/pdf";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
        String[] selectionArgs = new String[] { "application/pdf", "/storage/emulated/0/RecentFile" };
        String[] selectionArgss = new String[] {"%" + recentFolder + "%"};

        Cursor cursor = context.getContentResolver().query(uri, projection,
                whereClause, null, sortOrder);

        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
        int idPath = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
        int idTitle = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
        int idSize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
        int date_added = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);

        if (cursor != null){
            while (cursor.moveToNext()){
                String pdfId = cursor.getString(idColumn);
                String path = cursor.getString(idPath);
                String title = cursor.getString(idTitle);
                long size = cursor.getLong(idSize);
                String date = cursor.getString(date_added);
                int slashFirstIndex = path.lastIndexOf("/");
                String subString = path.substring(0, slashFirstIndex);

                boolean isSelected = false;
                RecentModel modelPdf = new RecentModel(pdfId, title, path, convertSize(size), date, isSelected);
                if (subString.contains("RecentFile")) {
                    fileList.add(modelPdf);
                }

            }
        }


        return fileList;
    }
    public static ArrayList<PdfModel> fetchPdfFromFolder(Context context, String folderName){
        ArrayList<PdfModel> fileList = new ArrayList<>();

        Uri uri = MediaStore.Files.getContentUri("external");
        String [] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
        };

        String mimeType = "application/pdf";
        String selection = MediaStore.Files.FileColumns.DATA + " like ? ";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
        String[] selectionArgss = new String[] { "application/pdf", "/storage/emulated/0/Downloads" };
        String[] selectionArgs = new String[] {"%" + folderName + "%"};
        //String[] selectionArgs = new String[] { mimeType, folderName };

        Cursor cursor = context.getContentResolver().query(uri, projection,
                whereClause, null, sortOrder);

        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
        int idPath = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
        int idTitle = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
        int idSize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
        int date_added = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);
        int bucket = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME);

        if (cursor != null){
            while (cursor.moveToNext()){

                PdfModel pdfModel = new PdfModel();
                String pdfId = cursor.getString(idColumn);
                String path = cursor.getString(idPath);
                String title = cursor.getString(idTitle);
                long size = cursor.getLong(idSize);
                String date = cursor.getString(date_added);
                String bucketName = cursor.getString(bucket);
                int slashFirstIndex = path.lastIndexOf("/");
                String subString = path.substring(0, slashFirstIndex);

                boolean isSelected = false;
                //PdfModel modelPdf = new PdfModel(pdfId, title, path, convertSize(size), date, isSelected);
                if (folderName.endsWith(bucketName)){
                    pdfModel.setId(pdfId);
                    pdfModel.setTitle(title);
                    pdfModel.setPath(path);
                    pdfModel.setSize(convertSize(size));
                    pdfModel.setDate(date);
                    fileList.add(pdfModel);
                }



            }
        }

        return fileList;
    }

    public static ArrayList<RecentModel> pdfFileDocument(Context context, String folderName){
        ArrayList<RecentModel> fileList = new ArrayList<>();

        Uri uri = MediaStore.Files.getContentUri("external");
        String [] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
        };

        String mimeType = "application/pdf";
        String selection = MediaStore.Files.FileColumns.DATA + " like ? ";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
        String[] selectionArgss = new String[] { mimeType, "%" + folderName + "%" };
        String[] selectionArgs = new String[] {"%" + folderName + "%"};
        //String[] selectionArgs = new String[] { mimeType, folderName };

        Cursor cursor = context.getContentResolver().query(uri, projection,
                whereClause, selectionArgs, sortOrder);

        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
        int idPath = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
        int idTitle = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
        int idSize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
        int date_added = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);
        int bucket = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME);

        if (cursor != null){
            while (cursor.moveToNext()){
                String pdfId = cursor.getString(idColumn);
                String path = cursor.getString(idPath);
                String title = cursor.getString(idTitle);
                long size = cursor.getLong(idSize);
                String date = cursor.getString(date_added);
                String bucketName = cursor.getString(bucket);
                int slashFirstIndex = path.lastIndexOf("/");
                String subString = path.substring(0, slashFirstIndex);

                boolean isSelected = false;
                RecentModel modelPdf = new RecentModel(pdfId, title, path, convertSize(size), date, isSelected);
                fileList.add(modelPdf);



            }
            cursor.close();
        }

        return fileList;
    }

    public static String convertSize(Long size) {
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
    public static void getPdfFileForBelowB(String folderName){
        ArrayList<PdfModel> pdfModels = new ArrayList<>();
        PdfModel item ;
        String file_ext = ".pdf";

        try{
            String folderPath =
                    String.valueOf(Environment.getExternalStorageDirectory());
            File dir = new File(folderName);
            File listPdf[] = dir.listFiles();

            if (listPdf != null){
                for (int i = 0; i < listPdf.length; i++){
                    File pdf_file = listPdf[i];

                    if (pdf_file.getName().endsWith(file_ext)){
                        item = new PdfModel();
                        item.setTitle(pdf_file.getName());
                        item.setPath(pdf_file.getAbsolutePath());
                        item.setDate(String.valueOf(pdf_file.lastModified()));
                        item.setSize(convertSize(pdf_file.length()));

                        pdfModels.add(item);
                    }
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static ArrayList<PdfModel> loadFileFolder(Context context, String folderName) {
        int limitFile = 1;
        int offset = 0;
        ArrayList<PdfModel> pdfModelArrayList = new ArrayList<>();
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED
        };
        String mimeType = "application/pdf";
        String selection = MediaStore.Files.FileColumns.DATA + " like ? ";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
        String[] selectionArgss = new String[] { "application/pdf", "/storage/emulated/0/Downloads" };
        String[] selectionArgs = new String[] {"%" + folderName + "%"};
        //String[] selectionArgs = new String[] {mimeType, "%" + folderName + "%"};
        String limit = limitFile + " OFFSET " + offset;
        //String limit = "10";
        //String offset = String.valueOf(items.size());
        int totalFile = pdfModelArrayList.size();
        Uri uri = MediaStore.Files.getContentUri("external");
        Cursor cursor
                = createCursor(context.getContentResolver(),
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
                pdfModelArrayList.add(pdfModel);

                int totalCount = cursor.getCount();
                int remaining = totalCount - limitFile;
            } while (cursor.moveToNext());
            cursor.close();

        }

        return pdfModelArrayList;

    }



    private static Cursor createCursor(ContentResolver contentResolver,
                                Uri uri, String[] projection,
                                String selection,
                                String[] selectionArgs,
                                String sortOrder,
                                String limit, int offset) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Bundle bundleSelection = createSelectionBundle(projection, selection, selectionArgs, sortOrder, limit, offset);
            return contentResolver.query(uri, projection, bundleSelection, null);

        }
        return contentResolver.query(uri, projection, selection, selectionArgs,
                sortOrder + " LIMIT " + limit);

    }

    private static Bundle createSelectionBundle(String[] projection, String selection, String[] selectionArgs, String sortOrder, String limitFile, int offset) {

        Bundle bundle = new Bundle();
        bundle.putString(ContentResolver.QUERY_ARG_LIMIT, limitFile);
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

 /*   private void loadItems() {
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
        *//*if (sortOrder.equals("ALPHABET")) {
            bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, new String[]{MediaStore.Files.FileColumns.DATE_ADDED});
        } else {
            bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, new String[]{MediaStore.Files.FileColumns.DATE_ADDED});
        }*//*

        bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
        bundle.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs);
        return bundle;
    }*/


}

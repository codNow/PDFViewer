package com.sasha.pdfviewer.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.sasha.pdfviewer.model.PdfModel;

import java.util.ArrayList;

public class FileUtils {


    public static ArrayList<PdfModel> allPdfFile(Context context){
        ArrayList<PdfModel> fileList = new ArrayList<>();
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
}

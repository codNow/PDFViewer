package com.sasha.pdfviewer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.tools.ImageViewActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConvertImageToPdf {


    public static void convertMultipleImages(ArrayList<String> imageList, String filePath) throws IOException {
        String directoryPath = Environment.getExternalStorageDirectory() + "/Converted Pdf/";

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(stream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

// Iterate through the images
        for (int i = 0; i < imageList.size(); i++) {
            Image img = new Image(ImageDataFactory.create(imageList.get(i)));
            document.add(img);
        }
// Close the document
        document.close();

//Write the pdf to a file
        File pdfFile = new File(directoryPath + ".pdf");
        FileOutputStream fos = new FileOutputStream(pdfFile);
        stream.writeTo(fos);
        stream.close();
        fos.close();
    }


    public static void compressAndConvertToPdf(ArrayList<Uri> imageList, String fileName, Context context) throws IOException{
        String directoryPath = Environment.getExternalStorageDirectory() + "/Converted Pdf/";
        File file = new File(directoryPath + fileName + ".pdf");
        PdfDocument pdf = new PdfDocument(new PdfWriter(file));
        // Create a Document
        Document document = new Document(pdf);
        // Iterate through the list of image URIs
        for (Uri imageUri : imageList) {
            ContentResolver contentResolver = context.getContentResolver();
            //get the bitmap from the uri

            Bitmap image = MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
            /*int width = image.getWidth();
            int height = image.getHeight();
            float bitmapRatio = (float)width / (float) height;
            if (bitmapRatio > 1) {
                width = 1024;
                height = (int) (width / bitmapRatio);
            } else {
                height = 1024;
                width = (int) (height * bitmapRatio);
            }
            Bitmap imageBitmap = Bitmap.createScaledBitmap(image, width, height, true);*/
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 75, stream);
            byte[] byteArray = stream.toByteArray();

            // Add the image to the PDF
            Image img = new Image(ImageDataFactory.create(byteArray));
            document.add(img);
        }

        // Close the document
        document.close();
    }

    public static void convertImageToPdf(ArrayList<ImageModel> imageList, String fileName, Context context) throws IOException{
        String directoryPath = Environment.getExternalStorageDirectory() + "/Converted PDF/";
        File file = new File( directoryPath +fileName+".pdf");

        if (!file.getParentFile().exists()){
            file.getParentFile().mkdir();
        }
        PdfDocument pdf = new PdfDocument(new PdfWriter(file));
        // Create a Document
        Document document = new Document(pdf);
        // Iterate through the list of image URIs
        for (ImageModel imageUri : imageList) {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = imageUri.getUri();
            //get the bitmap from the uri

            Bitmap image = MediaStore.Images.Media.getBitmap(contentResolver, uri);
            /*int width = image.getWidth();
            int height = image.getHeight();
            float bitmapRatio = (float)width / (float) height;
            if (bitmapRatio > 1) {
                width = 1024;
                height = (int) (width / bitmapRatio);
            } else {
                height = 1024;
                width = (int) (height * bitmapRatio);
            }
            Bitmap imageBitmap = Bitmap.createScaledBitmap(image, width, height, true);*/
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 75, stream);
            byte[] byteArray = stream.toByteArray();

            // Add the image to the PDF
            Image img = new Image(ImageDataFactory.create(byteArray));
            document.add(img);
        }

        // Close the document
        document.close();
    }
    public static void convertSingleImage(String path){
        Bitmap bitmap1 = BitmapFactory.decodeFile(path);
        android.graphics.pdf.PdfDocument pdfDocument = new android.graphics.pdf.PdfDocument();
        android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(
                1920, 1080, 0).create();
        android.graphics.pdf.PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        page.getCanvas().drawBitmap(bitmap1, 0, 0, null);
        pdfDocument.finishPage(page);
        //String changeName = getWithoutExtension(name);
        //String newPath = directoryPath  + ".pdf";
        //File newFile = new File(newPath);
        //pdfDocument.writeTo(new FileOutputStream(newFile));



        pdfDocument.close();
    }

}

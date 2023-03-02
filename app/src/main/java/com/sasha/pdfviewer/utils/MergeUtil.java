package com.sasha.pdfviewer.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import com.sasha.pdfviewer.model.PdfModel;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeUtil {

    public static void mergePfdFiles(ArrayList<String> filePaths, String output) throws IOException{
        PdfWriter writer = writer = new PdfWriter(output);
        PdfDocument outputPdf = new PdfDocument(writer);
        // Create a PDF merger
        PdfMerger merger = new PdfMerger(outputPdf);
        // Add each input PDF to the merger
        for (String inputPdf : filePaths) {
            PdfDocument pdf = pdf = new PdfDocument(new PdfReader(inputPdf));
            merger.merge(pdf, 1, pdf.getNumberOfPages());
            pdf.close();
        }
        // Close the output PDF
        outputPdf.close();
    }

    public static void mergeMultiplePdf(ArrayList<PdfModel> fileList, String output) throws IOException {

        ArrayList<String> selectedFilePaths = new ArrayList<>();
        for (PdfModel pdfModel : fileList) {
            if (pdfModel.isSelected()) {
                selectedFilePaths.add(pdfModel.getPath());
            }
        }

        try  {

            PdfWriter writer = new PdfWriter(output);
            PdfDocument pdf = new PdfDocument(writer);
            PdfMerger merger = new PdfMerger(pdf);

            for (String filePath1 : selectedFilePaths) {
                try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(new FileInputStream(filePath1)))) {
                    merger.merge(pdfDoc, 1, pdfDoc.getNumberOfPages());
                    pdfDoc.close();
                }
            }
            writer.close();
            pdf.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }




    }




}

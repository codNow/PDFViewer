package com.sasha.pdfviewer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfReader;
import com.shockwave.pdfium.PdfiumCore;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class ImageExtractUtil {

    public static void extractImages(String file, String outputFile, Context context) throws IOException {
        ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(
                new File(file), ParcelFileDescriptor.MODE_READ_ONLY
        );
        PdfiumCore pdfiumCore = new PdfiumCore(context);
        PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
        int totalPages = pdfRenderer.getPageCount();
        for (int i = 0; i <totalPages; i++){
            PdfRenderer.Page page = pdfRenderer.openPage(i);
            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            String filename = outputFile + (i+1) + ".jpg";
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            page.close();
        }
        pdfRenderer.close();
        fileDescriptor.close();
    }

}

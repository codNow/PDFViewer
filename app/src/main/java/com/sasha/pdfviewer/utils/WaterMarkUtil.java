package com.sasha.pdfviewer.utils;

import static com.itextpdf.kernel.pdf.PdfName.Watermark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.print.pdf.PrintedPdfDocument;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WaterMarkUtil {

    public static void waterMarkPdf(String input, String output, String textMark) throws IOException {
        try {
            // Open the input file for reading
            ParcelFileDescriptor inputFileDescriptor = ParcelFileDescriptor.open(new File(input), ParcelFileDescriptor.MODE_READ_ONLY);

            // Create a PDF renderer
            PdfRenderer pdfRenderer = new PdfRenderer(inputFileDescriptor);

            // Create a new PDF document
            PdfDocument pdfDocument = new PdfDocument();

            // Get the number of pages in the input file
            int pageCount = pdfRenderer.getPageCount();

            // Iterate through the pages
            for (int i = 0; i < pageCount; i++) {
                // Open the current page
                PdfRenderer.Page page = pdfRenderer.openPage(i);

                // Get the width and height of the current page
                int width = page.getWidth();
                int height = page.getHeight();

                // Create a bitmap for the current page
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                // Create a canvas for the bitmap


                // Draw the page on the canvas
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                // Create a paint for the watermark text
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setTextSize(30);
                Canvas canvas = new Canvas(bitmap);
                // Draw the watermark text on the canvas
                canvas.drawText(textMark, 50, 50, paint);
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, i).create();
                // Create a new page in the output PDF
                PdfDocument.Page pdfPage = pdfDocument.startPage(pageInfo);

                // Draw the bitmap with the watermark on the new page
                pdfPage.getCanvas().drawBitmap(bitmap, 0, 0, null);

                // Finish the page
                pdfDocument.finishPage(pdfPage);

                // Close the current page
                page.close();
            }

            // Write the output PDF to disk
            File outputFile = new File(output);
            pdfDocument.writeTo(new FileOutputStream(outputFile));

            // Close the input and output files
            pdfRenderer.close();
            pdfDocument.close();
            inputFileDescriptor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

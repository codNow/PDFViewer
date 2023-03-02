package com.sasha.pdfviewer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.media.Image;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.EditText;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.EncryptionConstants;
import com.itextpdf.kernel.pdf.EncryptionProperties;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class PdfUtils {

    public static final String TAG = "PdfUtils";
   /* int encryptionStrength = EncryptionAlgorithms.STANDARD_ENCRYPTION_128;*/

    public static boolean isConvertibleToText(String pdfPath) {
        PdfDocument pdfDoc = null;
        try {
            pdfDoc = new PdfDocument(new PdfReader(pdfPath));
            int n = pdfDoc.getNumberOfPages();
            for (int i = 1; i <= n; i++) {
                if (PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i)).trim().length() > 0) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Error reading PDF file", e);
            return false;
        } finally {
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }
        /*calling method */
        /*String pdfFilePath = "/path/to/pdf/file.pdf";
        if (PdfUtils.isConvertibleToText(pdfFilePath)) {
            // The PDF file is convertible to text
        } else {
            // The PDF file is not convertible to text
        }*/
    }
    public static boolean isFileNotLock(File pdfPath){
        try {
            new PdfRenderer(ParcelFileDescriptor.open(pdfPath, ParcelFileDescriptor.MODE_READ_ONLY));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void convertToText(String inputPath,String outputPath) throws IOException{
        PdfReader reader = new PdfReader(inputPath);
        PdfDocument pdfDocument = new PdfDocument(reader);
        int n = pdfDocument.getNumberOfPages();
        FileOutputStream fos = new FileOutputStream(outputPath +".doc");
        for (int i = 1; i <= n; i++){
            String text = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i));
            fos.write(text.getBytes());
        }
        pdfDocument.close();
        fos.close();

    }
    public static void encryptPdf(String inputFilePath, String outputFilePath, String userPassword, String ownerPassword) throws IOException {
        PdfReader pdfReader = new PdfReader(inputFilePath);
        PdfWriter pdfWriter = new PdfWriter(outputFilePath, new WriterProperties().setStandardEncryption(userPassword.getBytes(), ownerPassword.getBytes(),
                EncryptionConstants.ALLOW_PRINTING,
                EncryptionConstants.ENCRYPTION_AES_128 |
                        EncryptionConstants.DO_NOT_ENCRYPT_METADATA));
        PdfDocument pdfDoc = new PdfDocument(pdfReader, pdfWriter, new StampingProperties().useAppendMode());
        pdfDoc.close();

        /*calling method*/
      /*  try {
            PdfEncryptor.encryptPdf(inputFilePath, outputFilePath, userPassword, ownerPassword, encryptionStrength);
            Toast.makeText(this, "File Encrypted Successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error encrypting file", Toast.LENGTH_SHORT).show();
        }*/
    }
    public static void compressPdf(String inputFile, String outFile) throws IOException{
        PdfReader pdfReader = new PdfReader(inputFile);
       /* PdfWriter writer = new PdfWriter(outFile, new WriterProperties().setCompressionLevel(9));*/
        PdfWriter writer = new PdfWriter(outFile, new WriterProperties().setFullCompressionMode(true));
        PdfDocument pdfDocument = new PdfDocument(pdfReader, writer);
        pdfDocument.close();
    }

    public static void mergePdf(String mergePdf, String firstPdf, String secondPdf) throws IOException{
        PdfDocument outputPdf = new PdfDocument(new PdfWriter("merged.pdf"));
        PdfDocument first_Pdf = new PdfDocument(new PdfReader(firstPdf));
        PdfDocument second_Pdf = new PdfDocument(new PdfReader(secondPdf));
        int n = first_Pdf.getNumberOfPages();
        for (int i = 1; i <= n; i++) {
            first_Pdf.copyPagesTo(i, i, outputPdf);
        }
        n = second_Pdf.getNumberOfPages();
        for (int i = 1; i <= n; i++) {
            second_Pdf.copyPagesTo(i, i, outputPdf);
        }
        outputPdf.close();
        first_Pdf.close();
        second_Pdf.close();
    }

    public static void splitPdf(String inputFile, String outputFile) throws IOException{

        PdfReader reader = new PdfReader(inputFile);
        PdfDocument pdfDoc = new PdfDocument(reader);
        int numberOfPages = pdfDoc.getNumberOfPages();
        int pageCount = 0;
        int documentCount = 1;

        // Create a new PDF writer for each new document

        PdfWriter writer = new PdfWriter(outputFile + "part-" + documentCount + ".pdf");
        PdfDocument newPdfDoc = new PdfDocument(writer);

        for (int i = 1; i <= numberOfPages; i++) {
            pageCount++;
            newPdfDoc.addPage(pdfDoc.getPage(i));
            if (pageCount == 10) {
                newPdfDoc.close();
                documentCount++;
                writer = new PdfWriter(outputFile + "part-" + documentCount + ".pdf");
                newPdfDoc = new PdfDocument(writer);
                pageCount = 0;
            }
        }
        newPdfDoc.close();
        pdfDoc.close();
    }

    public static void waterMarkPdf(String inputFile, String outputFile, EditText waterMarkText) throws IOException{
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputFile), new PdfWriter(outputFile));
        PdfCanvas canvas = new PdfCanvas(pdfDoc.getFirstPage());

// Set the font and size for the watermark text
        canvas.setFontAndSize(PdfFontFactory.createFont(StandardFonts.HELVETICA), 42);

// Set the color for the watermark text
        canvas.setFillColor(new DeviceRgb(0,0,0));

// Rotate the canvas to write the watermark text at an angle
        canvas.saveState();
        canvas.beginText();
        float x = pdfDoc.getDefaultPageSize().getWidth() / 2;
        float y = pdfDoc.getDefaultPageSize().getHeight() / 2;
        float angle = 45;
        canvas.setTextMatrix( x, y);
        canvas.showText(waterMarkText.getText().toString());
        canvas.endText();
        canvas.restoreState();

        pdfDoc.close();
    }



    public  static void extractImages(String fileName, String outPutFile) throws IOException{
        String pdfFile = "path/to/pdf/file.pdf";
        //String outputDir = Environment.getExternalStorageDirectory()+"/ExtractedImages/";
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(fileName));
        int totalPages = pdfDoc.getNumberOfPages();
        PdfCanvasProcessor parser = new PdfCanvasProcessor(new ImageExtractionStrategy(outPutFile));
        for (int i = 1; i <= totalPages; i++) {
            parser.processPageContent(pdfDoc.getPage(i));
        }

    }


    private static class ImageExtractionStrategy implements IEventListener {
        private String outPutFile;
        private int imageCounter = 1;
        public ImageExtractionStrategy(String outPutFile) {
            this.outPutFile = outPutFile;
        }

        @Override
        public void eventOccurred(IEventData data, EventType type) {
            if (type.equals(EventType.RENDER_IMAGE)) {
                ImageRenderInfo image = (ImageRenderInfo) data;
                try {
                    String filename = outPutFile + "/image" + imageCounter + "." +
                            image.getImage().identifyImageFileExtension();
                    Files.write(Paths.get(filename), image.getImage().getImageBytes());
                    imageCounter++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public Set<EventType> getSupportedEvents() {
            return null;
        }
    }

     public static void copyFile(String listFile, String dest, String newName) throws IOException {

        File sourceFile = new File(String.valueOf(listFile));
        File file = new File(String.valueOf(listFile));
        String filename = file.getName();
        FileInputStream in = new FileInputStream(file);
        FileOutputStream out = new FileOutputStream(dest+filename);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.flush();
        out.close();
        sourceFile.delete();
    }


}

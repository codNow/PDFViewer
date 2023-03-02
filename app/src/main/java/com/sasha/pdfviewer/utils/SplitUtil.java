package com.sasha.pdfviewer.utils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.kernel.utils.PdfSplitter;
import com.itextpdf.kernel.utils.PageRange;
import com.itextpdf.layout.Document;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class SplitUtil {
    public  static void splitWithItext(String inputFile,
                                String outPutFile
                                /*int pageNumber*/) throws IOException  {

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputFile));
        int numberOfPages = pdfDoc.getNumberOfPages();
        int pageCounter = 1;
        int documentCounter = 1;
        for (int i = 1; i <= numberOfPages; i++) {
            String fileName = "Page - " + i +".pdf";
            //String fileName = "Page - " + documentCounter + ".pdf";
            String pageFilePath = outPutFile + fileName;
            PdfDocument pdfPage = new PdfDocument(new PdfWriter(pageFilePath));
            pdfPage.addNewPage();
            PdfCanvas canvas = new PdfCanvas(pdfPage.getFirstPage());
            canvas.addXObjectAt(pdfDoc.getPage(i).copyAsFormXObject(pdfPage), 0, 0);
            pdfPage.close();
            /*pdfPage.addPage(pdfDoc.getPage(i));
            pageCounter++;
            if (pageCounter > pageNumber){
                pdfPage.close();
                pageCounter = 1;
                documentCounter++;
            }
            pdfPage.close();*/
        }
        pdfDoc.close();
    }

    public static void splitPdfFile(String inputFile, String outputFile) throws IOException{
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputFile));
        int numberOfPages = pdfDoc.getNumberOfPages();
        for (int i = 1; i <= numberOfPages; i++) {
            String fileName = "Page - " + i + ".pdf";
            String pageFilePath = outputFile + File.separator + fileName;
            PdfDocument pdfPage = new PdfDocument(new PdfWriter(pageFilePath));
            pdfPage.addPage(pdfDoc.getPage(i));
            pdfPage.close();
        }
        pdfDoc.close();
    }
        /*PdfReader pdfReader = new PdfReader(inputFile);
        PdfDocument document = new PdfDocument(pdfReader);
        int numberOfPages = document.getNumberOfPages();
        int pageCount = 0;
        int documentCount = 1;
        PdfWriter writer = new PdfWriter(output +"part-"+ documentCount+ ".pdf");
        PdfDocument pdfDocument = new PdfDocument(writer);
        for (int i=0; i<=numberOfPages; i++){
            pageCount++;
            pdfDocument.addPage(document.getPage(i));
            if (pageCount == 2){
                pdfDocument.close();
                documentCount++;
                writer = new PdfWriter(output+"part-"+documentCount+".pdf");
                pdfDocument = new PdfDocument(writer);
                pageCount=0;
            }
        }
        pdfDocument.close();
        document.close();*/

}

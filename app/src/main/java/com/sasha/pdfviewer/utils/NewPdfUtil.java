package com.sasha.pdfviewer.utils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.IOException;

public class NewPdfUtil {

    public void createNewPdf(String filePath, String newText) throws IOException {
        File outputFile = new File(filePath);
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(filePath));
        Document document = new Document(pdfDocument);
        document.add(new Paragraph(newText));
        document.close();
    }
}


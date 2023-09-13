package com.sasha.pdfviewer.utils;

import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import com.itextpdf.kernel.pdf.EncryptionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfEncryption;
import com.itextpdf.kernel.pdf.PdfEncryptor;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.ReaderProperties;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.sasha.pdfviewer.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

public class EncryptDecrypt {
    byte[] buf = new byte[1024];
    public static boolean isLockable(File pdfPath){
        try {
            new PdfRenderer(ParcelFileDescriptor.open(pdfPath, ParcelFileDescriptor.MODE_READ_ONLY));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void encrypt(String inputFilePath, File outputFilePath,
                        String userPassword, String ownerPassword) throws IOException {
        /*PdfReader pdfReader = new PdfReader(inputFilePath);
        PdfWriter pdfWriter = new PdfWriter(outputFilePath,
                new WriterProperties().setStandardEncryption(
                userPassword.getBytes(), ownerPassword.getBytes(),
                EncryptionConstants.ALLOW_PRINTING,
                EncryptionConstants.ENCRYPTION_AES_128 |
                        EncryptionConstants.DO_NOT_ENCRYPT_METADATA));
        PdfDocument pdfDoc = new PdfDocument(pdfReader, pdfWriter,
                new StampingProperties().useAppendMode());
        pdfDoc.close();*/

        PdfDocument pdfDoc = new PdfDocument(
                new PdfReader(inputFilePath),
                new PdfWriter(String.valueOf(outputFilePath), new WriterProperties().setStandardEncryption(
                        userPassword.getBytes(),
                        ownerPassword.getBytes(),
                        EncryptionConstants.ALLOW_PRINTING,
                        EncryptionConstants.ENCRYPTION_AES_128 |
                                EncryptionConstants.DO_NOT_ENCRYPT_METADATA)));
        pdfDoc.close();

    }

    public static void decrypt(String inputFilePath, String outputFilePath,
                               String passwordText) throws IOException {

        PdfReader pdfReader = new PdfReader(inputFilePath,
                new ReaderProperties().setPassword(passwordText.getBytes()));
        PdfWriter writer = new PdfWriter(outputFilePath);
        PdfDocument document = new PdfDocument(pdfReader, writer);
        document.close();
        pdfReader.close();
        writer.close();



    }

    private static boolean removeFilePassword(String inputFile, String outPutFile, final
                    String inputPassword) throws IOException {
        try (PdfDocument document = new PdfDocument(
                new PdfReader(inputFile, new ReaderProperties()
                        .setPassword(inputPassword.getBytes())),
                new PdfWriter(outPutFile)
        )) {
            byte[] userPasswordBytes = document.getReader().computeUserPassword();

            // The result of user password computation logic can be null in case of
            // AES256 password encryption or non password encryption algorithm
            String userPassword = userPasswordBytes == null ? null :
                    new String(userPasswordBytes);

        }
        return true;
    }
}

package com.sasha.pdfviewer.model;

import java.util.Date;

public class RecentModel {

    private String pdfId;
    private String pdfTitle;
    private String pdfPath;
    private String pdfSize;
    private String pdfDate;


    public RecentModel() {
    }

    public RecentModel(String pdfId, String pdfTitle, String pdfPath, String pdfSize, String pdfDate) {
        this.pdfId = pdfId;
        this.pdfTitle = pdfTitle;
        this.pdfPath = pdfPath;
        this.pdfSize = pdfSize;
        this.pdfDate = pdfDate;
    }

    public String getPdfId() {
        return pdfId;
    }

    public void setPdfId(String pdfId) {
        this.pdfId = pdfId;
    }

    public String getPdfTitle() {
        return pdfTitle;
    }

    public void setPdfTitle(String pdfTitle) {
        this.pdfTitle = pdfTitle;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getPdfSize() {
        return pdfSize;
    }

    public void setPdfSize(String pdfSize) {
        this.pdfSize = pdfSize;
    }

    public String getPdfDate() {
        return pdfDate;
    }

    public void setPdfDate(String pdfDate) {
        this.pdfDate = pdfDate;
    }
}

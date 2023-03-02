package com.sasha.pdfviewer.model;

public class PdfModel {

    private String id;
    private String title;
    private String path;
    private String size;
    private String date;
    private boolean isSelected;

    public PdfModel() {
    }

    public PdfModel(String id, String title, String path, String size, String date,boolean isSelected) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.size = size;
        this.date = date;
        this.isSelected = isSelected;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

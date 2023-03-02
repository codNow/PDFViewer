package com.sasha.pdfviewer.model;


import android.net.Uri;

public class ImageModel {

    private Uri uri;
    private String imagePath;
    private String imageDate;


    public ImageModel(){

    }

    public ImageModel(Uri uri, String imagePath, String imageDate) {
        this.uri = uri;
        this.imagePath = imagePath;
        this.imageDate = imageDate;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageDate() {
        return imageDate;
    }

    public void setImageDate(String imageDate) {
        this.imageDate = imageDate;
    }
}

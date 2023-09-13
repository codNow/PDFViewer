package com.sasha.pdfviewer.model;

import android.net.Uri;

public class FolderModel {

    private  String path;
    private  String FolderName;
    private int numberOfPics = 0;
    private Uri image;
    private String firstPic;
    private boolean isSelected;

    public FolderModel(){

    }

    public FolderModel(String path, String folderName, int numberOfPics, Uri image, boolean isSelected) {
        this.path = path;
        FolderName = folderName;
        this.numberOfPics = numberOfPics;
        this.image = image;
        this.isSelected = isSelected;

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFolderName() {
        return FolderName;
    }

    public void setFolderName(String folderName) {
        FolderName = folderName;
    }

    public int getNumberOfPics() {
        return numberOfPics;
    }

    public void setNumberOfPics(int numberOfPics) {
        this.numberOfPics = numberOfPics;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }

    public void addpics(){
        this.numberOfPics++;
    }

    public String getFirstPic() {
        return firstPic;
    }

    public void setFirstPic(String firstPic) {
        this.firstPic = firstPic;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

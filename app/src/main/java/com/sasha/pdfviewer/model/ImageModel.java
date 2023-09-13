package com.sasha.pdfviewer.model;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class ImageModel implements Parcelable {

    private Uri uri;
    private String imageId;
    private String imageTitle;
    private String imagePath;
    private String imageDate;
    private boolean isSelected;
    private Bitmap bitmap;


    public ImageModel(){

    }

    public ImageModel(Uri uri, String imageId, String imageTitle, String imagePath,
                      String imageDate, boolean isSelected, Bitmap bitmap) {
        this.uri = uri;
        this.imageId = imageId;
        this.imageTitle = imageTitle;
        this.imagePath = imagePath;
        this.imageDate = imageDate;
        this.isSelected = isSelected;
        this.bitmap = bitmap;
    }

    public ImageModel(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        imagePath = in.readString();
        imageDate = in.readString();
        isSelected = in.readByte() != 0;
    }

    public static final Creator<ImageModel> CREATOR = new Creator<ImageModel>() {
        @Override
        public ImageModel createFromParcel(Parcel in) {
            return new ImageModel(in);
        }

        @Override
        public ImageModel[] newArray(int size) {
            return new ImageModel[size];
        }
    };

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeParcelable(uri, i);
        parcel.writeString(imagePath);
        parcel.writeString(imageDate);
        parcel.writeByte((byte) (isSelected ? 1 : 0));
    }
  /*  @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ImageModel)) {
            return false;
        }
        ImageModel other = (ImageModel) obj;
        return Objects.equals(uri, other.uri) &&
                Objects.equals(imagePath, other.imagePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, imagePath);
    }*/
}

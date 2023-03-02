package com.sasha.pdfviewer.model;

import java.util.Date;

public class ModelPdf {

    private String title;

    public ModelPdf(){

    }
    public ModelPdf(String title) {

        this.title = title;

    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}


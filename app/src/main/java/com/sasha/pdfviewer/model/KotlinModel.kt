package com.sasha.pdfviewer.model

import android.widget.TextView

class KotlinModel {

    fun helloKotlin(myTexting: TextView){
        val myText: String = "I am a kotlinger";
        myTexting.text = myText;
    }
}
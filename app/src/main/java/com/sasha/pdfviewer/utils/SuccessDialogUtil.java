package com.sasha.pdfviewer.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialDialogs;
import com.sasha.pdfviewer.R;

public class SuccessDialogUtil {

    public static void successDialog(TextView title, String textMessage, String questionText,
                                     Button positiveBtn, Button negativeBtn, Context context){

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_popup_layout);


    }

    public static void loadingDialog(TextView loadingText, Context context){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_dialog);

        loadingText = dialog.findViewById(R.id.loading_text);
        dialog.show();
        dialog.dismiss();
    }

}

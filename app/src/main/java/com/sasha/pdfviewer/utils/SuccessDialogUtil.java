package com.sasha.pdfviewer.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.tools.ToolsActivity;

public class SuccessDialogUtil {

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    public static void showSuccessDialog(View view, String dest,
                                         String title, String successTitle,
                                         String successQuestion, int successIcon) {
        Context context = view.getContext();
        Dialog successDialog = new Dialog(context);
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.upload_done_layout);
        TextView textView1 = successDialog.findViewById(R.id.successText);
        TextView textView2 = successDialog.findViewById(R.id.pathText);
        TextView textView3 = successDialog.findViewById(R.id.question_text);
        Button noButton = successDialog.findViewById(R.id.no_btn);
        Button yesButton = successDialog.findViewById(R.id.yes_btn);
        ImageView word_icon = successDialog.findViewById(R.id.done_icon);

        textView1.setText(successTitle);
        textView2.setText(dest);
        word_icon.setImageDrawable(context.getDrawable(successIcon));
        word_icon.setColorFilter(context.getColor(R.color.blue));
        textView3.setText(successQuestion);

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                successDialog.dismiss();
                Intent intent = new Intent(context, ToolsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                successDialog.dismiss();
            }
        });

        successDialog.show();
        successDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        successDialog.getWindow().setGravity(Gravity.TOP);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    public static void showSuccessDialogActivity(Context context, String dest,
                                                 String title, String successTitle,
                                                 String successQuestion, int successIcon) {

        Dialog successDialog = new Dialog(context);
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.upload_done_layout);
        TextView textView1 = successDialog.findViewById(R.id.successText);
        TextView textView2 = successDialog.findViewById(R.id.pathText);
        TextView textView3 = successDialog.findViewById(R.id.question_text);
        Button noButton = successDialog.findViewById(R.id.no_btn);
        Button yesButton = successDialog.findViewById(R.id.yes_btn);
        ImageView word_icon = successDialog.findViewById(R.id.done_icon);

        textView1.setText(successTitle);
        textView2.setText(dest + title);
        word_icon.setImageDrawable(context.getDrawable(successIcon));
        textView3.setText(successQuestion);

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                successDialog.dismiss();
                Intent intent = new Intent(context, ToolsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                successDialog.dismiss();
            }
        });

        successDialog.show();
        successDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        successDialog.getWindow().setGravity(Gravity.TOP);
    }


}

package com.sasha.pdfviewer.tools;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.utils.NewPdfUtil;
import com.sasha.pdfviewer.utils.SplitUtil;

import java.io.File;
import java.io.IOException;

public class NewPdfFileActivity extends AppCompatActivity {

    private EditText newText, textTitle;
    private Button createBtn;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pdf_file);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        newText = findViewById(R.id.editText);
        createBtn = findViewById(R.id.createButton);
        textTitle = findViewById(R.id.titleText);

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputText = newText.getText().toString();
                String title = textTitle.getText().toString();
                Dialog dialog = new Dialog(NewPdfFileActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.progress_dialog);
                dialog.setCanceledOnTouchOutside(false);
                TextView textView = dialog.findViewById(R.id.loading_text);
                textView.setText(R.string.new_file_progress);
                dialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);
                String destiny = Environment.getExternalStorageDirectory() +
                        "/NewFile Folder/"+title+".pdf";
                File dest = new File(destiny);

                if (!dest.getParentFile().exists()){
                    dest.getParentFile().mkdir();
                }
                dialog.show();
                if (destiny != null && !TextUtils.isEmpty(inputText)){
                    new CountDownTimer(2500, 1500){
                        @Override
                        public void onTick(long l) {

                        }
                        @Override
                        public void onFinish() {
                            try {
                                //int numberPages = 2;
                                if (!TextUtils.isEmpty(inputText) && !TextUtils.isEmpty(title)){
                                    File outputFile = new File(destiny);
                                    PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputFile));
                                    PdfCanvas canvas = new PdfCanvas(pdfDoc.addNewPage());
                                    PdfFont font = PdfFontFactory.createFont();
                                    canvas.beginText().setFontAndSize(font, 12).moveText(36, 750).showText(inputText).endText();
                                    pdfDoc.close();
                                    Toast.makeText(getApplicationContext(), R.string.new_file_success, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    newText.setText("");
                                    textTitle.setText("");
                                    popupSuccessDialog(destiny, title);
                                }
                                else{
                                    dialog.dismiss();
                                    Toast.makeText(NewPdfFileActivity.this, R.string.please_write_something, Toast.LENGTH_LONG).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }

                        }
                    }.start();
                }else {
                    dialog.dismiss();
                    Toast.makeText(NewPdfFileActivity.this, R.string.please_write_something, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void popupSuccessDialog(String dest, String title){
        Dialog successDialog = new Dialog(NewPdfFileActivity.this);
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.upload_done_layout);
        TextView textView1, textView2, textView3;
        Button noButton, yesButton;
        ImageView word_icon;

        textView1 = successDialog.findViewById(R.id.successText);
        textView2 = successDialog.findViewById(R.id.pathText);
        textView3 = successDialog.findViewById(R.id.question_text);
        noButton = successDialog.findViewById(R.id.no_btn);
        yesButton = successDialog.findViewById(R.id.yes_btn);
        word_icon = successDialog.findViewById(R.id.done_icon);

        textView1.setText(R.string.new_file_success);
        textView2.setText(dest+title);
        word_icon.setImageDrawable(getApplicationContext().getDrawable(R.drawable.na_na));
        textView3.setText(R.string.new_file_question);

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successDialog.dismiss();
                Intent intent = new Intent(NewPdfFileActivity.this, ToolsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successDialog.dismiss();

            }

        });
        successDialog.show();
        successDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        successDialog.getWindow().setGravity(Gravity.TOP);
    }
}
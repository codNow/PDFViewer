package com.sasha.pdfviewer.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itextpdf.kernel.pdf.EncryptionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.EncryptDecrypt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class LockAdapter extends RecyclerView.Adapter<LockAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PdfModel> pdfModelArrayList;

    public LockAdapter(Context context, ArrayList<PdfModel> pdfModelArrayList) {
        this.context = context;
        this.pdfModelArrayList = pdfModelArrayList;
    }

    @NonNull
    @Override
    public LockAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LockAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PdfModel modelPdf = pdfModelArrayList.get(position);

        String title = modelPdf.getTitle();
        String path = modelPdf.getPath();

        holder.pdfTitle.setText(modelPdf.getTitle());
        holder.pdfSize.setText(modelPdf.getSize());
        //holder.pdfPath.setText(modelPdf.getPdfPath());
        String filePath = modelPdf.getPath();
        File file = new File(filePath);
        String parentPath = file.getParentFile().getName();
       // holder.pdfPath.setText(parentPath);

        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                if (EncryptDecrypt.isLockable(file)){
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    holder.option_btn.setVisibility(View.GONE);
                    final Dialog dialog = new Dialog(view.getRootView().getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_popup_layout);
                    EditText editText;
                    Button cancelBtn, okBtn;
                    TextView question_text, textViewTitle, textMessage;
                    textViewTitle = dialog.findViewById(R.id.textTitle);
                    textMessage = dialog.findViewById(R.id.textMessage);
                    textMessage.setText("Enter your password");
                    textViewTitle.setText("Protect Pdf");
                    editText = dialog.findViewById(R.id.editText);
                    cancelBtn = dialog.findViewById(R.id.buttonNo);
                    okBtn = dialog.findViewById(R.id.buttonYes);
                    question_text = dialog.findViewById(R.id.question_text);
                    question_text.setText("Are you sure to lock this file ?");
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            holder.checkboxImage.setVisibility(View.GONE);
                            holder.option_btn.setVisibility(View.VISIBLE);
                        }
                    });
                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            encryptFile(position, v, filePath, editText, title);
                            dialog.dismiss();
                            holder.checkboxImage.setVisibility(View.GONE);

                        }
                    });
                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setGravity(Gravity.END);
                }
                else {
                    Toast.makeText(context, "This is file is already locked !", Toast.LENGTH_SHORT).show();
                }

            }
        });

        try {
            new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (Exception e) {
            e.printStackTrace();
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.file_lock2));
        }

    }

    private void encryptFile(int position, View v, String filePath, EditText password, String title) {
        String pwdText = password.getText().toString();
        Dialog dialog = new Dialog(v.getRootView().getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.progress_dialog);
        dialog.setCanceledOnTouchOutside(false);
        TextView textView = dialog.findViewById(R.id.loading_text);
        textView.setText(R.string.encrypted_dialog);
        dialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);
        File destiny = new File(Environment.getExternalStorageDirectory() +
                "/Locked Folder/" + title);

        if (!destiny.getParentFile().exists()){
            destiny.getParentFile().mkdir();
        }

        dialog.show();

        if (!TextUtils.isEmpty(pwdText)){
            new CountDownTimer(3500, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    // Perform your action here
                    PdfDocument pdfDoc = null;
                    try {
                        pdfDoc = new PdfDocument(
                                new PdfReader(filePath),
                                new PdfWriter(String.valueOf(destiny+".pdf"), new WriterProperties().setStandardEncryption(
                                        pwdText.getBytes(),
                                        pwdText.getBytes(),
                                        EncryptionConstants.ALLOW_PRINTING,
                                        EncryptionConstants.ENCRYPTION_AES_128 |
                                                EncryptionConstants.DO_NOT_ENCRYPT_METADATA))
                        );
                        pdfDoc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    dialog.dismiss();
                    Toast.makeText(context, "File is encrypted", Toast.LENGTH_SHORT).show();
                    popupDoneDialog(v,destiny, title);

                }
            }.start();

        }
        else{
            Toast.makeText(context, "please enter your password", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }

    private void popupDoneDialog(View v, File destiny, String title) {
        Dialog successDialog = new Dialog(v.getRootView().getContext());
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

        textView1.setText(R.string.locked_successfully_display);
        textView2.setText(destiny+title);
        word_icon.setImageDrawable(context.getDrawable(R.drawable.lock_icon));
        textView3.setText(R.string.locked_question);

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successDialog.dismiss();
                Intent intent = new Intent(context, ToolsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
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


    @Override
    public int getItemCount() {
        return pdfModelArrayList.size();
    }

    public void updateSearchList(ArrayList<PdfModel> searchList) {
        pdfModelArrayList = new ArrayList<>();
        pdfModelArrayList.addAll(searchList);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;
        private ImageView option_btn, imageView, checkboxImage;
        private CheckBox checkBox;
        private RelativeLayout pdfFile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.pdfPath);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            option_btn = itemView.findViewById(R.id.option_icon);
            imageView = itemView.findViewById(R.id.imageView);
            checkboxImage = itemView.findViewById(R.id.checking);

        }
    }
}

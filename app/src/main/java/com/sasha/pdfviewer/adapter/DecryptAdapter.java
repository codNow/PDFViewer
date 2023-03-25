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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.io.source.IRandomAccessSource;
import com.itextpdf.kernel.exceptions.BadPasswordException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.ReaderProperties;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.tools.ToolsActivity;
import com.sasha.pdfviewer.utils.EncryptDecrypt;
import com.sasha.pdfviewer.utils.PdfUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DecryptAdapter extends RecyclerView.Adapter<DecryptAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PdfModel> pdfModelArrayList;
    private ViewHolder holder;
    private boolean isError = false;

    public DecryptAdapter(Context context, ArrayList<PdfModel> pdfModelArrayList) {
        this.context = context;
        this.pdfModelArrayList = pdfModelArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_layout, parent, false);
        holder =  new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PdfModel modelPdf = pdfModelArrayList.get(position);

        String title = modelPdf.getTitle();
        String path = modelPdf.getPath();

        holder.pdfTitle.setText(modelPdf.getTitle());
        holder.pdfSize.setText(modelPdf.getSize());
        //holder.pdfPath.setText(modelPdf.getPdfPath());
        String filePath = modelPdf.getPath();
        File file = new File(filePath);
        String parentPath = file.getParentFile().getName();
        holder.pdfPath.setText(parentPath);
        try {
            new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (Exception e) {
            e.printStackTrace();
            holder.itemView.setVisibility(View.VISIBLE);
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.file_lock2));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!PdfUtils.isFileNotLock(file)) {
                    holder.option_btn.setVisibility(View.GONE);
                    holder.checkboxImage.setVisibility(View.VISIBLE);
                    final Dialog dialog = new Dialog(view.getRootView().getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.password_layout);
                    EditText editText;
                    Button cancelBtn, okBtn;
                    TextView question_text, textViewTitle, titleText;
                    titleText = dialog.findViewById(R.id.textTitle);
                    titleText.setText(R.string.removed_title);
                    editText = dialog.findViewById(R.id.pwdText);
                    textViewTitle = dialog.findViewById(R.id.advice_text);
                    textViewTitle.setText(R.string.enter_password_question);
                    cancelBtn = dialog.findViewById(R.id.buttonNo);
                    okBtn = dialog.findViewById(R.id.buttonYes);
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
                            holder.checkboxImage.setVisibility(View.GONE);
                            holder.option_btn.setVisibility(View.VISIBLE);
                            String pwdText = editText.getText().toString();
                            String destiny = Environment.getExternalStorageDirectory() +
                                    "/Unlocked Folder/" + title;
                            File dest = new File(destiny);
                            dialog.show();

                            if (!dest.getParentFile().exists()){
                                dest.getParentFile().mkdir();
                            }
                            Dialog loadingDialog = new Dialog(v.getRootView().getContext());
                            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            loadingDialog.setContentView(R.layout.progress_dialog);
                            loadingDialog.setCanceledOnTouchOutside(false);
                            loadingDialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);
                            TextView textView = loadingDialog.findViewById(R.id.loading_text);
                            textView.setText(R.string.unlock_progress);
                            new CountDownTimer(1500, 1500){

                                @Override
                                public void onTick(long l) {
                                    loadingDialog.show();
                                }

                                @Override
                                public void onFinish() {
                                    try {
                                        EncryptDecrypt.decrypt(filePath, destiny+".pdf", pwdText);
                                        popupSuccessDialog(view, filePath, destiny,title);
                                    } catch (BadPasswordException e) {
                                        loadingDialog.dismiss();
                                        dialog.show();
                                        // Incorrect password, show error message to user
                                        Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show();
                                        return;
                                    } catch (Exception e) {
                                        // Other error occurred, show error message to user
                                        Toast.makeText(context, "Error decrypting PDF", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    loadingDialog.dismiss();

                                }
                            }.start();

                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setGravity(Gravity.END);
                } else {
                    Snackbar.make(view, R.string.file_not_protected, Snackbar.LENGTH_SHORT).show();
                }

            }
        });

    }


    private void unlockPdf(int position, View v, String filePath, String title, String pwdText) {
        Dialog dialog = new Dialog(v.getRootView().getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_dialog);
        dialog.setCanceledOnTouchOutside(false);
        TextView textView = dialog.findViewById(R.id.loading_text);
        textView.setText(R.string.unlock_progress);
        dialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);
        String destiny = Environment.getExternalStorageDirectory() +
                "/Unlocked Folder/" + title;
        File dest = new File(destiny);
        dialog.show();

        if (!dest.getParentFile().exists()){
            dest.getParentFile().mkdir();
            decryptPdfFile(v, filePath, destiny, title, pwdText, dialog);
        }
        else{
            decryptPdfFile(v, filePath, destiny, title, pwdText, dialog);

        }
    }

    private void decryptPdfFile(View v, String filePath, String destiny, String title, String pwdText, Dialog dialog) {
        new CountDownTimer(2500, 1500){
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                try {
                    EncryptDecrypt.decrypt(filePath, destiny+".pdf", pwdText);
                    Toast.makeText(context, "Password Removed successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    popupSuccessDialog(v, filePath, destiny, title);
                }
                catch (BadPasswordException e){
                    e.printStackTrace();
                    Toast.makeText(context, "Incorrect Password", Toast.LENGTH_SHORT).show();
                }
                catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(context, "Wrong Password, Please try again", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        }.start();
    }

    private void popupSuccessDialog(View view, String filPath, String dest, String title){
        Dialog successDialog = new Dialog(view.getRootView().getContext());
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

        textView1.setText(R.string.unlocked_success);
        textView2.setText(dest+title);
        word_icon.setImageDrawable(context.getDrawable(R.drawable.unlock_icon2));
        textView3.setText(R.string.unlock_question);

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

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView pdfTitle, timeStamp, pdfSize, pdfPath;
        private ImageView option_btn, imageView, checkboxImage;
        private CheckBox checkBox;
        private LinearLayout pdfFile;
        private PDFView pdfView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfTitle = itemView.findViewById(R.id.pdfName);
            pdfPath = itemView.findViewById(R.id.pdfPath);
            pdfSize = itemView.findViewById(R.id.pdfSize);
            option_btn = itemView.findViewById(R.id.option_icon);
            checkBox = itemView.findViewById(R.id.recent_checkbox);
            imageView = itemView.findViewById(R.id.imageView);
            checkboxImage = itemView.findViewById(R.id.checking);
            pdfFile = itemView.findViewById(R.id.pdf_layout_id);
            pdfView = itemView.findViewById(R.id.pdfView);
        }
    }
}

package com.sasha.pdfviewer.tools;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.ImageAdapter;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.utils.ConvertImageToPdf;
import com.sasha.pdfviewer.utils.ImageUtils;
import com.sasha.pdfviewer.view.AllPdfFileViewActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CameraFolderActivity extends AppCompatActivity
        implements ImageAdapter.OnImageClickListener {

    private RecyclerView recyclerView;
    private ArrayList<ImageModel> imageList = new ArrayList<>();
    private ArrayList<ImageModel> selectList = new ArrayList<>();
    private int count = 0;
    private LinearLayout choose_layout;
    private TextView selectedText;
    private ImageView selectButton, shareButton, deleteButton;
    boolean isSelectAll = false;
    private boolean isSelectMode;
    private Button combineBtn;
    private ArrayList<ImageModel> selectedModels;
    private ProgressBar progressBar;
    private String folderName = "CameraPhotos";
    private ImageAdapter cameraAdapter;
    private Button convertBtn;
    private ArrayList<Uri> uris = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_photo);

        selectedText = findViewById(R.id.selectedText);

        recyclerView = findViewById(R.id.recyclerView);
        choose_layout = findViewById(R.id.chose_linear);
        combineBtn = findViewById(R.id.continueButton);
        progressBar = findViewById(R.id.progressbar);
        selectButton = findViewById(R.id.select_button);
        shareButton = findViewById(R.id.share_items);
        deleteButton = findViewById(R.id.delete_items);
        convertBtn = findViewById(R.id.convert_button);

        progressBar.setVisibility(View.VISIBLE);
        buttonListener();

        new CountDownTimer(1500, 1500){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                loadAllCameraPhoto();
                progressBar.setVisibility(View.GONE);
            }
        }.start();

        convertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupEnterFileName();
            }
        });

    }

    private void popupEnterFileName() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_popup_layout);

        EditText editText;
        Button cancelBtn, okBtn;
        TextView textTitle, textMessage;
        editText = dialog.findViewById(R.id.editText);
        cancelBtn = dialog.findViewById(R.id.buttonNo);
        okBtn = dialog.findViewById(R.id.buttonYes);
        textTitle = dialog.findViewById(R.id.textTitle);
        textTitle.setText("Pdf Merge");
        textMessage = dialog.findViewById(R.id.textMessage);
        textMessage.setText("Please enter your file name");
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = editText.getText().toString();
                convertImageToPdf(fileName);
                dialog.dismiss();

            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.END);
    }

    private void convertImageToPdf(String fileName) {
        String dir = Environment.getExternalStorageDirectory() + "/ConvertedPdf/"+fileName;
        File file = new File(dir);
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdir();
        }
        final Dialog convertDialog = new Dialog(this);
        convertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        convertDialog.setContentView(R.layout.progress_dialog);
        TextView titleText = convertDialog.findViewById(R.id.loading_text);
        titleText.setText(R.string.images_progress);
        convertDialog.setCancelable(false);
        convertDialog.getWindow ().setBackgroundDrawableResource (android.R.color.transparent);
        convertDialog.show();
        new CountDownTimer(1500, 1500){

            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                ArrayList<ImageModel> selectedImages = cameraAdapter.getSelectImages();
                try {
                    startConvertFile(selectedImages, fileName, dir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                convertDialog.dismiss();
            }
        }.start();
    }

    private void startConvertFile(ArrayList<ImageModel> selectedImages,
                                  String fileName, String directory) throws IOException {
        ConvertImageToPdf.convertImageToPdf( selectedImages, fileName, CameraFolderActivity.this);
        popupSuccessDialog(fileName, directory);

    }

    private void popupSuccessDialog(String fileName, String directory) {
        Context context = CameraFolderActivity.this;
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.upload_done_layout);
        TextView title, messageText, questionText;
        ImageView titleIcon;
        Button positiveBtn, negativeBtn;

        title = dialog.findViewById(R.id.successText);
        messageText = dialog.findViewById(R.id.pathText);
        questionText = dialog.findViewById(R.id.question_text);
        titleIcon = dialog.findViewById(R.id.done_icon);
        positiveBtn = dialog.findViewById(R.id.yes_btn);
        negativeBtn = dialog.findViewById(R.id.no_btn);

        title.setText(R.string.images_success);
        messageText.setText(directory + fileName);
        questionText.setText(R.string.images_question);
        titleIcon.setImageDrawable(context.getDrawable(R.drawable.m_p));

        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(new Intent(CameraFolderActivity.this, ToolsActivity.class));
            }
        });
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(getIntent());
                dialog.dismiss();
                deselectAll();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.TOP);
    }

    private void loadAllCameraPhoto(){
        recyclerView.setHasFixedSize(true);
        imageList = ImageUtils.allImageFile(this, folderName);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        cameraAdapter = new ImageAdapter(this, imageList, this);
        recyclerView.setAdapter(cameraAdapter);

    }
    private void buttonListener() {
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSelectAll) {
                    //ArrayList<PdfModel> selectedFile = multiSelectAdapter.getSelectedItems();
                    selectAll();
                    //After checking all items change button text
                    selectButton.setImageDrawable(getDrawable(R.drawable.deselect_all_icon));
                    isSelectAll = true;
                    cameraAdapter.notifyDataSetChanged();

                } else {
                    //If button text is Deselect All remove check from all items
                    //multiSelectAdapter.removeSelection();
                    deselectAll();
                    isSelectAll = false;
                    //After checking all items change button text
                    selectButton.setImageDrawable(getDrawable(R.drawable.select_all_icon));
                    cameraAdapter.notifyDataSetChanged();
                }
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<ImageModel> selectedFilePaths = cameraAdapter.getSelectImages();
                for (ImageModel imageModel : selectedFilePaths) {
                    String filePath = imageModel.getImagePath();
                    File file = new File(filePath);
                    uris.add(Uri.parse(filePath));
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, "Share"));
                }

            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (count > 0) {
                    Dialog alertDialog = new Dialog(view.getRootView().getContext());
                    alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    alertDialog.setContentView(R.layout.delete_layout);
                    final TextView textTile, textMessage;
                    final Button yesButton, noButton;
                    final ImageView close_button;

                    textTile = alertDialog.findViewById(R.id.textTitle);
                    textMessage = alertDialog.findViewById(R.id.textMessage);
                    yesButton = alertDialog.findViewById(R.id.buttonYes);
                    noButton = alertDialog.findViewById(R.id.buttonNo);
                    close_button = alertDialog.findViewById(R.id.close_btn);

                    textTile.setText(R.string.delete_file_title);
                    textMessage.setText(R.string.delete_question);
                    yesButton.setText(R.string.yes);
                    noButton.setText(R.string.no);
                    yesButton.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onClick(View v) {
                            int position = cameraAdapter.getSelectedPosition();
                            ArrayList<ImageModel> selectedFilePaths = cameraAdapter.getSelectImages();
                            if (selectedFilePaths != null) {
                                for (ImageModel pdfModel : selectedFilePaths) {
                                    String filePath = pdfModel.getImagePath();
                                    //String id = pdfModel.getId();
                                    File file = new File(filePath);
                                    if (file.exists()) {
                                        file.delete();
                                        selectedFilePaths.remove(file);
                                      /*  Uri contentUris = ContentUris.withAppendedId(
                                                MediaStore.Files.getContentUri("external"),
                                                Long.parseLong(id));
                                        getApplicationContext().getContentResolver().delete(
                                                contentUris, null, null
                                        );*/
                                        //startActivity(getIntent());
                                        imageList.remove(position);
                                        recyclerView.getAdapter().notifyItemRemoved(position);
                                        count = 0;
                                        updateCount(count);

                                    }

                                }
                            } else {
                                Toast.makeText(CameraFolderActivity.this, "Please select atleast one", Toast.LENGTH_SHORT).show();
                            }
                            alertDialog.dismiss();
                        }
                    });
                    noButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    close_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
                    alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    alertDialog.getWindow().getAttributes().windowAnimations
                            = R.style.SideMenuAnimation;
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.getWindow().setGravity(Gravity.END);


                }
                else{
                    Toast.makeText(CameraFolderActivity.this, "please select atleast one", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void selectAll() {
        for (ImageModel item : imageList) {
            item.setSelected(true);
            if (item.isSelected()){
                count = count + 1;
                updateCount(count);
            }
        }
        cameraAdapter.notifyDataSetChanged();
    }
    private void deselectAll() {
        for (ImageModel item : imageList) {
            item.setSelected(false);
            if (!item.isSelected()){
                count = 0;
                updateCount(count);
            }
        }
        cameraAdapter.notifyDataSetChanged();
    }
    private void updateCount(int counts) {
        if (counts == 0){
            selectedText.setText("Select Files");
        }
        else{
            selectedText.setText(counts + " Selected");
        }

    }

    @Override
    public void onImageClick(ImageModel imageModel, int position) {
        if (imageModel.isSelected()){
            convertBtn.setVisibility(View.VISIBLE);
            count = count +1;
            updateCount(count);
        }
        else if (!imageModel.isSelected()){
            count = count - 1;
            updateCount(count);
        }
        else {
            count = 0;
            updateCount(count);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
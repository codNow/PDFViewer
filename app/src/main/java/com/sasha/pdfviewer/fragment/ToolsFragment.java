package com.sasha.pdfviewer.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.ConvertPdfAdapter;
import com.sasha.pdfviewer.folderList.DecryptedFolderActivity;
import com.sasha.pdfviewer.folderList.EncryptedFolderActivity;
import com.sasha.pdfviewer.folderList.ExtractedImagesFolderActivity;
import com.sasha.pdfviewer.folderList.ImageToPdfFolderActivity;
import com.sasha.pdfviewer.folderList.MergeFolderActivity;
import com.sasha.pdfviewer.folderList.SplitFolder;
import com.sasha.pdfviewer.folderList.WatermarkFolderActivity;
import com.sasha.pdfviewer.folderList.WordFolderActivity;
import com.sasha.pdfviewer.tools.AllToolsViewActivity;
import com.sasha.pdfviewer.tools.MergedPdfActivity;
import com.sasha.pdfviewer.tools.NewPdfFileActivity;
import com.sasha.pdfviewer.utils.PdfUtils;
import com.sasha.pdfviewer.view.AllImageActivity;

import java.util.ArrayList;

public class ToolsFragment extends Fragment {


    private LinearLayout profile_card;
    private LinearLayout pdfToWord, newPdf, imageToPdf,
            pdfToImage, unlockPdf, lockPdf,
            waterMarkPdf, compressPdf, splitPdf;
    private BottomNavigationView bottomNavigationView;
    private ActivityResultLauncher<Intent> resultLauncher, newResultLauncher;
    private ArrayList<String> inputPdfs;
    private String paths;
    private Dialog dialog;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private Toolbar toolbar;
    private CardView wordCard, convertCard,lockCard, combineCard,
            unlockCard, splitCard, extractCard, waterMarkCard;
    String folder = Environment.getExternalStorageDirectory()+
            "/CustomFolder/";
    private Uri imageUri, singleUri;
    private String addButton;
    private TextView noOfText, noOfConvert, noOfLock, noOfUnlock,
            noOfSplit, noOfMark, noOfCombine, noOfImage;

    private ConvertPdfAdapter convertPdfAdapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tools, container, false);


        pdfToWord = view.findViewById(R.id.pdfToWord);
        newPdf = view.findViewById(R.id.newPdf);
        imageToPdf = view.findViewById(R.id.imagetoPdf);
        pdfToImage = view.findViewById(R.id.pdftoImage);
        unlockPdf = view.findViewById(R.id.unlockPdf);
        lockPdf = view.findViewById(R.id.encryptPdf);
        compressPdf = view.findViewById(R.id.compressPdf);
        splitPdf = view.findViewById(R.id.splitPdf);
        waterMarkPdf = view.findViewById(R.id.waterMark);
        lockCard = view.findViewById(R.id.lock_cardView);
        unlockCard = view.findViewById(R.id.unlock_cardView);
        combineCard = view.findViewById(R.id.combine_cardView);
        splitCard = view.findViewById(R.id.split_cardView);
        wordCard = view.findViewById(R.id.word_cardView);
        convertCard = view.findViewById(R.id.convert_cardView);
        waterMarkCard = view.findViewById(R.id.waterMark_cardView);
        progressBar = view.findViewById(R.id.progressbar);
        scrollView = view.findViewById(R.id.scrollView);
        extractCard = view.findViewById(R.id.extracted_cardView);
        noOfSplit = view.findViewById(R.id.noOfSplit);
        noOfText = view.findViewById(R.id.noOfText);
        noOfCombine = view.findViewById(R.id.noOfCombine);
        noOfConvert = view.findViewById(R.id.noOfConvert);
        noOfImage = view.findViewById(R.id.noOfImage);
        noOfLock = view.findViewById(R.id.noOfLock);
        noOfUnlock = view.findViewById(R.id.noOfUnlock);
        noOfMark = view.findViewById(R.id.noOfMark);
        profile_card = view.findViewById(R.id.profile_card);


        Context context = getContext();
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_popup_layout);

        listener();
        getAllTheNumbers();



        return view;
    }


    private void getAllTheNumbers() {
        String splitFolder = Environment.getExternalStorageDirectory()+
                "/Split PDF/";
        String imageFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"ExtractedImages";
        String wordFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"Text Doc";
        String markFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"Marked PDF";
        String unlock = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"Unlocked PDF";
        String lockFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"Locked PDF";
        String combineFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"Combined PDF";
        String convertFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/" +"Converted PDF";

        noOfSplit.setText(String.valueOf(PdfUtils.getFileNumber(splitFolder)));
        noOfImage.setText(String.valueOf(PdfUtils.getImageNumber(imageFolder)));
        noOfText.setText(String.valueOf(PdfUtils.getWordNumber(wordFolder)));
        noOfMark.setText(String.valueOf(PdfUtils.getFileNumber(markFolder)));
        noOfUnlock.setText(String.valueOf(PdfUtils.getFileNumber(unlock)));
        noOfLock.setText(String.valueOf(PdfUtils.getFileNumber(lockFolder)));
        noOfCombine.setText(String.valueOf(PdfUtils.getFileNumber(combineFolder)));
        noOfConvert.setText(String.valueOf(PdfUtils.getFileNumber(convertFolder)));

    }

    private void listener() {
        unlockPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AllToolsViewActivity.class);
                intent.putExtra("unlockBtn", "unlockBtn");
                startActivity(intent);
            }
        });
        newPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), NewPdfFileActivity.class));
            }
        });
        extractCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ExtractedImagesFolderActivity.class));
                //getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ExtractedFragment()).addToBackStack(null).commit();
            }
        });
        splitPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AllToolsViewActivity.class);
                intent.putExtra("splitBtn", "splitBtn");
                startActivity(intent);
            }
        });
        waterMarkPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AllToolsViewActivity.class);
                intent.putExtra("waterBtn", "waterBtn");
                startActivity(intent);
            }
        });

        compressPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MergedPdfActivity.class);
                intent.putExtra("mergeBtn", "mergeBtn");
                startActivity(intent);
            }
        });

        lockPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AllToolsViewActivity.class);
                intent.putExtra("lockBtn", "lockBtn");
                startActivity(intent);
            }
        });

        pdfToWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AllToolsViewActivity.class);
                intent.putExtra("wordBtn", "wordBtn");
                startActivity(intent);
            }
        });
        imageToPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AllImageActivity.class));
                //getImagesFromDevice();
            }
        });
        pdfToImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AllToolsViewActivity.class);
                intent.putExtra("toImageBtn", "toImageBtn");
                startActivity(intent);
            }
        });

        /*Folder Intent here..*/
        lockCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), EncryptedFolderActivity.class));
            }
        });
        unlockCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), DecryptedFolderActivity.class));
            }
        });
        combineCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), MergeFolderActivity.class));
            }
        });
        splitCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SplitFolder.class));
            }
        });
        convertCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ImageToPdfFolderActivity.class));
            }
        });
        wordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), WordFolderActivity.class));
            }
        });
        waterMarkCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), WatermarkFolderActivity.class));
            }
        });
    }
}
package com.sasha.pdfviewer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;

import java.io.File;

public class RecentUtil {
    static Activity activity;

    public static void recentFiles(String filePath){
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        String recentFile = preferences.getString("RECENT_FILE", filePath);

        if (!recentFile.equals("")){
            File recentObjectFile = new File(recentFile);
        }
    }
}

package com.halcyonwaves.apps.backupmyapps;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class PackageInformation {
    private String appname = "";
    private String pname = "";
    private String versionName = "";
    private int versionCode = 0;
    private Drawable icon;
    
    private void prettyPrint() {
        Log.v(appname + "\t" + pname + "\t" + versionName + "\t" + versionCode);
    }
}

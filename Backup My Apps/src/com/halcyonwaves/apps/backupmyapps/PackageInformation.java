package com.halcyonwaves.apps.backupmyapps;

import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * This class handles the information for one single application package installed
 * on the Android device.
 * 
 * @author Tim Huetz
 * @since 1.0
 */
public class PackageInformation {
    private String appname = "";
    private String pname = "";
    private String versionName = "";
    private int versionCode = 0;
    private Drawable icon;
    
    /**
     * Constructor of this class.
     * 
     * @param applicationName The human readable name of the application.
     * @param packageName The full qualified name of the package.
     * @param versionName The official version name of the application.
     * @param versionCode The internal version code of the application.
     * @param appIcon The icon of the application.
     * @author Tim Huetz
     * @since 1.0
     */
    public PackageInformation(String applicationName, String packageName, String versionName, int versionCode, Drawable appIcon) {
    	this.appname = applicationName;
    	this.pname = packageName;
    	this.versionName = versionName;
    	this.versionCode = versionCode;
    	this.icon = appIcon;
    }
    
    /**
     * Get the icon of the managed application.
     * 
     * @author Tim Huetz
     * @since 1.0
     * @return A handle to the applications icon.
     */
    public Drawable getIcon() {
    	return this.icon;
    }
    
    /**
     * Log the information of the managed package to the
     * logging manager.
     * 
     * @author Tim Huetz
     * @since 1.0
     */
    public void logPackage() {
    	Log.v(PackageInformation.class.getSimpleName(), "Application(" + this.appname + "), Package(" + this.pname + "), VersionOfficial(" + this.versionName + "), VersionInternal(" + this.versionCode + ")" );
    }
}

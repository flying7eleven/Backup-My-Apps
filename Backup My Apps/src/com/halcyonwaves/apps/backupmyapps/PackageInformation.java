package com.halcyonwaves.apps.backupmyapps;

import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * This class handles the information for one single application package installed on the Android
 * device.
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
	public PackageInformation( String applicationName, String packageName, String versionName, int versionCode, Drawable appIcon ) {
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
	 * Checks if the component belongs to Android or not.
	 * 
	 * @return True if the component belongs to Android, false if not.
	 */
	public boolean isSystemComponent() {
		return this.pname.toLowerCase().startsWith( "com.android." ) || this.pname.toLowerCase().startsWith( "com.example" ) || this.pname.equalsIgnoreCase( "android" ) || this.pname.equalsIgnoreCase( "android.tts" ) || this.pname.toLowerCase().startsWith( "com.lge." );
	}

	/**
	 * Log the information of the managed package to the logging manager.
	 * 
	 * @author Tim Huetz
	 * @since 1.0
	 */
	public void logPackage() {
		Log.v( PackageInformation.class.getSimpleName(), "Application(" + this.appname + "), Package(" + this.pname + "), VersionOfficial(" + this.versionName + "), VersionInternal(" + this.versionCode + ")" );
	}

	/**
	 * Get the name of the package of the managed application.
	 * 
	 * @author Tim Huetz
	 * @since 1.0
	 * @return The package name of the application.
	 */
	public String getPackageName() {
		return this.pname;
	}

	/**
	 * Get the name of the managed application.
	 * 
	 * @author Tim Huetz
	 * @since 1.0
	 * @return The name of the application.
	 */
	public String getApplicationName() {
		return this.appname;
	}

	/**
	 * Get the internally used version code number.
	 * 
	 * @author Tim Huetz
	 * @since 1.0
	 * @return The version code which is used internally.
	 */
	public int getVersionCode() {
		return this.versionCode;
	}

	/**
	 * Get the human readable version name.
	 * 
	 * @author Tim Huetz
	 * @since 1.0
	 * @return The human readable name of the version.
	 */
	public String getVersionName() {
		return this.versionName;
	}
}

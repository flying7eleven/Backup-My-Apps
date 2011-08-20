package com.halcyonwaves.apps.backupmyapps;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * Class for fetching information about the installed applications on an Android device.
 * 
 * @author Tim Huetz
 * @since 1.0
 */
public class PackageInformationManager {
	private PackageManager internalPackageManager = null;

	/**
	 * Constructor of this class.
	 * 
	 * @author Tim Huetz
	 * @since 1.0
	 * @param applicationContext The application context to fetch some information.
	 */
	public PackageInformationManager( Context applicationContext ) {
		this.internalPackageManager = applicationContext.getPackageManager();
		if( null == this.internalPackageManager ) {
			// TODO: throw an exception
		}
	}

	/**
	 * Get a list of all installed applications.
	 * 
	 * @author Tim Huetz
	 * @since 1.0
	 * @return A list of all found packages.
	 */
	public ArrayList< PackageInformation > getInstalledApps() {
		// allocated some resources and get the package list
		ArrayList< PackageInformation > res = new ArrayList< PackageInformation >();
		List< PackageInfo > packs = this.internalPackageManager.getInstalledPackages( 0 );

		// loop through all packages
		for( int i = 0; i < packs.size(); i++ ) {
			// get the package information
			PackageInfo p = packs.get( i );

			// get some extended properties
			String appname = p.applicationInfo.loadLabel( this.internalPackageManager ).toString();
			Drawable icon = p.applicationInfo.loadIcon( this.internalPackageManager );

			// add the found package to the array we want to return
			res.add( new PackageInformation( appname, p.packageName, p.versionName, p.versionCode, icon ) );
		}

		// return the gathered information
		return res;
	}
}

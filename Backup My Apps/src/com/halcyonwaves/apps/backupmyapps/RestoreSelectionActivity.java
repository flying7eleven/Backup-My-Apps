package com.halcyonwaves.apps.backupmyapps;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

/**
 * 
 * @author Tim Huetz
 * @since 0.3
 */
public class RestoreSelectionActivity extends ListActivity {
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		//
		Bundle extras = getIntent().getExtras();
		if( null == extras ) {
			// TODO: ERROR; this should NEVER happen
		}

		//
		List< String > foundPackages = new ArrayList< String >();
		int numberOfPackages = extras.getInt( "packages" );
		for( int i = 0; i < numberOfPackages; i++ ) {
			String currentPackage = extras.getString( "package" + i );
			foundPackages.add( currentPackage );
			Log.v( RestoreSelectionActivity.class.getSimpleName(), "Found package: " + currentPackage );
		}

		ListAdapter adapter = new ArrayAdapter< String >( this, android.R.layout.simple_list_item_1, (String[])foundPackages.toArray(new String[0]) );
		this.setListAdapter( adapter );
	}
	
	private void installPackage(String pathToApk) { // see http://www.anddev.org/androidpermissioninstall_packages_not_granted-t5858.html for more
		// see http://stackoverflow.com/questions/5803999/install-apps-silently-with-granted-install-packages-permission
		//Uri packageUri = Uri.fromFile( new File( pathToApk ) );
		// set permissions
		//PackageManager pm = this.applicationContext.getPackageManager();
		// pm.addPermission(p);//error
		// pm.installPackage(packageUri, null, 1);
	}
	
	/**
	 * Try to open the market with the supplied package name. If this fails, open
	 * the browser URL of the market and search the package there.
	 * 
	 * @param packageName The name of the package to install.
	 */
	private void installPackageFromMarket( String packageName ) {
		try {
			Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=" + packageName ) );
			this.startActivity( browserIntent );
		} catch( Exception outerException ) {
			Log.e( RestoreBackupDataTask.class.getSimpleName(), "Failed to open the market directly. The exception was: " + outerException.getMessage() );
			try {
				Log.v( RestoreBackupDataTask.class.getSimpleName(), "Opening browser directly!!" );
				Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( "https://market.android.com/details?id=" + packageName ) );
				this.startActivity( browserIntent );
				Log.v( RestoreBackupDataTask.class.getSimpleName(), "Browser closed!" );
			} catch( Exception innerException ) {
				Log.e( RestoreBackupDataTask.class.getSimpleName(), "Failed to open the market in the browser. The exception was: " + innerException.getMessage() );
			}
		}
	}

}

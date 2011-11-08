package com.halcyonwaves.apps.backupmyapps;

import java.util.ArrayList;
import java.util.List;

import com.halcyonwaves.apps.backupmyapps.tasks.RestoreBackupDataTask;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * This class implements a dialog where the user can select the package which should actually be
 * restored on a new device.
 * 
 * @author Tim Huetz
 * @since 0.3
 */
public class RestoreSelectionActivity extends ListActivity {
	private String[] packageNames = null;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		// just track this event
		MainActivity.analyticsTracker.trackPageView( "/applicationRestoreAppsSelectApp" );
		
		// set the custom layout of this view
		this.setContentView( R.layout.restorelistactivity );
		
		// get the information supplied to this activity
		Bundle extras = getIntent().getExtras();
		if( null == extras ) {
			// TODO: ERROR; this should NEVER happen
		}

		// loop through the supplied data set and extract the required information
		List< String > foundPackageNames = new ArrayList< String >();
		List< String > foundApplicationNames = new ArrayList< String >();
		int numberOfPackages = extras.getInt( "packages" );
		for( int i = 0; i < numberOfPackages; i++ ) {
			String currentPackage = extras.getString( "packageName" + i );
			String currentApplication = extras.getString( "applicationName" + i );
			foundApplicationNames.add( currentApplication );
			foundPackageNames.add( currentPackage );
			Log.v( RestoreSelectionActivity.class.getSimpleName(), "Found package: " + currentPackage + " (" + currentApplication + ")" );
		}

		// store an array of the package names in the class members
		this.packageNames = (String[])foundPackageNames.toArray( new String[ 0 ] );

		// tell the view which data it should display
		ListAdapter adapter = new ArrayAdapter< String >( this, android.R.layout.simple_list_item_1, (String[])foundApplicationNames.toArray( new String[ 0 ] ) );
		this.setListAdapter( adapter );
	}

	@Override
	protected void onListItemClick( ListView l, View v, int position, long id ) {
		// try to install the package through the market
		this.installPackageFromMarket( this.packageNames[ position ] );

		// call the handler for this event of the super class
		super.onListItemClick( l, v, position, id );
	}

	/**
	 * Try to open the market with the supplied package name. If this fails, open the browser URL of
	 * the market and search the package there.
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
				Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( "http://market.android.com/details?id=" + packageName ) );
				this.startActivity( browserIntent );
				Log.v( RestoreBackupDataTask.class.getSimpleName(), "Browser closed!" );
			} catch( Exception innerException ) {
				Log.e( RestoreBackupDataTask.class.getSimpleName(), "Failed to open the market in the browser. The exception was: " + innerException.getMessage() );
			}
		}
	}

}

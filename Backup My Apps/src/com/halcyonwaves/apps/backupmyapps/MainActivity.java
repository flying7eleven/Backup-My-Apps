package com.halcyonwaves.apps.backupmyapps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	private Button buttonBackupInstalledApplications = null;
	private Button buttonRestoreInstalledApplications = null;
	private PackageInformationManager packageInformationManager = null;
	private static final String BACKUP_FILENAME = "installedApplications.backupmyapps";
	private final File storagePath = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS );

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		// create the layout of the main activity
		super.onCreate( savedInstanceState );
		setContentView( R.layout.main );

		// get some control handles
		this.buttonBackupInstalledApplications = (Button)this.findViewById( R.id.buttonBackupInstalledApplications );
		this.buttonRestoreInstalledApplications = (Button)this.findViewById( R.id.buttonRestoreInstalledApplications );

		// if there is no backup file, disable the restore button
		if( true ) { // TODO: this
			Log.v( MainActivity.class.getSimpleName(), "No backup file found, disabling the restore button." );
			this.buttonRestoreInstalledApplications.setEnabled( false );
		}

		// be sure that the storage path exists
		this.storagePath.mkdirs();

		// get the package manager for this activity
		this.packageInformationManager = new PackageInformationManager( this );

		// add a click handler for the button to backup the installed applications
		this.buttonBackupInstalledApplications.setOnClickListener( new OnClickListener() {
			public void onClick( View v ) {
				// just log some information
				Log.v( MainActivity.class.getSimpleName(), "Using following external storage directory: " + MainActivity.this.storagePath );

				// show a progress dialog
				ProgressDialog backupProgressDialog = ProgressDialog.show( MainActivity.this, "", MainActivity.this.getString( R.string.progressDialogBackupInProgress ), true );

				// try to open the output file
				File backupFile = new File( MainActivity.this.storagePath, MainActivity.BACKUP_FILENAME );
				try {
					backupFile.createNewFile();
					OutputStream backupFileStream = new FileOutputStream( backupFile );
					PrintStream backupFilePrintStream = new PrintStream( backupFileStream );

					// write the XML meta information
					backupFilePrintStream.print( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
					backupFilePrintStream.print( "<BackupMyApps version=\"1.0\">" );

					// loop through the found packages and write them into the file
					ArrayList< PackageInformation > foundPackages = MainActivity.this.packageInformationManager.getInstalledApps();
					for( PackageInformation currentPackage : foundPackages ) {
						if( !currentPackage.isSystemComponent() ) {
							backupFilePrintStream.print( "<InstalledApp packageName=\"" + currentPackage.getPackageName() + "\" " );
							backupFilePrintStream.print( "humanReadableName=\"" + currentPackage.getApplicationName() + "\" " );
							backupFilePrintStream.print( "versionCode=\"" + currentPackage.getVersionCode() + "\" " );
							backupFilePrintStream.print( "versionName=\"" + currentPackage.getVersionName() + "\"" );
							backupFilePrintStream.print( "/>" );
						}
					}

					// write the closing tags and close the stream
					backupFilePrintStream.print( "</BackupMyApps>" );
					backupFileStream.close();

					// as we succeeded in writing the file, we can enable the restore button now and disable the
					// progress dialog
					MainActivity.this.buttonRestoreInstalledApplications.setEnabled( true );
					backupProgressDialog.dismiss();
				} catch( IOException e ) {
					Log.e( MainActivity.class.getSimpleName(), "Failed to create the backup file. The message was: " + e.getMessage() );
				}
			}
		} );

		// add a click handler for the button to restore the applications
		this.buttonRestoreInstalledApplications.setOnClickListener( new OnClickListener() {
			public void onClick( View v ) {
				// TODO this
			}
		} );

		// if no external storage is mounted, show an error and close
		if( !Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED ) ) {
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( this );
			dialogBuilder.setMessage( R.string.dialogMessageNoExternalStorageMounted );
			dialogBuilder.setCancelable( false );
			dialogBuilder.setPositiveButton( R.string.buttonOk, new DialogInterface.OnClickListener() {
				public void onClick( DialogInterface dialog, int id ) {
					MainActivity.this.finish();
				}
			} );
			AlertDialog infoDialog = dialogBuilder.create();
			infoDialog.show();
		}
	}
}
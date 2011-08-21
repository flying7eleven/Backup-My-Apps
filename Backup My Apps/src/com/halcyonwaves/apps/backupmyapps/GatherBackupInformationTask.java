package com.halcyonwaves.apps.backupmyapps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Class for gathering the backup information in an asynchronous way.
 * 
 * @author Tim Huetz
 * @since 0.2
 */
public class GatherBackupInformationTask extends AsyncTask< Void, Void, Boolean > {
	private File storagePath = null;
	private Context applicationContext = null;
	private String backupFilename = "";
	private PackageInformationManager packageInformationManager = null;
	private IAsyncTaskFeedback feedbackClass = null;

	/**
	 * Constructor for this class.
	 * 
	 * @param applicationContext The context of the application.
	 * @param storagePath The path to the backup file.
	 * @param backupFilename The name of the backup file.
	 * @param feedbackClass The class which should handle the feedback of this task.
	 * @author Tim Huetz
	 * @since 0.2
	 */
	public GatherBackupInformationTask( Context applicationContext, File storagePath, String backupFilename, IAsyncTaskFeedback feedbackClass ) {
		this.storagePath = storagePath;
		this.applicationContext = applicationContext;
		this.backupFilename = backupFilename;
		this.feedbackClass = feedbackClass;
		this.packageInformationManager = new PackageInformationManager( this.applicationContext );
	}

	@Override
	protected Boolean doInBackground( Void... arg0 ) {
		// just log some information
		Log.v( GatherBackupInformationTask.class.getSimpleName(), "Using following external storage directory: " + this.storagePath );

		// try to open the output file
		File backupFile = new File( this.storagePath, this.backupFilename );
		try {
			backupFile.createNewFile();
			OutputStream backupFileStream = new FileOutputStream( backupFile );
			PrintStream backupFilePrintStream = new PrintStream( backupFileStream );

			// write the XML meta information
			backupFilePrintStream.print( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
			backupFilePrintStream.print( "<BackupMyApps version=\"1.0\">" );

			// loop through the found packages and write them into the file
			ArrayList< PackageInformation > foundPackages = this.packageInformationManager.getInstalledApps();
			for( PackageInformation currentPackage : foundPackages ) {
				if( !currentPackage.isSystemComponent() ) {
					backupFilePrintStream.print( "<InstalledApp packageName=\"" + currentPackage.getPackageName() + "\" " );
					backupFilePrintStream.print( "humanReadableName=\"" + currentPackage.getApplicationName().replace( "&", "&amp;" ).replace( "'", "&apos;" ).replace( "\"", "&quot;" ).replace( "<", "&lt;" ).replace( ">", "&gt;" ) + "\" " );
					backupFilePrintStream.print( "versionCode=\"" + currentPackage.getVersionCode() + "\" " );
					backupFilePrintStream.print( "versionName=\"" + currentPackage.getVersionName() + "\"" );
					backupFilePrintStream.print( "/>" );
				}
			}

			// write the closing tags and close the stream
			backupFilePrintStream.print( "</BackupMyApps>" );
			backupFileStream.close();

		} catch( IOException e ) {
			Log.e( GatherBackupInformationTask.class.getSimpleName(), "Failed to create the backup file. The message was: " + e.getMessage() );
			this.feedbackClass.taskFailed();
			return false;
		}

		// it seems that we succeeded
		this.feedbackClass.taskSuccessfull();
		return true;
	}

}

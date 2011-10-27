package com.halcyonwaves.apps.backupmyapps.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import com.halcyonwaves.apps.backupmyapps.IAsyncTaskFeedback;
import com.halcyonwaves.apps.backupmyapps.packages.PackageInformation;
import com.halcyonwaves.apps.backupmyapps.packages.PackageInformationManager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

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

			// create and initialize the XML serializer
			StringWriter backupXmlStringWriter = new StringWriter();
			XmlSerializer backupSerializer = Xml.newSerializer();
			backupSerializer.setOutput( backupXmlStringWriter );
			backupSerializer.startDocument( "UTF-8", true );
			backupSerializer.startTag( "", "InstalledApplications" );
			backupSerializer.attribute( "", "version", "0.5" );
			backupSerializer.attribute( "", "androidModel", android.os.Build.MODEL );
			backupSerializer.attribute( "", "androidDevice", android.os.Build.DEVICE );
			backupSerializer.attribute( "", "androidProduct", android.os.Build.PRODUCT );
			backupSerializer.attribute( "", "androidManufacturer", android.os.Build.MANUFACTURER );

			// loop through the found packages and write them into the file
			ArrayList< PackageInformation > foundPackages = this.packageInformationManager.getInstalledApps();
			// backupSerializer.attribute( "", "numberOfApplications", String.valueOf(
			// foundPackages.size() ) ); // TODO: all application, includes system applications
			// which are ignored
			for( PackageInformation currentPackage : foundPackages ) {
				if( !currentPackage.isSystemComponent() ) {
					if( currentPackage == null || currentPackage.getApplicationName() == null || currentPackage.getPackageName() == null || currentPackage.getVersionName() == null ) {
						Log.e( GatherBackupInformationTask.class.getSimpleName(), "It seems that one of the fields of one of the packages had a 'null' entry in a required field. Skipping package." ); // TODO: find a way to inform the user
						continue;
					}
					backupSerializer.startTag( "", "InstalledApp" );
					backupSerializer.attribute( "", "applicationName", currentPackage.getApplicationName() );
					backupSerializer.attribute( "", "packageName", currentPackage.getPackageName() );
					backupSerializer.attribute( "", "versionCode", String.valueOf( currentPackage.getVersionCode() ) );
					backupSerializer.attribute( "", "versionName", currentPackage.getVersionName() );
					backupSerializer.endTag( "", "InstalledApp" );
				}
			}

			// close the surrounding tag and finish the document creation
			backupSerializer.endTag( "", "InstalledApplications" );
			backupSerializer.endDocument();

			// write the XML code into the file and close it
			backupFilePrintStream.print( backupXmlStringWriter.toString() );
			backupFileStream.close();

		} catch( IOException e ) {
			Log.e( GatherBackupInformationTask.class.getSimpleName(), "Failed to create the backup file. The message was: " + e.getMessage() );
			return false;
		}

		// it seems that we succeeded
		return true;
	}

	@Override
	protected void onPostExecute( Boolean result ) {
		if( result ) {
			this.feedbackClass.taskSuccessfull( this, null );
		} else {
			this.feedbackClass.taskFailed( this, null );
		}
	}
}

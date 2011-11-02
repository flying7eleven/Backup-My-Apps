package com.halcyonwaves.apps.backupmyapps.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.halcyonwaves.apps.backupmyapps.IAsyncTaskFeedback;
import com.halcyonwaves.apps.backupmyapps.MainActivity;
import com.halcyonwaves.apps.backupmyapps.SettingsActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Class for uploading the backup file to the Dropbox account.
 * 
 * @author Tim Huetz
 * @since 0.5.1
 */
public class UploadToDropboxTask  extends AsyncTask< Void, Void, Boolean > {
	private SharedPreferences applicationPreferences = null;
	private DropboxAPI< AndroidAuthSession > dropboxDatabaseApi = null;
	private IAsyncTaskFeedback usedFeedbackClass = null;
	private String fileToUpload = "";
	
	/**
	 * Constructor of this class.
	 * 
	 * @param uploadFile The file which should be uploaded.
	 * @param applicationContext The application context used to get the preferences.
	 * @param feedbackClass The class to which the feedback should be send.
	 */
	public UploadToDropboxTask( String uploadFile, Context applicationContext, IAsyncTaskFeedback feedbackClass ) {
		// set some class attributes
		this.fileToUpload = uploadFile;
		this.usedFeedbackClass = feedbackClass;
		
		// get the preference object for this application
		this.applicationPreferences = PreferenceManager.getDefaultSharedPreferences( applicationContext );
		
		// setup the Dropbox API client
		AppKeyPair appKeys = new AppKeyPair( MainActivity.DROPBOX_API_APP_KEY, MainActivity.DROPBOX_API_APP_SECRET );
		AndroidAuthSession session = new AndroidAuthSession( appKeys, MainActivity.DROPBOX_API_APP_ACCESS_TYPE );
		this.dropboxDatabaseApi = new DropboxAPI< AndroidAuthSession >( session );
		
		// get the key and the secret from the settings
		String key = this.applicationPreferences.getString( SettingsActivity.PREFERENCE_SYNCHRONIZATION_DROPBOX_ACCESS_KEY, "" );
		String secret = this.applicationPreferences.getString( SettingsActivity.PREFERENCE_SYNCHRONIZATION_DROPBOX_ACCESS_SECRET, "" );

		// create the required Dropbox access token object
		AccessTokenPair tokens = new AccessTokenPair( key, secret );

		// set the loaded access token pair
		this.dropboxDatabaseApi.getSession().setAccessTokenPair( tokens );
	}
	
	@Override
	protected Boolean doInBackground( Void... arg0 ) {
		// define the backup filename
		String backupFilename = android.os.Build.DEVICE + "-" + android.os.Build.MODEL + ".backupmyapps";
		backupFilename = backupFilename.replace( ' ', '-' );

		// get the correct filename
		File backupFile = new File( this.fileToUpload );
		
		// try to upload the backup file
		try {
			FileInputStream inputStream = new FileInputStream( backupFile );
			Entry newEntry = this.dropboxDatabaseApi.putFileOverwrite( "/" + backupFilename, inputStream, backupFile.length(), null );
			
			// it seems that we succeeded
			Log.i( "BackupMyAppsDropbox", "The uploaded file's rev is: " + newEntry.rev );
			return true;
			
		} catch( DropboxUnlinkedException e ) {
			Log.e( "BackupMyAppsDropbox", "The Dropbox account is not linked to the application anymore. Cannot upload the backup file.", e ); // TODO: handle this by telling it to the user
		} catch( DropboxException e ) {
			Log.e( "BackupMyAppsDropbox", "Something went wrong while uploading the backup file to the Dropbox account.", e ); // TODO: handle this by telling it to the user
		} catch( FileNotFoundException e ) {
			Log.e( "BackupMyAppsDropbox", "The backup file was not found.", e );
		}
		
		// it seems taht we failed
		return false;
	}
	
	@Override
	protected void onPostExecute( Boolean result ) {
		super.onPostExecute( result );

		// give the application feedback
		if( result ) {
			this.usedFeedbackClass.taskSuccessfull( this, null );
		} else {
			this.usedFeedbackClass.taskFailed( this, null );
		}
	}
}

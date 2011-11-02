package com.halcyonwaves.apps.backupmyapps.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
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
 * Class for downloading the backup file from the Dropbox account.
 * 
 * @author Tim Huetz
 * @since 0.5.1
 */
public class DownloadFromDropboxTask extends AsyncTask< Void, Void, Boolean > {
	private SharedPreferences applicationPreferences = null;
	private DropboxAPI< AndroidAuthSession > dropboxDatabaseApi = null;
	private IAsyncTaskFeedback usedFeedbackClass = null;
	private String fileToRestore = "";
	
	/**
	 * Constructor of this class.
	 * 
	 * @param fileToRestoreFromDb The file which should be downloaded from the Dropbox account.
	 * @param applicationContext The application context used to get the preferences.
	 * @param feedbackClass The class to which the feedback should be send.
	 */
	public DownloadFromDropboxTask( String fileToRestoreFromDb, Context applicationContext, IAsyncTaskFeedback feedbackClass ) {
		// set some class attributes
		this.fileToRestore = fileToRestoreFromDb;
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
		// get a temporary file
		File restoreFile;
		try {
			// get a temporary file
			restoreFile = File.createTempFile( "backupfile", "backupmyapps" );
			
			// try to download the file
			FileOutputStream outputFile = new FileOutputStream( restoreFile );
			this.dropboxDatabaseApi.getFile( this.fileToRestore, null, outputFile, null );

			// task succeeeded
			this.fileToRestore = restoreFile.toString();
			return true;
			
		} catch( IOException e ) {
			Log.e( "RestoreSelectFileActivity", "Failed to download the backup file from the Dropbox account.", e ); // TODO: show an error message
		} catch(DropboxException e) {
			Log.e( "RestoreSelectFileActivity", "Failed to download the backup file from the Dropbox account.", e ); // TODO: show an error message
		}
		
		// failed to do this task
		return false;
	}
	
	@Override
	protected void onPostExecute( Boolean result ) {
		super.onPostExecute( result );

		// give the application feedback
		if( result ) {
			this.usedFeedbackClass.taskSuccessfull( this, (Object)this.fileToRestore );
		} else {
			this.usedFeedbackClass.taskFailed( this, null );
		}
	}

}

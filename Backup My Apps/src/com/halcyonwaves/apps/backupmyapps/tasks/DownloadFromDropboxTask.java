package com.halcyonwaves.apps.backupmyapps.tasks;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.halcyonwaves.apps.backupmyapps.IAsyncTaskFeedback;
import com.halcyonwaves.apps.backupmyapps.MainActivity;
import com.halcyonwaves.apps.backupmyapps.SettingsActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

/**
 * Class for downloading the backup file from the Dropbox account.
 * 
 * @author Tim Huetz
 * @since 0.5
 */
public class DownloadFromDropboxTask extends AsyncTask< Void, Void, Boolean > {
	private SharedPreferences applicationPreferences = null;
	private DropboxAPI< AndroidAuthSession > dropboxDatabaseApi = null;
	
	/**
	 * Constructor of this class.
	 * 
	 * @param applicationContext The application context used to get the preferences.
	 * @param feedbackClass The class to which the feedback should be send.
	 */
	public DownloadFromDropboxTask( Context applicationContext, IAsyncTaskFeedback feedbackClass ) {
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
		// TODO Auto-generated method stub
		return null;
	}

}

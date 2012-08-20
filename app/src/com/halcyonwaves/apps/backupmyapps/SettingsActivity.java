package com.halcyonwaves.apps.backupmyapps;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.TextView;

public class SettingsActivity extends PreferenceActivity {
	private Preference buildVersionPreference = null;
	private CheckBoxPreference loginIntoDropbox = null;
	private SharedPreferences applicationPreferences = null;
	private DropboxAPI< AndroidAuthSession > dropboxDatabaseApi = null;
	
	public final static String PREFERENCE_SYNCHRONIZATION_DROPBOX_ACCESS_KEY = "synchronization.dropboxAccessKey";
	public final static String PREFERENCE_SYNCHRONIZATION_DROPBOX_ACCESS_SECRET = "synchronization.dropboxAccessSecret";

	/**
	 * Get the version name of the application itself.
	 * 
	 * @author Tim Huetz
	 * @since 0.4
	 * @return The version name of the application itself.
	 */
	private String getApplicationVersion() {
		try {
			return this.getPackageManager().getPackageInfo( this.getPackageName(), 0 ).versionName;
		} catch( NameNotFoundException e ) {
			return "<unknown>";
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		//
		if( this.dropboxDatabaseApi.getSession().authenticationSuccessful() ) {
			try {
				// MANDATORY call to complete authentication
				this.dropboxDatabaseApi.getSession().finishAuthentication();

				// get the access token pair
				AccessTokenPair tokens = this.dropboxDatabaseApi.getSession().getAccessTokenPair();

				// store the access token pair in the applications settings
				Editor sharedPreferenceEditor = this.applicationPreferences.edit();
				sharedPreferenceEditor.putString( SettingsActivity.PREFERENCE_SYNCHRONIZATION_DROPBOX_ACCESS_KEY, tokens.key );
				sharedPreferenceEditor.putString( SettingsActivity.PREFERENCE_SYNCHRONIZATION_DROPBOX_ACCESS_SECRET, tokens.secret );
				if( !sharedPreferenceEditor.commit() ) {
					// ((CheckBoxPreference)this.loginIntoDropbox).setChecked( false );
					Log.e( "BackupMyAppsDropbox", "Error during the authentication with Dropbox! Failed to store the keys." );
					return;
				}

				// just log the state
				Log.i( "BackupMyAppsDropbox", "Authentication with Dropbox successfull!" );

			} catch( IllegalStateException e ) {
				Log.e( "BackupMyAppsDropbox", "Error during the authentication with Dropbox!", e );
			}
		} else {
			((CheckBoxPreference)this.loginIntoDropbox).setChecked( false );
		}
	}

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		// create the preference dialog
		super.onCreate( savedInstanceState );
		this.addPreferencesFromResource( R.xml.appreferences );

		// get the preference object for this application
		this.applicationPreferences = PreferenceManager.getDefaultSharedPreferences( this.getApplicationContext() );
		
		// setup the Dropbox API client
		AppKeyPair appKeys = new AppKeyPair( MainActivity.DROPBOX_API_APP_KEY, MainActivity.DROPBOX_API_APP_SECRET );
		AndroidAuthSession session = new AndroidAuthSession( appKeys, MainActivity.DROPBOX_API_APP_ACCESS_TYPE );
		this.dropboxDatabaseApi = new DropboxAPI< AndroidAuthSession >( session );

		// if we already have stored authentication keys, use them
		if( this.applicationPreferences.getString( "synchronization.dropboxAccessKey", "" ).length() > 0 ) {
			// get the key and the secret from the settings
			String key = this.applicationPreferences.getString( SettingsActivity.PREFERENCE_SYNCHRONIZATION_DROPBOX_ACCESS_KEY, "" );
			String secret = this.applicationPreferences.getString( SettingsActivity.PREFERENCE_SYNCHRONIZATION_DROPBOX_ACCESS_SECRET, "" );

			// create the required Dropbox access token object
			AccessTokenPair tokens = new AccessTokenPair( key, secret );

			// set the loaded access token pair
			this.dropboxDatabaseApi.getSession().setAccessTokenPair( tokens );

			// just log that we set the correct auth tokens
			Log.i( "BackupMyAppsSettings", "Successfully set the stored Dropbox authentication tokens." );
		}

		// get the preference object and create an on click handler
		this.buildVersionPreference = (Preference)this.findPreference( "BUILD_VERSION_PREFERENCE" );
		this.buildVersionPreference.setOnPreferenceClickListener( new OnPreferenceClickListener() {
			public boolean onPreferenceClick( Preference preference ) {
				Dialog dialogAbout = new Dialog( SettingsActivity.this );
				dialogAbout.setCanceledOnTouchOutside( true );

				dialogAbout.setContentView( R.layout.aboutdialog );
				dialogAbout.setTitle( R.string.dialogTitleAboutDialog );

				TextView appInformation = (TextView)dialogAbout.findViewById( R.id.textViewAboutAppName );
				appInformation.setText( String.format( getString( R.string.textViewAboutAppName ), SettingsActivity.this.getApplicationVersion() ) );

				dialogAbout.show();
				return true;
			}
		} );

		// get the entry to login into Dropbox and setup the onClick handler
		this.loginIntoDropbox = (CheckBoxPreference)this.findPreference( "synchronization.useDropbox" );
		this.loginIntoDropbox.setOnPreferenceClickListener( new OnPreferenceClickListener() {
			public boolean onPreferenceClick( Preference preference ) {
				Log.v( "BackupMyAppsPreferences", "Dropbox checkbox OnClickHandler called!" );
				if( ((CheckBoxPreference)SettingsActivity.this.loginIntoDropbox).isChecked() ) {
					SettingsActivity.this.dropboxDatabaseApi.getSession().startAuthentication( SettingsActivity.this );
				} else {
					// to deauthenticate, just delete the stored tokens
					Editor sharedPreferenceEditor = SettingsActivity.this.applicationPreferences.edit();
					sharedPreferenceEditor.putString( SettingsActivity.PREFERENCE_SYNCHRONIZATION_DROPBOX_ACCESS_KEY, "" );
					sharedPreferenceEditor.putString( SettingsActivity.PREFERENCE_SYNCHRONIZATION_DROPBOX_ACCESS_SECRET, "" );
					if( !sharedPreferenceEditor.commit() ) {
						Log.e( "BackupMyAppsDropbox", "Failed to perform the deauthentication." );
					} else {
						Log.i( "BackupMyAppsDropbox", "Dropbox deauthentication successfull." );
					}
				}
				return true;
			}
		} );

		// update the version number in the summary of the build-version object
		this.buildVersionPreference.setSummary( String.format( this.getString( R.string.preferenceSummaryBuildVersion ), SettingsActivity.this.getApplicationVersion() ) );
	}
}

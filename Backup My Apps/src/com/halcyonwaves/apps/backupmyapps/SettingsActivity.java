package com.halcyonwaves.apps.backupmyapps;

import com.dropbox.client2.session.AccessTokenPair;

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
		if( MainActivity.dropboxDatabaseApi.getSession().authenticationSuccessful() ) {
			try {
				// MANDATORY call to complete authentication
				MainActivity.dropboxDatabaseApi.getSession().finishAuthentication();

				// get the access token pair
				AccessTokenPair tokens = MainActivity.dropboxDatabaseApi.getSession().getAccessTokenPair();

				// store the access token pair in the applications settings
				Editor sharedPreferenceEditor = this.applicationPreferences.edit();
				sharedPreferenceEditor.putString( "synchronization.dropboxAccessKey", tokens.key );
				sharedPreferenceEditor.putString( "synchronization.dropboxAccessSecret", tokens.secret );
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
			// ((CheckBoxPreference)this.loginIntoDropbox).setChecked( false );
		}
	}

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		// create the preference dialog
		super.onCreate( savedInstanceState );
		this.addPreferencesFromResource( R.xml.appreferences );

		// get the preference object for this application
		this.applicationPreferences = PreferenceManager.getDefaultSharedPreferences( this.getApplicationContext() );

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
					MainActivity.dropboxDatabaseApi.getSession().startAuthentication( SettingsActivity.this );
				} else {
					// to deauthenticate, just delete the stored tokens
					Editor sharedPreferenceEditor = SettingsActivity.this.applicationPreferences.edit();
					sharedPreferenceEditor.putString( "synchronization.dropboxAccessKey", "" );
					sharedPreferenceEditor.putString( "synchronization.dropboxAccessSecret", "" );
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

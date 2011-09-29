package com.halcyonwaves.apps.backupmyapps;

import android.app.Dialog;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.TextView;

public class SettingsActivity extends PreferenceActivity {
	private Preference buildVersionPreference = null;

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
	public void onCreate( Bundle savedInstanceState ) {
		// create the preference dialog
		super.onCreate( savedInstanceState );
		this.addPreferencesFromResource( R.xml.appreferences );

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
		
		// update the version number in the summary of the build-version object
		this.buildVersionPreference.setSummary( String.format(  this.getString( R.string.preferenceSummaryBuildVersion ), SettingsActivity.this.getApplicationVersion() ) );
	}
}

package com.halcyonwaves.apps.backupmyapps;

import java.io.File;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.halcyonwaves.apps.backupmyapps.tasks.GatherBackupInformationTask;
import com.halcyonwaves.apps.backupmyapps.tasks.UploadToDropboxTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements IAsyncTaskFeedback {
	public static final String DROPBOX_API_APP_KEY = "lacao8kfka8qr5b";
	public static final String DROPBOX_API_APP_SECRET = "ue58x1dzuakhog4";
	public final static AccessType DROPBOX_API_APP_ACCESS_TYPE = AccessType.APP_FOLDER;
	private Button buttonBackupInstalledApplications = null;
	private Button buttonRestoreInstalledApplications = null;
	private TextView textViewAdditionalInformation = null;
	private Dialog dialogHelp = null;
	private ProgressDialog backupProgressDialog = null;
	private ProgressDialog uploadProgressDialog = null;
	private static final String BACKUP_FILENAME = "installedApplications.backupmyapps";
	private final File storagePath = Environment.getExternalStorageDirectory();
	private SharedPreferences applicationPreferences = null;
	private static final String PREFERENCES_LAST_WHATSNEW_DIALOG = "com.halcyonwaves.apps.backupmyapps.lastWhatsNewDialog";
	private DropboxAPI< AndroidAuthSession > dropboxDatabaseApi = null;
	private final static int DIALOG_WHATSNEW = 1; 
	public static GoogleAnalyticsTracker analyticsTracker = null;

	@Override
	protected Dialog onCreateDialog( int id ) {
		Dialog dialogToShow;
		switch( id ) {
			case MainActivity.DIALOG_WHATSNEW:
				dialogToShow = new Dialog( this );

				// setup the dialogs content
				dialogToShow.setTitle( this.getString( R.string.textViewWhatsNewLabel ) );
				dialogToShow.setContentView( R.layout.dialog_whatsnew );
				dialogToShow.setCancelable( false );

				// just tell the dialog what to do with the close button
				Button closeButton = (Button)dialogToShow.findViewById( R.id.buttonCloseWhatsNewDialog );
				closeButton.setOnClickListener( new OnClickListener() {
					public void onClick( View v ) {
						dismissDialog( MainActivity.DIALOG_WHATSNEW );
					}
				} );
				break;
			default:
				dialogToShow = null;
		}
		return dialogToShow;
	}
	
	@Override
	protected void onResume() {
		// call the super method
		super.onResume();
		
		// track this event
		MainActivity.analyticsTracker.trackPageView( "/MainActivity" );
	}
	
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		// create the layout of the main activity
		super.onCreate( savedInstanceState );
		setContentView( R.layout.main );

		// get the Google Analytics tracker instance
		MainActivity.analyticsTracker = GoogleAnalyticsTracker.getInstance();
		
		// setup the tracker and set a manual dispatch 
		MainActivity.analyticsTracker.startNewSession( "UA-26870251-1", this );
		
		// setup the product version
		try {
			MainActivity.analyticsTracker.setProductVersion( String.valueOf( this.getPackageManager().getPackageInfo( this.getPackageName(), 0 ).versionCode ),  this.getPackageManager().getPackageInfo( this.getPackageName(), 0 ).versionName );
		} catch( NameNotFoundException e1 ) {
			Log.w( "BackupMyAppsMainActivity", "Cannot set the product version to the tracking class. The following exception occurred: ", e1 );
		}
		
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
			Log.i( "BackupMyAppsDropbox", "Successfully set the stored Dropbox authentication tokens." );
		}

		// get some control handles
		this.buttonBackupInstalledApplications = (Button)this.findViewById( R.id.buttonBackupInstalledApplications );
		this.buttonRestoreInstalledApplications = (Button)this.findViewById( R.id.buttonRestoreInstalledApplications );
		this.textViewAdditionalInformation = (TextView)this.findViewById( R.id.textViewAdditionalInformation );

		// set the correct text for the label for additional information
		this.textViewAdditionalInformation.setText( String.format( this.getString( R.string.textViewAdditionalInformation ), "\"" + this.storagePath + "\"" ) );

		// if there is no backup file, disable the restore button
		File backupFile = new File( this.storagePath, MainActivity.BACKUP_FILENAME );
		if( !backupFile.exists() ) {
			Log.v( MainActivity.class.getSimpleName(), "No backup file found, disabling the restore button." );
			this.buttonRestoreInstalledApplications.setEnabled( false );
		}

		// be sure that the storage path exists
		this.storagePath.mkdirs();

		// add a click handler for the button to backup the installed applications
		this.buttonBackupInstalledApplications.setOnClickListener( new OnClickListener() {
			public void onClick( View v ) {
				// show a progress dialog
				MainActivity.this.backupProgressDialog = ProgressDialog.show( MainActivity.this, "", MainActivity.this.getString( R.string.progressDialogBackupInProgress ), true );

				// create and execute the backup task
				GatherBackupInformationTask gatherTask = new GatherBackupInformationTask( MainActivity.this, MainActivity.this.storagePath, MainActivity.BACKUP_FILENAME, MainActivity.this );
				gatherTask.execute();
			}
		} );

		// add a click handler for the button to restore the applications
		this.buttonRestoreInstalledApplications.setOnClickListener( new OnClickListener() {
			public void onClick( View v ) {
				Intent selectRestoreFileIntent = new Intent( MainActivity.this, RestoreSelectFileActivity.class );
				selectRestoreFileIntent.putExtra( "BackupFileName", (new File( MainActivity.this.storagePath, MainActivity.BACKUP_FILENAME ) ).toString() );
				MainActivity.this.startActivity( selectRestoreFileIntent );
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

		// for each application update, show the What's new? dialog
		try {
			final int applicationVersionCode = this.getPackageManager().getPackageInfo( this.getPackageName(), 0 ).versionCode;
			if( this.applicationPreferences.getInt( MainActivity.PREFERENCES_LAST_WHATSNEW_DIALOG, 0 ) < applicationVersionCode ) { 
				// show the dialog
				this.showDialog( MainActivity.DIALOG_WHATSNEW );

				// after we showed the dialog, save that we showed the dialog for this version
				Editor prefsEditor = this.applicationPreferences.edit();
				prefsEditor.putInt( MainActivity.PREFERENCES_LAST_WHATSNEW_DIALOG, applicationVersionCode );
				if( !prefsEditor.commit() ) {
					Log.e( "BackupMyAppsMainActivity", "Failed to commit the changes to the local settings database for storing the state of the \"What's new?\" dialog." );
				}
				prefsEditor = null;
			}
		} catch( NameNotFoundException e ) {
			Log.e( "BackupMyAppsMainActivity", "Failed to retrieve the version number for the application: ", e );
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();

		// if the user enabled statistics, send them now
		if( true ) { // TODO: check the preferences
			MainActivity.analyticsTracker.dispatch();
		}
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch( item.getItemId() ) {
			case R.id.menuSettings:
				Intent preferenceIntent = new Intent( MainActivity.this, SettingsActivity.class );
				this.startActivity( preferenceIntent );
				return true;
			case R.id.menuFeedback:
				MainActivity.analyticsTracker.trackPageView( "/FeedbackMenuItem" );
				final Intent emailIntent = new Intent( android.content.Intent.ACTION_SEND );
				emailIntent.setType( "plain/text" );
				emailIntent.putExtra( android.content.Intent.EXTRA_EMAIL, new String[]{ "backupmyapps@halcyonwaves.com" } );
				emailIntent.putExtra( android.content.Intent.EXTRA_SUBJECT, "Feedback: Backup My Apps (Android)" );
				emailIntent.putExtra( android.content.Intent.EXTRA_TEXT, this.getString( R.string.intentSendFeedbackBodyText ) );
				this.startActivity( Intent.createChooser( emailIntent, this.getString( R.string.intentSendFeedback ) ) );
				return true;
			case R.id.menuHelp:
				MainActivity.analyticsTracker.trackPageView( "/HelpMenuItem" );
				if( null == this.dialogHelp ) {
					this.dialogHelp = new Dialog( this );
					this.dialogHelp.setCanceledOnTouchOutside( true );

					this.dialogHelp.setContentView( R.layout.helpdialog );
					this.dialogHelp.setTitle( R.string.dialogTitleHelpDialog );
				}
				this.dialogHelp.show();
				return true;
			default:
				return super.onOptionsItemSelected( item );
		}
	}
	
	@Override
	protected void onDestroy() {
		// stop the tracker session again
		MainActivity.analyticsTracker.stopSession();
		
		// call the super method
		super.onDestroy();
	};

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate( R.menu.mainmenu, menu );
		return true;
	}

	public void taskSuccessfull( Object sender, Object data ) {
		if( sender.getClass().getSimpleName().equalsIgnoreCase( GatherBackupInformationTask.class.getSimpleName() ) ) {
			// enable the restore button, because we succeeded creating the backup
			this.buttonRestoreInstalledApplications.setEnabled( true );

			// close the progress dialog
			this.backupProgressDialog.dismiss();
			this.backupProgressDialog = null;
			
			// if we should sync, copy the file to the Dropbox account
			if( this.applicationPreferences.getBoolean( "synchronization.useDropbox", false ) ) {
				// show a new dialog
				this.uploadProgressDialog = ProgressDialog.show( MainActivity.this, "", this.getString( R.string.progressDialogUploadingToDropbox ), true );
				
				// generate the filename for the upload
				File backupFile = new File( this.storagePath, MainActivity.BACKUP_FILENAME );
				
				// try to upload the file
				UploadToDropboxTask uploadTask = new UploadToDropboxTask( backupFile.toString(), this.getApplicationContext(), this );
				uploadTask.execute();
				
				// be sure that we don't execute the following code in this case
				return;
			}

			// inform the user that we succeeded in backuping the data
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( this );
			dialogBuilder.setMessage( R.string.dialogMessageBackupSucceeded );
			dialogBuilder.setCancelable( false );
			dialogBuilder.setPositiveButton( R.string.buttonOk, new DialogInterface.OnClickListener() {
				public void onClick( DialogInterface dialog, int id ) {
					// nothing to do here
				}
			} );
			dialogBuilder.show();
		} else if( sender.getClass().getSimpleName().equalsIgnoreCase( UploadToDropboxTask.class.getSimpleName() ) ) {
			// close the progress dialog
			this.uploadProgressDialog.dismiss();
			this.uploadProgressDialog = null;
			
			// inform the user that we succeeded in backuping the data
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( this );
			dialogBuilder.setMessage( R.string.dialogMessageBackupSucceeded );
			dialogBuilder.setCancelable( false );
			dialogBuilder.setPositiveButton( R.string.buttonOk, new DialogInterface.OnClickListener() {
				public void onClick( DialogInterface dialog, int id ) {
					// nothing to do here
				}
			} );
			dialogBuilder.show();
		}
	}

	public void taskFailed( Object sender, Object data ) {
		if( sender.getClass().getSimpleName().equalsIgnoreCase( GatherBackupInformationTask.class.getSimpleName() ) ) {
			// close the progress dialog
			this.backupProgressDialog.dismiss();
			this.backupProgressDialog = null;

			// notify the user that we failed
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( this );
			dialogBuilder.setMessage( R.string.dialogMessageBackupFailed );
			dialogBuilder.setCancelable( false );
			dialogBuilder.setPositiveButton( R.string.buttonOk, new DialogInterface.OnClickListener() {
				public void onClick( DialogInterface dialog, int id ) {
					// nothing to do here
				}
			} );
			dialogBuilder.show();
		}
	}
}
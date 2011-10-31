package com.halcyonwaves.apps.backupmyapps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.halcyonwaves.apps.backupmyapps.tasks.GatherBackupInformationTask;
import com.halcyonwaves.apps.backupmyapps.tasks.RestoreBackupDataTask;

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
	private ProgressDialog restoreProgressDialog = null;
	private static final String BACKUP_FILENAME = "installedApplications.backupmyapps";
	private final File storagePath = Environment.getExternalStorageDirectory();
	private SharedPreferences applicationPreferences = null;
	private static final String PREFERENCES_LAST_WHATSNEW_DIALOG = "com.halcyonwaves.apps.backupmyapps.lastWhatsNewDialog";
	public static DropboxAPI< AndroidAuthSession > dropboxDatabaseApi = null;
	private final static int DIALOG_WHATSNEW = 1; 

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
	public void onCreate( Bundle savedInstanceState ) {
		// create the layout of the main activity
		super.onCreate( savedInstanceState );
		setContentView( R.layout.main );

		// get the preference object for this application
		this.applicationPreferences = PreferenceManager.getDefaultSharedPreferences( this.getApplicationContext() );

		// setup the Dropbox API client
		AppKeyPair appKeys = new AppKeyPair( MainActivity.DROPBOX_API_APP_KEY, MainActivity.DROPBOX_API_APP_SECRET );
		AndroidAuthSession session = new AndroidAuthSession( appKeys, MainActivity.DROPBOX_API_APP_ACCESS_TYPE );
		MainActivity.dropboxDatabaseApi = new DropboxAPI< AndroidAuthSession >( session );

		// if we already have stored authentication keys, use them
		if( this.applicationPreferences.getString( "synchronization.dropboxAccessKey", "" ).length() > 0 ) {
			// get the key and the secret from the settings
			String key = this.applicationPreferences.getString( SettingsActivity.PREFERENCE_SYNCHRONIZATION_DROPBOX_ACCESS_KEY, "" );
			String secret = this.applicationPreferences.getString( SettingsActivity.PREFERENCE_SYNCHRONIZATION_DROPBOX_ACCESS_SECRET, "" );

			// create the required Dropbox access token object
			AccessTokenPair tokens = new AccessTokenPair( key, secret );

			// set the loaded access token pair
			MainActivity.dropboxDatabaseApi.getSession().setAccessTokenPair( tokens );

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
				// show a progress dialog
				//MainActivity.this.restoreProgressDialog = ProgressDialog.show( MainActivity.this, "", MainActivity.this.getString( R.string.progressDialogRestoreInProgress ), true );

				// create and execute the restore task
				//RestoreBackupDataTask backupTask = new RestoreBackupDataTask( MainActivity.this.storagePath, MainActivity.BACKUP_FILENAME, MainActivity.this );
				//backupTask.execute();
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
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch( item.getItemId() ) {
			case R.id.menuSettings:
				Intent preferenceIntent = new Intent( MainActivity.this, SettingsActivity.class );
				this.startActivity( preferenceIntent );
				return true;
			case R.id.menuHelp:
				if( null == this.dialogHelp ) {
					this.dialogHelp = new Dialog( this );
					this.dialogHelp.setCanceledOnTouchOutside( true );

					this.dialogHelp.setContentView( R.layout.helpdialog );
					this.dialogHelp.setTitle( R.string.dialogTitleHelpDialog );
				}
				this.dialogHelp.show();
				return true;
			case R.id.menuPackageInformationHelp:
				// ask the user for sending the requested information
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( this );
				dialogBuilder.setMessage( R.string.dialogMessageAskForPackageInformation );
				dialogBuilder.setCancelable( false );
				dialogBuilder.setPositiveButton( R.string.buttonOk, new DialogInterface.OnClickListener() {
					public void onClick( DialogInterface dialog, int id ) {
						// nothing to do here
					}
				} );
				AlertDialog infoDialog = dialogBuilder.create();
				infoDialog.show();
				return true;
			default:
				return super.onOptionsItemSelected( item );
		}
	}

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

			// if we should sync, copy the file to the Dropbox account
			if( this.applicationPreferences.getBoolean( "synchronization.useDropbox", false ) ) {
				// define the backup filename
				String backupFilename = android.os.Build.DEVICE + "-" + android.os.Build.MODEL + ".backupmyapps";
				backupFilename = backupFilename.replace( ' ', '-' );

				// upload the file
				File backupFile = new File( this.storagePath, MainActivity.BACKUP_FILENAME );
				
				// try to upload the backup file
				try {
					FileInputStream inputStream = new FileInputStream( backupFile );
					Entry newEntry = MainActivity.dropboxDatabaseApi.putFileOverwrite( "/" + backupFilename, inputStream, backupFile.length(), null );
					Log.i( "BackupMyAppsDropbox", "The uploaded file's rev is: " + newEntry.rev );
				} catch( DropboxUnlinkedException e ) {
					Log.e( "BackupMyAppsDropbox", "The Dropbox account is not linked to the application anymore. Cannot upload the backup file.", e ); // TODO: handle this by telling it to the user
				} catch( DropboxException e ) {
					Log.e( "BackupMyAppsDropbox", "Something went wrong while uploading the backup file to the Dropbox account.", e ); // TODO: handle this by telling it to the user
				} catch( FileNotFoundException e ) {
					Log.e( "BackupMyAppsDropbox", "The backup file was not found.", e );
				}
			}

			// close the progress dialog
			this.backupProgressDialog.dismiss();
			this.backupProgressDialog = null;

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

		} else if( sender.getClass().getSimpleName().equalsIgnoreCase( RestoreBackupDataTask.class.getSimpleName() ) ) {
			// close the progress dialog
			this.restoreProgressDialog.dismiss();
			this.restoreProgressDialog = null;

			// open the dialog for the selection of the applications to restore
			Intent restoreSelectionActivity = new Intent( MainActivity.this, RestoreSelectionActivity.class );
			@SuppressWarnings( "unchecked" )
			HashMap< String, String > packageInformationList = (HashMap< String, String >)data;
			restoreSelectionActivity.putExtra( "packages", packageInformationList.size() );
			int i = 0;
			for( String key : packageInformationList.keySet() ) {
				restoreSelectionActivity.putExtra( "packageName" + i, key );
				restoreSelectionActivity.putExtra( "applicationName" + i, packageInformationList.get( key ) );
				i++;
			}
			MainActivity.this.startActivity( restoreSelectionActivity );
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

		} else if( sender.getClass().getSimpleName().equalsIgnoreCase( RestoreBackupDataTask.class.getSimpleName() ) ) {
			// close the progress dialog
			this.restoreProgressDialog.dismiss();
			this.restoreProgressDialog = null;

			// TODO: notify the user that we failed
		}
	}
}
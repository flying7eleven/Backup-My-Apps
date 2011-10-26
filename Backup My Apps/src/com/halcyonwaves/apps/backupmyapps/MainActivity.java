package com.halcyonwaves.apps.backupmyapps;

import java.io.File;
import java.util.HashMap;

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
	private Button buttonBackupInstalledApplications = null;
	private Button buttonRestoreInstalledApplications = null;
	private TextView textViewAdditionalInformation = null;
	private Dialog dialogHelp = null;
	private ProgressDialog backupProgressDialog = null;
	private ProgressDialog restoreProgressDialog = null;
	private static final String BACKUP_FILENAME = "installedApplications.backupmyapps";
	private final File storagePath = Environment.getExternalStorageDirectory();
	private SharedPreferences applicationPreferences = null;
	private static final String PREFERENCES_USER_ASKED_ABOUT_PACKAGE_INFORMATION = "com.halcyonwaves.apps.backupmyapps.userAskedToSendPackageInformation";

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		// create the layout of the main activity
		super.onCreate( savedInstanceState );
		setContentView( R.layout.main );
		
		// get the preference object for this application
		this.applicationPreferences = PreferenceManager.getDefaultSharedPreferences( this.getApplicationContext() );

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
				MainActivity.this.restoreProgressDialog = ProgressDialog.show( MainActivity.this, "", MainActivity.this.getString( R.string.progressDialogRestoreInProgress ), true );

				// create and execute the restore task
				RestoreBackupDataTask backupTask = new RestoreBackupDataTask( MainActivity.this.storagePath, MainActivity.BACKUP_FILENAME, MainActivity.this );
				backupTask.execute();
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
		
		// if this is the first application run, ask the user about the package list
		if( this.applicationPreferences.getBoolean( MainActivity.PREFERENCES_USER_ASKED_ABOUT_PACKAGE_INFORMATION, true ) ) {
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
			
			// store the value which indicates that the user was already asked to send the information
			Editor prefsEditor = this.applicationPreferences.edit();
			prefsEditor.putBoolean( MainActivity.PREFERENCES_USER_ASKED_ABOUT_PACKAGE_INFORMATION, false );
			prefsEditor.commit();
			prefsEditor = null;
		}
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch( item.getItemId() ) {
			case R.id.menuSettings:
				Intent preferenceIntent = new Intent(MainActivity.this, SettingsActivity.class);
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

			// close the progress dialog
			this.backupProgressDialog.dismiss();
			this.backupProgressDialog = null;
			
			// inform the user that we succeeded in backupping the data
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
			for( String key: packageInformationList.keySet() ) {
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
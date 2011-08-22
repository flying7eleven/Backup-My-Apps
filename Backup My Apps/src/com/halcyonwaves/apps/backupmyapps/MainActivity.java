package com.halcyonwaves.apps.backupmyapps;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements IAsyncTaskFeedback {
	private Button buttonBackupInstalledApplications = null;
	private Button buttonRestoreInstalledApplications = null;
	private TextView textViewAdditionalInformation = null;
	private Dialog dialogHelp = null;
	private Dialog dialogAbout = null;
	private ProgressDialog backupProgressDialog = null;
	private static final String BACKUP_FILENAME = "installedApplications.backupmyapps";
	private final File storagePath = Environment.getExternalStorageDirectory();

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		// create the layout of the main activity
		super.onCreate( savedInstanceState );
		setContentView( R.layout.main );

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
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( MainActivity.this );
				dialogBuilder.setMessage( R.string.dialogMessageNotImplemented );
				dialogBuilder.setCancelable( false );
				dialogBuilder.setPositiveButton( R.string.buttonOk, new DialogInterface.OnClickListener() {
					public void onClick( DialogInterface dialog, int id ) {
						dialog.dismiss();
					}
				} );
				AlertDialog infoDialog = dialogBuilder.create();
				infoDialog.show();
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
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch( item.getItemId() ) {
			case R.id.menuHelp:
				if( null == this.dialogHelp ) {
					this.dialogHelp = new Dialog( this );
					this.dialogHelp.setCanceledOnTouchOutside( true );

					this.dialogHelp.setContentView( R.layout.helpdialog );
					this.dialogHelp.setTitle( R.string.dialogTitleHelpDialog );
				}
				this.dialogHelp.show();
				return true;
			case R.id.menuAbout:
				if( null == this.dialogAbout ) {
					this.dialogAbout = new Dialog( this );
					this.dialogAbout.setCanceledOnTouchOutside( true );

					this.dialogAbout.setContentView( R.layout.aboutdialog );
					this.dialogAbout.setTitle( R.string.dialogTitleAboutDialog );

					TextView appInformation = (TextView)this.dialogAbout.findViewById( R.id.textViewAboutAppName );
					appInformation.setText( String.format( this.getString( R.string.textViewAboutAppName ), "0.3" ) ); // TODO:
																														// get
																														// the
																														// version
																														// name
																														// correctly

					TextView aboutInformation = (TextView)this.dialogAbout.findViewById( R.id.textViewAboutInformation );
					aboutInformation.setText( this.getString( R.string.textViewAboutInformation ) );

					ImageView image = (ImageView)this.dialogAbout.findViewById( R.id.imageViewApplicationIcon );
					image.setImageResource( R.drawable.icon );
				}
				this.dialogAbout.show();
				return true;
			case R.id.menuExit:
				this.finish();
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

	public void taskSuccessfull() {
		// enable the restore button, because we succeeded creating the backup
		this.buttonRestoreInstalledApplications.setEnabled( true );

		// close the progress dialog
		this.backupProgressDialog.dismiss();
		this.backupProgressDialog = null;
	}

	public void taskFailed() {
		// close the progress dialog
		this.backupProgressDialog.dismiss();
		this.backupProgressDialog = null;

		// TODO: notify the user that we failed
	}
}
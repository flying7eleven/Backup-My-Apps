package com.halcyonwaves.apps.backupmyapps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.halcyonwaves.apps.backupmyapps.tasks.DownloadFromDropboxTask;
import com.halcyonwaves.apps.backupmyapps.tasks.RestoreBackupDataTask;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * This class implements a dialog where the user can select the backup file he or she wants to
 * restore.
 * 
 * @author Tim Huetz
 * @since 0.5
 */
public class RestoreSelectFileActivity extends ListActivity implements IAsyncTaskFeedback {
	private String[] foundFilePathsArray = null;
	private ProgressDialog restoreProgressDialog = null;
	private ProgressDialog downloadFileProgressDialog = null;
	private SharedPreferences applicationPreferences = null;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		List< String > foundFileNames = new ArrayList< String >();
		List< String > foundFilePaths = new ArrayList< String >();

		// set the custom layout of this view
		this.setContentView( R.layout.restore_selectfile );
		
		// get the preference object for this application
		this.applicationPreferences = PreferenceManager.getDefaultSharedPreferences( this.getApplicationContext() );

		// get the information supplied to this activity
		Bundle extras = getIntent().getExtras();
		if( null == extras ) {
			// TODO: ERROR; this should NEVER happen
		}
		
		// if there is a local file to restore, show this in our list
		File backupFile = new File( extras.getString( "BackupFileName" ) );
		if( backupFile.exists() ) {
			foundFileNames.add( "<" + this.getString( R.string.listItemLocalBackupFile ) + ">" );
			foundFilePaths.add( extras.getString( "BackupFileName" ) );
			Log.v( "RestoreSelectFileActivity", "Added the local backup file as possible restore source." );
		}
		
		// if Dropbox-Sync is active, search for all backup files and put them into the list
		if( this.applicationPreferences.getBoolean( "synchronization.useDropbox", false ) ) {
			Entry rootDirectoryMetadata = null;
			List< Entry > directoryContent = null;
			try {
				rootDirectoryMetadata = MainActivity.dropboxDatabaseApi.metadata( "/", 100, null, true, null );
				directoryContent = rootDirectoryMetadata.contents;
				for( Entry currentEntry : directoryContent ) {
					if( currentEntry.path.toLowerCase().endsWith( ".backupmyapps" ) ) {
						foundFileNames.add( "Dropbox: " + currentEntry.path.substring( 1, currentEntry.path.length() - 13  ) );
						foundFilePaths.add( currentEntry.path );
						Log.v( "RestoreSelectFileActivity>>", "Found a new backup file in the Dropbox directory: " + currentEntry.path );
					}
				}
			} catch( DropboxException e ) {
				Log.e( "RestoreSelectFileActivity", "Error while fetching the backup files in the Dropbox folder.", e );
			}
		}
		
		// store an array of the package names in the class members
		this.foundFilePathsArray = (String[])foundFilePaths.toArray( new String[ 0 ] );

		// tell the view which data it should display
		ListAdapter adapter = new ArrayAdapter< String >( this, android.R.layout.simple_list_item_1, (String[])foundFileNames.toArray( new String[ 0 ] ) );
		this.setListAdapter( adapter );
	}
	
	@Override
	protected void onListItemClick( ListView l, View v, int position, long id ) {
		// get the name of the file to restore
		String fileToRestore = this.foundFilePathsArray[ position ]; 
		
		// check if it is a local or a Dropbox file; if its a Dropbox file, download it
		if( !(new File( fileToRestore )).exists() ) {
			/*// get a temporary file
			File restoreFile;
			try {
				// get a temporary file
				restoreFile = File.createTempFile( "backupfile", "backupmyapps" );
				
				// try to download the file
				FileOutputStream outputFile = new FileOutputStream( restoreFile );
				MainActivity.dropboxDatabaseApi.getFile( fileToRestore, null, outputFile, null );

				// set the new filename
				fileToRestore = restoreFile.toString();
				
			} catch( IOException e ) {
				Log.e( "RestoreSelectFileActivity", "Failed to download the backup file from the Dropbox account.", e ); // TODO: show an error message
			} catch(DropboxException e) {
				Log.e( "RestoreSelectFileActivity", "Failed to download the backup file from the Dropbox account.", e ); // TODO: show an error message
			}*/
			
			// show a progress dialog that the application is downloading the file
			RestoreSelectFileActivity.this.downloadFileProgressDialog = ProgressDialog.show( RestoreSelectFileActivity.this, "", RestoreSelectFileActivity.this.getString( R.string.progressDialogDownloadingFromDropbox ), true );

			// execute the background task which should download the file
			DownloadFromDropboxTask downloadTask = new DownloadFromDropboxTask();
			downloadTask.execute();
			
			// call the handler for this event of the super class
			super.onListItemClick( l, v, position, id );
			return;
		}
		
		// show a progress dialog
		RestoreSelectFileActivity.this.restoreProgressDialog = ProgressDialog.show( RestoreSelectFileActivity.this, "", RestoreSelectFileActivity.this.getString( R.string.progressDialogRestoreInProgress ), true );

		// create and execute the restore task
		RestoreBackupDataTask backupTask = new RestoreBackupDataTask( fileToRestore, RestoreSelectFileActivity.this );
		backupTask.execute();

		// call the handler for this event of the super class
		super.onListItemClick( l, v, position, id );
	}

	public void taskSuccessfull( Object sender, Object data ) {
		if( sender.getClass().getSimpleName().equalsIgnoreCase( RestoreBackupDataTask.class.getSimpleName() ) ) {
			// close the progress dialog
			this.restoreProgressDialog.dismiss();
			this.restoreProgressDialog = null;

			// open the dialog for the selection of the applications to restore
			Intent restoreSelectionActivity = new Intent( RestoreSelectFileActivity.this, RestoreSelectionActivity.class );
			@SuppressWarnings( "unchecked" )
			HashMap< String, String > packageInformationList = (HashMap< String, String >)data;
			restoreSelectionActivity.putExtra( "packages", packageInformationList.size() );
			int i = 0;
			for( String key : packageInformationList.keySet() ) {
				restoreSelectionActivity.putExtra( "packageName" + i, key );
				restoreSelectionActivity.putExtra( "applicationName" + i, packageInformationList.get( key ) );
				i++;
			}
			RestoreSelectFileActivity.this.startActivity( restoreSelectionActivity );
		} else if( sender.getClass().getSimpleName().equalsIgnoreCase( DownloadFromDropboxTask.class.getSimpleName() ) ){
			// close the download progress dialog
			this.downloadFileProgressDialog.dismiss();
			this.downloadFileProgressDialog = null;
			
			// show a progress dialog
			RestoreSelectFileActivity.this.restoreProgressDialog = ProgressDialog.show( RestoreSelectFileActivity.this, "", RestoreSelectFileActivity.this.getString( R.string.progressDialogRestoreInProgress ), true );

			// get the filename from the executing task
			String fileToRestore = (String)data;
	
			// create and execute the restore task
			RestoreBackupDataTask backupTask = new RestoreBackupDataTask( fileToRestore, RestoreSelectFileActivity.this );
			backupTask.execute();
		}
	}

	public void taskFailed( Object sender, Object data ) {
		if( sender.getClass().getSimpleName().equalsIgnoreCase( RestoreBackupDataTask.class.getSimpleName() ) ) {
			// close the progress dialog
			this.restoreProgressDialog.dismiss();
			this.restoreProgressDialog = null;
			
			// show an error message
			// TODO: this
			
			// close this activity and return to the main activity
			this.finish();
		} else 	if( sender.getClass().getSimpleName().equalsIgnoreCase( DownloadFromDropboxTask.class.getSimpleName() ) ) {
			// close the progress dialog
			this.downloadFileProgressDialog.dismiss();
			this.downloadFileProgressDialog = null;
			
			// show an error message
			// TODO: this
			
			// close this activity and return to the main activity
			this.finish();
		}
	}
}

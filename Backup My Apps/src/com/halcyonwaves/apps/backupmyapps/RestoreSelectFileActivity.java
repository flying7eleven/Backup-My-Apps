package com.halcyonwaves.apps.backupmyapps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

/**
 * This class implements a dialog where the user can select the backup file he or she wants to
 * restore.
 * 
 * @author Tim Huetz
 * @since 0.6
 */
public class RestoreSelectFileActivity extends ListActivity {
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		List< String > foundFileNames = new ArrayList< String >();
		List< String > foundFilePaths = new ArrayList< String >();

		// set the custom layout of this view
		this.setContentView( R.layout.restore_selectfile );

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

		// tell the view which data it should display
		ListAdapter adapter = new ArrayAdapter< String >( this, android.R.layout.simple_list_item_1, (String[])foundFileNames.toArray( new String[ 0 ] ) );
		this.setListAdapter( adapter );
	}
}

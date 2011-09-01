package com.halcyonwaves.apps.backupmyapps;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

/**
 * 
 * @author Tim Huetz
 * @since 0.3
 */
public class RestoreSelectionActivity extends ListActivity {
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		//
		Bundle extras = getIntent().getExtras();
		if( null == extras ) {
			// TODO: ERROR; this should NEVER happen
		}

		//
		List< String > foundPackages = new ArrayList< String >();
		int numberOfPackages = extras.getInt( "packages" );
		for( int i = 0; i < numberOfPackages; i++ ) {
			String currentPackage = extras.getString( "package" + i );
			foundPackages.add( currentPackage );
			Log.v( RestoreSelectionActivity.class.getSimpleName(), "Found package: " + currentPackage );
		}

		ListAdapter adapter = new ArrayAdapter< String >( this, android.R.layout.simple_list_item_1, (String[])foundPackages.toArray(new String[0]) );
		this.setListAdapter( adapter );
	}
}

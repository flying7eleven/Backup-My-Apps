package com.halcyonwaves.apps.backupmyapps;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

/**
 * 
 * @author Tim Huetz
 * @since 0.3
 */
public class RestoreSelectionActivity extends ListActivity {
	private String[] foundPackage = null;
	
	public RestoreSelectionActivity(String[] foundPackages) {
		this.foundPackage = foundPackages;
	}
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		
		ListAdapter adapter = new ArrayAdapter< String >( this, android.R.layout.simple_list_item_1, this.foundPackage );
		this.setListAdapter( adapter );
	}
}

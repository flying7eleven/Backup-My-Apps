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
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		// Create some mock data
		String[] testValues = new String[]{ "Test1", "Test2", "Test3", "Test4" };

		ListAdapter adapter = new ArrayAdapter< String >( this, android.R.layout.simple_list_item_1, testValues );
		this.setListAdapter( adapter );
	}
}

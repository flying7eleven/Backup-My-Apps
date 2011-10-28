package com.halcyonwaves.apps.backupmyapps;

import android.app.ListActivity;
import android.os.Bundle;

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

		// set the custom layout of this view
		this.setContentView( R.layout.restore_selectfile );
	}
}

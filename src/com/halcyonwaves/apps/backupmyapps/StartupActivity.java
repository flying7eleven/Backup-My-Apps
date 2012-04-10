package com.halcyonwaves.apps.backupmyapps;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

public class StartupActivity extends SherlockActivity {

	@Override
	protected void onCreate( final Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.startup_layout );
	}
}

package com.halcyonwaves.apps.backupmyapps;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	private Button buttonBackupInstalledApplications = null;
	private Button buttonRestoreInstalledApplications = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// create the layout of the main activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // get some control handles
        this.buttonBackupInstalledApplications = (Button)this.findViewById( R.id.buttonBackupInstalledApplications );
        this.buttonRestoreInstalledApplications = (Button)this.findViewById( R.id.buttonRestoreInstalledApplications );
        
        // add a click handler for the button to backup the installed applications
        this.buttonBackupInstalledApplications.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				// TODO this
			}
		});
        
        // add a click handler for the button to restore the applications
        this.buttonRestoreInstalledApplications.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				// TODO this
			}
		});
    }
}
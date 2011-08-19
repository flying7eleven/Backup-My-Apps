package com.halcyonwaves.apps.backupmyapps;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
               
        // if there is no backup file, disable the restore button
        if( true ) { // TODO: this
        	this.buttonRestoreInstalledApplications.setEnabled( false );
        }
        
        // add a click handler for the button to backup the installed applications
        this.buttonBackupInstalledApplications.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
		        File newxmlfile = new File(Environment.getExternalStorageDirectory()+"/applicationBackupList.xml");
		        try{
		                newxmlfile.createNewFile();
		        }catch(IOException e){
		                Log.e("IOException", "exception in createNewFile() method");
		        }
			}
		});
        
        // add a click handler for the button to restore the applications
        this.buttonRestoreInstalledApplications.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				// TODO this
			}
		});
        
        // if no external storage is mounted, show an error and close
        if( Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED ) ) {
        	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        	dialogBuilder.setMessage(R.string.dialogMessageNoExternalStorageMounted);
        	dialogBuilder.setCancelable(false);
        	dialogBuilder.setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int id) {
        			MainActivity.this.finish();
        		}
        	});
        	AlertDialog infoDialog = dialogBuilder.create();
        	infoDialog.show();
        }
    }
}
package com.halcyonwaves.apps.backupmyapps;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

/**
 * Class for restoring backed-up applications in an asynchronous way.
 * 
 * @author Tim Huetz
 * @since 0.3
 */
public class RetoreBackupDataTask extends AsyncTask< Void, Void, Boolean > {
	private File storagePath = null;
	private Context applicationContext = null;
	private String backupFilename = "";
	private IAsyncTaskFeedback feedbackClass = null;
	
	/**
	 * Constructor for this class.
	 * 
	 * @param applicationContext The context of the application.
	 * @param storagePath The path to the backup file.
	 * @param backupFilename The name of the backup file.
	 * @param feedbackClass The class which should handle the feedback of this task.
	 * @author Tim Huetz
	 * @since 0.3
	 */
	public RetoreBackupDataTask( Context applicationContext, File storagePath, String backupFilename, IAsyncTaskFeedback feedbackClass ) {
		this.storagePath = storagePath;
		this.applicationContext = applicationContext;
		this.backupFilename = backupFilename;
		this.feedbackClass = feedbackClass;
	}
	
	@Override
	protected Boolean doInBackground( Void... arg0 ) {
		// TODO Auto-generated method stub
		
		//
		//Intent intent = new Intent(Intent.ACTION_VIEW);
		//intent.setData(Uri.parse("market://details?id=com.android.example"));
		//this.applicationContext.startActivity( intent );
		return null;
	}

}

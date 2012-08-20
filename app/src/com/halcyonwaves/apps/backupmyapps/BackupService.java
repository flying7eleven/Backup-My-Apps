package com.halcyonwaves.apps.backupmyapps;

import android.app.IntentService;
import android.content.Intent;

/**
 * This class implements the background service which does the automatic
 * backup of the installed applications.
 * 
 * @author Tim Huetz
 * @since 0.4
 */
public class BackupService extends IntentService {
// see http://developer.android.com/guide/topics/fundamentals/services.html
	/**
	 * Default constructor of this class.
	 * 
	 * @author Tim Huetz
	 * @since 0.4
	 */
	public BackupService() {
		super( "BackupMyAppsBackupService" );
	}
	
	@Override
	protected void onHandleIntent( Intent arg0 ) {
		// TODO Auto-generated method stub

	}

}

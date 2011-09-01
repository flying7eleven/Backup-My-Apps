package com.halcyonwaves.apps.backupmyapps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Class for restoring backed-up applications in an asynchronous way.
 * 
 * @author Tim Huetz
 * @since 0.3
 */
public class RestoreBackupDataTask extends AsyncTask< Void, Void, Boolean > {
	private File storagePath = null;
	private Context applicationContext = null;
	private String backupFilename = "";
	private IAsyncTaskFeedback feedbackClass = null;
	private List< String > itemList = null;

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
	public RestoreBackupDataTask( Context applicationContext, File storagePath, String backupFilename, IAsyncTaskFeedback feedbackClass ) {
		this.storagePath = storagePath;
		this.applicationContext = applicationContext;
		this.backupFilename = backupFilename;
		this.feedbackClass = feedbackClass;
		this.itemList = new ArrayList< String >();
	}

	/**
	 * Create a document object from the input string.
	 * 
	 * @param xml The XML string which should be parsed.
	 * @return The document object which represents the parsed backup file.
	 * @author Tim Huetz
	 * @since 0.3
	 */
	private Document XMLfromString( String xml ) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream( new StringReader( xml ) );
			doc = db.parse( is );
		} catch( ParserConfigurationException e ) {
			Log.e( RestoreBackupDataTask.class.getSimpleName(), "Failed to parse the backup file (ParserConfigurationException): " + e.getMessage() );
			return null;
		} catch( SAXException e ) {
			Log.e( RestoreBackupDataTask.class.getSimpleName(), "Failed to parse the backup file (SAXException). Wrong XML structure: " + e.getMessage() );
			return null;
		} catch( IOException e ) {
			Log.e( RestoreBackupDataTask.class.getSimpleName(), "Failed to parse the backup file (IOException): " + e.getMessage() );
			return null;
		}

		// return the parsed document
		return doc;
	}
	
	private void installPackage(String pathToApk) { // see http://www.anddev.org/androidpermissioninstall_packages_not_granted-t5858.html for more
		// see http://stackoverflow.com/questions/5803999/install-apps-silently-with-granted-install-packages-permission
		//Uri packageUri = Uri.fromFile( new File( pathToApk ) );
		// set permissions
		//PackageManager pm = this.applicationContext.getPackageManager();
		// pm.addPermission(p);//error
		// pm.installPackage(packageUri, null, 1);
	}
	
	/**
	 * Try to open the market with the supplied package name. If this fails, open
	 * the browser URL of the market and search the package there.
	 * 
	 * @param packageName The name of the package to install.
	 */
	private void installPackageFromMarket( String packageName ) {
		try {
			Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=" + packageName ) );
			this.applicationContext.startActivity( browserIntent );
		} catch( Exception outerException ) {
			Log.e( RestoreBackupDataTask.class.getSimpleName(), "Failed to open the market directly. The exception was: " + outerException.getMessage() );
			try {
				Log.v( RestoreBackupDataTask.class.getSimpleName(), "Opening browser directly!!" );
				Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( "https://market.android.com/details?id=" + packageName ) );
				this.applicationContext.startActivity( browserIntent );
				Log.v( RestoreBackupDataTask.class.getSimpleName(), "Browser closed!" );
			} catch( Exception innerException ) {
				Log.e( RestoreBackupDataTask.class.getSimpleName(), "Failed to open the market in the browser. The exception was: " + innerException.getMessage() );
			}
		}
	}

	@Override
	protected Boolean doInBackground( Void... arg0 ) {
		// just log some information
		Log.v( RestoreBackupDataTask.class.getSimpleName(), "Using following external storage directory: " + this.storagePath );

		// try to open the input file
		try {
			// get the input stream to the backup file
			InputStream backupFileStream = new FileInputStream( new File( this.storagePath, this.backupFilename ) );

			// prepare the file for reading
			InputStreamReader inputreader = new InputStreamReader( backupFileStream );
			BufferedReader buffreader = new BufferedReader( inputreader );

			// read the whole file into the memory
			String line, full = "";
			while( (line = buffreader.readLine()) != null ) { // TODO: optimize this
				full += line;
			}

			// get the xml document from the string
			Document backupFile = this.XMLfromString( full );

			// loop through all package nodes
			NodeList packageNodes = backupFile.getElementsByTagName( "InstalledApp" );
			for( int currentNodeId = 0; currentNodeId < packageNodes.getLength(); currentNodeId++ ) {
				Element currentPackage = (Element)packageNodes.item( currentNodeId );

				// just log that we found a new package
				Log.v( RestoreBackupDataTask.class.getSimpleName(), "Found package to restore: " + currentPackage.getAttribute( "packageName" ) + " (" + currentPackage.getAttribute( "applicationName" ) + ")" );

				// add the package to the item list
				this.itemList.add( currentPackage.getAttribute( "packageName" ) );
			}

		} catch( FileNotFoundException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// it seems that we succeeded doing anything
		return true;
	}

	@Override
	protected void onPostExecute( Boolean result ) {
		if( result ) {
			this.feedbackClass.taskSuccessfull( this, this.itemList );
		} else {
			this.feedbackClass.taskFailed( this, null );
		}
	}
}

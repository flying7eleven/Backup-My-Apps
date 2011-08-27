package com.halcyonwaves.apps.backupmyapps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
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

		} catch( FileNotFoundException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//
		// Intent intent = new Intent(Intent.ACTION_VIEW);
		// intent.setData(Uri.parse("market://details?id=com.android.example"));
		// this.applicationContext.startActivity( intent );
		return null;
	}

}

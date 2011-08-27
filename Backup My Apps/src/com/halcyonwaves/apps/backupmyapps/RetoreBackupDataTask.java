package com.halcyonwaves.apps.backupmyapps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
			System.out.println( "XML parse error: " + e.getMessage() );
			return null;
		} catch( SAXException e ) {
			System.out.println( "Wrong XML file structure: " + e.getMessage() );
			return null;
		} catch( IOException e ) {
			System.out.println( "I/O exeption: " + e.getMessage() );
			return null;
		}

		// return the parsed document
		return doc;
	}

	@Override
	protected Boolean doInBackground( Void... arg0 ) {
		// just log some information
		Log.v( RetoreBackupDataTask.class.getSimpleName(), "Using following external storage directory: " + this.storagePath );

		// try to open the input file
		File backupFile = new File( this.storagePath, this.backupFilename );

		//
		// Intent intent = new Intent(Intent.ACTION_VIEW);
		// intent.setData(Uri.parse("market://details?id=com.android.example"));
		// this.applicationContext.startActivity( intent );
		return null;
	}

}

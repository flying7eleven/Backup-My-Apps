package com.halcyonwaves.apps.backupmyapps.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.halcyonwaves.apps.backupmyapps.IAsyncTaskFeedback;
import com.halcyonwaves.apps.backupmyapps.utils.PackageVersion;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Class for restoring backed-up applications in an asynchronous way.
 * 
 * @author Tim Huetz
 * @since 0.3
 */
public class RestoreBackupDataTask extends AsyncTask< Void, Void, Boolean > {
	private File backupFile = null;
	private IAsyncTaskFeedback feedbackClass = null;
	private HashMap< String, String > itemList = null;

	/**
	 * Constructor for this class.
	 * 
	 * @param storagePath The path to the backup file.
	 * @param backupFilename The name of the backup file.
	 * @param feedbackClass The class which should handle the feedback of this task.
	 * @author Tim Huetz
	 * @since 0.3
	 */
	public RestoreBackupDataTask( File storagePath, String backupFilename, IAsyncTaskFeedback feedbackClass ) {
		this.backupFile = new File( storagePath, backupFilename );
		this.feedbackClass = feedbackClass;
		this.itemList = new HashMap< String, String >();
	}
	
	/**
	 * Constructor for this class.
	 * 
	 * @param storagePath The path to the backup file.
	 * @param backupFilename The name of the backup file.
	 * @param feedbackClass The class which should handle the feedback of this task.
	 * @author Tim Huetz
	 * @since 0.5
	 */
	public RestoreBackupDataTask( String fileToRestore, IAsyncTaskFeedback feedbackClass ) {
		this.backupFile = new File( fileToRestore );
		this.feedbackClass = feedbackClass;
		this.itemList = new HashMap< String, String >();
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
		Log.v( RestoreBackupDataTask.class.getSimpleName(), "Using following external storage file: " + this.backupFile );

		// try to open the input file
		try {
			// get the input stream to the backup file
			InputStream backupFileStream = new FileInputStream( this.backupFile );

			// prepare the file for reading
			InputStreamReader inputreader = new InputStreamReader( backupFileStream );
			BufferedReader buffreader = new BufferedReader( inputreader, 8192 );

			// read the whole file into the memory
			String line, full = "";
			while( (line = buffreader.readLine()) != null ) { // TODO: optimize this
				full += line;
			}

			// get the xml document from the string
			Document backupFile = this.XMLfromString( full );
			
			// check the version of the backup file
			NamedNodeMap fileAttributes = backupFile.getElementsByTagName( "InstalledApplications" ).item( 0 ).getAttributes();
			for( int i = 0; i < fileAttributes.getLength(); i++ ) {
				Node currentNode = fileAttributes.item( i );
				Attr currentAttribute = (Attr)currentNode;
				if( currentAttribute == null ) {
					continue;
				}
				Log.v( "BackupFileVersion", "Found a new attribute with the following name: " + currentAttribute.getName() );
				if( currentAttribute.getName().equalsIgnoreCase( "version" ) ) {
					final PackageVersion supportedVersion = new PackageVersion( "0.5" );
					PackageVersion backupfileVersion = new PackageVersion( currentAttribute.getValue() );
					if( !supportedVersion.equals( backupfileVersion ) ) {
						Log.e( "BackupFileVersion", "It seems that the format of the backup file is not supported by this application." ); // TODO:
																																			// show
																																			// the
																																			// version
																																			// numbers
						return false;
					}
				}
			}

			// loop through all package nodes
			NodeList packageNodes = backupFile.getElementsByTagName( "InstalledApp" );
			for( int currentNodeId = 0; currentNodeId < packageNodes.getLength(); currentNodeId++ ) {
				Element currentPackage = (Element)packageNodes.item( currentNodeId );

				// just log that we found a new package
				Log.v( RestoreBackupDataTask.class.getSimpleName(), "Found package to restore: " + currentPackage.getAttribute( "packageName" ) + " (" + currentPackage.getAttribute( "applicationName" ) + ")" );

				// add the package to the item list
				this.itemList.put( currentPackage.getAttribute( "packageName" ), currentPackage.getAttribute( "applicationName" ) );
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

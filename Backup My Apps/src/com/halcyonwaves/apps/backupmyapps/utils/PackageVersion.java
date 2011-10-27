package com.halcyonwaves.apps.backupmyapps.utils;

import android.util.Log;

/**
 * This class implements some functions to parse a version string and compare the version to other
 * versions.
 * 
 * @author Tim Huetz
 * @since 0.6
 */
public class PackageVersion {
	private int majorVersion = -1;
	private int minorVersion = -1;
	private int buildVersion = -1;

	/**
	 * Constructor of this class which parses a version string into a version number.
	 * 
	 * @param versionString The string which should be parsed.
	 */
	public PackageVersion( String versionString ) {
		// just log the event
		Log.v( "PackageVersion", "Constructor got following version string: " + versionString );

		// try to split the version string
		String[] versionStringParts = versionString.trim().split( "\\." );
		try {
			this.majorVersion = Integer.parseInt( versionStringParts[ 0 ] );
			this.minorVersion = Integer.parseInt( versionStringParts[ 1 ] );
			if( versionStringParts.length > 2 ) {
				this.buildVersion = Integer.parseInt( versionStringParts[ 2 ] );
			}
		} catch( Exception e ) {
			Log.e( "PackageVersion", "Failed to parse the version string: ", e );
		}
	}

	/**
	 * Compare the current version object to an other object.
	 * 
	 * @param otherVersion The other PackageVersion object to compare with.
	 * @return True if the PackageVersion objects represent the same version.
	 */
	public boolean equals( PackageVersion otherVersion ) {
		return this.majorVersion == otherVersion.majorVersion && this.minorVersion == otherVersion.minorVersion && this.buildVersion == otherVersion.buildVersion;
	}
}

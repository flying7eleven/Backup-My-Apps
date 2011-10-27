package com.halcyonwaves.apps.backupmyapps.utils;

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
		// TODO: implement this
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

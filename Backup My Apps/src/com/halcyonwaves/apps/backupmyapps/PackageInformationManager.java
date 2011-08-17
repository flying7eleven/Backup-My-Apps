package com.halcyonwaves.apps.backupmyapps;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageInformationManager {
	private PackageManager internalPackageManager = null;
	
	public PackageInformationManager() {
		// TODO: implement this
	}
	
	public ArrayList<PackageInformation> getInstalledApps(boolean getSysPackages) {
	    ArrayList<PackageInformation> res = new ArrayList<PackageInformation>();        
	    List<PackageInfo> packs = this.internalPackageManager.getInstalledPackages(0);
	    for(int i=0;i<packs.size();i++) {
	        PackageInfo p = packs.get(i);
	        if ((!getSysPackages) && (p.versionName == null)) {
	            continue ;
	        }
	        PackageInformation newInfo = new PackageInformation();
	        newInfo.appname = p.applicationInfo.loadLabel(this.internalPackageManager).toString();
	        newInfo.pname = p.packageName;
	        newInfo.versionName = p.versionName;
	        newInfo.versionCode = p.versionCode;
	        newInfo.icon = p.applicationInfo.loadIcon(this.internalPackageManager);
	        res.add(newInfo);
	    }
	    return res; 
	}
}

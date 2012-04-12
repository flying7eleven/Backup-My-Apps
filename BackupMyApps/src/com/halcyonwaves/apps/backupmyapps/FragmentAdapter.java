package com.halcyonwaves.apps.backupmyapps;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.halcyonwaves.apps.backupmyapps.fragments.BackupFragment;
import com.halcyonwaves.apps.backupmyapps.fragments.IntroductionFragment;
import com.halcyonwaves.apps.backupmyapps.fragments.RestoreFragment;
import com.viewpagerindicator.TitleProvider;

class FragmentAdapter extends FragmentPagerAdapter implements TitleProvider {

	private Context appContext = null;

	public FragmentAdapter( final FragmentManager fm, final Context applicationContext ) {
		super( fm );
		this.appContext = applicationContext;
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public Fragment getItem( final int position ) {
		switch( position ) {
			case 0:
				return new IntroductionFragment();
			case 1:
				return new BackupFragment();
			case 2:
				return new RestoreFragment();
			default:
				throw new IllegalStateException( "Invalid fragment selected." );
		}
	}

	@Override
	public String getTitle( final int position ) {
		switch( position ) {
			case 0:
				return this.appContext.getString( R.string.fragment_title_introduction );
			case 1:
				return this.appContext.getString( R.string.fragment_title_backup );
			case 2:
				return this.appContext.getString( R.string.fragment_title_restore );
			default:
				throw new IllegalStateException( "Invalid fragment selected." );
		}
	}
}
package com.halcyonwaves.apps.backupmyapps;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class StartupActivity extends SherlockFragmentActivity {
	
	private static final String[] CONTENT = new String[] { "Recent", "Artists", "Albums", "Songs", "Playlists", "Genres" };
	private ViewPager mPager = null;
	private PageIndicator mIndicator = null;
	private GoogleMusicAdapter mAdapter = null;

	@Override
	protected void onCreate( final Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.startup_layout );
		
		mAdapter = new GoogleMusicAdapter( getSupportFragmentManager() );
		mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setAdapter( mAdapter );
		mIndicator = (TabPageIndicator)findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
	}
	
	class GoogleMusicAdapter extends TestFragmentAdapter implements TitleProvider {
		public GoogleMusicAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return TestFragment.newInstance(StartupActivity.CONTENT[position % StartupActivity.CONTENT.length]);
		}

		@Override
		public int getCount() {
			return StartupActivity.CONTENT.length;
		}

		@Override
		public String getTitle(int position) {
			return StartupActivity.CONTENT[position % StartupActivity.CONTENT.length].toUpperCase();
		}
	}
}

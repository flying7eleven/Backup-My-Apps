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

	class GoogleMusicAdapter extends TestFragmentAdapter implements TitleProvider {

		public GoogleMusicAdapter( final FragmentManager fm ) {
			super( fm );
		}

		@Override
		public int getCount() {
			return StartupActivity.CONTENT.length;
		}

		@Override
		public Fragment getItem( final int position ) {
			return TestFragment.newInstance( StartupActivity.CONTENT[ position % StartupActivity.CONTENT.length ] );
		}

		@Override
		public String getTitle( final int position ) {
			return StartupActivity.CONTENT[ position % StartupActivity.CONTENT.length ].toUpperCase();
		}
	}

	private static final String[] CONTENT = new String[] { "Recent", "Artists", "Albums", "Songs", "Playlists", "Genres" };
	private GoogleMusicAdapter mAdapter = null;
	private PageIndicator mIndicator = null;

	private ViewPager mPager = null;

	@Override
	protected void onCreate( final Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.startup_layout );

		this.mAdapter = new GoogleMusicAdapter( this.getSupportFragmentManager() );
		this.mPager = (ViewPager) this.findViewById( R.id.pager );
		this.mPager.setAdapter( this.mAdapter );
		this.mIndicator = (TabPageIndicator) this.findViewById( R.id.indicator );
		this.mIndicator.setViewPager( this.mPager );
	}
}

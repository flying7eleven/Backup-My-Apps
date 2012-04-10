package com.halcyonwaves.apps.backupmyapps;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;

public class StartupActivity extends SherlockFragmentActivity {

	private FragmentAdapter mAdapter = null;
	private PageIndicator mIndicator = null;

	private ViewPager mPager = null;

	@Override
	protected void onCreate( final Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.startup_layout );

		this.mAdapter = new FragmentAdapter( this.getSupportFragmentManager() );
		this.mPager = (ViewPager) this.findViewById( R.id.pager );
		this.mPager.setAdapter( this.mAdapter );
		this.mIndicator = (TabPageIndicator) this.findViewById( R.id.indicator );
		this.mIndicator.setViewPager( this.mPager );
	}
}

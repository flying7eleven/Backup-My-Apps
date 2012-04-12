package com.halcyonwaves.apps.backupmyapps;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

public class StartupActivity extends SherlockFragmentActivity {

	private FragmentAdapter mAdapter = null;
	private PageIndicator mIndicator = null;
	private ViewPager mPager = null;

	@Override
	protected void onCreate( final Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.activity_startup );

		this.mAdapter = new FragmentAdapter( this.getSupportFragmentManager(), this.getApplicationContext() );
		this.mPager = (ViewPager) this.findViewById( R.id.pager );
		this.mPager.setAdapter( this.mAdapter );
		this.mIndicator = (TitlePageIndicator) this.findViewById( R.id.indicator );
		this.mIndicator.setViewPager( this.mPager );
	}

	@Override
	public boolean onCreateOptionsMenu( final Menu menu ) {
		// menu.add( "Test" ).setIcon( android.R.drawable.ic_menu_preferences
		// ).setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS );
		this.getSupportMenuInflater().inflate( R.menu.menu_main, menu );
		return super.onCreateOptionsMenu( menu );
	}
}

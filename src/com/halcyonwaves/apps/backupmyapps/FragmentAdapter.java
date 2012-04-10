package com.halcyonwaves.apps.backupmyapps;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.viewpagerindicator.TitleProvider;

class FragmentAdapter extends FragmentPagerAdapter implements TitleProvider {

	protected static final String[] CONTENT = new String[] { "This", "Is", "A", "Test", };

	public FragmentAdapter( final FragmentManager fm ) {
		super( fm );
	}

	@Override
	public int getCount() {
		return FragmentAdapter.CONTENT.length;
	}

	@Override
	public Fragment getItem( final int position ) {
		return TestFragment.newInstance( FragmentAdapter.CONTENT[ position % FragmentAdapter.CONTENT.length ] );
	}

	@Override
	public String getTitle( final int position ) {
		return FragmentAdapter.CONTENT[ position % FragmentAdapter.CONTENT.length ].toUpperCase();
	}

	public void setCount( final int count ) {
		if( (count > 0) && (count <= 10) ) {
			this.notifyDataSetChanged();
		}
	}
}
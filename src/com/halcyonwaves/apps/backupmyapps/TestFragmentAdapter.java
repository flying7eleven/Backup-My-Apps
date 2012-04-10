package com.halcyonwaves.apps.backupmyapps;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class TestFragmentAdapter extends FragmentPagerAdapter {

	protected static final String[] CONTENT = new String[] { "This", "Is", "A", "Test", };

	private int mCount = TestFragmentAdapter.CONTENT.length;

	public TestFragmentAdapter( final FragmentManager fm ) {
		super( fm );
	}

	@Override
	public int getCount() {
		return this.mCount;
	}

	@Override
	public Fragment getItem( final int position ) {
		return TestFragment.newInstance( TestFragmentAdapter.CONTENT[ position % TestFragmentAdapter.CONTENT.length ] );
	}

	public void setCount( final int count ) {
		if( (count > 0) && (count <= 10) ) {
			this.mCount = count;
			this.notifyDataSetChanged();
		}
	}
}
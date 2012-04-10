/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.viewpagerindicator;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.halcyonwaves.apps.backupmyapps.R;

/**
 * This widget implements the dynamic action bar tab behavior that can change
 * across different configurations or circumstances.
 */
public class TabPageIndicator extends HorizontalScrollView implements PageIndicator {

	public static class TabView extends LinearLayout {

		private int mIndex;
		private TabPageIndicator mParent;

		public TabView( final Context context, final AttributeSet attrs ) {
			super( context, attrs );
		}

		public int getIndex() {
			return this.mIndex;
		}

		public void init( final TabPageIndicator parent, final String text, final int index ) {
			this.mParent = parent;
			this.mIndex = index;

			final TextView textView = (TextView) this.findViewById( android.R.id.text1 );
			textView.setText( text );
		}

		@Override
		public void onMeasure( final int widthMeasureSpec, final int heightMeasureSpec ) {
			super.onMeasure( widthMeasureSpec, heightMeasureSpec );

			// Re-measure if we went beyond our maximum size.
			if( (this.mParent.mMaxTabWidth > 0) && (this.getMeasuredWidth() > this.mParent.mMaxTabWidth) ) {
				super.onMeasure( MeasureSpec.makeMeasureSpec( this.mParent.mMaxTabWidth, MeasureSpec.EXACTLY ), heightMeasureSpec );
			}
		}
	}

	private final LayoutInflater mInflater;

	private ViewPager.OnPageChangeListener mListener;
	int mMaxTabWidth;
	private int mSelectedTabIndex;

	private final OnClickListener mTabClickListener = new OnClickListener() {

		@Override
		public void onClick( final View view ) {
			final TabView tabView = (TabView) view;
			TabPageIndicator.this.mViewPager.setCurrentItem( tabView.getIndex() );
		}
	};

	private final LinearLayout mTabLayout;
	Runnable mTabSelector;

	private ViewPager mViewPager;

	public TabPageIndicator( final Context context ) {
		this( context, null );
	}

	public TabPageIndicator( final Context context, final AttributeSet attrs ) {
		super( context, attrs );
		this.setHorizontalScrollBarEnabled( false );

		this.mInflater = LayoutInflater.from( context );

		this.mTabLayout = new LinearLayout( this.getContext() );
		this.addView( this.mTabLayout, new ViewGroup.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT ) );
	}

	private void addTab( final String text, final int index ) {
		// Workaround for not being able to pass a defStyle on pre-3.0
		final TabView tabView = (TabView) this.mInflater.inflate( R.layout.vpi__tab, null );
		tabView.init( this, text, index );
		tabView.setFocusable( true );
		tabView.setOnClickListener( this.mTabClickListener );

		this.mTabLayout.addView( tabView, new LinearLayout.LayoutParams( 0, android.view.ViewGroup.LayoutParams.FILL_PARENT, 1 ) );
	}

	private void animateToTab( final int position ) {
		final View tabView = this.mTabLayout.getChildAt( position );
		if( this.mTabSelector != null ) {
			this.removeCallbacks( this.mTabSelector );
		}
		this.mTabSelector = new Runnable() {

			@Override
			public void run() {
				final int scrollPos = tabView.getLeft() - ((TabPageIndicator.this.getWidth() - tabView.getWidth()) / 2);
				TabPageIndicator.this.smoothScrollTo( scrollPos, 0 );
				TabPageIndicator.this.mTabSelector = null;
			}
		};
		this.post( this.mTabSelector );
	}

	@Override
	public void notifyDataSetChanged() {
		this.mTabLayout.removeAllViews();
		final TitleProvider adapter = (TitleProvider) this.mViewPager.getAdapter();
		final int count = ((PagerAdapter) adapter).getCount();
		for( int i = 0; i < count; i++ ) {
			this.addTab( adapter.getTitle( i ), i );
		}
		if( this.mSelectedTabIndex > count ) {
			this.mSelectedTabIndex = count - 1;
		}
		this.setCurrentItem( this.mSelectedTabIndex );
		this.requestLayout();
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		if( this.mTabSelector != null ) {
			// Re-post the selector we saved
			this.post( this.mTabSelector );
		}
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if( this.mTabSelector != null ) {
			this.removeCallbacks( this.mTabSelector );
		}
	}

	@Override
	public void onMeasure( final int widthMeasureSpec, final int heightMeasureSpec ) {
		final int widthMode = MeasureSpec.getMode( widthMeasureSpec );
		final boolean lockedExpanded = widthMode == MeasureSpec.EXACTLY;
		this.setFillViewport( lockedExpanded );

		final int childCount = this.mTabLayout.getChildCount();
		if( (childCount > 1) && ((widthMode == MeasureSpec.EXACTLY) || (widthMode == MeasureSpec.AT_MOST)) ) {
			if( childCount > 2 ) {
				this.mMaxTabWidth = (int) (MeasureSpec.getSize( widthMeasureSpec ) * 0.4f);
			} else {
				this.mMaxTabWidth = MeasureSpec.getSize( widthMeasureSpec ) / 2;
			}
		} else {
			this.mMaxTabWidth = -1;
		}

		final int oldWidth = this.getMeasuredWidth();
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
		final int newWidth = this.getMeasuredWidth();

		if( lockedExpanded && (oldWidth != newWidth) ) {
			// Recenter the tab display if we're at a new (scrollable) size.
			this.setCurrentItem( this.mSelectedTabIndex );
		}
	}

	@Override
	public void onPageScrolled( final int arg0, final float arg1, final int arg2 ) {
		if( this.mListener != null ) {
			this.mListener.onPageScrolled( arg0, arg1, arg2 );
		}
	}

	@Override
	public void onPageScrollStateChanged( final int arg0 ) {
		if( this.mListener != null ) {
			this.mListener.onPageScrollStateChanged( arg0 );
		}
	}

	@Override
	public void onPageSelected( final int arg0 ) {
		this.setCurrentItem( arg0 );
		if( this.mListener != null ) {
			this.mListener.onPageSelected( arg0 );
		}
	}

	@Override
	public void setCurrentItem( final int item ) {
		if( this.mViewPager == null ) {
			throw new IllegalStateException( "ViewPager has not been bound." );
		}
		this.mSelectedTabIndex = item;
		final int tabCount = this.mTabLayout.getChildCount();
		for( int i = 0; i < tabCount; i++ ) {
			final View child = this.mTabLayout.getChildAt( i );
			final boolean isSelected = (i == item);
			child.setSelected( isSelected );
			if( isSelected ) {
				this.animateToTab( item );
			}
		}
	}

	@Override
	public void setOnPageChangeListener( final OnPageChangeListener listener ) {
		this.mListener = listener;
	}

	@Override
	public void setViewPager( final ViewPager view ) {
		final PagerAdapter adapter = view.getAdapter();
		if( adapter == null ) {
			throw new IllegalStateException( "ViewPager does not have adapter instance." );
		}
		if( !(adapter instanceof TitleProvider) ) {
			throw new IllegalStateException( "ViewPager adapter must implement TitleProvider to be used with TitlePageIndicator." );
		}
		this.mViewPager = view;
		view.setOnPageChangeListener( this );
		this.notifyDataSetChanged();
	}

	@Override
	public void setViewPager( final ViewPager view, final int initialPosition ) {
		this.setViewPager( view );
		this.setCurrentItem( initialPosition );
	}
}

package com.halcyonwaves.apps.backupmyapps;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public final class TestFragment extends Fragment {

	private static final String KEY_CONTENT = "TestFragment:Content";

	public static TestFragment newInstance( final String content ) {
		final TestFragment fragment = new TestFragment();

		final StringBuilder builder = new StringBuilder();
		for( int i = 0; i < 20; i++ ) {
			builder.append( content ).append( " " );
		}
		builder.deleteCharAt( builder.length() - 1 );
		fragment.mContent = builder.toString();

		return fragment;
	}

	private String mContent = "???";

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState ) {
		if( (savedInstanceState != null) && savedInstanceState.containsKey( TestFragment.KEY_CONTENT ) ) {
			this.mContent = savedInstanceState.getString( TestFragment.KEY_CONTENT );
		}

		final TextView text = new TextView( this.getActivity() );
		text.setGravity( Gravity.CENTER );
		text.setText( this.mContent );
		text.setTextSize( 20 * this.getResources().getDisplayMetrics().density );
		text.setPadding( 20, 20, 20, 20 );

		final LinearLayout layout = new LinearLayout( this.getActivity() );
		layout.setLayoutParams( new LayoutParams( android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT ) );
		layout.setGravity( Gravity.CENTER );
		layout.addView( text );

		return layout;
	}

	@Override
	public void onSaveInstanceState( final Bundle outState ) {
		super.onSaveInstanceState( outState );
		outState.putString( TestFragment.KEY_CONTENT, this.mContent );
	}
}
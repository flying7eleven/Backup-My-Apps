package com.halcyonwaves.apps.backupmyapps.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.halcyonwaves.apps.backupmyapps.R;

public class RestoreFragment extends SherlockFragment {

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState ) {
		return inflater.inflate( R.layout.fragment_restore, container, false );
	}
}

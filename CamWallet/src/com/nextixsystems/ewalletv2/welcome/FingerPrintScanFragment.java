package com.nextixsystems.ewalletv2.welcome;

import com.stanlinho.fpewallet.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;

import com.stanlinho.fpewallet.R;

import com.stanlinho.extra.CameraPreview;

public class FingerPrintScanFragment extends Fragment {
	



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_welcome_fpscan, container, false);
		
		return rootView;
	}
	


}
package com.stanlinho.extra;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
        mHolder = getHolder();
        mHolder.addCallback(this);
	}

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
        mHolder = getHolder();
        mHolder.addCallback(this);
	}


	@Override
	public boolean performClick() {
		// TODO Auto-generated method stub
		return super.performClick();
	}

	private SurfaceHolder mHolder;
    private Camera mCamera;
    private static final String TAG = "CAMERA_PREVIEW";
	private boolean isPreviewing;

    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
       // mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
        
    public SurfaceHolder getmHolder() {
		return mHolder;
	}

	public void setmHolder(SurfaceHolder mHolder) {
		this.mHolder = mHolder;
	}

	public Camera getmCamera() {
		return mCamera;
	}

	public void setmCamera(Camera mCamera) {
		this.mCamera = mCamera;
	}

	public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			} catch (IOException e) {
				Log.d(TAG, "Error setting camera preview: " + e.getMessage());
			}
		}
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			mCamera.stopPreview();
			isPreviewing = false;
			mCamera.release();
		}
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null){
	    return;
        }
        if (mCamera == null){
    	    return;
            }
        if (isPreviewing) {
        	try {
        		mCamera.stopPreview();
        	} catch (Exception e) {
        		Log.d(TAG, "Error stopping camera preview: " + e.getMessage());
        	}
        }
		try {
            //Perform any size changes rotations or reformatting here
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            isPreviewing = true;

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}


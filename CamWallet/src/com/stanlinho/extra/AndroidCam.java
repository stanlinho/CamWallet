/**
 * 
 */
package com.stanlinho.extra;

//z
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.Surface;
//-z

/**
 * @author stanlinho
 *
 */
public class AndroidCam {
	//z
	private static final String TAG = "PICTURE_TAKER";
	private Camera camera;
	private List <String> effects;
	private List <Camera.Size> sizes;
	
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	
	public static Camera getPortraitCameraInstance(Activity activity){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	        setCameraDisplayOrientation(activity, CameraInfo.CAMERA_FACING_BACK, c);
	       
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}

	private static void setCameraDisplayOrientation(Activity activity,
	         int cameraId, android.hardware.Camera camera) {
	     android.hardware.Camera.CameraInfo info =
	             new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay()
	             .getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	 }
		Camera.PictureCallback picturecallback = new Camera.PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] arg0, Camera arg1) {
				// TODO Auto-generated method stub
				
			}
		};
		
			//-z

}

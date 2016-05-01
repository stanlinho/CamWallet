package com.nextixsystems.ewalletv2.sessions.helpers;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.stanlinho.fpewallet.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class AppUtils {

	public static void getImageLoader(String url, ImageView imageView) {

		ImageLoader.getInstance().displayImage(
				url,
				imageView,
				new DisplayImageOptions.Builder().resetViewBeforeLoading(true)
						.cacheInMemory(true).cacheOnDisk(true)
						.postProcessor(null)
						.imageScaleType(ImageScaleType.EXACTLY)
						.delayBeforeLoading(2000)
						.bitmapConfig(Bitmap.Config.RGB_565)
						.showImageOnLoading(R.drawable.icon_fundstransfer)
						.considerExifParams(true).build());
	}
}

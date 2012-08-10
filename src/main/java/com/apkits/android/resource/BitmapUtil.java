/**
 * Copyright (C) 2012 ToolkitForAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apkits.android.resource;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * </br><b>name : </b>		BitmapScaleUitl
 * </br><b>description :</b>图片工具。从Android 2.2 版本的BitmapUtil中扣出来的，兼容到1.6版本
 * </br>@author : 			桥下一粒砂
 * </br><b>e-mail : </b>	chenyoca@gmail.com
 * </br><b>weibo : </b>		@桥下一粒砂
 * </br><b>date : </b>		2012-7-18 下午9:37:23
 *
 */
public class BitmapUtil {

	private static final int OPTIONS_NONE = 0x0;
	
	private static final int OPTIONS_SCALE_UP = 0x1;

	public static final int OPTIONS_RECYCLE_INPUT = 0x2;

	/**
	 * <b>description :</b>		将图片切成圆角图
	 * </br><b>time :</b>		2012-8-10 下午10:27:52
	 * @param bitmap			源图片
	 * @param pixels			需要切角的像素大小
	 * @return
	 */
	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {  
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);  
        Canvas canvas = new Canvas(output);  
        final Paint paint = new Paint();  
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
        final RectF rectF = new RectF(rect);  
        final float roundPx = pixels;  
  
        paint.setAntiAlias(true);  
        canvas.drawARGB(0, 0, 0, 0);  
        paint.setColor(Color.TRANSPARENT);  
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
        canvas.drawBitmap(bitmap, rect, rect, paint);  
        return output;  
    }
	
	/**
	 * <b>description :</b>		指定长度宽度进行缩放
	 * </br><b>time :</b>		2012-8-10 下午10:29:24
	 * @param source			源图
	 * @param targetWidth		目标宽度
	 * @param targetHeight		目标高度
	 * @return
	 */
	public static Bitmap extractThumbnail(Bitmap source, int targetWidth, int targetHeight) {
		return extractThumbnail(source, targetWidth, targetHeight, OPTIONS_NONE);
	}

	/**
	 * <b>description :</b>		按比例缩放
	 * </br><b>time :</b>		2012-8-10 下午10:30:27
	 * @param source			源图
	 * @param targetWidth		目标宽度
	 * @param targetHeight		目标高度
	 * @return
	 */
	public static Bitmap prorateThumbnail(Bitmap source, int targetWidth, int targetHeight) {
		if (source == null) {
			return null;
		}
		int srcWidth = source.getWidth();
		int srcHeight = source.getHeight();
		if (srcWidth < srcHeight) {
			targetHeight = srcHeight * targetWidth / srcWidth;
		} else { 
			targetWidth = srcWidth * targetHeight / srcHeight;
		}
		return extractThumbnail(source, targetWidth, targetHeight, OPTIONS_NONE);
	}

	private static Bitmap extractThumbnail(Bitmap source, int targetWidth,int targetHeight, int options) {
		if (source == null) {
			return null;
		}
		float scale;
		if (source.getWidth() < source.getHeight()) {
			scale = targetWidth / (float) source.getWidth();
		} else {
			scale = targetHeight / (float) source.getHeight();
		}
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		Bitmap thumbnail = transform(matrix, source, targetWidth, targetHeight,OPTIONS_SCALE_UP | options);
		return thumbnail;
	}

	/***
	 * Transform source Bitmap to targeted width and height.
	 */
	private static Bitmap transform(Matrix scaler, Bitmap source,
			int targetWidth, int targetHeight, int options) {
		boolean scaleUp = (options & OPTIONS_SCALE_UP) != 0;
		boolean recycle = (options & OPTIONS_RECYCLE_INPUT) != 0;

		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b2);
			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
					+ Math.min(targetWidth, source.getWidth()), deltaYHalf
					+ Math.min(targetHeight, source.getHeight()));
			int dstX = (targetWidth - src.width()) / 2;
			int dstY = (targetHeight - src.height()) / 2;
			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
					- dstY);
			c.drawBitmap(source, src, dst, null);
			if (recycle) {
				source.recycle();
			}
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();

		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect = (float) targetWidth / targetHeight;

		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		} else {
			float scale = targetWidth / bitmapWidthF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		}

		Bitmap b1;
		if (scaler != null) {
			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),source.getHeight(), scaler, true);
		} else {
			b1 = source;
		}
		if (recycle && b1 != source) {
			source.recycle();
		}
		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);
		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth, targetHeight);
		if (b2 != b1) {
			if (recycle || b1 != source) {
				b1.recycle();
			}
		}
		return b2;
	}
}
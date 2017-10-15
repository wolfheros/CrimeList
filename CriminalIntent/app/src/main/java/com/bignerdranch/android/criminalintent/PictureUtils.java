package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

/**
 * Created by james_huker on 9/13/17.
 * 此类使用来解决BitMap类不能够自动缩放位图照片的问题
 */

public class PictureUtils {
    public static Bitmap getScaledBitmap(String path , int destWidth , int destHeight) {
        // Read in the dimensions of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 将inJustDecodeBounds 的值设为true 导致以下的两个方法确定图片宽和高的时候，为输入的原始大小。
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path , options);
        // 设定宽和高都为原始照片的大小
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        // Figure out how much to scale down by
        int inSampleSize =1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcWidth / destHeight);
            }else {
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }
        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        // Read in and create final bitmap
        return BitmapFactory.decodeFile(path , options);
    }
    // 在视图还没有加载时无法得知需要多大的视图，所以编写合理缩放方法进行估算需要多大的视图。
    public static Bitmap getScaledBitmap(String path , Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path , size.x , size.y);
    }
}

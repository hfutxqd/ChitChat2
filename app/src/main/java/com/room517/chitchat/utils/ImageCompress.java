package com.room517.chitchat.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by imxqd on 2016/6/9.
 * 图片压缩
 */
public class ImageCompress {

    public static final int MAX_IMAGE_SIZE = 1024 * 500;//最大500K

    /**
     *
     * @param path The path of temp image file
     * @param context The context of the application
     * @return Path of the temp image file
     */
    public static String compress(String path, Context context)
    {
        Bitmap bitmap = compressImageBySizeAndQuality(path);
        try {
            File dir = new File(context.getFilesDir(), "tmp");
            dir.mkdir();
            File file = new File(path);
            File tmp = new File(dir, file.getName());
            compressToJPEG(bitmap,tmp);
            return tmp.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void cleanTmp(Context context)
    {
        File dir = new File(context.getFilesDir(), "tmp");
        for(File file: dir.listFiles())
        {
            file.delete();
        }
    }

    //通过减少分辨率来压缩图片
    private static Bitmap compressImageBySizeAndQuality(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        options.inJustDecodeBounds = false;
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        Point scr = DisplayUtil.getScreenSize();
        int hh = scr.y;
        int ww = scr.x;
        int size = 1;
        if (outWidth > outHeight && outWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            size = outWidth / ww;
        } else if (outWidth < outHeight && outHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            size = outHeight / hh;
        }

        options.inSampleSize = size;//设置缩放比例
        bitmap = BitmapFactory.decodeFile(path, options);
        return compressBitmapByQuality(bitmap);
    }

    //通过降低质量来压缩图片
    private static Bitmap compressBitmapByQuality(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        int quality = 80;
        while (baos.toByteArray().length > MAX_IMAGE_SIZE) {
            baos.reset();//清空baos
            image.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            quality -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        return BitmapFactory.decodeStream(isBm, null, null);
    }

    /**
     *
     * @param bitmap    The bitmap you want to save.
     * @param filename  The filename you want to save to.
     * @throws IOException  Maybe throws IOException.
     */
    public static void compressToJPEG(Bitmap bitmap, File filename) throws IOException {
        if(!filename.exists())
            filename.createNewFile();
        OutputStream out = new FileOutputStream(filename);
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,out);
        out.close();
    }
}

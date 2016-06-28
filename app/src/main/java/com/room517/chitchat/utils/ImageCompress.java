package com.room517.chitchat.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import com.room517.chitchat.App;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * Created by imxqd on 2016/6/9.
 * 图片压缩
 */
public class ImageCompress {

    public static final int MAX_IMAGE_SIZE = 1024 * 500;//最大500K
    public static final String FILE_PATTERN = "yyyyMMddHHmmssSSS";

    /**
     *
     * @param path The path of temp image file
     * @return Path of the temp image file
     */
    public static String compress(String path)
    {
        Bitmap bitmap = compressImageBySize(path);
        ByteArrayOutputStream out = compressBitmapByQuality(bitmap);
        DateTime time = new DateTime(System.currentTimeMillis());
        File dir = new File(App.getApp().getFilesDir(), "tmp");
        if(dir.isDirectory()){
            dir.mkdir();
        }
        File tmp = new File(dir, time.toString(FILE_PATTERN) + new Random().nextInt(1000) + ".jpg");
        try {
            FileOutputStream outputStream = new FileOutputStream(tmp);
            outputStream.write(out.toByteArray(), 0, out.toByteArray().length);
            return tmp.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void cleanTmp()
    {
        File dir = new File(App.getApp().getFilesDir(), "tmp");
        File[] files = dir.listFiles();
        if(files != null){
            for(File file: files)
            {
                file.delete();
            }
        }
    }

    //通过减少分辨率来压缩图片
    private static Bitmap compressImageBySize(String path) {
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
        return bitmap;
    }

    //通过降低质量来压缩图片
    private static ByteArrayOutputStream compressBitmapByQuality(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int quality = 80;
        while (baos.toByteArray().length > MAX_IMAGE_SIZE) {
            baos.reset();//清空baos
            image.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            quality -= 10;//每次都减少10
        }
        return baos;
    }

    /**
     *
     * @param bitmap    The bitmap you want to save.
     * @param filename  The filename you want to save to.
     * @throws IOException  Maybe throws IOException.
     */
    public static void compressToJPEG(Bitmap bitmap, String filename) throws IOException {
        File file = new File(filename);
        if(!file.exists())
            file.createNewFile();
        OutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100,out);
        out.close();
    }
}

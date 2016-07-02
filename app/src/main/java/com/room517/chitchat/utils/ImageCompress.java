package com.room517.chitchat.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.os.Environment;

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
    private static final String IMAGES_DIR = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
    private static final String FILE_PREFIX = "chitchat_";
    private static final String FILE_PATTERN = "yyyyMMddHHmmssSSS";
    private static final String FILE_SUFFIX = ".jpg";

    /**
     * @param path The path of temp image file
     * @return Path of the temp image file
     */
    public static String compress(String path) {
        Bitmap bitmap = compressImageBySize(path);
        ByteArrayOutputStream out = compressBitmapByQuality(bitmap);
        DateTime time = new DateTime(System.currentTimeMillis());
        File dir = new File(App.getApp().getFilesDir(), "tmp");
        if (!dir.isDirectory()) {
            dir.mkdirs();
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

    /**
     * 清除压缩图片时占用的磁盘缓存
     */
    public static void cleanTmp() {
        File dir = new File(App.getApp().getFilesDir(), "tmp");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    /**
     * 清除分享图片时占用的磁盘文件
     */
    public static void cleanShareTmp() {
        File dir = App.getApp().getExternalFilesDir("tmp");
        File[] files = new File[0];
        if (dir != null) {
            files = dir.listFiles();
        }
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    /**
     * 通过减少分辨率来压缩图片
     *
     * @param path 图片的文件路径
     * @return 压缩过后的Bitmap
     */
    private static Bitmap compressImageBySize(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
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

    /**
     * 通过降低质量来压缩图片
     *
     * @param image 源bitmap
     * @return 压缩过后的ByteArrayOutputStream对象
     */
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
     * @param bitmap   The bitmap you want to save.
     * @param filename The filename you want to save to.
     * @throws IOException Maybe throws IOException.
     */
    public static void compressToJPEG(Bitmap bitmap, String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.close();
    }

    /**
     * 保存文件到本地,用以图库浏览
     *
     * @param bitmap Bitmap对象
     * @return 保存文件的路径
     * @throws IOException
     */
    public static String saveImage(Bitmap bitmap) throws IOException {
        DateTime time = new DateTime(System.currentTimeMillis());
        String filename = FILE_PREFIX + time.toString(FILE_PATTERN) + FILE_SUFFIX;
        ImageCompress.compressToJPEG(bitmap, IMAGES_DIR + File.separator + filename);
        MediaScannerConnection.scanFile(App.getApp(),
                new String[]{IMAGES_DIR + File.separator + filename}
                , null, null);
        return IMAGES_DIR + File.separator + filename;
    }

    /**
     * 保存文件到本地,用以分享
     *
     * @param bitmap Bitmap对象
     * @return 保存文件的路径
     * @throws IOException
     */
    @SuppressWarnings("ConstantConditions")
    public static String saveImageForShare(Bitmap bitmap) throws IOException {
        String dir = App.getApp().getExternalFilesDir("tmp").getPath();
        DateTime time = new DateTime(System.currentTimeMillis());
        String filename = FILE_PREFIX + time.toString(FILE_PATTERN) + FILE_SUFFIX;
        ImageCompress.compressToJPEG(bitmap, dir + File.separator + filename);
        return dir + File.separator + filename;
    }
}

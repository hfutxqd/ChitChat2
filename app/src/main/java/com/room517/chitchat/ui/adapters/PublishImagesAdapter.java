package com.room517.chitchat.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ns.mutiphotochoser.utils.DisplayUtils;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.utils.ImageCompress;
import com.room517.chitchat.utils.JsonUtil;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadFile;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;


/**
 * Created by imxqd on 2016/6/9.
 * 发布动态时显示图片的RecycleView的适配器
 */
public class PublishImagesAdapter extends RecyclerView.Adapter<PublishImagesAdapter.ImagesHolder>{

    private ArrayList<String> mList;
    private Hashtable<String, Integer> mUploadIdTable;
    private Context context;
    private RecyclerView recyclerView;

    private ArrayList<String> mUrls;

    public PublishImagesAdapter(Context context)
    {
        mList = new ArrayList<>();
        mUploadIdTable = new Hashtable<>();
        mUrls = new ArrayList<>();
        this.context = context;
    }

    public void set(ArrayList<String> list)
    {
        UploadService.stopAllUploads();
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void setRecyclerView(RecyclerView recyclerView)
    {
        this.recyclerView = recyclerView;
    }

    public void clear()
    {
        mList.clear();
        notifyDataSetChanged();
    }

    public void stopAll()
    {
        UploadService.stopAllUploads();
    }

    public ArrayList<String> getAll()
    {
        return mList;
    }

    public void upload(UploadCallBack callBack) throws FileNotFoundException, MalformedURLException {
        UploadService.stopAllUploads();
        mUploadIdTable.clear();
        mUrls.clear();
        for(int i = 0; i < mList.size(); i++)
        {
            String file = mList.get(i);
            String uploadId = new MultipartUploadRequest(context, Def.Network.EXPLORE_UPLOAD_URL)
                    .addFileToUpload(file, "image", new File(file).getName())
                    .setNotificationConfig(getNotificationConfig(new File(file).getName()))
                    .startUpload();
            mUploadIdTable.put(uploadId, i);
        }
        this.callBack = callBack;
    }

    private UploadNotificationConfig getNotificationConfig(String filename) {

        return new UploadNotificationConfig()
                .setTitle(filename)
                .setInProgressMessage(context.getString(R.string.uploading))
                .setCompletedMessage(context.getString(R.string.upload_success))
                .setErrorMessage(context.getString(R.string.upload_error))
                .setAutoClearOnSuccess(true)
                .setClearOnAction(true)
                .setRingToneEnabled(true);
    }

    public final UploadServiceBroadcastReceiver uploadReceiver =
            new UploadServiceBroadcastReceiver() {
                @Override
                public void onProgress(String uploadId, int progress) {
                    int pos = mUploadIdTable.get(uploadId);
                    ImagesHolder holder = (ImagesHolder)
                            recyclerView.getChildViewHolder(recyclerView.getChildAt(pos));
                    holder.cover.setVisibility(View.VISIBLE);
                    holder.progress.setVisibility(View.VISIBLE);
                    holder.progress.setProgress(progress);
                    holder.result.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError(String uploadId, Exception exception) {
                    int pos = mUploadIdTable.get(uploadId);
                    ImagesHolder holder = (ImagesHolder)
                            recyclerView.getChildViewHolder(recyclerView.getChildAt(pos));
                    holder.cover.setVisibility(View.VISIBLE);
                    holder.progress.setVisibility(View.INVISIBLE);
                    holder.result.setImageDrawable(context.getResources()
                            .getDrawable(R.drawable.ic_retry_red_400_36dp));
                    holder.result.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCompleted(String uploadId, int serverResponseCode, byte[] serverResponseBody) {
                    try {
                        int pos = mUploadIdTable.get(uploadId);
                        String s = new String(serverResponseBody, "UTF-8");
                        ImagesHolder holder = (ImagesHolder)
                                recyclerView.getChildViewHolder(recyclerView.getChildAt(pos));
                        holder.cover.setVisibility(View.VISIBLE);
                        holder.progress.setVisibility(View.INVISIBLE);
                        holder.result.setVisibility(View.VISIBLE);
                        if(JsonUtil.getParam(s,"success").getAsBoolean())
                        {
                            mUploadIdTable.remove(uploadId);
                            holder.result.setImageDrawable(context.getResources()
                                    .getDrawable(R.drawable.ic_done_teal_500_36dp));

                            String url = JsonUtil.getParam(s,"url").getAsString();
                            mUrls.add(url);
                            if(mUploadIdTable.size() == 0)
                            {
                                callBack.onSuccess(mUrls);
                            }
                        }else {
                            holder.result.setImageDrawable(context.getResources()
                                    .getDrawable(R.drawable.ic_error_red_400_36dp));
                        }

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(String uploadId) {

                }
            };

    @Override
    public ImagesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImagesHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.publish_image_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ImagesHolder holder, int position) {
        ImageView imageView = holder.image;
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(DisplayUtils.dip2px(120,
                imageView.getContext()), DisplayUtils.dip2px(120, imageView.getContext()));
        imageView.setLayoutParams(lp);
        ImageLoader.getInstance().displayImage("file://" + mList.get(position), imageView);
    }



    @Override
    public int getItemCount() {
        return mList.size();
    }

    private UploadCallBack callBack = null;
    public interface UploadCallBack{
        void onSuccess(ArrayList<String> urls);
    }

    public class ImagesHolder extends RecyclerView.ViewHolder{
        public ImageView image, result;
        public View cover;
        public ProgressBar progress;
        public ImagesHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.publish_image);
            result = (ImageView) itemView.findViewById(R.id.publish_image_upload_result);
            cover = itemView.findViewById(R.id.publish_cover);
            progress = (ProgressBar) itemView.findViewById(R.id.publish_progress);
        }
    }
}

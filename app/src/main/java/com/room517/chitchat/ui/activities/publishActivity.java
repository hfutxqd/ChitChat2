package com.room517.chitchat.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.room517.chitchat.App;
import com.room517.chitchat.R;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.ExploreService;
import com.room517.chitchat.model.Explore;
import com.room517.chitchat.ui.adapters.PublishImagesAdapter;
import com.room517.chitchat.utils.ImageCompress;
import com.room517.chitchat.utils.JsonUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import xyz.imxqd.photochooser.constant.Constant;


public class PublishActivity extends BaseActivity {

    private FloatingActionButton mFab;
    private Toolbar mToolbar;
    private RecyclerView mImagesList;
    private EditText mText;

    private PublishImagesAdapter mAdapter;

    private static final int REQUEST_PICK_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        super.init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.publish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_publish:
                publish();
                break;
        }
        return true;
    }


    @Override
    protected void onResume() {
        mAdapter.uploadReceiver.register(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mAdapter.uploadReceiver.unregister(this);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_PICK_PHOTO:
                final ArrayList<String> images = data.getStringArrayListExtra(Constant.EXTRA_PHOTO_PATHS);
                final ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.publish_waiting)
                        , getString(R.string.publish_compress));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final ArrayList<String> tmpImages = new ArrayList<>();
                        for (String src : images) {
                            tmpImages.add(ImageCompress.compress(src));
                        }
                        PublishActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.set(tmpImages);
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();

                break;
        }

    }

    @Override
    public void onBackPressed() {
        if (mAdapter.getAll().size() > 0 || mText.getText().length() > 0) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.publish_exit_title)
                    .setMessage(R.string.publish_exit_message)
                    .setPositiveButton(R.string.act_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAdapter.stopAll();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.act_cancel, null)
                    .show();
        } else {
            finish();
        }
    }

    private void publish() {
        try {
            mAdapter.upload(new PublishImagesAdapter.UploadCallBack() {
                @Override
                public void onSuccess(ArrayList<String> urls) {
                    if (urls == null) {
                        urls = new ArrayList<>();
                    }
                    String[] urlArr = new String[urls.size()];
                    urls.toArray(urlArr);
                    if (urlArr.length == 0 && mText.getText().toString().trim().length() == 0) {
                        return;
                    }
                    String text = mText.getText().toString();
                    Retrofit retrofit = RetrofitHelper.getExploreUrlRetrofit();
                    ExploreService service = retrofit.create(ExploreService.class);
                    Explore explore = new Explore();
                    explore.setColor(App.getMe().getColor());
                    explore.setNickname(App.getMe().getName());
                    explore.setDevice_id(App.getMe().getId());
                    explore.setContent(new Explore.Content(text, urlArr));
                    RxHelper.ioMain(service.publish(explore), new SimpleObserver<ResponseBody>() {

                        @Override
                        public void onError(Throwable throwable) {
                            super.onError(throwable);
                            throwable.printStackTrace();
                            Toast.makeText(PublishActivity.this, R.string.publish_error, Toast.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void onNext(ResponseBody body) {
                            try {
                                String json = body.string();
                                if (JsonUtil.getParam(json, "success").getAsBoolean()) {
                                    Toast.makeText(PublishActivity.this, R.string.publish_sucess
                                            , Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(PublishActivity.this, R.string.publish_error, Toast.LENGTH_SHORT)
                                        .show();
                            }

                        }
                    });
                }
            });
        } catch (FileNotFoundException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initMember() {
        mAdapter = new PublishImagesAdapter(this);
    }

    @Override
    protected void findViews() {
        mToolbar = f(R.id.toolbar);
        mFab = f(R.id.fab);
        mImagesList = f(R.id.publish_images_list);
        mText = f(R.id.publish_text);
    }

    @Override
    protected void initUI() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mImagesList.setAdapter(mAdapter);
        mAdapter.setRecyclerView(mImagesList);
        mImagesList.setLayoutManager(new GridLayoutManager(this, 3));
    }

    @Override
    protected void setupEvents() {
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.room517.chitchat.action.CHOSE_PHOTOS");
                intent.putExtra(Constant.EXTRA_PHOTO_LIMIT, 9);
                startActivityForResult(intent, REQUEST_PICK_PHOTO);
            }
        });
    }

    @Override
    protected void onDestroy() {
        ImageCompress.cleanTmp();
        super.onDestroy();
    }
}

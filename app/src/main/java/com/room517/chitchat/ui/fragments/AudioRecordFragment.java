package com.room517.chitchat.ui.fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.model.AudioInfo;
import com.room517.chitchat.ui.views.recording.AudioRecorder;
import com.room517.chitchat.ui.views.recording.VoiceVisualizer;

import java.io.File;

/**
 * Created by ywwynm on 2016/7/8.
 * 记录声音的Fragment
 */
public class AudioRecordFragment extends BaseFragment {

    public static final int ANIM_DURATION = 360;

    public static AudioRecordFragment newInstance(int accentColor) {
        Bundle args = new Bundle();
        args.putInt(Def.Key.COLOR, accentColor);
        AudioRecordFragment fragment = new AudioRecordFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private VoiceVisualizer mVisualizer;

    private AudioRecorder mRecorder;

    private CardView mCvAsBt;
    private TextView mTv;

    private int mAccentColor;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecorder.stopListening(false);
        mRecorder.release();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_audio_record;
    }

    @Override
    protected void initMember() {
        mAccentColor = getArguments().getInt(Def.Key.COLOR);
        mRecorder = new AudioRecorder();
    }

    @Override
    protected void findViews() {
        mVisualizer = f(R.id.voice_visualizer);

        mCvAsBt = f(R.id.cv_audio_record_as_bt);
        mTv     = f(R.id.tv_audio_record);
    }

    @Override
    protected void initUI() {
        initCardAppearance();

        mVisualizer.setRenderColor(mAccentColor);

        mRecorder.link(mVisualizer);
        mRecorder.startListening();
    }

    private void initCardAppearance() {
        mCvAsBt.setCardElevation(4);

        int[] colors = getResources().getIntArray(R.array.material_500);
        int index = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == mAccentColor) {
                index = i;
                break;
            }
        }

        Activity activity = getActivity();
        if (index >= 11 && index <= 13) {
            mCvAsBt.setCardBackgroundColor(
                    ContextCompat.getColor(activity, R.color.app_purple));
        } else {
            mCvAsBt.setCardBackgroundColor(
                    ContextCompat.getColor(activity, R.color.app_orange));
        }
    }

    @Override
    protected void setupEvents() {
        mCvAsBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int act = event.getAction();
                System.out.println(act);
                if (act == MotionEvent.ACTION_DOWN) {
                    ObjectAnimator.ofFloat(mCvAsBt, "CardElevation", 8).setDuration(96).start();
                    startRecording();
                } else if (act == MotionEvent.ACTION_UP) {
                    ObjectAnimator.ofFloat(mCvAsBt, "CardElevation", 4).setDuration(96).start();
                    stopRecording();
                }
                return true;
            }
        });
    }

    private void startRecording() {
        mRecorder.startRecording();
        updateUiElements(true);
    }

    private void stopRecording() {
        mRecorder.stopRecording();
        mRecorder.saveToWaveFile();

        File wavFile = mRecorder.getSavedFile();
        Uri uri = Uri.fromFile(wavFile);
        MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), uri);
        int duration = mediaPlayer.getDuration();
        mediaPlayer.release();

        int maxDecibel     = mRecorder.getMaxDecibel();
        int averageDecibel = mRecorder.getAverageDecibel();

        AudioInfo audioInfo = new AudioInfo(
                uri.toString(), duration, maxDecibel, averageDecibel);
        RxBus.get().post(Def.Event.AUDIO_RECORDED, audioInfo);

        mRecorder.startAnotherListening();
        updateUiElements(false);
    }

    private void updateUiElements(boolean toRecording) {
        mVisualizer.animate().alpha(toRecording ? 1.0f : 0.06f).setDuration(ANIM_DURATION);

        mTv.setText(toRecording ? R.string.act_release_to_stop : R.string.act_hold_and_speak);
    }
}

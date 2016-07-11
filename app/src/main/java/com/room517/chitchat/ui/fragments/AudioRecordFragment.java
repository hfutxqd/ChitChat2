package com.room517.chitchat.ui.fragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hwangjr.rxbus.RxBus;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.model.AudioInfo;
import com.room517.chitchat.ui.views.recording.AudioRecorder;
import com.room517.chitchat.ui.views.recording.VoiceVisualizer;
import com.room517.chitchat.utils.DisplayUtil;

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

    private Button mBt;

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

        mBt = f(R.id.bt_audio_record);
    }

    @Override
    protected void initUI() {
        mVisualizer.setRenderColor(DisplayUtil.getLightColor(mAccentColor));

        mRecorder.link(mVisualizer);
        mRecorder.startListening();
    }

    @Override
    protected void setupEvents() {
        mBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecorder.isRecording()) {
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
                } else if (mRecorder.isListening()) {
                    mRecorder.startRecording();
                    updateUiElements(true);
                }
            }
        });
    }

    private void updateUiElements(boolean toRecording) {
        mVisualizer.animate().alpha(toRecording ? 1.0f : 0.16f).setDuration(ANIM_DURATION);
    }
}

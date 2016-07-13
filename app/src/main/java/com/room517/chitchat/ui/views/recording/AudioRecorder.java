package com.room517.chitchat.ui.views.recording;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.orhanobut.logger.Logger;
import com.room517.chitchat.Def;
import com.room517.chitchat.utils.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import io.kvh.media.amr.AmrEncoder;

/**
 * Created by tyorikan on 2015/06/09.
 * Updated by ywwynm on 2015/10/02 to meet requirements.
 *
 * Sampling AudioRecord Input
 * This output send to {@link VoiceVisualizer}
 */
public class AudioRecorder {

    // 转码限制，采样率必须为 8000Hz，否则无法正确地转换为 AMR 格式
    private static final int RECORDING_SAMPLE_RATE = 8000;

    private int mSamplingInterval = 100;

    private AudioRecord mAudioRecord;

    private int mBuffSize;

    private List<VoiceVisualizer> mVoiceVisualizers = new ArrayList<>();

    private boolean mIsListening;
    private boolean mIsRecording;
    private boolean mStartAnotherListening = false;

    private File mPcmFile;
    private File mAmrFile;

    private int mMaxDecibel = Integer.MIN_VALUE;
    private int mDecibelSum = 0;
    private int mRecordTimes = 0;

    public AudioRecorder() {
        initAudioRecord();
    }

    /**
     * link to VisualizerView
     *
     * @param voiceVisualizer {@link VoiceVisualizer}
     */
    public void link(VoiceVisualizer voiceVisualizer) {
        mVoiceVisualizers.add(voiceVisualizer);
    }

    /**
     * setter of samplingInterval
     *
     * @param samplingInterval interval volume sampling
     */
    public void setSamplingInterval(int samplingInterval) {
        mSamplingInterval = samplingInterval;
    }

    /**
     * getter isListening
     *
     * @return true:recording, false:not recording
     */
    public boolean isListening() {
        return mIsListening;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    private void initAudioRecord() {
        int buffSize = AudioRecord.getMinBufferSize(
                RECORDING_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, /* 必须使用单声道，否则无法得到正确的音频效果 */
                AudioFormat.ENCODING_PCM_16BIT
        );

        mAudioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RECORDING_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, /* 必须使用单声道，否则无法得到正确的音频效果 */
                AudioFormat.ENCODING_PCM_16BIT,
                buffSize
        );

        if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            mBuffSize = buffSize;
        }
    }

    /**
     * start AudioRecord.read
     */
    public void startListening() {
        String fileName = FileUtil.newSimpleFileName() + ".pcm";
        mPcmFile = FileUtil.createFile(Def.Meta.APP_DIR + "/tmpAudio", fileName);
        if (mPcmFile == null) {
            Logger.e("mPcmFile is null in startListening.");
            return;
        }

        mIsListening = true;
        mAudioRecord.startRecording();
        new RecordingThread().start();
    }

    /**
     * stop AudioRecord.read
     */
    public void stopListening(boolean saveFile) {
        mIsListening = false;
        mIsRecording = false;

        if (saveFile) {
            saveToAmrFile();
        }

        if (mVoiceVisualizers != null && !mVoiceVisualizers.isEmpty()) {
            for (int i = 0; i < mVoiceVisualizers.size(); i++) {
                mVoiceVisualizers.get(i).receive(0);
            }
        }

        FileUtil.deleteDirectory(Def.Meta.APP_DIR + "/tmpAudio");
    }

    public void startRecording() {
        String fileName = FileUtil.newSimpleFileName() + ".amr";
        mAmrFile = FileUtil.createFile(Def.Meta.APP_DIR + "/audio", fileName);
        if (mAmrFile == null) {
            Logger.e("mAmrFile is null in startListening.");
            return;
        }
        mIsRecording = true;
    }

    public void stopRecording() {
        mIsRecording = false;
    }

    public void startAnotherListening() {
        String fileName = FileUtil.newSimpleFileName() + ".pcm";
        mPcmFile = FileUtil.createFile(Def.Meta.APP_DIR + "/tmpAudio", fileName);
        if (mPcmFile == null) {
            Logger.e("mPcmFile is null in startAnotherListening.");
            return;
        }

        mMaxDecibel = Integer.MIN_VALUE;
        mDecibelSum = 0;
        mRecordTimes = 0;

        mStartAnotherListening = true;
    }

    public File getSavedAmrFile() {
        return mAmrFile;
    }

    public int getMaxDecibel() {
        return mMaxDecibel;
    }

    public int getAverageDecibel() {
        return mDecibelSum / mRecordTimes;
    }

    public void saveToAmrFile() {
        try {
            AmrEncoder.init(0);
            List<short[]> armsList = new ArrayList<>();
            FileInputStream  fis = new FileInputStream (mPcmFile);
            FileOutputStream fos = new FileOutputStream(mAmrFile);

            final byte[] header = new byte[] { 0x23, 0x21, 0x41, 0x4D, 0x52, 0x0A };
            fos.write(header);

            int byteSize = 320;
            byte[] buff = new byte[byteSize];
            while (fis.read(buff, 0, byteSize) > 0) {
                short[] shortTemp = new short[160];
                ByteBuffer.wrap(buff).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortTemp);
                armsList.add(shortTemp);
            }

            for (int i = 0; i < armsList.size(); i++) {
                int size = armsList.get(i).length;
                byte[] encodedData = new byte[size * 2];
                int len = AmrEncoder.encode(
                        AmrEncoder.Mode.MR122.ordinal(), armsList.get(i), encodedData);
                if (len > 0) {
                    byte[] tempBuf = new byte[len];
                    System.arraycopy(encodedData, 0, tempBuf, 0, len);
                    fos.write(tempBuf, 0, len);
                }
            }

            AmrEncoder.exit();

            fos.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * release member object
     */
    public void release() {
        mAudioRecord.release();
        mAudioRecord = null;
    }

    class RecordingThread extends Thread {

        long time = System.currentTimeMillis();

        @Override
        public void run() {
            FileOutputStream fos = getRecordingStream();

            int readSize;
            byte[] audioBytes = new byte[mBuffSize];
            while (mIsListening) {
                readSize  = mAudioRecord.read(audioBytes, 0, mBuffSize);

                if (System.currentTimeMillis() - time >= mSamplingInterval) {
                    int decibel = calculateDecibel(audioBytes, readSize);
                    System.out.println(decibel);

                    if (mIsListening) {
                        mDecibelSum += decibel;
                        mRecordTimes++;
                        if (decibel > mMaxDecibel) {
                            mMaxDecibel = decibel;
                        }
                    }

                    if (mVoiceVisualizers != null && !mVoiceVisualizers.isEmpty()) {
                        for (int i = 0; i < mVoiceVisualizers.size(); i++) {
                            mVoiceVisualizers.get(i).receive(decibel);
                        }
                    }
                    time = System.currentTimeMillis();
                }

                if (mStartAnotherListening) {
                    releaseStream(fos);
                    fos = getRecordingStream();
                    mStartAnotherListening = false;
                }

                if (mIsRecording) {
                    if (fos != null && readSize != AudioRecord.ERROR_INVALID_OPERATION) {
                        try {
                            fos.write(audioBytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            releaseStream(fos);
        }

        private FileOutputStream getRecordingStream() {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mPcmFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return fos;
        }

        private void releaseStream(FileOutputStream fos) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private int calculateDecibel(byte[] buf, int byteReadSize) {
            if (byteReadSize == 0) {
                return 0;
            }

            long sum = 0;
            for (int i = 0; i < buf.length / 2; i++) {
                short data = (short) ((buf[i * 2] & 0xff) | (buf[i * 2 + 1] << 8));
                sum += data * data;
            }

            double amplitude = sum / (double) (byteReadSize / 2); // 振幅
            double decibel = 10 * Math.log10(amplitude);

            System.out.println("decibel:" + decibel);
            return (int) decibel;
        }
    }
}

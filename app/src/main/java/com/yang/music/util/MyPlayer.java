package com.yang.music.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * 音乐播放类
 * Created by yang on 2017/6/10.
 */

public class MyPlayer {

    //索引
    public static final int INDEX_STONE_ENTER = 0;
    public static final int INDEX_STONE_CANCEL = 1;
    public static final int INDEX_STONE_COIN = 2;

    private static final String[] SONG_NAMES = {"enter.mp3", "cancel.mp3", "coin.mp3"};

    //音效
    private static MediaPlayer[] mTonePlayer = new MediaPlayer[SONG_NAMES.length];

    private static MediaPlayer mMusicPlayer;

    /**播放音效
     * @param context
     * @param index
     */
    public static void playTone(Context context, int index) {
        //加载音效文件
        AssetManager manager = context.getAssets();

        if (mTonePlayer[index] == null){
            mTonePlayer[index] = new MediaPlayer();

            try {
                AssetFileDescriptor fileDescriptor = manager.openFd(SONG_NAMES[index]);
                mTonePlayer[index].setDataSource(fileDescriptor.getFileDescriptor(),
                        fileDescriptor.getStartOffset(),fileDescriptor.getLength());
                mTonePlayer[index].prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mTonePlayer[index].start();
    }

    /**
     * 播放歌曲
     *
     * @param context
     * @param fileName
     */
    public static void playSong(Context context, String fileName) {
        if (mMusicPlayer == null) {
            mMusicPlayer = new MediaPlayer();
        }

        //强制重置
        mMusicPlayer.reset();

        //加载音乐文件
        AssetManager assetManager = context.getAssets();
        try {
            AssetFileDescriptor fileDescriptor = assetManager.openFd(fileName);
            mMusicPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mMusicPlayer.prepare();
            //播放
            mMusicPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     *
     * @param context
     */
    public static void stopTheSong(Context context) {
        if (mMusicPlayer != null) {
            mMusicPlayer.stop();
        }
    }
}

package com.example.triviaapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private final IBinder binder = new MusicServiceBinder();

    public class MusicServiceBinder extends Binder {
        MusicService getService() {

            return MusicService.this;
        }
    }




    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void toggleMute() {
        // Toggle the mute state
        isMuted = !isMuted;

        // Update shared preferences
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isMuted", isMuted);
        editor.apply();

        // Update the mute state in the MediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(isMuted ? 0 : 1, isMuted ? 0 : 1);
        }
    }


    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }





    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.song);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }





    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}

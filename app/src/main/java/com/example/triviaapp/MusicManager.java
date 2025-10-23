package com.example.triviaapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MusicManager {


    private static MusicManager instance;
    private MusicService musicService;
    private boolean musicServiceBound = false;

    private MusicManager() {

    }

    public static synchronized MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void init(Context context) {
        Intent intent = new Intent(context, MusicService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void release(Context context) {

        if (musicServiceBound) {
            context.unbindService(serviceConnection);
            musicServiceBound = false;
        }
    }

    public void play() {
        if (musicServiceBound && musicService != null) {
            musicService.play();
        }
    }

    public void pause() {
        if (musicServiceBound && musicService != null) {
            musicService.pause();
        }
    }



    public void toggleMute() {

        if (musicServiceBound && musicService != null) {
            musicService.toggleMute();
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) service;
            musicService = binder.getService();
            musicServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            musicServiceBound = false;
        }
    };
}

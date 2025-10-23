package com.example.triviaapp;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameModeActivity extends MainMenuActivity {
    private int soundId;
    private MusicManager musicManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_mode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_select), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        SoundPool soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        soundId = soundPool.load(this, R.raw.button_pressed, 1);


        Intent intent = new Intent(GameModeActivity.this, Game.class);

        Button standardButton = findViewById(R.id.button_standard);
        standardButton.setOnClickListener(v -> {
            soundPool.play(soundId, 1, 1, 0, 0, 1);
            intent.putExtra("GAME_MODE", "STANDARD");
            startActivity(intent);

        });

        Button blitzButton = findViewById(R.id.button_blitz);
        blitzButton.setOnClickListener(v -> {
            soundPool.play(soundId, 1, 1, 0, 0, 1);
            intent.putExtra("GAME_MODE", "BLITZ");
            startActivity(intent);
        });



    }


    @Override
    protected void onPause() {
        super.onPause();

        MusicManager.getInstance().pause();

    }

    @Override
    protected void onResume() {
        super.onResume();
  MusicManager.getInstance().play();

    }

}
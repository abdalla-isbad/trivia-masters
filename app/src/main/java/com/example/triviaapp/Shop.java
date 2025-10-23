package com.example.triviaapp;

import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;
import java.util.Map;

public class Shop extends MainMenuActivity {
    SoundPool soundPool;
    private TextView cashTextView;
    private PlayerDataManager playerDataManager;
    private PlayerInfo currentPlayerInfo;
    private int purchase;
    private Button buySkipButton,buyDoubleButton,buyOmitButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.shop), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        buyDoubleButton = findViewById(R.id.buyDoubleButton);
        buyOmitButton = findViewById(R.id.buyOmitButton);
        buySkipButton = findViewById(R.id.buySkipButton);
        purchase = soundPool.load(this, R.raw.purchase, 1);

        playerDataManager = new PlayerDataManager(this);
        cashTextView = findViewById(R.id.cashCounterTextView);
        playerDataManager.fetchPlayerInfo(playerInfo -> {
            currentPlayerInfo = playerInfo;
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                currentPlayerInfo = playerDataManager.loadLocalPlayerInfo();
            }
            cashTextView.setText(String.format(Locale.getDefault(), "%d", playerInfo.getCash()));

            initPowerUpButtons(buyDoubleButton,25);
            initPowerUpButtons(buyOmitButton,50);
            initPowerUpButtons(buySkipButton,75);


        });




        buyDoubleButton.setOnClickListener(v -> {
            purchasePowerUp(buyDoubleButton,25);
            initPowerUpButtons(buyDoubleButton,25);

        });

        buySkipButton.setOnClickListener(v -> {
            purchasePowerUp(buySkipButton,75);
            initPowerUpButtons(buySkipButton,75);

        });

        buyOmitButton.setOnClickListener(v -> {
            purchasePowerUp(buyOmitButton,50);
            initPowerUpButtons(buyOmitButton,50);

        });
    }




    private void initPowerUpButtons(Button powerUpButton,int price){
        int playerCash = currentPlayerInfo.getCash();
        Map<String, Integer> playerPowerUps = currentPlayerInfo.getPowerUps();
        String powerUpName = (String) powerUpButton.getTag();
        Integer powerUpCount = playerPowerUps.get(powerUpName);
        String buttonText = powerUpButton.getText().toString();

        if (powerUpCount != null) {
            String newVal = "("+powerUpCount.toString()+")";
            String finalString = buttonText.replaceAll("\\(.*?\\)", newVal);
            powerUpButton.setEnabled(powerUpCount < 10 && playerCash >= price);
            if(powerUpCount == 10 || playerCash < price){
                powerUpButton.setBackgroundColor(Color.DKGRAY);
            } else {
                powerUpButton.setBackgroundColor(Color.parseColor("#E67E22"));
            }
            powerUpButton.setText(finalString);
        } else {
            powerUpButton.setEnabled(false);
        }
    }

    private void purchasePowerUp(Button powerUpButton,int price){
        if(currentPlayerInfo.getCash()>=price) {
            String powerUpName = (String) powerUpButton.getTag();
            Integer powerUpCount = currentPlayerInfo.getPowerUps().get(powerUpName);

            if (powerUpCount != null) {

                currentPlayerInfo.updatePowerUp(powerUpName, 1);
                currentPlayerInfo.addCash(-price);
                soundPool.play(purchase, 1, 1, 0, 0, 1);
                // Then update the button text
                powerUpCount = currentPlayerInfo.getPowerUps().get(powerUpName);
                String buttonText = powerUpButton.getText().toString();
                String newVal = "("+powerUpCount.toString()+")";
                String finalString = buttonText.replaceAll("\\(.*?\\)", newVal);
                powerUpButton.setText(finalString);

                // Update cash text view
                cashTextView.setText(String.format(Locale.getDefault(), "%d", currentPlayerInfo.getCash()));
                cashTextView.setTextColor(Color.RED);
                new Handler().postDelayed(() -> {
                    cashTextView.setTextColor(Color.WHITE);
                }, 450);

                initPowerUpButtons(powerUpButton, price);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {

                    playerDataManager.savePlayerInfo(currentPlayerInfo, new PlayerDataManager.SavePlayerInfoCallback() {
                        @Override
                        public void onCallback() {
                            playerDataManager.fetchPlayerInfo(playerInfo -> {
                                currentPlayerInfo = playerInfo;
                                initPowerUpButtons(powerUpButton, price);
                            });
                        }
                    });
                } else {

                    playerDataManager.saveLocalPlayerInfo(currentPlayerInfo);
                    currentPlayerInfo = playerDataManager.loadLocalPlayerInfo();
                    initPowerUpButtons(powerUpButton, price);
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        soundPool.autoPause();
        MusicManager.getInstance().pause();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the SoundPool resources
        soundPool.release();
        soundPool = null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.getInstance().play();

    }


}
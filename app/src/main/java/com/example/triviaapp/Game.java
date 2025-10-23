package com.example.triviaapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


public class Game extends GameModeActivity {
    private List<Question> questions;
    private TextView questionText;
    private Button answerButton1, answerButton2, answerButton3;
    private ImageButton skipButton,dblButton,omitButton;
    private ImageView imageSrc;
    private int currentQuestionIndex,highScore,correct,highestCombo,
            wrong,buttonPress,gameOver, omit,skip,dbl,currentCombo,
            cash,score,blitzModifier,startingCash,totalCashEarned = 0;

    private SoundPool soundPool;
    private PlayerDataManager playerDataManager;
    private Gson gson;
    private TextView cashTextView,scoreTextView,skipTextView,doubleTextView,omitTextView, comboTextView;

    private CountDownTimer gameTimer;
    private PlayerInfo currentPlayerInfo;
    private long timeLeft;
    private boolean isBlitz = false;
    private Random random;
    private boolean isOmit,isDouble,isLocal = false;
    int doublePoints=1;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        random = new Random();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();
        buttonPress = soundPool.load(this, R.raw.button_pressed, 1);
        highScore = soundPool.load(this, R.raw.high_score, 1);
        correct = soundPool.load(this, R.raw.correct, 1);
        wrong = soundPool.load(this, R.raw.wrong, 1);
        gameOver = soundPool.load(this, R.raw.game_over, 1);

        cashTextView = findViewById(R.id.cashCounterTextView);
        scoreTextView = findViewById(R.id.scoreCounterTextView);
        playerDataManager = new PlayerDataManager(this);
        sharedPreferences = getSharedPreferences("PlayerInfo", MODE_PRIVATE);


        skipButton = findViewById(R.id.skipButton);
        dblButton = findViewById(R.id.doubleButton);
        omitButton = findViewById(R.id.omitButton);

        skipTextView = findViewById(R.id.skipCountTextView);
        doubleTextView = findViewById(R.id.doubleCountTextView);
        omitTextView = findViewById(R.id.omitCountTextView);

        gson = new Gson();

        questionText = findViewById(R.id.questionTextView);
        answerButton1 = findViewById(R.id.answerButton1);
        answerButton2 = findViewById(R.id.answerButton2);
        answerButton3 = findViewById(R.id.answerButton3);
        imageSrc = findViewById(R.id.questionImageView);



        playerDataManager.fetchPlayerInfo(playerInfo -> {

            currentPlayerInfo = playerInfo;

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                currentPlayerInfo = playerDataManager.loadLocalPlayerInfo();
                isLocal=true;
            }

            startingCash = currentPlayerInfo.getCash();
            highestCombo=currentPlayerInfo.getHighestCombo();
            cashTextView.setText(String.format(Locale.getDefault(), "%d", startingCash));
            Map<String, Integer> playerPowerUps = currentPlayerInfo.getPowerUps();
            skip = currentPlayerInfo.getPowerUps().getOrDefault("skip_question", 0);
            dbl= currentPlayerInfo.getPowerUps().getOrDefault("double_points", 0);
            omit = currentPlayerInfo.getPowerUps().getOrDefault("omit_wrong_answer", 0);

            initCombo();
            Questions questionsWrapper = new Questions(this);
            this.questions = questionsWrapper.getQuestions();
            Collections.shuffle(this.questions);
            updateUI();

            loadQuestion(0);

        });



        answerButton1.setOnClickListener(v -> checkAnswer(answerButton1.getText().toString(), answerButton1));
        answerButton2.setOnClickListener(v -> checkAnswer(answerButton2.getText().toString(), answerButton2));
        answerButton3.setOnClickListener(v -> checkAnswer(answerButton3.getText().toString(), answerButton3));


        skipButton.setOnClickListener(v -> {

            if (currentPlayerInfo.getPowerUps().getOrDefault("skip_question", 0) > 0) {
                usePowerUp("skip_question");
                nextQuestion();
            }
        });


        dblButton.setOnClickListener(v -> {
            if (currentPlayerInfo.getPowerUps().getOrDefault("double_points", 0) > 0) {
                isDouble = true;
                usePowerUp("double_points");

                dblButton.setEnabled(false);
            }
        });






        omitButton.setOnClickListener(v -> {
            if (currentPlayerInfo.getPowerUps().getOrDefault("omit_wrong_answer", 0) > 0) {
                isOmit = true;
                usePowerUp("omit_wrong_answer");
                loadQuestion(currentQuestionIndex);

                omitButton.setEnabled(false);
            }
        });



        String gameMode = getIntent().getStringExtra("GAME_MODE");
        if ("STANDARD".equals(gameMode)) {
            timeLeft = 60000;
        } else if ("BLITZ".equals(gameMode)) {
            timeLeft = 5000;
            isBlitz = true;
        }



        startTimer();

    }

    private void startTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        gameTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {

                if (isBlitz && currentQuestionIndex < questions.size() - 1)
                {
                    timeLeft=5000;
                    score-=10;
                    playerDataManager.updateQuestionStats(questions.get(currentQuestionIndex).getCategory(),false);
                    scoreTextView.setText(String.format(Locale.getDefault(), "Score: %d", score));
                    soundPool.play(wrong, 1, 1, 0, 0, 1);
                    loadQuestion(currentQuestionIndex++);
                    startTimer();

                }
                else{
                    gameOver();
                }

            }
        }.start();
    }


    private void updateTimerText() {
        TextView timerTextView = findViewById(R.id.timerTextView);
        int seconds = (int) (timeLeft / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d", seconds);
        timerTextView.setText(timeFormatted);
    }

    private void gameOver() {

        gameTimer.cancel();
        playerDataManager.updatePlayerScore(score,highestCombo);
        final Dialog gameOverDialog = new Dialog(this);
        gameOverDialog.setContentView(R.layout.dialog_game_over);
        TextView highScoreTextView = gameOverDialog.findViewById(R.id.highScoreTextView);
        gameOverDialog.setCanceledOnTouchOutside(false);
        soundPool.play(gameOver, 1, 1, 0, 0, 1);
        gameOverDialog.show();
        if (score >currentPlayerInfo.highScore){
            highScoreTextView.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> {
                // Play the sound after the delay
                soundPool.play(highScore, 1, 1, 0, 0, 1);
            }, 400);
            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                    highScoreTextView,
                    PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.5f, 1.0f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.5f, 1.0f)
            );
            animator.setDuration(1000);
            animator.setRepeatCount(Animation.INFINITE);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.start();
        }

        Log.d("GameActivity", "Showing game over dialog.");




        TextView scoreView = gameOverDialog.findViewById(R.id.scoreTextView);
        TextView cashView = gameOverDialog.findViewById(R.id.cashTextView);

        scoreView.setText(String.format(Locale.getDefault(), "Score: %d", score));
        scoreTextView.setText(String.format(Locale.getDefault(), "Score: %d", score));
        cashView.setText(String.format(Locale.getDefault(), "%d", totalCashEarned));
        if(isLocal) {
            playerDataManager.updatePlayerCash(totalCashEarned);
        }
        Button acceptButton = gameOverDialog.findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(v -> {
            gameOverDialog.dismiss();

            soundPool.play(buttonPress, 1, 1, 0, 0, 1);
            Intent intent = new Intent(Game.this, MainMenuActivity.class);

            startActivity(intent);
            finish();
        });




    }



    private void flashButton(final Button button, boolean isCorrect) {
        int flashColor = isCorrect ? Color.GREEN : Color.RED;
        button.setBackgroundColor(flashColor);
        scoreTextView.setTextColor(flashColor);

        new Handler().postDelayed(() -> {

            button.setBackgroundColor(Color.parseColor("#E67E22"));
            scoreTextView.setTextColor(Color.WHITE);

        }, 300);
    }


    private void loadQuestion(int index) {
        //initPowerUps( dblButton, doubleTextView,dbl);
        Question currentQuestion = questions.get(index);

        String[] answers = currentQuestion.getAnswers().clone();
        List<String> answersList = Arrays.asList(answers);
        if(!isOmit){Collections.shuffle(answersList);}

        int correctAnswerIndex = currentQuestion.getCorrectAnswer();
        questionText.setText(currentQuestion.getQuestionText());
        answerButton1.setText(answersList.get(0));
        answerButton2.setText(answersList.get(1));
        answerButton3.setText(answersList.get(2));


        if(isOmit) {
            highlightIncorrectAnswer(correctAnswerIndex, answersList);
        }
        else {

            answerButton1.setBackgroundColor(Color.TRANSPARENT);
            answerButton2.setBackgroundColor(Color.TRANSPARENT);
            answerButton3.setBackgroundColor(Color.TRANSPARENT);
        }

        if (currentQuestion.getImageSrc() != 0) {
            imageSrc.setImageResource(currentQuestion.getImageSrc());
            imageSrc.setVisibility(View.VISIBLE);
        } else {
            imageSrc.setVisibility(View.GONE);
        }

        resetButtonListeners();
        checkPowerUpAvailability();
    }


    private void highlightIncorrectAnswer(int correctAnswerIndex, List<String> answersList) {
        int incorrectIndex = -1;
        do {
            incorrectIndex = random.nextInt(3);
        } while (incorrectIndex == correctAnswerIndex);

        switch (incorrectIndex) {
            case 0:

                answerButton1.setBackgroundColor(Color.DKGRAY);
                break;
            case 1:

                answerButton2.setBackgroundColor(Color.DKGRAY);
                break;
            case 2:

                answerButton3.setBackgroundColor(Color.DKGRAY);
                break;
        }
    }


    private void resetButtonListeners() {
        View.OnClickListener answerButtonListener = v -> {
            Button b = (Button) v;
            String buttonText = b.getText().toString();
            checkAnswer(buttonText, b);
        };

        answerButton1.setOnClickListener(answerButtonListener);
        answerButton2.setOnClickListener(answerButtonListener);
        answerButton3.setOnClickListener(answerButtonListener);
    }

    private void nextQuestion() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questions.size();
        loadQuestion(currentQuestionIndex);

    }


    private void checkAnswer(String chosenAnswer, Button selectedButton) {

        Question currentQuestion = questions.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getAnswers()[currentQuestion.getCorrectAnswer()];
        boolean isCorrect = chosenAnswer.equals(correctAnswer);
        if (isBlitz){timeLeft = 5000; startTimer(); blitzModifier = 5;
            if (currentQuestionIndex == questions.size() - 1) {
                gameOver();
            }
        }
        if(isDouble){doublePoints=2;}

        if (isCorrect) {
            // Correct answer
            score+=((10+blitzModifier)*doublePoints);
            cash+=((5+blitzModifier)*doublePoints);
            totalCashEarned +=((5+blitzModifier)*doublePoints);

            currentCombo++;
            if(currentCombo>highestCombo){
                highestCombo=currentCombo;
            }
            updateComboDisplay(true);
            if(!isLocal){playerDataManager.updatePlayerCash((5+blitzModifier)*doublePoints);}

            playerDataManager.updateQuestionStats(questions.get(currentQuestionIndex).getCategory(),true);
            // Log.d(TAG, "After updateQuestionStats for correct question: " + gson.toJson(currentPlayerInfo));
            flashButton(selectedButton, true);
            soundPool.play(correct, 1, 1, 0, 0, 1);

        } else {
            currentCombo=0;
            updateComboDisplay(false);
            score-=10-blitzModifier;
            if(isBlitz){gameOver();}
            playerDataManager.updateQuestionStats(questions.get(currentQuestionIndex).getCategory(),false);
            flashButton(selectedButton, false);
            soundPool.play(wrong, 1, 1, 0, 0, 1);
        }
        scoreTextView.setText(String.format(Locale.getDefault(), "Score: %d", score));
        cashTextView.setText(String.format(Locale.getDefault(), "%d", cash+startingCash));
        blitzModifier=0;
        doublePoints=1;
        isDouble=false;
        isOmit=false;
        dblButton.clearFocus();

        new Handler().postDelayed(this::nextQuestion, 300);

    }

    private void updateComboDisplay(boolean isCorrect) {
        if(isCorrect&&currentCombo>1){
            comboTextView.setText("COMBO X"+currentCombo);
            comboTextView.setVisibility(View.VISIBLE);
            animateComboText();
        }
        else{
            comboTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void initCombo(){
        comboTextView = new TextView(this);
        comboTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
        comboTextView.setTextColor(Color.GREEN);
        comboTextView.setVisibility(View.INVISIBLE);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.broadwayy);
        comboTextView.setTypeface(typeface);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.topMargin = 100;

        ((ViewGroup)findViewById(android.R.id.content)).addView(comboTextView, params);

    }

    private void usePowerUp(String powerUpType) {

        int currentCount = currentPlayerInfo.getPowerUps().getOrDefault(powerUpType, 0);
        if (currentCount > 0) {
            currentPlayerInfo.getPowerUps().put(powerUpType, currentCount- 1);
            playerDataManager.updatePlayerPowerUp(powerUpType, - 1);
            updateUI();
        }
    }
    private void checkPowerUpAvailability() {
        Map<String, Integer> powerUps = currentPlayerInfo.getPowerUps();
        dblButton.setEnabled(powerUps.getOrDefault("double_points", 0) > 0);
        omitButton.setEnabled(powerUps.getOrDefault("omit_wrong_answer", 0) > 0);
        doubleTextView.setText(String.valueOf(powerUps.getOrDefault("double_points", 0)));
        omitTextView.setText(String.valueOf(powerUps.getOrDefault("omit_wrong_answer", 0)));
    }

    private void updateUI() {
        runOnUiThread(() -> {
            skipTextView.setText(String.valueOf(currentPlayerInfo.getPowerUps().getOrDefault("skip_question", 0)));
            doubleTextView.setText(String.valueOf(currentPlayerInfo.getPowerUps().getOrDefault("double_points", 0)));
            omitTextView.setText(String.valueOf(currentPlayerInfo.getPowerUps().getOrDefault("omit_wrong_answer", 0)));

            skipButton.setEnabled(currentPlayerInfo.getPowerUps().getOrDefault("skip_question", 0) > 0);
            dblButton.setEnabled(currentPlayerInfo.getPowerUps().getOrDefault("double_points", 0) > 0);
            omitButton.setEnabled(currentPlayerInfo.getPowerUps().getOrDefault("omit_wrong_answer", 0) > 0);
        });
    }



    private void animateComboText() {
        comboTextView.setScaleX(0f);
        comboTextView.setScaleY(0f);
        comboTextView.setAlpha(1f);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(comboTextView, "scaleX", 0f, 1.5f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(comboTextView, "scaleY", 0f, 1.5f, 1f);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(comboTextView, "alpha", 1f, 0f);
        fadeOut.setStartDelay(1000);
        fadeOut.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleUpX, scaleUpY, fadeOut);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                comboTextView.setVisibility(View.INVISIBLE);
            }
        });
        animatorSet.start();
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
        soundPool.release();
        soundPool = null;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Toast.makeText(this, "Back button is disabled during the game", Toast.LENGTH_SHORT).show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.getInstance().play();

    }
}
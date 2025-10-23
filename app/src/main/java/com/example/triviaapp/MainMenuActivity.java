package com.example.triviaapp;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class MainMenuActivity extends AppCompatActivity {
    private boolean isMuted = false;

    private SoundPool soundPool;
    private int soundId;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
     FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    SignInButton signInButton;
    Button signOutButton;




    private PlayerInfo currentPlayerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.menu), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        PlayerDataManager manager = new PlayerDataManager(this);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                updateUI(user);
            }
        };

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(view -> signIn());
        signOutButton = findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(view -> signOut());




        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        soundId = soundPool.load(this, R.raw.button_pressed, 1);
        ImageButton muteButton = findViewById(R.id.button_mute);

        giveButtonSound(R.id.button_play);
        giveButtonSound(R.id.button_shop);
        giveButtonSound(R.id.button_quit);
        giveButtonSound(R.id.button_stats);



// Call this method when the mute button is clicked
        muteButton.setOnClickListener(v -> toggleMute());


        Button playButton = findViewById(R.id.button_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                soundPool.play(soundId, 1, 1, 0, 0, 1);


                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent = new Intent(MainMenuActivity.this, GameModeActivity.class);
                        startActivity(intent);

                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                }, 400);
            }
        });

        Button statsButton = findViewById(R.id.button_stats);
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play the sound for button press
                soundPool.play(soundId, 1, 1, 0, 0, 1);


                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent = new Intent(MainMenuActivity.this, Stats.class);
                        startActivity(intent);

                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                }, 400);
            }
        });

        Button shopButton = findViewById(R.id.button_shop);
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play the sound for button press
                soundPool.play(soundId, 1, 1, 0, 0, 1);


                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent = new Intent(MainMenuActivity.this, Shop.class);
                        startActivity(intent);
                        // Optionally, apply an animation to smooth the transition
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                }, 400);
            }
        });



        Button quitButton = findViewById(R.id.button_quit);
        quitButton.setOnClickListener(v -> {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishAffinity();
                    System.exit(0);
                }
            }, 300);
        });



    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {

        mAuth.signOut();

        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> updateUI(null));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                    } else {

                    }
                });
    }


    private void toggleMute() {

        isMuted = !isMuted;

        MusicManager.getInstance().toggleMute();
        ImageButton muteButton = findViewById(R.id.button_mute);
        muteButton.setImageResource(isMuted ? android.R.drawable.ic_lock_silent_mode : android.R.drawable.ic_lock_silent_mode_off);
    }



    private void giveButtonSound(int buttonId){
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> soundPool.play(soundId,1,1,0,0,1));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

    }



    private void updateUI(FirebaseUser user) {
        if (user != null) {
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
        } else {
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
        }
    }




    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    MusicManager.getInstance().init(this);
    MusicManager.getInstance().play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
        MusicManager.getInstance().release(this);

    }

    @Override
    protected void onPause(){
        super.onPause();
        MusicManager.getInstance().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.getInstance().play();

    }





}






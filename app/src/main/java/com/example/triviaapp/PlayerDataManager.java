package com.example.triviaapp;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class PlayerDataManager {
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private PlayerInfo localPlayerInfo;
    private LocalPlayerInfo localPlayerDb;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public PlayerDataManager(Context context) {
        mAuth = FirebaseAuth.getInstance();
        gson = new Gson();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = context.getSharedPreferences("PlayerInfo", Context.MODE_PRIVATE);
        localPlayerInfo = new PlayerInfo("GUEST","guest@guest.co");

        localPlayerDb = Room.databaseBuilder(context.getApplicationContext(),
                        LocalPlayerInfo.class, "trivia-database")
                .fallbackToDestructiveMigration()

                .build();

    }

    public void updatePlayerScore(int score,int highestCombo) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference ref = databaseReference.child("players").child(user.getUid());
            ref.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    PlayerInfo p = mutableData.getValue(PlayerInfo.class);
                    if (p == null) {
                        return Transaction.success(mutableData);
                    }
                    p.addScore(score);
                    p.incrementTotalGames();
                    p.setHighestCombo(highestCombo);
                    mutableData.setValue(p);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {

                    Log.d(TAG, "updatePlayerScore:onComplete:" + databaseError);
                }
            });
        }
        else {
            localPlayerInfo.addScore(score);
            localPlayerInfo.incrementTotalGames();
            localPlayerInfo.setHighestCombo(highestCombo);
            saveLocalPlayerInfo(localPlayerInfo);
        }
    }




    public void updatePlayerCash(int cashDelta) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference ref = databaseReference.child("players").child(user.getUid()).child("cash");

            ref.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    Integer currentCash = mutableData.getValue(Integer.class);
                    if (currentCash == null) {
                        currentCash = 0;
                    }
                    mutableData.setValue(currentCash + cashDelta);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                    Log.d(TAG, "updatePlayerCash:onComplete:" + databaseError);
                }
            });
        }
        else{

            localPlayerInfo.addCash(cashDelta);
            saveLocalPlayerInfo(localPlayerInfo);
        }
    }

    public void updatePlayerPowerUp(String powerUpType, int change) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference ref = databaseReference.child("players").child(user.getUid()).child("powerUps").child(powerUpType);

            ref.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    Integer currentValue = mutableData.getValue(Integer.class);
                    if (currentValue == null) {
                        currentValue = 0;
                    }
                    mutableData.setValue(currentValue + change);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                    Log.d(TAG, "updatePlayerPowerUp:onComplete:" + databaseError);
                }
            });
        }
        else{
            localPlayerInfo.updatePowerUp(powerUpType,change);
            saveLocalPlayerInfo(localPlayerInfo);
        }

    }


    public void updateQuestionStats(String category, boolean correct) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference ref = databaseReference.child("players").child(user.getUid());

            ref.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    PlayerInfo p = mutableData.getValue(PlayerInfo.class);
                    if (p == null) {
                        return Transaction.success(mutableData);
                    }
                    // Increment right or wrong answers
                    if (correct) {
                        p.questionsRight++;
                        int categoryCount = p.categories.getOrDefault(category, 0) + 1;
                        p.categories.put(category, categoryCount);
                    } else {
                        p.questionsWrong++;
                    }
                    mutableData.setValue(p);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                    Log.d(TAG, "updateQuestionStats:onComplete:" + databaseError);
                }
            });
        }
        else {

            localPlayerInfo = loadLocalPlayerInfo();
            if (correct) {
                localPlayerInfo.incrementCorrect();
                int categoryCount = localPlayerInfo.categories.getOrDefault(category, 0) + 1;
                localPlayerInfo.updateCategory(category, categoryCount);
            } else {
                localPlayerInfo.incrementWrong();
            }
            saveLocalPlayerInfo(localPlayerInfo);
        }
    }


    public void fetchPlayerInfo(final OnPlayerInfoReceivedListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference ref = databaseReference.child("players").child(user.getUid());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    PlayerInfo playerInfo = dataSnapshot.getValue(PlayerInfo.class);
                    if (playerInfo == null) {

                        playerInfo = new PlayerInfo(user.getUid(), user.getEmail());
                        savePlayerInfo(playerInfo);
                    }
                    if (listener != null) {
                        listener.onReceived(playerInfo);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "fetchPlayerInfo:onCancelled", databaseError.toException());
                }
            });
        } else {
            PlayerInfo localPlayerInfo = loadLocalPlayerInfo();
            if (listener != null) {
                listener.onReceived(localPlayerInfo);
            }
        }
    }


    public PlayerInfo loadLocalPlayerInfo() {


        final PlayerInfo[] playerInfo = new PlayerInfo[1];
        Thread thread = new Thread(() -> {

            playerInfo[0] = localPlayerDb.playerInfoDAO().getLatestPlayerInfo();
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return playerInfo[0] != null ? playerInfo[0] : new PlayerInfo("GUEST","guest@guest.co");

    }


    public void savePlayerInfo(PlayerInfo playerInfo, SavePlayerInfoCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("players").child(user.getUid());
            ref.setValue(playerInfo, (DatabaseError databaseError, DatabaseReference databaseReference) -> {
                if (databaseError != null) {
                    Log.d(TAG, "savePlayerInfo:onFailure:" + databaseError.getMessage());
                } else {
                    Log.d(TAG, "savePlayerInfo:onSuccess");
                    callback.onCallback();
                }
            });
        }
    }

    public interface SavePlayerInfoCallback {
        void onCallback();
    }
    public void savePlayerInfo(PlayerInfo playerInfo) {
        savePlayerInfo(playerInfo, null);
    }
    public void saveLocalPlayerInfo(PlayerInfo playerInfo) {
        new Thread(() -> localPlayerDb.playerInfoDAO().insertPlayerInfo(playerInfo)).start();
    }

    public interface OnPlayerInfoReceivedListener {
        void onReceived(PlayerInfo playerInfo);
    }







}

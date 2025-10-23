package com.example.triviaapp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Entity
public class PlayerInfo {
    @PrimaryKey(autoGenerate = false)
    public int id=1;
    @ColumnInfo(name = "displayName")
    public String displayName;
    @ColumnInfo(name = "email")
    public String email;
    @ColumnInfo(name = "lastTenScores")
    public List<Integer> lastTenScores = new ArrayList<>();
    @ColumnInfo(name = "cash")
    public int cash=500;
    @ColumnInfo(name = "totalGames")
    public int totalGames;
    @ColumnInfo(name = "questionsRight")
    public int questionsRight;
    @ColumnInfo(name = "questionsWrong")
    public int questionsWrong;
    @ColumnInfo(name = "powerUps")
    public Map<String, Integer> powerUps = new HashMap<>();
    @ColumnInfo(name = "highScore")
    public int highScore;
    @ColumnInfo(name = "highestCombo")
    public int highestCombo;
    @ColumnInfo(name = "categories")
    public Map<String, Integer> categories = new HashMap<>();
    @ColumnInfo(name = "lockedCategories")
    public Map<String, Integer> lockedCategories = new HashMap<>();


    public PlayerInfo() {
    }

    public PlayerInfo(String displayName, String email) {
        this.displayName = displayName;
        this.email = email;
        this.powerUps.put("double_points", 1);
        this.powerUps.put("skip_question", 10);
        this.powerUps.put("omit_wrong_answer", 4);
        this.categories.put("Science", 0);
        this.categories.put("Music", 0);
        this.categories.put("Geography", 0);
        this.categories.put("History", 0);
        this.categories.put("Film", 0);
        this.categories.put("Politics", 0);
        this.categories.put("Art", 0);
        this.categories.put("Sport", 0);
        this.lockedCategories.put("Politics",0);
        this.lockedCategories.put("Art", 0);
        this.lockedCategories.put("Sport", 0);
        this.highScore = 0;
        this.highestCombo =0;
        this.totalGames =0;
    }

    public void addScore(int score) {

        if (lastTenScores.size() >= 10) {
            lastTenScores.remove(0);
        }
        lastTenScores.add(score);

        // Update high score if necessary
        if (score > highScore) {
            highScore = score;
        }
    }

    public void updateHighestCombo(int combo) {
        if (combo > this.highestCombo) {
            this.highestCombo = combo;
        }
    }

    public int getHighestCombo() {
        return highestCombo;
    }

    public void addCash(int cashToAdd){
        cash +=cashToAdd;
    }
    public void setHighestCombo(int combo){
        highestCombo=combo;
    }
    public int getCash(){
        return this.cash;
    }

    public void incrementCorrect(){
        this.questionsRight++;
    }
    public void incrementWrong(){
        this.questionsWrong++;
    }
    public void incrementTotalGames(){this.totalGames++;}

    public void answerQuestion(boolean correct, String category) {
        if (correct) {
            questionsRight++;
            categories.put(category, categories.getOrDefault(category, 0) + 1);
        } else {
            questionsWrong++;
        }
    }

    public void updateCategory(String category, int value) {
            this.categories.put(category, value);
    }

    public void updateLockedCategory(String category, int value) {
            lockedCategories.put(category, value);
    }

    public void updatePowerUp(String powerUp, int value) {
        int currentVal = this.powerUps.getOrDefault(powerUp, 0);
        this.powerUps.put(powerUp, currentVal + value);

    }

    public Map<String, Integer> getPowerUps(){
        return this.powerUps;
    }

    public void usePowerUp(String powerUp) {
        if (powerUps.containsKey(powerUp) && powerUps.get(powerUp) > 0) {
            powerUps.put(powerUp, powerUps.get(powerUp) - 1);
        } else {

        }
    }

    public String getBestCategory() {
        String bestCategory = null;
        int highestScore = -1;
        for (Map.Entry<String, Integer> category : categories.entrySet()) {
            if (category.getValue() > highestScore) {
                highestScore = category.getValue();
                bestCategory = category.getKey();
            }
        }
        return bestCategory;
    }

    public String getWorstCategory() {
        String worstCategory = null;
        int lowestScore = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> category : categories.entrySet()) {
            if (category.getValue() < lowestScore) {
                lowestScore = category.getValue();
                worstCategory = category.getKey();
            }
        }
        return worstCategory;
    }


    public List<Integer> getLastTenScores() {
        return lastTenScores;
    }


    public void setPowerUps(Map<String, Integer> newPowerUps) {
        this.powerUps = new HashMap<>(newPowerUps);
    }


    public int getTotalGamesPlayed() {
        return this.totalGames;
    }
}

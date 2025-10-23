package com.example.triviaapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class Stats extends AppCompatActivity {
    PlayerDataManager playerDataManager;
    private PlayerInfo currentPlayerInfo;

    private TextView bestCategory,worstCategory,highestCombo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.stats), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        playerDataManager = new PlayerDataManager(this);

        playerDataManager.fetchPlayerInfo(playerInfo -> {
            currentPlayerInfo = playerInfo;
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                // Player is not signed in and local player info is not loaded, load local player info
                currentPlayerInfo = playerDataManager.loadLocalPlayerInfo();
            }

            runOnUiThread(() -> {
                // Assuming you have TextViews with these IDs in your layout
                TextView tvTotalGames = findViewById(R.id.tvTotalGames);
                TextView bestCat = findViewById(R.id.bestCat);
                TextView worstCat = findViewById(R.id.worstCat);
                TextView highestCombo = findViewById(R.id.highestCombo);

                // Example of setting the text, replace these with actual data retrieval
                tvTotalGames.setText(String.valueOf(currentPlayerInfo.getTotalGamesPlayed()));
                bestCat.setText(currentPlayerInfo.getBestCategory());
                worstCat.setText(currentPlayerInfo.getWorstCategory());
                highestCombo.setText(String.valueOf(currentPlayerInfo.getHighestCombo()));

                // For the scores graph
                List<Integer> scores = currentPlayerInfo.getLastTenScores(); // Assuming this method exists
                populateScoresGraph(scores);
            });




        });



    }

    private void populateScoresGraph(List<Integer> scores) {
        LineChart chart = findViewById(R.id.chart);
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < scores.size(); i++) {
            entries.add(new Entry(i, scores.get(i)));
        }
        chart.getDescription().setEnabled(false);
        LineDataSet dataSet = new LineDataSet(entries, "Last 10 Scores");
        dataSet.setValueTextColor(Color.RED);
        dataSet.setCircleColor(Color.RED);
        dataSet.setHighLightColor(Color.RED);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setColor(Color.RED);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getXAxis().setDrawLabels(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setDrawLabels(false);
        dataSet.setValueTextSize(12f);
        chart.getLegend().setEnabled(false);
        chart.invalidate();
    }



}
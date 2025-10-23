package com.example.triviaapp;


import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
public class Questions {
    private List<Question> questions;
    public Questions(Context context) {
        questions = new ArrayList<>();
        loadQuestionsFromJson(context);
    }

    private void loadQuestionsFromJson(Context context) {
        try {
            InputStream is = context.getAssets().open("questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String questionText = obj.getString("questionText");
                String category = obj.getString("category");
                JSONArray answersJson = obj.getJSONArray("answers");
                String[] answers = new String[answersJson.length()];
                for (int j = 0; j < answersJson.length(); j++) {
                    answers[j] = answersJson.getString(j);
                }
                int correctAnswer = obj.getInt("correctAnswer");
                String imageName = obj.optString("imageSrc");
                Integer imageSrc = imageName.isEmpty() ? 0 : context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
                questions.add(new Question(questionText, category, imageSrc, answers, correctAnswer));
                Log.d("LoadQuestion", "Image Resource ID: " + imageSrc + " for name: " + imageName);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Question getQuestion(int index){
        return questions.get(index);
    }
    public List<Question> getQuestions(){return questions;}
    public int size(){
        return questions.size();
    }

}

package com.example.triviaapp;

public class Question {
    private String questionText;
    private String category;
    private Integer imageSrc;
    private String[] answers;
    private int correctAnswer;


public Question(String questionText, String category, Integer imageSrc, String[] answers, int correctAnswer)
{
    this.questionText = questionText;
    this.category = category;
    this.imageSrc = imageSrc;
    this.answers = answers;
    this.correctAnswer = correctAnswer;
}

public String getQuestionText(){
    return questionText;
}
public String getCategory(){
    return category;
}
public Integer getImageSrc(){
    return imageSrc;
}

public String[] getAnswers(){
    return answers;
}

public int getCorrectAnswer(){
    return correctAnswer;
}

public void setQuestionText(String qText){
    this.questionText = qText;
}

public void setCategory(String cText){
    this.category = cText;
}

public void setImageSrc(Integer src){
    this.imageSrc = src;
}

public void setAnswers(String[] qAnswers){
    this.answers = qAnswers;
}

public void setCorrectAnswer(int cAnswer){
    this.correctAnswer = cAnswer;
}

}

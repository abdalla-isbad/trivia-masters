package com.example.triviaapp;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
@ProvidedTypeConverter
public class Converters {
    private static final Gson GSON = new Gson();
    @TypeConverter
    public static Map<String, Integer> fromMapString(String value) {
        Type mapType = new TypeToken<Map<String, Integer>>() {}.getType();
        return GSON.fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromMap(Map<String, Integer> map) {
        return GSON.toJson(map);
    }

    @TypeConverter
    public static List<Integer> fromListString(String value) {
        Type listType = new TypeToken<List<Integer>>() {}.getType();
        return GSON.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<Integer> list) {

        return GSON.toJson(list);
    }
}

package me.iwf.photopicker.utils;


import android.content.Context;
import android.content.SharedPreferences;

import me.iwf.photopicker.PickerApp;

public class SharedPrefUtils {
    public static String tag = "photoPicker";

    public static void save(String name, String data) {

        SharedPreferences sharedPreferences = PickerApp.getInstance().getSharedPreferences(tag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(name, data);
        editor.commit();
    }

    public static String load(String name) {
        String data = "";

        SharedPreferences sharedPreferences = PickerApp.getInstance().getSharedPreferences(tag, Context.MODE_PRIVATE);
        data = sharedPreferences.getString(name, data);

        return data;
    }

    public static void clear() {
        clear(tag);
    }

    public static void clear(String tag) {
        SharedPreferences sharedPreferences = PickerApp.getInstance().getSharedPreferences(tag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }
}

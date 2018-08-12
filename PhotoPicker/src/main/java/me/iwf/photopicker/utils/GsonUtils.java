package me.iwf.photopicker.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by LockyLuo on 2018/8/12.
 */

public class GsonUtils {
    public static Gson getGson(){
        return new GsonBuilder().serializeNulls().serializeSpecialFloatingPointValues().create();
    }
}

package me.iwf.photopicker.utils;

import android.Manifest;

/**
 * Created by lockyluo on 2018-08-10.
 */
public interface CommonData {
    String providerAuth="me.iwf.photopicker.fileProvider";
    String[] pers=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};
}

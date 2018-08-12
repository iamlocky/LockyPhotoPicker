package me.iwf.photopicker.utils;

import android.Manifest;
import android.os.Environment;

/**
 * Created by lockyluo on 2018-08-10.
 */
public interface CommonData {
    String providerAuth= "photoPicker.fileProvider";
    String[] pers=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};
}

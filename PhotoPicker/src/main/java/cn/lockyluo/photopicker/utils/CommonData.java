package cn.lockyluo.photopicker.utils;

import android.Manifest;
import android.os.Environment;

import cn.lockyluo.photopicker.PickerApp;

/**
 * Created by lockyluo on 2018-08-10.
 */
public interface CommonData {
    String providerAuth= "cn.lockyluo.photopicker.fileProvider";
    String[] permissions =new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};
}

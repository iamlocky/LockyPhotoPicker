package cn.lockyluo.photopicker;

import android.app.Application;

/**
 * Created by LockyLuo on 2018/8/12.
 */

public class PickerApp extends Application {
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }

    public static Application getInstance() {
        return instance;
    }
}

package cn.lockyluo.photopicker;

import android.app.Application;
import android.content.Context;

/**
 * Created by LockyLuo on 2018/8/12.
 */

public class PickerApp extends Application {
    private static Context instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }

    public static void init(Context instance) {
        PickerApp.instance = instance;
    }

    public static Context getInstance() {
        return instance;
    }
}

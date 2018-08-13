package cn.lockyluo.photopicker.utils;

import android.widget.Toast;
import cn.lockyluo.photopicker.PickerApp;


public class ToastUtil {
    private static Toast mToast;//控制toast时间
    private static Toast toast;

    public static void show(String text) {
        if (text==null)
            text="";
        if (mToast == null) {
            mToast = Toast.makeText(PickerApp.getInstance(), text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

}

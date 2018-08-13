package cn.lockyluo.photopicker.utils;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

/**
 * Created by LockyLuo on 2018/8/12.
 */

public class PickerFileProvider extends FileProvider {
    public static String providerAuth ="";


    public static Uri getUriForFile(Context context, File file) {
        providerAuth=context.getPackageName()+".fileProvider";
        Log.d("PickerFileProvider", "getUriForFile: "+providerAuth);
        return getUriForFile(context,providerAuth,file);
    }


}

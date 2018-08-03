package me.iwf.photopicker;

import android.app.Activity;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Updated by LockyLuo on 18/8/02.
 */
public class PhotoPickUtils {

    public static void onActivityResult(int requestCode, int resultCode, Intent data,PickHandler pickHandler ) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PhotoPicker.REQUEST_CODE) {//第一次，选择图片后返回
                if (data != null) {
                    ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                    pickHandler.onPickSuccess(photos);

                } else {
                    pickHandler.onPickFail("选择图片失败");
                }
            }else if (requestCode == PhotoPreview.REQUEST_CODE){//如果是预览与删除后返回
                if (data != null) {
                    ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                    pickHandler.onPreviewBack(photos);
                } else {
                   // pickHandler.onPickFail("选择图片失败");
                }

            }
        }else {

            if (requestCode == PhotoPicker.REQUEST_CODE){
                pickHandler.onPickCancle();
            }
        }


    }

    public static void startPick(Activity context,boolean showGif,int photoCount,ArrayList<String> photos){
        PhotoPicker.builder()
                .setPhotoCount(photoCount)
                .setShowCamera(false)
                .setShowGif(showGif)
                .setSelected(photos)
                .setPreviewEnabled(true)
                .start(context, PhotoPicker.REQUEST_CODE);
    }



    public interface  PickHandler{
        void onPickSuccess(ArrayList<String> photos);
        void onPreviewBack(ArrayList<String> photos);
        void onPickFail(String error);
        void onPickCancle();
    }
}

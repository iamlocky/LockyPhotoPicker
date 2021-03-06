package cn.lockyluo.photopicker;

import android.app.Activity;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Updated by LockyLuo on 18/8/02.
 */
public class PhotoPickUtils {

    public static void onActivityResult(int requestCode, int resultCode, Intent data, PickHandler pickHandler) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PhotoPicker.REQUEST_CODE) {//选择图片后返回
                if (data != null) {
                    ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                    pickHandler.onPickSuccess(photos);
                } else {
                    pickHandler.onPickFail("未选择图片");
                }
            } else if (requestCode == PhotoPreview.REQUEST_CODE) {//如果是预览后返回
                if (data != null) {
                    ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                    pickHandler.onPreviewBack(photos);
                } else {
                    pickHandler.onPreviewBack(new ArrayList<String>());
                }

            }
        } else {
            if (requestCode == PhotoPicker.REQUEST_CODE) {
                pickHandler.onPickCancel();
            }
        }
    }

    public static void startPick(Activity context, boolean showGif, int photoCount, ArrayList<String> photos) {
        startPick(context,showGif,false,photoCount,photos);
    }

    public static void startPick(Activity context, boolean showGif, boolean launchCamera, int photoCount, ArrayList<String> photos) {
        PhotoPicker.builder()
                .setPhotoCount(photoCount)
                .setShowCamera(true)
                .setShowGif(showGif)
                .setSelected(photos)
                .setPreviewEnabled(true)
                .setLaunchCamera(launchCamera)
                .start(context, PhotoPicker.REQUEST_CODE);
    }

    public static void startPick(Activity context, boolean showGif, boolean launchCamera, int photoCount,int order, ArrayList<String> photos) {
        PhotoPicker.builder()
                .setOrder(order)
                .setPhotoCount(photoCount)
                .setShowCamera(true)
                .setShowGif(showGif)
                .setSelected(photos)
                .setPreviewEnabled(true)
                .setLaunchCamera(launchCamera)
                .start(context, PhotoPicker.REQUEST_CODE);
    }


    public interface PickHandler {
        void onPickSuccess(ArrayList<String> photos);

        void onPreviewBack(ArrayList<String> photos);

        void onPickFail(String error);

        void onPickCancel();
    }
}

package cn.lockyluo.photopicker.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import cn.lockyluo.photopicker.PhotoPickUtils;
import cn.lockyluo.photopicker.PickerApp;
import cn.lockyluo.photopicker.utils.CommonData;
import cn.lockyluo.photopicker.utils.ImageCaptureManager;
import cn.lockyluo.photopicker.utils.ToastUtil;

/**
 * Updated by LockyLuo on 18/8/3.
 */
public class MultiPickResultView extends FrameLayout {
    private static final String TAG = "MultiPickResultView";
    @IntDef({ACTION_SELECT, ACTION_ONLY_SHOW})

    //Tell the compiler not to store annotation data in the .class file
    @Retention(RetentionPolicy.SOURCE)

    //Declare the NavigationMode annotation
    public @interface MultiPicAction {
    }


    public static final int ACTION_SELECT = 1;//该组件用于图片选择
    public static final int ACTION_ONLY_SHOW = 2;//该组件仅用于图片显示

    private int action;

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    private int maxCount;


    android.support.v7.widget.RecyclerView recyclerView;
    PhotoAdapter photoAdapter;
    ArrayList<String> selectedPhotos;

    public MultiPickResultView(Context context) {
        this(context, null, 0);
    }

    public MultiPickResultView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiPickResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (PickerApp.getInstance()==null){
            PickerApp.init(context.getApplicationContext());
        }
        initView(context, attrs);
        initData(context, attrs);
        initEvent(context, attrs);


    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        super.setBackgroundColor(color);
        if (recyclerView!=null) {
            recyclerView.setBackgroundColor(color);
        }
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (recyclerView!=null) {
                recyclerView.setBackground(background);
            }
        }
    }

    private void initEvent(Context context, AttributeSet attrs) {

    }

    private void initData(Context context, AttributeSet attrs) {

    }

    private void initView(Context context, AttributeSet attrs) {
        int padding=dp2Px(context,5);
        recyclerView = new android.support.v7.widget.RecyclerView(context, attrs);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL));
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(padding,padding,padding,padding);
        recyclerView.setLayoutParams(params);
        setBackgroundColor(Color.WHITE);

        this.addView(recyclerView);
    }

    public int dp2Px(Context context,int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        int px = (int) (dp * density + .5f);
        return px;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MultiPickResultView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    public void init(Activity context, int maxCount, @MultiPicAction int action, List<String> photos) {
        this.action = action;
        this.maxCount = maxCount;

        if (action == MultiPickResultView.ACTION_ONLY_SHOW) {//当只用作显示图片时,一行显示3张
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL));
        }

        selectedPhotos = new ArrayList<>();

        this.action = action;
        if (photos != null && photos.size() > 0) {
            selectedPhotos.addAll(photos);
        }
        photoAdapter = new PhotoAdapter(context, selectedPhotos, this.maxCount);
        photoAdapter.setAction(action);
        recyclerView.setAdapter(photoAdapter);

    }


    public void showPics(List<String> paths) {
        if (paths != null) {
            selectedPhotos.clear();
            selectedPhotos.addAll(paths);
            photoAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 一键启动相机
     */
    public void launchCamera(){
        if (photoAdapter!=null){
            photoAdapter.startPicker(true);
        }else {
            Log.e(TAG, "launchCamera: photoAdapter is null",new Throwable());
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        PhotoPickUtils.onActivityResult(requestCode, resultCode, data, new PhotoPickUtils.PickHandler() {
            @Override
            public void onPickSuccess(ArrayList<String> photos) {
                photoAdapter.refresh(photos);
            }

            @Override
            public void onPreviewBack(ArrayList<String> photos) {
                photoAdapter.setPhotoPaths(photos);
            }

            @Override
            public void onPickFail(String error) {
                ToastUtil.show(error);

                selectedPhotos.clear();
                photoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPickCancel() {
            }
        });

    }

    /**
     * 获取真实路径地址
     * @return
     */
    public List<String> getPhotos() {
        return selectedPhotos;
    }

    /**
     * 获取uri地址
     * @return
     */
    public List<Uri> getPhotosUri(){
        List<Uri> uriList=new ArrayList<>();
        if (selectedPhotos!=null){
            for (int i = 0; i < selectedPhotos.size(); i++) {
                uriList.add(ImageCaptureManager.fileToUri(getContext(),selectedPhotos.get(i)));
            }
        }
        return uriList;
    }

}

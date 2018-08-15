package cn.lockyluo.photopicker.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import cn.lockyluo.photopicker.PhotoPickUtils;
import cn.lockyluo.photopicker.PickerApp;
import cn.lockyluo.photopicker.R;
import cn.lockyluo.photopicker.utils.ImageCaptureManager;
import cn.lockyluo.photopicker.utils.ToastUtil;

/**
 * Updated by LockyLuo on 18/8/3.
 */
public class MultiPickResultView extends LinearLayout {
    private static final String TAG = "MultiPickResultView";
    private View view;
    private int screenWidth = 100;

    @IntDef({ACTION_SELECT, ACTION_ONLY_SHOW})
    //Tell the compiler not to store annotation data in the .class file
    @Retention(RetentionPolicy.SOURCE)
    //Declare the NavigationMode annotation
    public @interface MultiPicAction {
    }

    public static final int ACTION_SELECT = 1;//该组件用于图片选择
    public static final int ACTION_ONLY_SHOW = 2;//该组件仅用于图片显示

    private int maxCount;
    private int order=-1;//该值用于区分使用了多个MultiPickResultView的场景，避免回传时更新所有的MultiPickResultView，默认为-1


    public RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private ArrayList<String> selectedPhotos;

    public MultiPickResultView(Context context) {
        this(context, null, 0);
    }

    public MultiPickResultView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiPickResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (PickerApp.getInstance() == null) {
            PickerApp.init(context.getApplicationContext());
        }
        initView(context, attrs);

    }

    public int getOrder() {
        return order;
    }

    //建议在init前调用
    public void setOrder(int order) {
        this.order = order;
        if (photoAdapter!=null){
            photoAdapter.setOrder(order);
        }
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        super.setBackgroundColor(color);
        if (recyclerView != null) {
            recyclerView.setBackgroundColor(color);
        }
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (recyclerView != null) {
                recyclerView.setBackground(background);
            }
        }
    }


    private void initView(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        view = LayoutInflater.from(context).inflate(R.layout.__picker_content_layout, this);
        recyclerView = view.findViewById(R.id.recyclerview_content);

        setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widMode = MeasureSpec.getMode(widthMeasureSpec);
        int heiMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        screenWidth = PickerApp.getInstance().getResources().getDisplayMetrics().widthPixels;

        if (widMode == MeasureSpec.AT_MOST) {
            width = screenWidth / 3;
            if (getSuggestedMinimumWidth() < width) {
                setMinimumWidth(width);
            }
        }

        if (heiMode == MeasureSpec.AT_MOST) {
            height = screenWidth / 3;
            if (getSuggestedMinimumHeight() < height) {
                setMinimumHeight(height);
            }
        }
//        setMeasuredDimension(width, height);
    }

    public int dp2Px(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        int px = (int) (dp * density + .5f);
        return px;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MultiPickResultView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    public void init(Context context, int maxCount, @MultiPicAction int action, List<String> photos) {
        this.maxCount = maxCount;

        if (action == MultiPickResultView.ACTION_ONLY_SHOW) {//当只用作显示图片时,一行显示3张
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL));
        }

        selectedPhotos = new ArrayList<>();

        if (photos != null && photos.size() > 0) {
            selectedPhotos.addAll(photos);
        }
        photoAdapter = new PhotoAdapter(context, selectedPhotos, this.maxCount);
        photoAdapter.setOrder(order);
        photoAdapter.setAction(action);
        recyclerView.setAdapter(photoAdapter);

    }


    public void showPictures(List<String> paths) {
        if (paths != null) {
            selectedPhotos.clear();
            selectedPhotos.addAll(paths);
            photoAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 一键启动相机
     */
    public void launchCamera() {
        if (photoAdapter != null) {
            photoAdapter.startPicker(true);
        } else {
            Log.e(TAG, "launchCamera: photoAdapter is null", new Throwable());
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int resultOrder= -1;
        if (data!=null) {
            resultOrder = data.getIntExtra("order",-1);
        }
        if (resultOrder!=order){
            Log.d(TAG, "onActivityResult: order doesn't match, ignored");
            return;
        }
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
     *
     * @return
     */
    public List<String> getPhotos() {
        return selectedPhotos;
    }

    /**
     * 获取uri地址
     *
     * @return
     */
    public List<Uri> getPhotosUri() {
        List<Uri> uriList = new ArrayList<>();
        if (selectedPhotos != null) {
            for (int i = 0; i < selectedPhotos.size(); i++) {
                uriList.add(ImageCaptureManager.fileToUri(getContext(), selectedPhotos.get(i)));
            }
        }
        return uriList;
    }

}

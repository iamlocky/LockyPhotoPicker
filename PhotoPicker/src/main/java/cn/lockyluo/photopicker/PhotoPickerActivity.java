package cn.lockyluo.photopicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.lockyluo.photopicker.entity.Photo;
import cn.lockyluo.photopicker.event.OnItemCheckListener;
import cn.lockyluo.photopicker.event.SimpleListener;
import cn.lockyluo.photopicker.fragment.ImagePagerFragment;
import cn.lockyluo.photopicker.fragment.PhotoPickerFragment;
import cn.lockyluo.photopicker.utils.ToastUtil;
import cn.lockyluo.photopicker.widget.Titlebar;

import static android.widget.Toast.LENGTH_LONG;
import static cn.lockyluo.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static cn.lockyluo.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static cn.lockyluo.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static cn.lockyluo.photopicker.PhotoPicker.EXTRA_LAUNCH_CAMERA;
import static cn.lockyluo.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static cn.lockyluo.photopicker.PhotoPicker.EXTRA_ORDER;
import static cn.lockyluo.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static cn.lockyluo.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static cn.lockyluo.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static cn.lockyluo.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static cn.lockyluo.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;

public class PhotoPickerActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "PhotoPickerActivity";
    private PhotoPickerFragment pickerFragment;
    private ImagePagerFragment imagePagerFragment;
    //private MenuItem menuDoneItem;

    private int maxCount = DEFAULT_MAX_COUNT;

    private boolean showGif = false;
    private int columnNumber = DEFAULT_COLUMN_NUMBER;
    private ArrayList<String> originalPhotos = null;

    private Titlebar titlebar;
    private int order;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, true);
        boolean previewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);
        boolean launchCamera = getIntent().getBooleanExtra(EXTRA_LAUNCH_CAMERA, false);
        order=getIntent().getIntExtra(EXTRA_ORDER, -1);
        setShowGif(showGif);

        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.__picker_activity_photo_picker);

        titlebar = (Titlebar) findViewById(R.id.titlebar);
        titlebar.init(this);


        maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
        columnNumber = getIntent().getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
        originalPhotos = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_PHOTOS);

        pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag("tag");
        if (pickerFragment == null) {
            pickerFragment = PhotoPickerFragment
                    .newInstance(showCamera, showGif, previewEnabled, columnNumber, maxCount, originalPhotos);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, pickerFragment, "tag")
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }
        pickerFragment.setSimpleListener(new SimpleListener() {
            @Override
            public void onDone(String message) {
                titlebar.setTitle(message);
            }
        });
        titlebar.setLeftOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //右边的点击事件
        titlebar.getTvRight().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
                if (photos != null && photos.size() > 0) {
                    setResultAndFinish(photos);
                } else {
                    if (imagePagerFragment!=null&&imagePagerFragment.isVisible()) {
                        photos = new ArrayList<>();
                        photos.add(imagePagerFragment.getCurrentPhotoPath());
                        setResultAndFinish(photos);
                    } else
                        ToastUtil.show(getString(R.string.__picker_has_no_photo));
                }
            }
        });

        pickerFragment.setOnGridAdapterItemCheckListener(new OnItemCheckListener() {
            @Override
            public boolean OnItemCheck(int position, Photo photo, final boolean isCheck, int selectedItemCount) {

                int total = selectedItemCount + (isCheck ? -1 : 1);
                // menuDoneItem.setEnabled(total > 0);
                if (maxCount <= 1) {
                    List<Photo> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                    if (!photos.contains(photo)) {
                        photos.clear();
                        pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
                    }
                    return true;
                }

                if (total > maxCount) {
                    ToastUtil.show(getString(R.string.__picker_over_max_count_tips, maxCount));

                    return false;
                }
                titlebar.getTvRight().setText(getString(R.string.__picker_done_with_count, total, maxCount));
                return true;
            }
        });

        if (launchCamera) {
            pickerFragment.launchCamera();
        }
    }

    void setResultAndFinish(ArrayList photos) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, photos);
        intent.putExtra("order",order);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it complete.
     */
    @Override
    public void onBackPressed() {
        if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            super.onBackPressed();
        }
    }


    public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
        this.imagePagerFragment = imagePagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this.imagePagerFragment)
                .addToBackStack(null)
                .commit();
    }


    public PhotoPickerActivity getActivity() {
        return this;
    }

    public boolean isShowGif() {
        return showGif;
    }

    public void setShowGif(boolean showGif) {
        this.showGif = showGif;
    }
}

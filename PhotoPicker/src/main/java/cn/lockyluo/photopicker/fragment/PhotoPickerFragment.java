package cn.lockyluo.photopicker.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.List;

import cn.lockyluo.photopicker.PhotoPickerActivity;
import cn.lockyluo.photopicker.PhotoPreview;
import cn.lockyluo.photopicker.R;
import cn.lockyluo.photopicker.adapter.PhotoGridAdapter;
import cn.lockyluo.photopicker.adapter.PopupDirectoryListAdapter;
import cn.lockyluo.photopicker.entity.Photo;
import cn.lockyluo.photopicker.entity.PhotoDirectory;
import cn.lockyluo.photopicker.event.OnItemCheckListener;
import cn.lockyluo.photopicker.event.OnPhotoClickListener;
import cn.lockyluo.photopicker.event.SimpleListener;
import cn.lockyluo.photopicker.utils.ImageCaptureManager;
import cn.lockyluo.photopicker.utils.MediaStoreHelper;
import cn.lockyluo.photopicker.utils.ToastUtil;

import static android.app.Activity.RESULT_OK;
import static cn.lockyluo.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static cn.lockyluo.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static cn.lockyluo.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static cn.lockyluo.photopicker.utils.MediaStoreHelper.INDEX_ALL_PHOTOS;

/**
 * Created by donglua on 15/5/31.
 * Updated by lockyluo on 2018-08-10.
 */
public class PhotoPickerFragment extends Fragment {
    private static final String TAG = "PhotoPickerFragment";
    private ImageCaptureManager captureManager;
    private PhotoGridAdapter photoGridAdapter;

    private PopupDirectoryListAdapter listAdapter;
    //所有photos的路径
    private List<PhotoDirectory> directories = new ArrayList<>();
    //传入的已选照片
    private ArrayList<String> originalPhotos = new ArrayList<>();

    private int SCROLL_THRESHOLD = 30;
    int column;
    //目录弹出框的一次最多显示的目录数目
    public static int COUNT_MAX = 5;
    private final static String EXTRA_CAMERA = "camera";
    private final static String EXTRA_COLUMN = "column";
    private final static String EXTRA_COUNT = "count";
    private final static String EXTRA_GIF = "gif";
    private final static String EXTRA_ORIGIN = "origin";
    private ListPopupWindow listPopupWindow;
    private RequestManager mGlideRequestManager;
    private Context mContext;
    private Bundle mediaStoreArgs;

    private OnItemCheckListener onItemCheckListener = new OnItemCheckListener() {
        @Override
        public boolean OnItemCheck(int position, Photo path, boolean isCheck, int selectedItemCount) {
            return false;
        }
    };

    private SimpleListener simpleListener;

    public static PhotoPickerFragment newInstance(boolean showCamera, boolean showGif,
                                                  boolean previewEnable, int column, int maxCount, ArrayList<String> originalPhotos) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_CAMERA, showCamera);
        args.putBoolean(EXTRA_GIF, showGif);
        args.putBoolean(EXTRA_PREVIEW_ENABLED, previewEnable);
        args.putInt(EXTRA_COLUMN, column);
        args.putInt(EXTRA_COUNT, maxCount);
        args.putStringArrayList(EXTRA_ORIGIN, originalPhotos);
        PhotoPickerFragment fragment = new PhotoPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnGridAdapterItemCheckListener(OnItemCheckListener onItemCheckListener) {
        this.onItemCheckListener = onItemCheckListener;
    }

    public void setSimpleListener(SimpleListener simpleListener) {
        this.simpleListener = simpleListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setRetainInstance(true);

        mGlideRequestManager = Glide.with(this);

        directories = new ArrayList<>();
        originalPhotos = getArguments().getStringArrayList(EXTRA_ORIGIN);

        column = getArguments().getInt(EXTRA_COLUMN, DEFAULT_COLUMN_NUMBER);
        boolean showCamera = getArguments().getBoolean(EXTRA_CAMERA, true);
        boolean previewEnable = getArguments().getBoolean(EXTRA_PREVIEW_ENABLED, true);

        photoGridAdapter = new PhotoGridAdapter(mContext, mGlideRequestManager, directories, originalPhotos, column);
        photoGridAdapter.setShowCamera(showCamera);
        photoGridAdapter.setPreviewEnable(previewEnable);

        mediaStoreArgs = new Bundle();

        boolean showGif = getArguments().getBoolean(EXTRA_GIF);
        mediaStoreArgs.putBoolean(EXTRA_SHOW_GIF, showGif);


        captureManager = new ImageCaptureManager(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.__picker_fragment_photo_picker, container, false);
        listAdapter = new PopupDirectoryListAdapter(mGlideRequestManager, directories);
        getPhotoGridAdapter().setOnItemCheckListener(onItemCheckListener);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_photos);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(column, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(photoGridAdapter);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final RelativeLayout rlBottom = rootView.findViewById(R.id.rl_bottom);
        final Button btSwitchDirectory = (Button) rootView.findViewById(R.id.button);

        final Button btnPreview = (Button) rootView.findViewById(R.id.btn_preview);

        listPopupWindow = new ListPopupWindow(getActivity());

//        listPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//替换背景
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int widths = wm.getDefaultDisplay().getWidth();
        listPopupWindow.setWidth(widths);//ListPopupWindow.MATCH_PARENT还是会有边距，直接拿到屏幕宽度来设置也不行，因为默认的background有左右padding值。

        listPopupWindow.setAnchorView(rlBottom);
        listPopupWindow.setAdapter(listAdapter);
        listPopupWindow.setModal(true);
        listPopupWindow.setDropDownGravity(Gravity.BOTTOM);

        listPopupWindow.setAnimationStyle(R.style.__picker_mystyle);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listPopupWindow.dismiss();

                PhotoDirectory directory = directories.get(position);

                btSwitchDirectory.setText(directory.getName());

                photoGridAdapter.setCurrentDirectoryIndex(position);
                photoGridAdapter.notifyDataSetChanged();
            }
        });

        photoGridAdapter.setOnPhotoClickListener(new OnPhotoClickListener() {
            @Override
            public void onClick(View v, int position, boolean showCamera) {
                final int index = showCamera ? position - 1 : position;

                List<String> photos = photoGridAdapter.getCurrentPhotoPaths();

                int[] screenLocation = new int[2];
                v.getLocationOnScreen(screenLocation);
                ImagePagerFragment imagePagerFragment =
                        ImagePagerFragment.newInstance(photos, index);
                imagePagerFragment.setSimpleListener(simpleListener);
                ((PhotoPickerActivity) getActivity()).addImagePagerFragment(imagePagerFragment);
            }
        });

        photoGridAdapter.setOnCameraClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        btSwitchDirectory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listPopupWindow.isShowing()) {
                    listPopupWindow.dismiss();
                } else if (!getActivity().isFinishing()) {
                    adjustHeight();
                    listPopupWindow.show();
                }
            }
        });


        //预览按钮
        btnPreview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoGridAdapter.getSelectedPhotoPaths().size() > 0) {
                    PhotoPreview.builder()
                            .setPhotos(photoGridAdapter.getSelectedPhotoPaths())
                            .setCurrentItem(0)
                            .start(getActivity());
                } else {
                    ToastUtil.show(getString(R.string.__picker_has_no_photo));
                }
            }
        });


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Log.d(">>> Picker >>>", "dy = " + dy);
                if (Math.abs(dy) > SCROLL_THRESHOLD) {
                    mGlideRequestManager.pauseRequests();
                } else {
                    mGlideRequestManager.resumeRequests();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mGlideRequestManager.resumeRequests();
                }
            }
        });
        Log.d(TAG, "onCreateView: ");
        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImageCaptureManager.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            captureManager.galleryAddPic();
            if (directories.size() > 0) {
                String path = captureManager.getCurrentPhotoPath();
                PhotoDirectory directory = directories.get(INDEX_ALL_PHOTOS);
                directory.getPhotos().add(INDEX_ALL_PHOTOS, new Photo(path.hashCode(), path));
                directory.setCoverPath(path);
                photoGridAdapter.notifyDataSetChanged();
            }
        }
    }

    public void launchCamera() {//已适配Android 7.+
        try {
            Intent intent = captureManager.dispatchTakePictureIntent();
            startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public PhotoGridAdapter getPhotoGridAdapter() {
        return photoGridAdapter;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        captureManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        captureManager.onRestoreInstanceState(savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }

    public ArrayList<String> getSelectedPhotoPaths() {
        return photoGridAdapter.getSelectedPhotoPaths();
    }

    public void adjustHeight() {
        if (listAdapter == null) return;
        int count = listAdapter.getCount();
        count = count < COUNT_MAX ? count : COUNT_MAX;
        if (listPopupWindow != null) {
            listPopupWindow.setHeight(count * getResources().getDimensionPixelOffset(R.dimen.__picker_item_directory_height));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        MediaStoreHelper.getPhotoDirs(getActivity(), mediaStoreArgs,
                new MediaStoreHelper.PhotosResultCallback() {
                    @Override
                    public void onResultCallback(List<PhotoDirectory> dirs) {
                        if (dirs.size() <= 1)
                            return;
                        directories.clear();
                        directories = dirs;
                        photoGridAdapter.setPhotoDirectories(dirs);
                        listAdapter.setDirectories(dirs);
                        photoGridAdapter.notifyDataSetChanged();
                        listAdapter.notifyDataSetChanged();
                        adjustHeight();
                    }
                });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (directories == null) {
            return;
        }

        for (PhotoDirectory directory : directories) {
            directory.getPhotoPaths().clear();
            directory.getPhotos().clear();
            directory.setPhotos(null);
        }
        directories.clear();
        directories = null;
    }
}
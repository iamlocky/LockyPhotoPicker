package cn.lockyluo.photopicker.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;


import java.util.ArrayList;
import java.util.List;

import cn.lockyluo.photopicker.R;
import cn.lockyluo.photopicker.adapter.PhotoPagerAdapter;
import cn.lockyluo.photopicker.entity.Photo;
import cn.lockyluo.photopicker.event.SimpleListener;

/**
 * Created by donglua on 15/6/21.
 */
public class ImagePagerFragment extends Fragment {

    public final static String ARG_CURRENT_ITEM = "ARG_CURRENT_ITEM";
    private static final String TAG = "ImagePagerFragment";
    private ArrayList<String> paths = new ArrayList<>();
    private ViewPager mViewPager;
    private PhotoPagerAdapter mPagerAdapter;
    private SimpleListener simpleListener;

    private int currentItem = 0;


    public static ImagePagerFragment newInstance(List<String> paths, int currentItem) {
        ImagePagerFragment f = new ImagePagerFragment();
        f.setPaths(paths);
        Bundle args = new Bundle();
        args.putInt(ARG_CURRENT_ITEM, currentItem);

        f.setArguments(args);

        return f;
    }

    public void setSimpleListener(SimpleListener simpleListener) {
        this.simpleListener = simpleListener;
    }

    public void setPaths(List<String> paths) {
        this.paths.addAll(paths);
    }

    public void setPhotos(List<String> paths, int currentItem) {
        this.paths.clear();
        this.paths.addAll(paths);
        this.currentItem = currentItem;

        mViewPager.setCurrentItem(currentItem);
        mViewPager.getAdapter().notifyDataSetChanged();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();

        if (bundle != null) {
            currentItem = bundle.getInt(ARG_CURRENT_ITEM);
        }

        mPagerAdapter = new PhotoPagerAdapter(Glide.with(this), paths);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.__picker_picker_fragment_image_pager, container, false);

        mViewPager = rootView.findViewById(R.id.vp_photos);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(currentItem);
        mViewPager.setOffscreenPageLimit(5);

        if (simpleListener != null) {
            simpleListener.onDone((currentItem + 1) + "/" + paths.size());
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (simpleListener != null) {
                    simpleListener.onDone((position + 1) + "/" + paths.size());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return rootView;
    }


    public ViewPager getViewPager() {
        return mViewPager;
    }


    public ArrayList<String> getPaths() {
        return paths;
    }


    public int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }

    public String getCurrentPhotoPath() {
        return paths.get(getCurrentItem());
    }

    @Override
    public void onDestroy() {
        if (simpleListener != null) {
            simpleListener.onDone(getString(R.string.__picker_title));
        }
        super.onDestroy();
        paths.clear();
        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }
    }
}

package cn.lockyluo.photopicker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import cn.lockyluo.photopicker.fragment.ImagePagerFragment;
import cn.lockyluo.photopicker.widget.MultiPickResultView;
import cn.lockyluo.photopicker.widget.Titlebar;

import static cn.lockyluo.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;
import static cn.lockyluo.photopicker.PhotoPreview.EXTRA_ACTION;
import static cn.lockyluo.photopicker.PhotoPreview.EXTRA_CURRENT_ITEM;
import static cn.lockyluo.photopicker.PhotoPreview.EXTRA_PHOTOS;

/**
 * Created by donglua on 15/6/24.
 * Updated by LockyLuo on 18/8/3.
 */
public class PhotoPagerActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private ImagePagerFragment pagerFragment;
    private static final String TAG = "PhotoPagerActivity";
    private Titlebar titlebar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.__picker_activity_photo_pager);
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int currentItem = getIntent().getIntExtra(EXTRA_CURRENT_ITEM, 0);
        List<String> paths = getIntent().getStringArrayListExtra(EXTRA_PHOTOS);
        int action = getIntent().getIntExtra(EXTRA_ACTION, MultiPickResultView.ACTION_ONLY_SHOW);
        if (pagerFragment == null) {
            pagerFragment =
                    (ImagePagerFragment) getSupportFragmentManager().findFragmentById(R.id.photoPagerFragment);
        }
        pagerFragment.setPhotos(paths, currentItem);
        titlebar = findViewById(R.id.titlebar);
        titlebar.init(this);

        titlebar.setLeftOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (action == MultiPickResultView.ACTION_SELECT) {
            titlebar.setRight(getApplicationContext().getResources().getDrawable(R.drawable.__picker_delete), "", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = pagerFragment.getViewPager().getCurrentItem();
                    if (pagerFragment.getPaths().size() > 0) {
                        pagerFragment.getPaths().remove(position);
                        pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();
                        if (pagerFragment.getPaths().size() == 0) {
                            titlebar.setTitle(getString(R.string.__picker_preview) + " " + getString(R.string.__picker_image_index, 0,
                                    pagerFragment.getPaths().size()));
                        }
                    }
                    setDataResult();
                }
            });
        }

        titlebar.setTitle(getString(R.string.__picker_preview));


        pagerFragment.getViewPager().addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                titlebar.setTitle(getString(R.string.__picker_preview) + " " + getString(R.string.__picker_image_index, pagerFragment.getViewPager().getCurrentItem() + 1,
                        pagerFragment.getPaths().size()));
            }
        });
    }


    @Override
    public void onBackPressed() {

        setDataResult();
        finish();

        super.onBackPressed();
    }

    private void setDataResult() {
        Intent intent = new Intent();
        intent.putExtra(KEY_SELECTED_PHOTOS, pagerFragment.getPaths());
        setResult(RESULT_OK, intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deletePic() {
        final int index = pagerFragment.getCurrentItem();
        final String deletedPath = pagerFragment.getPaths().get(index);

        Snackbar snackbar = Snackbar.make(pagerFragment.getView(), R.string.__picker_deleted_a_photo,
                Snackbar.LENGTH_LONG);

        if (pagerFragment.getPaths().size() <= 1) {

            // show confirm dialog
            new AlertDialog.Builder(this)
                    .setTitle(R.string.__picker_confirm_to_delete)
                    .setPositiveButton(R.string.__picker_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            pagerFragment.getPaths().remove(index);
                            pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();
                            onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.__picker_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();

        } else {

            snackbar.show();

            pagerFragment.getPaths().remove(index);
            pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();
        }

        snackbar.setAction(R.string.__picker_undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pagerFragment.getPaths().size() > 0) {
                    pagerFragment.getPaths().add(index, deletedPath);
                } else {
                    pagerFragment.getPaths().add(deletedPath);
                }
                pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();
                pagerFragment.getViewPager().setCurrentItem(index, true);
            }
        });
    }


}

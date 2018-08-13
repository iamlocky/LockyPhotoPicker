package cn.lockyluo.photopicker.widget;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import cn.lockyluo.photopicker.PhotoPickUtils;
import cn.lockyluo.photopicker.PhotoPreview;
import cn.lockyluo.photopicker.R;
import cn.lockyluo.photopicker.utils.AndroidLifecycleUtils;
import cn.lockyluo.photopicker.utils.ImageCaptureManager;
import cn.lockyluo.photopicker.utils.ToastUtil;

/**
 * Created by donglua on 15/5/31.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private ArrayList<String> photoPaths;
    private LinkedHashSet<String> photoSet = new LinkedHashSet<>();
    private LayoutInflater inflater;

    private Context mContext;
    private RequestOptions options = new RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.__picker_default_weixin)
            .error(R.drawable.__picker_ic_broken_image_black_48dp);
    private RequestManager glide;

    private static final String TAG = "PhotoAdapter";
    private int padding;

    public void setAction(@MultiPickResultView.MultiPicAction int action) {
        this.action = action;
    }

    private int action;
    private int maxCount;
    final static int TYPE_ADD = 1;
    final static int TYPE_PHOTO = 2;

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }


    public PhotoAdapter(Context mContext, ArrayList<String> photoPaths, int maxCount) {
        this.maxCount = maxCount;
        this.photoPaths = photoPaths;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
        padding = dp2Px(8);
        glide = Glide.with(mContext);
    }

    public void add(ArrayList<String> photoPaths) {
        if (photoPaths != null && photoPaths.size() > 0) {
            this.photoPaths.addAll(photoPaths);
            notifyDataSetChanged();
        }

    }

    public void refresh(ArrayList<String> photoPaths) {
        photoSet.clear();
        photoSet.addAll(this.photoPaths);
        this.photoPaths.clear();

        if (photoPaths != null && photoPaths.size() > 0) {
            boolean bo=photoSet.addAll(photoPaths);
            this.photoPaths.addAll(photoSet);
            if (!bo) {
                ToastUtil.show(mContext.getString(R.string.__picker_had_selected_same_picture));
            }
        } else {
            Log.e(TAG, "refresh: photoPaths is null");
        }
        notifyDataSetChanged();
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        switch (viewType) {
            case TYPE_ADD:
                itemView = inflater.inflate(R.layout.__picker_item_add, parent, false);
                break;
            case TYPE_PHOTO:
                itemView = inflater.inflate(R.layout.__picker_item_photo, parent, false);
                break;
        }
        return new PhotoViewHolder(itemView, viewType);

    }

    public int dp2Px(int dp) {
        // px/dp = density;
        float density = mContext.getResources().getDisplayMetrics().density;
        int px = (int) (dp * density + .5f);
        return px;
    }


    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, final int position) {

        if (action == MultiPickResultView.ACTION_SELECT) {
            //图片选择模式
            if (getItemViewType(position) == TYPE_ADD) {//最后一个如果没满最大值，点击能够添加图片
                holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPicker(false);
                    }
                });


            } else {
                holder.ivPhoto.setPadding(padding, padding, padding, padding);

                String pathStr = photoPaths.get(position);
                Uri uri;

                if (pathStr.startsWith("http")) {
                    uri = Uri.parse(pathStr);
                } else {
                    uri = ImageCaptureManager.fileToUri(mContext, pathStr);
                }
                boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(holder.ivPhoto.getContext());

                if (canLoadImage) {
                    glide
                            .load(uri)
                            .thumbnail(0.1f)
                            .apply(options.centerCrop())
                            .into(holder.ivPhoto);
                }

                holder.deleteBtn.setVisibility(View.VISIBLE);
                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        photoPaths.remove(position);
                        notifyDataSetChanged();
                    }
                });

                holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPreview(position);
                    }
                });
            }
        } else if (action == MultiPickResultView.ACTION_ONLY_SHOW) {
            //仅显示
            glide
                    .load(photoPaths.get(position))
                    .apply(options.centerInside())
                    .thumbnail(0.1f)
                    .into(holder.ivPhoto);

            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPreview(position);
                }
            });
        }
    }

    //预览照片
    void startPreview(int position) {
        PhotoPreview.builder()
                .setPhotos(photoPaths)
                .setAction(action)
                .setCurrentItem(position)
                .start((Activity) mContext);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == photoPaths.size() && position != maxCount) ? TYPE_ADD : TYPE_PHOTO;
    }

    @Override
    public int getItemCount() {
        int count = photoPaths.size() + 1;
        if (count > maxCount) {
            count = maxCount;
        }
        return action == MultiPickResultView.ACTION_SELECT ? count : photoPaths.size();
    }

    public void startPicker(boolean launchCamera) {
        if (photoPaths != null && photoPaths.size() == maxCount) {
            ToastUtil.show("已选了" + maxCount + "张图片");
        } else {
            PhotoPickUtils.startPick((Activity) mContext, true, launchCamera, maxCount - photoPaths.size(), new ArrayList<String>());
        }
    }

    public void setPhotoPaths(ArrayList<String> photoPaths) {
        this.photoPaths.clear();
        this.photoPaths.addAll(photoPaths);
        notifyDataSetChanged();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private View vSelected;
        public View cover;
        public View deleteBtn;

        public PhotoViewHolder(View itemView, int type) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);

            if (type == TYPE_ADD) {
                return;
            }
            vSelected = itemView.findViewById(R.id.v_selected);
            vSelected.setVisibility(View.GONE);
            cover = itemView.findViewById(R.id.cover);
            cover.setVisibility(View.GONE);
            deleteBtn = itemView.findViewById(R.id.v_delete);
            deleteBtn.setVisibility(View.GONE);
        }
    }

}

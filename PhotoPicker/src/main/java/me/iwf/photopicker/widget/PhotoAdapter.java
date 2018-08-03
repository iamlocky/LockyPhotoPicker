package me.iwf.photopicker.widget;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

import me.iwf.photopicker.PhotoPickUtils;
import me.iwf.photopicker.PhotoPreview;
import me.iwf.photopicker.R;

/**
 * Created by donglua on 15/5/31.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private ArrayList<String> photoPaths;
    private LayoutInflater inflater;

    private Context mContext;
    private RequestOptions options = new RequestOptions().dontAnimate();

    private static final String TAG = "PhotoAdapter";

    public void setAction(@MultiPickResultView.MultiPicAction int action) {
        this.action = action;
    }

    private int action;

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    private int maxCount = 9;


    public PhotoAdapter(Context mContext, ArrayList<String> photoPaths, int maxCount) {
        this.maxCount = maxCount;
        this.photoPaths = photoPaths;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
        padding = dp2Px(8);

    }

    public void add(ArrayList<String> photoPaths) {
        if (photoPaths != null && photoPaths.size() > 0) {
            this.photoPaths.addAll(photoPaths);
            notifyDataSetChanged();
        }

    }

    public void refresh(ArrayList<String> photoPaths) {
        this.photoPaths.clear();
        if (photoPaths != null && photoPaths.size() > 0) {
            this.photoPaths.addAll(photoPaths);
        } else {
            Log.e(TAG, "refresh: photoPaths is null");
        }
        notifyDataSetChanged();
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.__picker_item_photo, parent, false);
        return new PhotoViewHolder(itemView);
    }

    public int dp2Px(int dp) {
        // px/dip = density;
        float density = mContext.getResources().getDisplayMetrics().density;
        int px = (int) (dp * density + .5f);
        return px;
    }

    int padding;

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, final int position) {

        if (action == MultiPickResultView.ACTION_SELECT) {
            holder.ivPhoto.setPadding(padding, padding, padding, padding);

            if (position == getItemCount() - 1) {//最后一个始终是+号，点击能够跳去添加图片
                options = options
                        .centerCrop()
                        .placeholder(R.drawable.icon_pic_default)
                        .error(R.drawable.icon_pic_default);
                Glide.with(mContext)
                        .load("")
                        .thumbnail(0.1f)
                        .apply(options)
                        .into(holder.ivPhoto);
                holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (photoPaths != null && photoPaths.size() == maxCount) {
                            Toast.makeText(mContext, "已选了" + maxCount + "张图片", Toast.LENGTH_SHORT).show();
                        } else {
                            PhotoPickUtils.startPick((Activity) mContext, false, maxCount, photoPaths);
                        }
                    }
                });

                holder.deleteBtn.setVisibility(View.GONE);

            } else {
                String pathStr = photoPaths.get(position);
                Log.e("file", pathStr);
                Uri uri;

                if (pathStr.startsWith("http")) {
                    uri = Uri.parse(pathStr);
                } else {
                    uri = Uri.fromFile(new File(pathStr));
                }
                options = options
                        .centerCrop()
                        .placeholder(R.drawable.__picker_default_weixin)
                        .error(R.drawable.__picker_ic_broken_image_black_48dp);
                Glide.with(mContext)
                        .load(uri)
                        .thumbnail(0.1f)
                        // .bitmapTransform(new RoundedCornersTransformation(mContext,6,0))
                        .apply(options)
                        .into(holder.ivPhoto);


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
                        PhotoPreview.builder()
                                .setPhotos(photoPaths)
                                .setAction(action)
                                .setCurrentItem(position)
                                .start((Activity) mContext);
                    }
                });
            }
        } else if (action == MultiPickResultView.ACTION_ONLY_SHOW) {

            Log.d("pic", photoPaths.get(position));
            options = options.placeholder(R.drawable.__picker_default_weixin)
                    .centerInside()
                    .error(R.drawable.__picker_ic_broken_image_black_48dp);

            Glide.with(mContext)
                    .load(photoPaths.get(position))
                    .thumbnail(0.1f)
                    .apply(options)
                    .into(holder.ivPhoto);

            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PhotoPreview.builder()
                            .setPhotos(photoPaths)
                            .setAction(action)
                            .setCurrentItem(position)
                            .start((Activity) mContext);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return action == MultiPickResultView.ACTION_SELECT ? photoPaths.size() + 1 : photoPaths.size();
    }


    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private View vSelected;
        public View cover;
        public View deleteBtn;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            vSelected = itemView.findViewById(R.id.v_selected);
            vSelected.setVisibility(View.GONE);
            cover = itemView.findViewById(R.id.cover);
            cover.setVisibility(View.GONE);
            deleteBtn = itemView.findViewById(R.id.v_delete);
            deleteBtn.setVisibility(View.GONE);
        }
    }

}

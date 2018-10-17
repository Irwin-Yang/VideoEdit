package com.ruanchao.videoedit.ui.video;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.bumptech.glide.Glide;
import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.adapter.GlideImageLoader;
import com.ruanchao.videoedit.adapter.SubAdapter;
import com.ruanchao.videoedit.base.BasePresenter;
import com.ruanchao.videoedit.base.BaseViewHolder;
import com.ruanchao.videoedit.bean.ToolItem;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.DensityUtil;
import com.ruanchao.videoedit.util.FileUtil;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;

import java.io.File;
import java.util.List;

public class MainPresenter extends BasePresenter<IMainView> {
    private Context context;
    public static final int BANNER_TYPE = 1;
    public static final int FAST_TOOL_TYPE = 2;
    public static final int ALL_TOOL_TYPE = 3;
    public static final int VIDEO_LIST_TITLE_TYPE = 4;
    public static final int VIDEO_LIST_TYPE = 5;
    public static final int MATERIAL_TYPE = 6;
    public static final int VIDEO_BOX_TYPE = 7;
    private int mImageWidth = 0 ;

    private VideoGridAdapter videoGridAdapter;

    public MainPresenter(Context context) {
        this.context = context;
    }

    public DelegateAdapter initRecyclerView(RecyclerView recyclerView) {
        //初始化
        //创建VirtualLayoutManager对象
        VirtualLayoutManager layoutManager = new VirtualLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        //设置回收复用池大小，（如果一屏内相同类型的 View 个数比较多，需要设置一个合适的大小，防止来回滚动时重新创建 View）
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        recyclerView.setRecycledViewPool(viewPool);
        viewPool.setMaxRecycledViews(0, 20);

        //设置适配器
        DelegateAdapter delegateAdapter = new DelegateAdapter(layoutManager, true);
        recyclerView.setAdapter(delegateAdapter);
        return delegateAdapter;
    }

    public SubAdapter initBannerAdapter(final List<Integer> mImages){
        return new SubAdapter(context,new LinearLayoutHelper(), R.layout.main_banner_layout,1, BANNER_TYPE) {

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                Banner banner = ((BaseViewHolder) holder).getView(R.id.banner);
                //设置banner样式
                banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR)
                        .setImageLoader(new GlideImageLoader())
                        .setImages(mImages)
                        //.setBannerAnimation(Transformer.DepthPage)
                        .isAutoPlay(true)
                        .setDelayTime(2000)
                        .setIndicatorGravity(BannerConfig.CENTER)
                        .start();
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public SubAdapter initFastToolAdapter(final List<ToolItem> fastTools){
        GridLayoutHelper fastToolGridLayoutHelper = new GridLayoutHelper(3);
        fastToolGridLayoutHelper.setBgColor(context.getColor(R.color.common_theme_color));
        return new SubAdapter(context, fastToolGridLayoutHelper,R.layout.gridview_fast_tool_item_layout, fastTools.size(), FAST_TOOL_TYPE){
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                super.onBindViewHolder(holder, position);
                TextView itemName = ((BaseViewHolder) holder).getView(R.id.tv_fast_tool_item);
                itemName.setText(fastTools.get(position).getItemName());
                ImageView itemIcon= ((BaseViewHolder) holder).getView(R.id.iv_fast_tool_item_icon);
                itemIcon.setImageResource(fastTools.get(position).getItemIcon());
                itemIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isViewAttached()){
                            viewRef.get().onFastToolItemClick(position,v);
                        }
                    }
                });
            }
        };
    }

    public SubAdapter initAllToolAdapter(final List<ToolItem> mAllTools){
        GridLayoutHelper allGridLayoutHelper = new GridLayoutHelper(4);
        allGridLayoutHelper.setBgColor(context.getResources().getColor(R.color.all_tool_bg_color));
        allGridLayoutHelper.setVGap(1);
        allGridLayoutHelper.setHGap(1);
        return new SubAdapter(context, allGridLayoutHelper,R.layout.gridview_fast_tool_item_layout, mAllTools.size(),ALL_TOOL_TYPE){
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                super.onBindViewHolder(holder, position);
                TextView itemName = ((BaseViewHolder) holder).getView(R.id.tv_fast_tool_item);
                itemName.setTextSize(12);
                holder.itemView.setBackgroundColor(Color.WHITE);
                itemName.setTextColor(Color.BLACK);
                itemName.setText(mAllTools.get(position).getItemName());
                ImageView itemIcon= ((BaseViewHolder) holder).getView(R.id.iv_fast_tool_item_icon);
                itemIcon.setImageResource(mAllTools.get(position).getItemIcon());
                itemIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isViewAttached()){
                            viewRef.get().onAllToolItemClick(position,v);
                        }
                    }
                });
            }
        };
    }

    public SubAdapter initVideoListTitleAdapter(){
        return new SubAdapter(context,new LinearLayoutHelper(0),R.layout.edited_main_title_layout,1,VIDEO_LIST_TITLE_TYPE){
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                BaseViewHolder baseViewHolder = (BaseViewHolder) holder;
                baseViewHolder.getView(R.id.tv_more_video).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File file = new File(Constans.VIDEO_PATH);
                        FileUtil.startVideoFile(context,file);
                    }
                });
            }
        };
    }

    public SubAdapter initVideoListAdapter(final List<VideoInfo> mVideoInfos){
        LinearLayoutHelper linearLayoutHelper = new LinearLayoutHelper();
        return new SubAdapter(context,linearLayoutHelper,R.layout.video_list_layout,1,VIDEO_LIST_TYPE){
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                BaseViewHolder baseViewHolder = ((BaseViewHolder) holder);
                GridView gridView = baseViewHolder.getView(R.id.gv_video_list);
                final ImageView mNoEditVideoView = baseViewHolder.getView(R.id.iv_no_edit_video);
                if (mVideoInfos == null || mVideoInfos.size() == 0){
                    mNoEditVideoView.setVisibility(View.VISIBLE);
                }
                setOnHasDataListener(new OnHasDataListener() {
                    @Override
                    public void onHasData() {
                        if (mNoEditVideoView.getVisibility() == View.VISIBLE){
                            mNoEditVideoView.setVisibility(View.GONE);
                        }
                    }
                });
                videoGridAdapter = new VideoGridAdapter(mVideoInfos);
                gridView.setAdapter(videoGridAdapter);

            }
        };
    }

    public SubAdapter initMaterialAdapter(final int type){
        return new SubAdapter(context,new LinearLayoutHelper(), R.layout.material_layout,1, type){
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                BaseViewHolder baseViewHolder = (BaseViewHolder) holder;
                ImageView bannerImage = baseViewHolder.getView(R.id.iv_banner);
                if (type == MATERIAL_TYPE) {
                    bannerImage.setImageResource(R.mipmap.material_image);
                }else if (type == VIDEO_BOX_TYPE){
                    bannerImage.setImageResource(R.mipmap.box_image_banner);
                    bannerImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, VideoBoxListActivity.class);
                            context.startActivity(intent);
                        }
                    });
                }
            }
        };
    }

    public SubAdapter initMaterialTitleAdapter(final int type){
        return new SubAdapter(context,
                new LinearLayoutHelper(0),R.layout.edited_main_title_layout,1,type){
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                BaseViewHolder baseViewHolder = (BaseViewHolder) holder;
                TextView mTitleNameView = baseViewHolder.getView(R.id.tv_title_name);

                if (type == MATERIAL_TYPE) {
                    mTitleNameView.setText("视频素材中心");
                }else if (type == VIDEO_BOX_TYPE){
                    mTitleNameView.setText("视频稿件箱");
                }
                baseViewHolder.getView(R.id.tv_more_video).setVisibility(View.INVISIBLE);
            }
        };

    }

    class VideoGridAdapter extends BaseAdapter{

        List<VideoInfo> mVideoInfos;
        public VideoGridAdapter(List<VideoInfo> mVideoInfos) {
            this.mVideoInfos = mVideoInfos;
        }

        @Override
        public int getCount() {
            int count = mVideoInfos.size() > 3 ? 3 : mVideoInfos.size();
            if (mOnHasDataListener !=null && count > 0){
                mOnHasDataListener.onHasData();
            }
            return count;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.video_list_item_layout, null);
            TextView videoTitle = view.findViewById(R.id.tv_video_title);
            ImageView videoImg = view.findViewById(R.id.iv_video_img);
                String videoPath = mVideoInfos.get(position).getPath();
                if (mImageWidth == 0){
                    mImageWidth = (DensityUtil.getWindowWidth()) / 3;
                }

            Glide.with(context)
                    .load(Uri.fromFile(new File(videoPath)))
                    .override(mImageWidth,mImageWidth)
                    .centerCrop()
                    .placeholder(R.mipmap.video_item_default_image)
                    .into(videoImg);
                videoTitle.setText(mVideoInfos.get(position).getVideoTitle());
                videoImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        VideoShowActivity.start(context, mVideoInfos.get(position).getPath(),Constans.TYPE_VIDEO);
                    }
                });
            return view;
        }


    }

    public VideoGridAdapter getVideoGridAdapter() {
        return videoGridAdapter;
    }

    private OnHasDataListener mOnHasDataListener;
    private void setOnHasDataListener(OnHasDataListener onHasDataListener){
        mOnHasDataListener = onHasDataListener;
    }
    interface OnHasDataListener{
        void onHasData();
    }
}

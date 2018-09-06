package com.ruanchao.videoedit.ui.video;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        fastToolGridLayoutHelper.setBgColor(context.getColor(R.color.fast_tool_bg_color));
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
        return new SubAdapter(context,new LinearLayoutHelper(0),R.layout.edited_vidoe_layout,1,VIDEO_LIST_TITLE_TYPE);
    }

    public SubAdapter initVideoListAdapter(final List<VideoInfo> mVideoInfos){
        GridLayoutHelper mVideoGridLayoutHelper = new GridLayoutHelper(3);

        int editVideoCount = 0;
        if (mVideoInfos != null) {
            editVideoCount = mVideoInfos.size() > 3 ? 3 : mVideoInfos.size();
        }
        return new SubAdapter(context, mVideoGridLayoutHelper, R.layout.video_list_item_layout, editVideoCount,VIDEO_LIST_TYPE) {
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                super.onBindViewHolder(holder, position);
                ImageView videoImg = ((BaseViewHolder) holder).getView(R.id.iv_video_img);
                TextView videoTitle = ((BaseViewHolder) holder).getView(R.id.tv_video_title);
                String videoPath = mVideoInfos.get(position).getVideoPath();
                videoImg.setTag(R.id.image_tag1, videoPath);

                if (videoImg.getTag(R.id.image_tag1).equals(videoPath)) {
                    Glide.with(context)
                            .load(Uri.fromFile(new File(videoPath)))
                            .placeholder(R.mipmap.video_item_default_image)
                            .into(videoImg);
                } else {
                    Glide.with(context)
                            .load(Uri.fromFile(new File((String) videoImg.getTag(R.id.image_tag1))))
                            .placeholder(R.mipmap.video_item_default_image)
                            //.override(DensityUtil.dip2px(MainActivity.this, 100), DensityUtil.dip2px(MainActivity.this, 100))
                            .fitCenter()
                            .into(videoImg);
                }
                videoTitle.setText(mVideoInfos.get(position).getVideoTitle());
                videoImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        VideoShowActivity.start(context, mVideoInfos.get(position).getVideoPath());
                    }
                });
            }
        };
    }
}

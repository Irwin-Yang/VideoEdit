package com.ruanchao.videoedit.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.bean.VideoInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataListAdapter extends RecyclerView.Adapter{

    private List<VideoInfo> mVideoInfos = new ArrayList<>();
    private Context context;

    public DataListAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.data_list_item_layout, null, false);
        return new DataHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        DataHolder dataHolder = (DataHolder) holder;
        dataHolder.mItemPath.setText(mVideoInfos.get(position).getPath());
        dataHolder.mItemTitle.setText(mVideoInfos.get(position).getVideoName());
        String videoPath = mVideoInfos.get(position).getPath();
        Glide.with(context)
                .load(Uri.fromFile(new File(videoPath)))
                .centerCrop()
                .placeholder(R.mipmap.video_item_default_image)
                .into(dataHolder.mItemIcon);
        dataHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnclickListener != null){
                    mOnclickListener.setOnclick(mVideoInfos.get(position));
                }
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder != null) {
            DataHolder dataHolder = (DataHolder) holder;
            Glide.clear(dataHolder.mItemIcon);
        }
        super.onViewRecycled(holder);

    }

    @Override
    public int getItemCount() {
        return mVideoInfos.size();
    }

    public void setVideoInfos(List<VideoInfo> mVideoInfos) {
        this.mVideoInfos = mVideoInfos;
        notifyDataSetChanged();
    }

    class DataHolder extends RecyclerView.ViewHolder{

        public TextView mItemPath;
        public TextView mItemTitle;
        public ImageView mItemIcon;

        public DataHolder(View itemView) {
            super(itemView);

            mItemPath = itemView.findViewById(R.id.tv_data_item_path);
            mItemTitle = itemView.findViewById(R.id.tv_data_item_title);
            mItemIcon = itemView.findViewById(R.id.iv_data_item_icon);
        }
    }

    private OnclickListener mOnclickListener;

    public void setOnclickListener(OnclickListener onclickListener){
        mOnclickListener = onclickListener;
    }

    public interface OnclickListener{
        void setOnclick(VideoInfo videoInfo);
    }
}

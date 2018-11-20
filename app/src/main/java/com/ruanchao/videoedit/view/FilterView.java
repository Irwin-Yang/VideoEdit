package com.ruanchao.videoedit.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.bean.FilterInfo;
import com.ruanchao.videoedit.util.GlideCircleTransform;

import java.util.ArrayList;
import java.util.List;

public class FilterView extends LinearLayout{

    private RecyclerView mRecyclerView;
    private Context mContext;
    private List<FilterInfo> mFilterInfoList = new ArrayList<>();
    private int lastClickPosition = -1;

    public final static int[] mFilterImage = {
            R.mipmap.filter1,
            R.mipmap.filter2,
            R.mipmap.filter3,
            R.mipmap.filter4,
            R.mipmap.filter5,
            R.mipmap.filter6,
            R.mipmap.filter7,
            R.mipmap.filter8,
            R.mipmap.filter9,
            R.mipmap.filter10,
            R.mipmap.filter11,
            R.mipmap.filter12,
            R.mipmap.filter1,
            R.mipmap.filter2,
            R.mipmap.filter3,
            R.mipmap.filter4,
            R.mipmap.filter5,
    };
    public final static String[] mFilterName =

            {
                    "怀旧",
                    "清新",
                    "锐化",
                    "素雅",
                    "补光",
                    "泛黄",
                    "高亮",
                    "全彩",
                    "虚光",
                    "冷色",
                    "微调",
                    "暖色",
                    "冷化",
                    "灰色",
                    "增强",
                    "经典",
                    "斑点"
            };


    public FilterView(Context context) {
        super(context);
        init(context);
    }

    public FilterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FilterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FilterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.filter_view_layout, this);
        mRecyclerView = view.findViewById(R.id.rv_filter);
        initData();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(new FilterAdapter());
    }

    private void initData() {
        for (int i = 0; i<17;i++){
            FilterInfo filterInfo = new FilterInfo();
            filterInfo.setFilterID(i + 1);
            filterInfo.setFilterImage(mFilterImage[i]);
            filterInfo.setFilterName(mFilterName[i]);
            mFilterInfoList.add(filterInfo);
        }
    }

    class FilterAdapter extends RecyclerView.Adapter{

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.filter_item_layout, parent, false);


            return new FilterHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            FilterHolder filterHolder = (FilterHolder) holder;
            Glide.with(mContext)
                    .load(mFilterInfoList.get(position).getFilterImage())
                    .transform(new GlideCircleTransform(mContext))
                    .into(filterHolder.mFilterImage);

            filterHolder.mFilterName.setText(mFilterInfoList.get(position).getFilterName());
            if (mFilterInfoList.get(position).isFilterStateChecked()){
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.record_bg_color));
            }else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFilterItemOnclickListener !=null){
                        mFilterItemOnclickListener.onItemClick(mFilterInfoList.get(position));
                        mFilterInfoList.get(position).setFilterStateChecked(true);
                        //把上一次点击的取消掉
                        if (lastClickPosition >=0){
                            mFilterInfoList.get(lastClickPosition).setFilterStateChecked(false);
                        }
                        lastClickPosition = position;
                        notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFilterInfoList.size();
        }
    }

    static class FilterHolder extends RecyclerView.ViewHolder{

        public ImageView mFilterImage;
        public TextView mFilterName;

        public FilterHolder(View itemView) {
            super(itemView);

            mFilterImage = itemView.findViewById(R.id.iv_filter_view);

            mFilterName = itemView.findViewById(R.id.tv_filter_name);

        }
    }

    private FilterItemOnclickListener mFilterItemOnclickListener;

    public void setFilterItemOnclickListener(FilterItemOnclickListener listener){
        mFilterItemOnclickListener = listener;
    }

    public interface FilterItemOnclickListener {
        void onItemClick(FilterInfo filterInfo);
    }

}

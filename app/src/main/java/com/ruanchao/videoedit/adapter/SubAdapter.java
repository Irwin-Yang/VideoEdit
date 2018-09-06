package com.ruanchao.videoedit.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.ruanchao.videoedit.base.BaseViewHolder;

public class SubAdapter extends DelegateAdapter.Adapter{

    private Context context;
    private LayoutHelper layoutHelper;
    private int count;
    private int layoutId;
    private int mViewTypeItem;

    public SubAdapter(Context context, LayoutHelper layoutHelper, int layoutId, int count,int viewTypeItem) {
        this.context = context;
        this.layoutHelper = layoutHelper;
        this.count = count;
        this.layoutId = layoutId;
        this.mViewTypeItem = viewTypeItem;
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return layoutHelper;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BaseViewHolder(LayoutInflater.from(context).inflate(layoutId,null));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    /**
     * 必须重写不然会出现滑动不流畅的情况
     */
    @Override
    public int getItemViewType(int position) {
        return mViewTypeItem;
    }

    /**
     * 外面可能需要重写，不然count数量就固定了，不符合预期
     * @return
     */
    @Override
    public int getItemCount() {
        return count;
    }
}

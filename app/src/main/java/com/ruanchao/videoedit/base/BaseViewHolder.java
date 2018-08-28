package com.ruanchao.videoedit.base;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

public class BaseViewHolder extends RecyclerView.ViewHolder{

    SparseArray<View> mViewArr;
    View itemView;

    public BaseViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        mViewArr = new SparseArray<>();
    }

    public<T extends View> T getView(int viewId){
        if (mViewArr.get(viewId) == null){
            View view = itemView.findViewById(viewId);
            mViewArr.put(viewId, view);
        }
        return (T) mViewArr.get(viewId);
    }
}

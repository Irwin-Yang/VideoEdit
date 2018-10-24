package com.ruanchao.videoedit.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.ruanchao.videoedit.R;

public class FilterView extends LinearLayout{

    private RecyclerView mRecyclerView;
    private Context mContext;

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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(new FilterAdapter());
    }

    class FilterAdapter extends RecyclerView.Adapter{

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.filter_item_layout, parent, false);


            return new FilterHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            FilterHolder filterHolder = (FilterHolder) holder;
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFilterItemOnclickListener !=null){
                        mFilterItemOnclickListener.onItemClick(position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return 17;
        }
    }

    static class FilterHolder extends RecyclerView.ViewHolder{

        public FilterHolder(View itemView) {
            super(itemView);

        }
    }

    private FilterItemOnclickListener mFilterItemOnclickListener;

    public void setFilterItemOnclickListener(FilterItemOnclickListener listener){
        mFilterItemOnclickListener = listener;
    }

    public interface FilterItemOnclickListener {
        void onItemClick(int position);
    }

}

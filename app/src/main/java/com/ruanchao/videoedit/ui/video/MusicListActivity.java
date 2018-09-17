package com.ruanchao.videoedit.ui.video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.ruanchao.videoedit.MainApplication;
import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.adapter.SubAdapter;
import com.ruanchao.videoedit.base.BaseActivity;
import com.ruanchao.videoedit.base.BaseViewHolder;
import com.ruanchao.videoedit.bean.Music;
import com.ruanchao.videoedit.util.FileManager;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MusicListActivity extends Activity {

    private static final String TAG = MusicListActivity.class.getSimpleName();
    RecyclerView mMusicRecycler;
    List<Music> mMusicList = new ArrayList<>();
    SubAdapter mMusicAdapter;
    Subscription subscribe;
    public static final String MUSIC_PATH = "music_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        ((CommonTitleBar) findViewById(R.id.titlebar)).setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == CommonTitleBar.ACTION_LEFT_TEXT) {
                    onBackPressed();
                }
            }
        });
        //TODO 需要下拉加载更多
        mMusicRecycler = findViewById(R.id.music_list);
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        viewPool.setMaxRecycledViews(1,10);
        mMusicRecycler.setRecycledViewPool(viewPool);
        VirtualLayoutManager virtualLayoutManager = new VirtualLayoutManager(this);
        mMusicRecycler.setLayoutManager(virtualLayoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.sw_recycleview_divicer));
        mMusicRecycler.addItemDecoration(divider);
        DelegateAdapter delegateAdapter = new DelegateAdapter(virtualLayoutManager);
        mMusicRecycler.setAdapter(delegateAdapter);
        mMusicAdapter = new SubAdapter(MusicListActivity.this, new LinearLayoutHelper(1), R.layout.music_item_layout,
                mMusicList.size(), 1) {
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                super.onBindViewHolder(holder, position);
                BaseViewHolder baseViewHolder = (BaseViewHolder) holder;
                TextView mMusicName = baseViewHolder.getView(R.id.tv_music_name);
                TextView mMusicAuthor = baseViewHolder.getView(R.id.tv_music_author);
                mMusicName.setText(mMusicList.get(position).getName());
                mMusicAuthor.setText(mMusicList.get(position).getArtist());
                baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String path = mMusicList.get(position).getPath();
                        Intent intent = getIntent().putExtra(MUSIC_PATH,path);
                        setResult(VideoEditActivity.REQUEST_CODE, intent);
                        finish();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return  mMusicList.size();
            }
        };
        delegateAdapter.addAdapter(mMusicAdapter);

        mMusicList = FileManager.getInstance(MainApplication.getContext()).getMusics();
        getMusicList();
    }

    public static void startActivityForResult(Activity context,int code){
        context.startActivityForResult(new Intent(context, MusicListActivity.class),code);
    }

    public void getMusicList() {
        subscribe = Observable.create(new Observable.OnSubscribe<List<Music>>() {
            @Override
            public void call(Subscriber<? super List<Music>> subscriber) {
                List<Music> musics = FileManager.getInstance(MainApplication.getContext()).getMusics();
                subscriber.onNext(musics);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Music>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<Music> musicList) {
                        mMusicList = musicList;
                        mMusicAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscribe != null && !subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
            subscribe = null;
        }

    }
}

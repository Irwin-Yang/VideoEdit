package com.ruanchao.videoedit.ui.video;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.adapter.DataListAdapter;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.view.RecycleViewDecoration;
import com.ruanchao.videoedit.view.state.MultiStateView;
import com.ruanchao.videoedit.view.state.SimpleMultiStateView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.ruanchao.videoedit.util.Constans.VIDEO_CATEGORY;
import static com.ruanchao.videoedit.util.Constans.TYPE_AUDIO;
import static com.ruanchao.videoedit.util.Constans.TYPE_IMAGE;
import static com.ruanchao.videoedit.util.Constans.TYPE_VIDEO;

public class VideoListFragment extends Fragment{

    private RecyclerView mVideoRecycler;
    private int mDataType;
    private DataListAdapter mDataListAdapter;

    protected SimpleMultiStateView mSimpleMultiStateView;

    public static VideoListFragment newInstance(int category){
        VideoListFragment videoListFragment = new VideoListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(VIDEO_CATEGORY, category);
        videoListFragment.setArguments(bundle);
        return videoListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_list_layout, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    private void initView(View view) {
        mVideoRecycler = view.findViewById(R.id.rv_video_recycler);
        mVideoRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDataListAdapter = new DataListAdapter(getActivity());
        mDataListAdapter.setOnclickListener(new DataListAdapter.OnclickListener() {
            @Override
            public void setOnclick(VideoInfo videoInfo) {
                VideoShowActivity.start(getActivity(), videoInfo.getPath(),mDataType);
            }
        });
        mVideoRecycler.addItemDecoration(new RecycleViewDecoration(getActivity(),RecycleViewDecoration.VERTICAL_LIST));
        mVideoRecycler.setAdapter(mDataListAdapter);
        mDataType = getArguments().getInt(VIDEO_CATEGORY);
        mSimpleMultiStateView = view.findViewById(R.id.SimpleMultiStateView);
        initStateView();
        mSimpleMultiStateView.setViewState(MultiStateView.STATE_LOADING);
    }

    private void initData() {
        Observable.create(new Observable.OnSubscribe<List<VideoInfo>>() {
            @Override
            public void call(Subscriber<? super List<VideoInfo>> subscriber) {
                List<VideoInfo> videoInfos = getVideoList();
                subscriber.onNext(videoInfos);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<VideoInfo>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mSimpleMultiStateView.setViewState(MultiStateView.STATE_EMPTY);
            }

            @Override
            public void onNext(List<VideoInfo> videoInfos) {
                if (videoInfos == null || videoInfos.size() == 0){
                    mSimpleMultiStateView.setViewState(MultiStateView.STATE_EMPTY);
                }else {
                    mDataListAdapter.setVideoInfos(videoInfos);
                    mSimpleMultiStateView.setViewState(MultiStateView.STATE_CONTENT);
                }
            }
        });
    }

    private List<VideoInfo> getVideoList() {
        String path = Constans.VIDEO_PATH;
        if (mDataType == TYPE_VIDEO){
            path = Constans.VIDEO_PATH;
        }else if (mDataType == TYPE_IMAGE){
            path = Constans.IMAGE_PATH;
        }else  if (mDataType  == TYPE_AUDIO){
            path = Constans.AUDIO_PATH;
        }
        File dataFile = new File(path);
        if(!dataFile.exists() || !dataFile.isDirectory()){
            return null;
        }
        File[] files = dataFile.listFiles();
        if (files == null || files.length == 0){
            return null;
        }
        List<VideoInfo> videoInfos = new ArrayList<>();
        for (File file:files){
            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setVideoName(file.getName());
            videoInfo.setPath(file.getAbsolutePath());
            videoInfo.setVideoTime(file.lastModified());
            videoInfos.add(videoInfo);
        }
        Collections.sort(videoInfos);
        return videoInfos;
    }

    public void initStateView() {
        if (mSimpleMultiStateView == null) return;
        mSimpleMultiStateView.setEmptyResource(R.layout.sw_view_empty)
                .setRetryResource(R.layout.sw_view_retry)
                .setLoadingResource(R.layout.sw_view_loading)
                .setNoNetResource(R.layout.sw_view_nonet)
                .build();
    }

}

package com.ruanchao.videoedit.ui.video;

import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.ruanchao.videoedit.MainApplication;
import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.adapter.SubAdapter;
import com.ruanchao.videoedit.base.BaseMvpActivity;
import com.ruanchao.videoedit.bean.EditInfo;
import com.ruanchao.videoedit.bean.ToolItem;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.event.EditFinishMsg;
import com.ruanchao.videoedit.ffmpeg.FFmpegCmd;
import com.ruanchao.videoedit.ui.tools.VideoEditToolActivity;
import com.ruanchao.videoedit.util.AliOssUtil;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.DateUtil;
import com.ruanchao.videoedit.util.FFmpegUtil;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ruanchao.videoedit.ui.video.MainPresenter.VIDEO_LIST_TITLE_TYPE;

public class MainActivity extends BaseMvpActivity<IMainView,MainPresenter> implements View.OnClickListener,IMainView {

    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "smallvideo";
    private static final String TAG = "MainActivity";
    private RecyclerView mMainRecycler;
    private DelegateAdapter mDelegateAdapter;
    private List<Integer> mImages = new ArrayList<>();
    private List<ToolItem> mFastTools = new ArrayList<>();
    private List<ToolItem> mAllTools = new ArrayList<>();
    private List<VideoInfo> mVideoInfos;
    private SubAdapter mVideoAdapter;
    private double y;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main);
        Button mBtnTest = findViewById(R.id.btn_test);
        mBtnTest.setOnClickListener(this);
        initList();
        initRecycler();
    }

    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter(MainApplication.getContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initRecycler() {
        mMainRecycler = findViewById(R.id.recycler_main);
        mDelegateAdapter = mPresenter.initRecyclerView(mMainRecycler);
        //banner
        SubAdapter bannerAdapter = mPresenter.initBannerAdapter(mImages);
        mDelegateAdapter.addAdapter(bannerAdapter);
        SubAdapter fastToolAdapter = mPresenter.initFastToolAdapter(mFastTools);
        mDelegateAdapter.addAdapter(fastToolAdapter);
        SubAdapter allToolAdapter = mPresenter.initAllToolAdapter(mAllTools);
        mDelegateAdapter.addAdapter(allToolAdapter);
        mDelegateAdapter.addAdapter(mPresenter.initVideoListTitleAdapter());
        mVideoInfos = parseLiveVideoFile();
        mVideoAdapter = mPresenter.initVideoListAdapter(mVideoInfos);
        mDelegateAdapter.addAdapter(mVideoAdapter);
        mDelegateAdapter.addAdapter(mPresenter.initMaterialTitleAdapter(MainPresenter.VIDEO_BOX_TYPE));
        mDelegateAdapter.addAdapter(mPresenter.initMaterialAdapter(MainPresenter.VIDEO_BOX_TYPE));
        mDelegateAdapter.addAdapter(mPresenter.initMaterialTitleAdapter(MainPresenter.MATERIAL_TYPE));
        mDelegateAdapter.addAdapter(mPresenter.initMaterialAdapter(MainPresenter.MATERIAL_TYPE));
        mDelegateAdapter.addAdapter(new SubAdapter(this,new LinearLayoutHelper(),
                R.layout.edited_main_title_layout,0,8));

        final double maxAlphaEffectHeight = 240.0;
        final CommonTitleBar titleBar = findViewById(R.id.titlebar);
        titleBar.setBackgroundColor(Color.parseColor("#00ffffff"));
        titleBar.toggleStatusBarMode();
        mMainRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            String alphaHex;
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                y = y + dy;
                if (y <= maxAlphaEffectHeight) {
                    int alpha = (int) (y / maxAlphaEffectHeight * 255);
                    alphaHex = Integer.toString(alpha, 16).toUpperCase();
                    if (alphaHex.length() == 1) {
                        alphaHex = "0" + alphaHex;
                    }
                    setToolbarTransparent(alphaHex, titleBar);
                }else {
                    if (!"ff".equals(alphaHex)) {
                        alphaHex = "ff";
                        setToolbarTransparent(alphaHex, titleBar);
                    }
                }
            }
        });
    }

    private void setToolbarTransparent(String alphaHex, CommonTitleBar titleBar) {
        String color = "#" + alphaHex + "03A9F4";
        titleBar.setBackgroundColor(Color.parseColor(color));
        String color2 = "#" + alphaHex + "ffffff";
        titleBar.getCenterTextView().setTextColor(Color.parseColor(color2));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_test:
                break;

            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EditFinishMsg editFinishMsg) {

        switch (editFinishMsg.getVideoInfo().getType()){
            case VideoInfo.TYPE_VIDEO:
                mVideoInfos.add(0, editFinishMsg.getVideoInfo());
                mPresenter.getVideoGridAdapter().notifyDataSetChanged();

                break;
                default:
                    break;
        }
    }

    /**
     * 执行ffmpeg命令行
     * @param commandLine commandLine
     */
    private void executeFFmpegCmd(final String commandLine){
        if(commandLine == null){
            return;
        }
        FFmpegCmd.execute(commandLine, new FFmpegCmd.OnHandleListener() {
            @Override
            public void onBegin() {
                Log.i(TAG, "handle video onBegin...");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"开始", Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onEnd(int result) {
                Log.i(TAG, "handle video onEnd...");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"结束", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void initList() {
        mImages.add(R.mipmap.banner1);
        mImages.add(R.mipmap.banner2);
        mImages.add(R.mipmap.banner3);
        ToolItem toolItem = new ToolItem();
        toolItem.setItemName("拍摄视频");
        toolItem.setItemIcon(R.mipmap.video_record);
        mFastTools.add(toolItem);
        ToolItem toolItem2 = new ToolItem();
        toolItem2.setItemName("编辑视频");
        toolItem2.setItemIcon(R.mipmap.edit_video);
        mFastTools.add(toolItem2);
        ToolItem toolItem3 = new ToolItem();
        toolItem3.setItemName("裁剪视频");
        toolItem3.setItemIcon(R.mipmap.video_to_cut);
        mFastTools.add(toolItem3);

        ToolItem toolItem4 = new ToolItem();
        toolItem4.setItemName("视频转gif");
        toolItem4.setItemIcon(R.mipmap.gif);
        mAllTools.add(toolItem4);
        ToolItem toolItem5 = new ToolItem();
        toolItem5.setItemName("格式转换");
        toolItem5.setItemIcon(R.mipmap.video_change);
        mAllTools.add(toolItem5);
        ToolItem toolItem6 = new ToolItem();
        toolItem6.setItemName("图片合视频");
        toolItem6.setItemIcon(R.mipmap.image_combine);
        mAllTools.add(toolItem6);
        ToolItem toolItem7 = new ToolItem();
        toolItem7.setItemName("加水印");
        toolItem7.setItemIcon(R.mipmap.video_water);
        mAllTools.add(toolItem7);
        ToolItem toolItem8 = new ToolItem();
        toolItem8.setItemName("提取音频");
        toolItem8.setItemIcon(R.mipmap.audio);
        mAllTools.add(toolItem8);
        ToolItem toolItem9 = new ToolItem();
        toolItem9.setItemName("加背景音乐");
        toolItem9.setItemIcon(R.mipmap.bg_audio);
        mAllTools.add(toolItem9);
        ToolItem toolItem10 = new ToolItem();
        toolItem10.setItemName("视频压缩");
        toolItem10.setItemIcon(R.mipmap.video_press);
        mAllTools.add(toolItem10);
        ToolItem toolItem11 = new ToolItem();
        toolItem11.setItemName("更多");
        toolItem11.setItemIcon(R.mipmap.more_tools);
        mAllTools.add(toolItem11);
    }

    private List<VideoInfo> parseLiveVideoFile() {
        File file = new File(Constans.VIDEO_PATH);
        if (!file.isDirectory()){
            return null;
        }
        File[] videoFiles = file.listFiles();
        List<VideoInfo> list = new ArrayList<>();
        for (File videoFile : videoFiles){
            if(videoFile.exists() && videoFile.getName().endsWith(".mp4")){
                VideoInfo liveVideoInfo = new VideoInfo();
                liveVideoInfo.setVideoPath(videoFile.getAbsolutePath());
                liveVideoInfo.setVideoName(videoFile.getName());
                liveVideoInfo.setVideoTime(videoFile.lastModified());
                liveVideoInfo.setVideoTitle(DateUtil.timeToDate(videoFile.lastModified()));
                list.add(liveVideoInfo);
            }
        }
        Collections.sort(list);
        return list;
    }

    @Override
    public void onFastToolItemClick(int position, View v) {
        switch (position){
            //拍摄视频
            case 0:
                RecordActivity.startRecordActivity(MainActivity.this);
                break;
            case 1:
                VideoEditActivity.start(this,null);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAllToolItemClick(int position, View v) {
        switch (position){
            //视频转gif
            case 0:
                VideoEditToolActivity.start(this , EditInfo.EDIT_TYPE_VIDEO_TO_GIF);
                break;
            case 1:
                break;
                //视频转gif
            case 2:
                VideoEditToolActivity.start(this , EditInfo.EDIT_TYPE_IMAGE_TO_VIDEO);
                break;
            default:
                break;
        }
    }

    @Override
    public void showToast(String msg) {

    }

    @Override
    public void showErr() {

    }
}

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
    private static final String srcFile = PATH + File.separator + "hello.mp4";
    private static final String appendVideo = PATH + File.separator + "test.mp4";
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
                R.layout.edited_main_title_layout,20,8));

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
                testVideo();
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

    private void testVideo() {
        String commandLine = null;
        int handleType =4;
        switch (handleType){
            case 0://视频转码:mp4转flv、wmv, 或者flv、wmv转Mp4
                String transformVideo = PATH + File.separator + "transformVideo.flv";
                commandLine = FFmpegUtil.transformVideo(srcFile, transformVideo);
                break;
            case 1://视频剪切
                String cutVideo = PATH + File.separator + "cutVideo.mp4";
                int startTime = 0;
                int duration = 20;
                commandLine = FFmpegUtil.cutVideo(srcFile, startTime, duration, cutVideo);
                break;
            case 2://视频合并
//                commandLine = FFmpegUtil.toTs(srcFile, ts1);
//                concatStep ++;
//                String concatVideo = PATH + File.separator + "concatVideo.mp4";
//                String appendVideo = PATH + File.separator + "test.mp4";
//                File concatFile = new File(PATH + File.separator + "fileList.txt");
//                try {
//                    FileOutputStream fileOutputStream = new FileOutputStream(concatFile);
//                    fileOutputStream.write(("file \'" + srcFile + "\'").getBytes());
//                    fileOutputStream.write("\n".getBytes());
//                    fileOutputStream.write(("file \'" + appendVideo + "\'").getBytes());
//                    fileOutputStream.flush();
//                    fileOutputStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                commandLine = FFmpegUtil.concatVideo(srcFile, concatFile.getAbsolutePath(), concatVideo);
                break;
            case 3://视频截图
                String screenShot = PATH + File.separator + "screenShot.jpg";
                String size = "1080x720";
                commandLine = FFmpegUtil.screenShot(srcFile, size, screenShot);
                break;
            case 4://视频添加水印
                //1、图片
                String photo = PATH + File.separator + "launcher.png";
                String photoMark = PATH + File.separator + "photoMark.mp4";
                commandLine = FFmpegUtil.addWaterMark(appendVideo, photo, photoMark);
                //2、文字
                //String text = "Hello,FFmpeg";
                //String textPath = PATH + File.separator + "text.jpg";
                //boolean result = BitmapUtil.textToPicture(textPath, text, this);
                //Log.i(TAG, "text to pitcture result=" + result);
                //String textMark = PATH + File.separator + "textMark.mp4";
                //commandLine = FFmpegUtil.addWaterMark(appendVideo, photo, textMark);
                break;
            case 5://视频转成gif
                String Video2Gif = PATH + File.separator + "Video2Gif.gif";
                int gifStart = 1;
                int gifDuration = 5;
                commandLine = FFmpegUtil.generateGif(srcFile, gifStart, gifDuration, Video2Gif);
                break;
            case 6://屏幕录制
//                String screenRecord = PATH + File.separator + "screenRecord.mp4";
//                String screenSize = "320x240";
//                int recordTime = 10;
//                commandLine = FFmpegUtil.screenRecord(screenSize, recordTime, screenRecord);
                break;
            case 7://图片合成视频
                //图片所在路径，图片命名格式img+number.jpg
                String picturePath = PATH + File.separator + "img/";
                String combineVideo = PATH + File.separator + "combineVideo.mp4";
                commandLine = FFmpegUtil.pictureToVideo(picturePath, combineVideo);
                break;
            case 8://视频解码播放
                //startActivity(new Intent(VideoHandleActivity.this, VideoPlayerActivity.class));
                return;
            case 9://视频画面拼接:分辨率、时长、封装格式不一致时，先把视频源转为一致
                String input1 = PATH + File.separator + "input1.mp4";
                String input2 = PATH + File.separator + "input2.mp4";
                String outputFile = PATH + File.separator + "multi.mp4";
                //commandLine = FFmpegUtil.multiVideo(input1, input2, outputFile, VideoLayout.LAYOUT_HORIZONTAL);
                break;
            default:
                break;
        }
        executeFFmpegCmd(commandLine);
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

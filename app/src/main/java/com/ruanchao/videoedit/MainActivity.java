package com.ruanchao.videoedit;

import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.ruanchao.videoedit.activity.BaseActivity;
import com.ruanchao.videoedit.activity.RecordActivity;
import com.ruanchao.videoedit.adapter.GlideImageLoader;
import com.ruanchao.videoedit.adapter.SubAdapter;
import com.ruanchao.videoedit.base.BaseViewHolder;
import com.ruanchao.videoedit.bean.ToolItem;
import com.ruanchao.videoedit.ffmpeg.FFmpegCmd;
import com.ruanchao.videoedit.util.DensityUtil;
import com.ruanchao.videoedit.util.FFmpegUtil;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "smallvideo";
    private static final String srcFile = PATH + File.separator + "hello.mp4";
    private static final String appendVideo = PATH + File.separator + "test.mp4";
    private static final String TAG = "MainActivity";
    RecyclerView mMainRecycler;
    DelegateAdapter mDelegateAdapter;
    RecyclerView.RecycledViewPool mRecycledViewPool;
    List<Integer> images = new ArrayList<>();
    List<ToolItem> fastTools = new ArrayList<>();
    List<ToolItem> allTools = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Button mBtnTest = findViewById(R.id.btn_test);
        mBtnTest.setOnClickListener(this);
        initList();
        initRecycler();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initRecycler() {
        mMainRecycler = findViewById(R.id.recycler_main);
        VirtualLayoutManager virtualLayoutManager = new VirtualLayoutManager(this);
        mMainRecycler.setLayoutManager(virtualLayoutManager);
        mRecycledViewPool = new RecyclerView.RecycledViewPool();
        mMainRecycler.setRecycledViewPool(mRecycledViewPool);
        mRecycledViewPool.setMaxRecycledViews(0,20);
        mDelegateAdapter = new DelegateAdapter(virtualLayoutManager,true);
        mMainRecycler.setAdapter(mDelegateAdapter);
        //banner
        mDelegateAdapter.addAdapter(
                new SubAdapter(this,new LinearLayoutHelper(), R.layout.main_banner_layout,1) {

                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                        super.onBindViewHolder(holder, position);
                        Banner banner = ((BaseViewHolder) holder).getView(R.id.banner);
                        //设置banner样式
                        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR)
                                .setImageLoader(new GlideImageLoader())
                                .setImages(images)
                                //.setBannerAnimation(Transformer.DepthPage)
                                .isAutoPlay(true)
                                .setDelayTime(2000)
                                .setIndicatorGravity(BannerConfig.CENTER)
                                .start();
                    }
                });
        GridLayoutHelper fastToolGridLayoutHelper = new GridLayoutHelper(3);
        mDelegateAdapter.addAdapter(
                new SubAdapter(this, fastToolGridLayoutHelper,R.layout.gridview_fast_tool_item_layout,fastTools.size()){
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
                                onFastToolItemClick(position,v);
                            }
                        });
                    }
                });

        fastToolGridLayoutHelper.setBgColor(getColor(R.color.fast_tool_bg_color));
        GridLayoutHelper allGridLayoutHelper = new GridLayoutHelper(4);

        mDelegateAdapter.addAdapter(
                new SubAdapter(this, allGridLayoutHelper,R.layout.gridview_fast_tool_item_layout,allTools.size()){
                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                        super.onBindViewHolder(holder, position);
                        TextView itemName = ((BaseViewHolder) holder).getView(R.id.tv_fast_tool_item);
                        itemName.setTextSize(12);
                        holder.itemView.setBackgroundColor(Color.WHITE);
                        itemName.setTextColor(Color.BLACK);
                        itemName.setText(allTools.get(position).getItemName());
                        ImageView itemIcon= ((BaseViewHolder) holder).getView(R.id.iv_fast_tool_item_icon);
                        itemIcon.setImageResource(allTools.get(position).getItemIcon());
                        itemIcon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onAllToolItemClick(position,v);
                                Toast.makeText(MainActivity.this,"当前快捷菜单："+ position, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
        allGridLayoutHelper.setBgColor(getResources().getColor(R.color.all_tool_bg_color));
        allGridLayoutHelper.setVGap(1);
        allGridLayoutHelper.setHGap(1);

        mDelegateAdapter.addAdapter(
                new SubAdapter(this,new LinearLayoutHelper(0),R.layout.edited_vidoe_layout,1));

    }

    private void onAllToolItemClick(int position, View v) {

    }

    private void onFastToolItemClick(int position, View v) {
        switch (position){
            //拍摄视频
            case 0:
                RecordActivity.startRecordActivity(MainActivity.this);
                break;
            default:
                break;
        }
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
        images.add(R.mipmap.banner1);
        images.add(R.mipmap.banner2);
        images.add(R.mipmap.banner3);
        ToolItem toolItem = new ToolItem();
        toolItem.setItemName("拍摄视频");
        toolItem.setItemIcon(R.mipmap.video_record);
        fastTools.add(toolItem);
        ToolItem toolItem2 = new ToolItem();
        toolItem2.setItemName("编辑视频");
        toolItem2.setItemIcon(R.mipmap.edit_video);
        fastTools.add(toolItem2);
        ToolItem toolItem3 = new ToolItem();
        toolItem3.setItemName("裁剪视频");
        toolItem3.setItemIcon(R.mipmap.video_to_cut);
        fastTools.add(toolItem3);

        ToolItem toolItem4 = new ToolItem();
        toolItem4.setItemName("视频转gif");
        toolItem4.setItemIcon(R.mipmap.gif);
        allTools.add(toolItem4);
        ToolItem toolItem5 = new ToolItem();
        toolItem5.setItemName("格式转换");
        toolItem5.setItemIcon(R.mipmap.video_change);
        allTools.add(toolItem5);
        ToolItem toolItem6 = new ToolItem();
        toolItem6.setItemName("图片合视频");
        toolItem6.setItemIcon(R.mipmap.image_combine);
        allTools.add(toolItem6);
        ToolItem toolItem7 = new ToolItem();
        toolItem7.setItemName("加水印");
        toolItem7.setItemIcon(R.mipmap.video_water);
        allTools.add(toolItem7);
        ToolItem toolItem8 = new ToolItem();
        toolItem8.setItemName("提取音频");
        toolItem8.setItemIcon(R.mipmap.audio);
        allTools.add(toolItem8);
        ToolItem toolItem9 = new ToolItem();
        toolItem9.setItemName("加背景音乐");
        toolItem9.setItemIcon(R.mipmap.bg_audio);
        allTools.add(toolItem9);
        ToolItem toolItem10 = new ToolItem();
        toolItem10.setItemName("视频压缩");
        toolItem10.setItemIcon(R.mipmap.video_press);
        allTools.add(toolItem10);
        ToolItem toolItem11 = new ToolItem();
        toolItem11.setItemName("更多");
        toolItem11.setItemIcon(R.mipmap.more_tools);
        allTools.add(toolItem11);
    }

}

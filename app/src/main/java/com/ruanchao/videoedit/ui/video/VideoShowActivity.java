package com.ruanchao.videoedit.ui.video;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.base.BaseActivity;
import com.ruanchao.videoedit.util.AliOssUtil;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.ShareUtil;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;
import com.umeng.socialize.utils.SocializeUtils;
import com.wuhenzhizao.titlebar.statusbar.StatusBarUtils;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import java.io.File;

public class VideoShowActivity extends BaseActivity {

    private static final String TAG = VideoShowActivity.class.getSimpleName();
    private CommonTitleBar toolbar;
    private static final String VIDEO_PATH = "video_path";
    private VideoView mVideoView;
    private int mVideoType;
    private ImageView mGifVideo;
    private ShareAction mShareAction;
    private String mPath;
    private ProgressDialog dialog;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_show);
        dialog = new ProgressDialog(this);
        dialog.setTitle("提示");
        dialog.setMessage("加载中，请稍后...");
        toolbar = findViewById(R.id.titlebar);
        toolbar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == CommonTitleBar.ACTION_LEFT_TEXT) {
                    finish();
                }else if (action == CommonTitleBar.ACTION_RIGHT_BUTTON){
                    share();
                }
            }
        });
        int statusBarHeight = StatusBarUtils.getStatusBarHeight(this);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        LinearLayout videoRootView = findViewById(R.id.ll_video_root);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) videoRootView.getLayoutParams();
        lp.setMargins(0, statusBarHeight, 0, 0);
        videoRootView.setLayoutParams(lp);
        mGifVideo = findViewById(R.id.iv_gif_video);
        mVideoType = getIntent().getIntExtra(Constans.VIDEO_CATEGORY, 0);
        mPath = getIntent().getStringExtra(VIDEO_PATH);
        if (mVideoType == Constans.TYPE_VIDEO) {
            mGifVideo.setVisibility(View.GONE);
            mVideoView.setVisibility(View.VISIBLE);
            mVideoView.setMediaController(new MediaController(this));
            Uri videoUri = Uri.parse(mPath);
            mVideoView.setVideoURI(videoUri);
            mVideoView.start();
        }else if (mVideoType == Constans.TYPE_IMAGE){
            mGifVideo.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.GONE);
            Glide.with(this)
                    .load(new File(mPath))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(mGifVideo);
        }
    }

    public static void start(Context context, String videoPath, int videoType){
        Intent intent = new Intent(context, VideoShowActivity.class);
        intent.putExtra(VIDEO_PATH,videoPath);
        intent.putExtra(Constans.VIDEO_CATEGORY, videoType);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoType == Constans.TYPE_VIDEO) {
            mVideoView.start();
        }
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    public void share(){
        /*增加自定义按钮的分享面板*/
        mShareAction = new ShareAction(this).setDisplayList(
                SHARE_MEDIA.WEIXIN)
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if (mVideoType == Constans.TYPE_VIDEO) {
                            uploadFile();
                        }else if (mVideoType == Constans.TYPE_IMAGE){
                            ShareUtil.shareEmoji(VideoShowActivity.this,mPath,share_media,shareListener);
                        }
                    }
                });
        mShareAction.open();
    }

    private UMShareListener shareListener = new UMShareListener() {
        @Override
        public void onStart(SHARE_MEDIA platform) {
            if (dialog != null && !dialog.isShowing()){
                dialog.show();
            }
        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
//            Toast.makeText(ShareDetailActivity.this,"成功了",Toast.LENGTH_LONG).show();
            if (dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            if (dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
            Toast.makeText(VideoShowActivity.this,"失败"+t.getMessage(),Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            if (dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
           Toast.makeText(VideoShowActivity.this,"取消了",Toast.LENGTH_LONG).show();

        }
    };

    private void uploadFile() {
        if (dialog != null && !dialog.isShowing()){
            dialog.show();
        }
        final String objectKey = System.currentTimeMillis() + mPath.substring(mPath.lastIndexOf("."));
        Log.i(TAG,"objectKey:" + objectKey);
        AliOssUtil.getInstance().upload(mPath, objectKey, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String videoUrl = "http://rc-video.oss-cn-beijing.aliyuncs.com/" + objectKey;
                        ShareUtil.shareVideo(VideoShowActivity.this,
                                videoUrl,
                                "http://www.umeng.com/images/pic/social/chart_1.png",
                                "小视频",
                                "自己编辑的视频",
                                SHARE_MEDIA.WEIXIN,
                                shareListener);
                    }
                });
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                if (dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                Toast.makeText(VideoShowActivity.this,"分享失败，可能网络异常",Toast.LENGTH_LONG).show();
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode,resultCode,data);
    }

    /**
     * 屏幕横竖屏切换时避免出现window leak的问题
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mShareAction.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UMShareAPI.get(this).release();
        mHandler.removeCallbacks(null);
    }


}

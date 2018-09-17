package com.ruanchao.videoedit.ui.video;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.base.BaseActivity;
import com.wuhenzhizao.titlebar.statusbar.StatusBarUtils;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

public class VideoShowActivity extends BaseActivity {

    private CommonTitleBar toolbar;
    private static final String VIDEO_PATH = "video_path";
    private VideoView mVideoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_show);
        toolbar = findViewById(R.id.titlebar);
        toolbar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == CommonTitleBar.ACTION_LEFT_BUTTON) {
                    finish();
                }
            }
        });
        int statusBarHeight = StatusBarUtils.getStatusBarHeight(this);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        LinearLayout videoRootView = findViewById(R.id.ll_video_root);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) videoRootView.getLayoutParams();
        lp.setMargins(0, statusBarHeight, 0, 0);
        videoRootView.setLayoutParams(lp);
        mVideoView.setMediaController(new MediaController(this));
        Uri videoUri = Uri.parse(getIntent().getStringExtra(VIDEO_PATH));
        mVideoView.setVideoURI(videoUri);
        mVideoView.start();
    }


    public static void start(Context context, String videoPath){
        Intent intent = new Intent(context, VideoShowActivity.class);
        intent.putExtra(VIDEO_PATH,videoPath);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.start();
    }
}

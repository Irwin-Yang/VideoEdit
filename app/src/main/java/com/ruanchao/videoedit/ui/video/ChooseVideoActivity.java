package com.ruanchao.videoedit.ui.video;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.base.BaseActivity;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.ffmpeg.FFmpegCmd;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.DateUtil;
import com.ruanchao.videoedit.util.FileUtil;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import java.io.File;

public class ChooseVideoActivity extends BaseActivity implements View.OnClickListener {

    private static final int CHOOSE_VIDEO_CODE = 101;
    private VideoInfo mInputVideoInfo;
    private CommonTitleBar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_video);
        Button mChooseVideo = findViewById(R.id.btn_choose_video);
        mChooseVideo.setOnClickListener(this);
        toolbar = findViewById(R.id.titlebar);
        toolbar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == CommonTitleBar.ACTION_LEFT_TEXT) {
                    finish();
                }
            }
        });
    }

    public static void start(Context context){
        context.startActivity(new Intent(context, ChooseVideoActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case CHOOSE_VIDEO_CODE:
                Uri uri = data.getData();
                String path = FileUtil.getFilePathByUri(this, uri);
                File file = new File(path);
                mInputVideoInfo = new VideoInfo();
                mInputVideoInfo.setPath(path);
                mInputVideoInfo.setVideoName(file.getName());
                mInputVideoInfo.setVideoTime(file.lastModified());
                mInputVideoInfo.setVideoName(DateUtil.timeToDate(file.lastModified()));
                mInputVideoInfo.setDuration(FFmpegCmd.getVideoDuration(mInputVideoInfo.getPath()));
                finish();
                VideoEditActivity.start(ChooseVideoActivity.this,mInputVideoInfo);
                break;
            default:
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
             case R.id.btn_choose_video:
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, CHOOSE_VIDEO_CODE);
                break;
                default:
                    break;
        }
    }
}

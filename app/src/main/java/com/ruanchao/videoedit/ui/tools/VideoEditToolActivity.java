package com.ruanchao.videoedit.ui.tools;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.base.BaseMvpActivity;
import com.ruanchao.videoedit.bean.EditInfo;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.event.EditFinishMsg;
import com.ruanchao.videoedit.ffmpeg.FFmpegCmd;
import com.ruanchao.videoedit.interf.OnStartEditListener;
import com.ruanchao.videoedit.ui.video.MusicListActivity;
import com.ruanchao.videoedit.ui.video.VideoEditActivity;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.DateUtil;
import com.ruanchao.videoedit.util.FileUtil;
import com.ruanchao.videoedit.view.tool.ImageToVideoView;
import com.ruanchao.videoedit.view.tool.VideoToGifLayout;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import rx.Subscriber;

public class VideoEditToolActivity extends BaseMvpActivity<IVideoEditToolView,VideoEditToolPresenter> implements IVideoEditToolView, View.OnClickListener, OnStartEditListener {

    private static final String EDIT_TYPE = "edit_type";
    private int mEditType;
    private Button mChooseVideo;
    private static final int CHOOSE_VIDEO_CODE = 100;
    private static final int IMAGE_REQUEST_CODE = 101;
    private VideoInfo mInputVideoInfo;
    private VideoToGifLayout mVideoToGifView;
    private ImageToVideoView mImageToVideoView;
    protected CommonTitleBar toolbar;
    private String TAG = VideoEditToolActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit_tool);
        mEditType = getIntent().getIntExtra(EDIT_TYPE,-1);
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null){
            return;
        }
        switch (requestCode){
            case CHOOSE_VIDEO_CODE:
                setVideo(data);
                break;
            case IMAGE_REQUEST_CODE:
                setImageInfo(data);
                break;
            default:
                break;
        }
    }

    private void setVideo(Intent data) {
        Uri uri = data.getData();
        String path = FileUtil.getFilePathByUri(this, uri);
        File file = new File(path);
        mInputVideoInfo = new VideoInfo();
        mInputVideoInfo.setVideoPath(path);
        mInputVideoInfo.setVideoName(file.getName());
        mInputVideoInfo.setVideoTime(file.lastModified());
        mInputVideoInfo.setVideoName(DateUtil.timeToDate(file.lastModified()));
        long videoDuration = FFmpegCmd.getVideoDuration(mInputVideoInfo.getVideoPath());
        mInputVideoInfo.setDuration(videoDuration);
        mVideoToGifView.setInputVideoInfo(mInputVideoInfo);
    }

    private void setImageInfo(Intent data) {
        String path;
        Cursor cursor = null;
        try {
            Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            //从系统表中查询指定Uri对应的照片
            cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            path = cursor.getString(columnIndex);  //获取照片路径
            File file = new File(path);
            mInputVideoInfo = new VideoInfo();
            mInputVideoInfo.setVideoPath(path);
            mInputVideoInfo.setVideoName(file.getName());
            mInputVideoInfo.setVideoTime(file.lastModified());
            mInputVideoInfo.setVideoName(DateUtil.timeToDate(file.lastModified()));
            mImageToVideoView.setInputVideoInfo(mInputVideoInfo);
        } catch (Exception e) {
            // TODO Auto-generatedcatch block
            e.printStackTrace();
        }finally {
            if (cursor !=null) {
                cursor.close();
            }
        }
    }

    private void initView() {
        mChooseVideo = findViewById(R.id.btn_choose_video);
        mChooseVideo.setOnClickListener(this);
        mVideoToGifView = findViewById(R.id.video_to_gif_view);
        mImageToVideoView = findViewById(R.id.image_to_video_view);
        mImageToVideoView.setOnStartEditListener(this);
        mVideoToGifView.setOnStartEditListener(this);
        toolbar = findViewById(R.id.titlebar);
        if (toolbar != null) {
            toolbar.setListener(new CommonTitleBar.OnTitleBarListener() {
                @Override
                public void onClicked(View v, int action, String extra) {
                    if (action == CommonTitleBar.ACTION_LEFT_TEXT) {
                        finish();
                    }
                }
            });
        }

        showEditView();
    }

    private void showEditView() {
        switch (mEditType){
            case EditInfo.EDIT_TYPE_VIDEO_TO_GIF:
                mVideoToGifView.setVisibility(View.VISIBLE);
                break;
            case EditInfo.EDIT_TYPE_IMAGE_TO_VIDEO:
                mImageToVideoView.setVisibility(View.VISIBLE);
                mChooseVideo.setText("选择编辑图片");
                break;
                default:
                    break;
        }
    }

    public static void start(Context context,int type){
        Intent intent = new Intent(context, VideoEditToolActivity.class);
        intent.putExtra(EDIT_TYPE, type);
        context.startActivity(intent);
    }

    @Override
    public VideoEditToolPresenter createPresenter() {
        return new VideoEditToolPresenter(this);
    }

    @Override
    public void showToast(String msg) {

    }

    @Override
    public void showErr() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_choose_video:

                if (mEditType == EditInfo.EDIT_TYPE_IMAGE_TO_VIDEO){
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, IMAGE_REQUEST_CODE);
                }else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                    startActivityForResult(intent, CHOOSE_VIDEO_CODE);
                }
                break;
            default:
                    break;
        }
    }

    @Override
    public void onStartEdit(EditInfo editInfo) {
        mPresenter.doEditVideo(editInfo,new Subscriber<EditInfo>() {
            @Override
            public void onStart() {
                super.onStart();
                showProgressDialog("提示","视频编辑中，请稍后。。。");
            }

            @Override
            public void onCompleted() {
                if (mProgressDialog != null){
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(VideoEditToolActivity.this,"执行失败", Toast.LENGTH_LONG).show();
                if (mProgressDialog != null){
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onNext(EditInfo editInfo) {
                Toast.makeText(VideoEditToolActivity.this,"执行结束", Toast.LENGTH_LONG).show();
                if (editInfo != null) {
                    EventBus.getDefault().post(new EditFinishMsg(editInfo.videoInfo));
                    if (editInfo.editType == EditInfo.EDIT_TYPE_VIDEO_TO_GIF){
                        mToast.setText("Gif生成成功，保存在"+editInfo.videoInfo.getVideoPath());
                        mToast.show();
                    }
                }else {
                    mToast.setText("视频编辑失败");
                    mToast.show();
                }
                //EventBus.getDefault().post(new EditFinishMsg(videoInfo));
                finish();
            }
        });
    }
}

package com.ruanchao.video_demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

    SurfaceView mSurfaceView;
    Button mStartRecord;
    Button mStopRecord;
    AudioRecordUtils mAudioRecordUtils;
    private static final int MY_PERMISSIONS_REQUEST = 1001;
    private static final int CAMERA_PERMISSIONS_REQUEST = 1002;
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /**
     * 被用户拒绝的权限列表
     */
    private List<String> mPermissionList = new ArrayList<>();
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = findViewById(R.id.sv_load_image_view);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);


        mStartRecord = findViewById(R.id.btn_start);
        mStartRecord.setOnClickListener(this);
        mStopRecord = findViewById(R.id.btn_stop);
        mStopRecord.setOnClickListener(this);
        mAudioRecordUtils = new AudioRecordUtils(MainActivity.this);
        Button mCovert = findViewById(R.id.btn_convert);
        mCovert.setOnClickListener(this);
        Button mStartRecord = findViewById(R.id.btn_start_preview);
        mStartRecord.setOnClickListener(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test_image);
        canvas.drawBitmap(bitmap,0,0,paint);
        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:
                checkPermissions();
                break;
            case R.id.btn_stop:
                mAudioRecordUtils.stopRecord();
                break;
            case R.id.btn_convert:
                mAudioRecordUtils.covert();
                break;
            case R.id.btn_start_preview:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                            PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS_REQUEST);
                    } else {
                        RecordActivity.start(MainActivity.this);
                    }
                }
                break;
             default:
                 break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, permissions[i] + " 权限被用户禁止！");
                    return;
                }
            }
            // 运行时权限的申请不是本demo的重点，所以不再做更多的处理，请同意权限申请。
            mAudioRecordUtils.startRecord();
        }else if (requestCode == CAMERA_PERMISSIONS_REQUEST){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, permissions[0] + " 权限被用户禁止！");
                return;
            }
            RecordActivity.start(MainActivity.this);
        }
    }

    private void checkPermissions() {
        // Marshmallow开始才用申请运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (!mPermissionList.isEmpty()) {
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
            }else {
                mAudioRecordUtils.startRecord();
            }
        }
    }

}

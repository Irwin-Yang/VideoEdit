package com.ruanchao.videoedit.ui.other;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.ruanchao.videoedit.R;

import com.ruanchao.videoedit.ui.video.MainActivity;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.FileUtil;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 第一次打开软件，需要在这里进行拷贝文件文件
 */

public class SplashActivity extends Activity {

    private ImageView mSplashView;
    private TextView mTimeTip;
    private Subscription mSubscribe;
    private final static int TIME_COUNT = 3;
    private SharedPreferences mSP;
    private Subscription mSubscribeLoadRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        mSplashView = findViewById(R.id.iv_splash);
        mTimeTip = findViewById(R.id.tv_time);
//        Glide.with(this)
//                .load("http://api.dujin.org/bing/1920.php")
//                .into(mSplashView);
        initData();
    }

    private void initData() {
        final int count = TIME_COUNT;
        mSP = getSharedPreferences(Constans.VIDEO_PREF, MODE_PRIVATE);
        if(!mSP.getBoolean(Constans.IS_LOADED_RESOURCE, false)){
            loadResource();
        }
        mSubscribe = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long aLong) {
                        return count - aLong.intValue();
                    }
                })
                .take(count + 1)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {

                        mTimeTip.setText("跳过" + integer + "s");
                    }
                });
    }

    private void loadResource() {

        mSubscribeLoadRes = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean result = FileUtil.copyFilesFromAssets(SplashActivity.this, "resource", Constans.VIDEO_SOURCE_PATH);
                subscriber.onNext(result);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            mSP.edit().putBoolean(Constans.IS_LOADED_RESOURCE,true).apply();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscribe != null) {
            mSubscribe.unsubscribe();
            mSubscribe = null;
        }
        if (mSubscribeLoadRes != null){
            mSubscribeLoadRes.unsubscribe();
            mSubscribeLoadRes = null;
        }
    }
}

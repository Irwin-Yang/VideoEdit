package com.ruanchao.videoedit.util;

import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.ruanchao.videoedit.MainApplication;

public class AliOssUtil {

    private volatile static AliOssUtil mInstance;
    private static Object obj = new Object();
    private OSS mOss;

    private AliOssUtil(){
        initOSS();
    }

    public static AliOssUtil getInstance(){
        if (mInstance == null){
            synchronized (obj){
                if (mInstance == null){
                    mInstance = new AliOssUtil();
                }
            }
        }
        return mInstance;
    }

    private void initOSS(){
        String endpoint = "http://oss-cn-beijing.aliyuncs.com";
// 在移动端建议使用STS方式初始化OSSClient。
// 更多信息可查看sample 中 sts 使用方式(https://github.com/aliyun/aliyun-oss-android-sdk/tree/master/app/src/main/java/com/alibaba/sdk/android/oss/app)
        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider("LTAI8UeZeC2wuHPB", "TwqXG3iLQrKeeGJWiv91XyevKheMy3","");
//该配置类如果不设置，会有默认配置，具体可看该类
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
//开启可以在控制台看到日志，并且会支持写入手机sd卡中的一份日志文件位置在SDCard_path\OSSLog\logs.csv  默认不开启
//日志会记录oss操作行为中的请求数据，返回数据，异常信息
//例如requestId,response header等
//android_version：5.1  android版本
//mobile_model：XT1085  android手机型号
//network_state：connected  网络状况
//network_type：WIFI 网络连接类型
//具体的操作行为信息:
//[2017-09-05 16:54:52] - Encounter local execpiton: //java.lang.IllegalArgumentException: The bucket name is invalid.
//A bucket name must:
//1) be comprised of lower-case characters, numbers or dash(-);
//2) start with lower case or numbers;
//3) be between 3-63 characters long.
//------>end of log
        OSSLog.enableLog();
        mOss = new OSSClient(MainApplication.getContext(), endpoint, credentialProvider);
    }

    public OSS getOss() {
        return mOss;
    }

    public OSSAsyncTask upload(String uploadFilePath, String objectKey, OSSCompletedCallback ossCompletedCallback) {
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest("rc-video", objectKey, uploadFilePath);
        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });
        OSSAsyncTask task = AliOssUtil.getInstance().getOss().asyncPutObject(put, ossCompletedCallback);
        return task;
        // task.cancel(); // 可以取消任务
        // task.waitUntilFinished(); // 可以等待直到任务完成
    }

}

package com.ruanchao.videoedit;

import android.app.Application;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

public class MainApplication extends Application{

    private static Application context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        UMConfigure.init(this,"5bac983ef1f55662d400004a"
                ,"Umeng",UMConfigure.DEVICE_TYPE_PHONE,"");
        UMConfigure.setLogEnabled(true);
        PlatformConfig.setWeixin("wx768fa033c0b1bb61", "ef3a74dce41c2cb1537bb12c64dac1fd");
    }

    public static Application getContext(){
        return context;
    }

}

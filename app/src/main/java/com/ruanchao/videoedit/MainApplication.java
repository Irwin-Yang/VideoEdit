package com.ruanchao.videoedit;

import android.app.Application;

import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

public class MainApplication extends Application{

    private static Application context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        //设置视频滤镜的Context
        UMConfigure.init(this,"5bac983ef1f55662d400004a"
                ,"Umeng",UMConfigure.DEVICE_TYPE_PHONE,"");
        UMConfigure.setLogEnabled(true);
        PlatformConfig.setWeixin("wx768fa033c0b1bb61", "ef3a74dce41c2cb1537bb12c64dac1fd");
    }

    public static Application getContext(){
        return context;
    }

}

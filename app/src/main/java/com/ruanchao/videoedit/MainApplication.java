package com.ruanchao.videoedit;

import android.app.Application;

public class MainApplication extends Application{

    private static Application context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Application getContext(){
        return context;
    }
}

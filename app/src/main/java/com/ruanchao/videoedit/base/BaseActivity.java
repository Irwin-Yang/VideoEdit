package com.ruanchao.videoedit.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.Toast;

import rx.subscriptions.CompositeSubscription;

public class BaseActivity extends Activity{

    public ProgressDialog mProgressDialog;

    public Toast mToast = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mToast = Toast.makeText(this,"", Toast.LENGTH_LONG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showProgressDialog(String title, String msg){
        mProgressDialog = ProgressDialog.show(this, title, msg, false, false);
    }
}

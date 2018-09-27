package com.ruanchao.videoedit.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.ruanchao.videoedit.R;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

/**
 * Created by ruanchao on 2018/4/19.
 */

public abstract class BaseMvpActivity<V extends IBaseView,T extends BasePresenter<V>> extends BaseActivity {

    protected T mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = createPresenter();
        mPresenter.attachView((V)this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public abstract T createPresenter();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
        mPresenter.unSubscribe();
    }
}

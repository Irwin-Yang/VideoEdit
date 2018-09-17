package com.ruanchao.videoedit.base;

import java.lang.ref.WeakReference;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by ruanchao on 2018/4/19.
 */

public class BasePresenter<T extends IBaseView> {

    protected WeakReference<T> viewRef;
    protected CompositeSubscription mSubscriptions = new CompositeSubscription();

    /**
     * 绑定view，一般在初始化中调用该方法
     */
    public void attachView(T view) {
        viewRef = new WeakReference<T>(view);
    }
    /**
     * 断开view，一般在onDestroy中调用
     */
    public void detachView() {
        viewRef.clear();
    }
    /**
     * 是否与View建立连接
     * 每次调用业务请求的时候都要出先调用方法检查是否与View建立连接
     */
    public boolean isViewAttached(){
        return viewRef != null && viewRef.get() != null;
    }

    public void unSubscribe() {
        if(mSubscriptions != null && mSubscriptions.isUnsubscribed()){
            mSubscriptions.unsubscribe();
            mSubscriptions = null;
        }
    }

}

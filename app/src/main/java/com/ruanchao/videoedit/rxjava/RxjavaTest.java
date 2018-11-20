package com.ruanchao.videoedit.rxjava;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class RxjavaTest {

    public static final String TAG = RxjavaTest.class.getSimpleName();
    private int i = 0;
    String data = null;
    String result;

    public static class Translation {

        private int status;

        private content content;
        private static class content {
            private String from;
            private String to;
            private String vendor;
            private String out;
            private int errNo;
        }

        //定义 输出返回数据 的方法
        public void show() {
            Log.d("RxJava", content.out );
        }
    }

    interface Api{
        @GET("ajax.php?a=fy&f=auto&t=auto&w=hi%20world")
        Observable<Translation> getCall();

        @GET("ajax.php?a=fy&f=auto&t=auto&w=hi%20world")
        Observable<Translation> getCall2();

        @GET()
        Observable<String> getData();
    }

    public Api getRetrofit(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://fy.iciba.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        Api api = retrofit.create(Api.class);
        return api;
    }

    /**
     * 轮训执行
     */
    public void testInterval(){

        Observable.interval(2,10, TimeUnit.SECONDS)
                .doOnNext(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        getRetrofit().getCall().subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<Translation>() {

                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(TAG, "请求失败");
                                    }

                                    @Override
                                    public void onNext(Translation result) {
                                        // e.接收服务器返回的数据
                                        result.show() ;
                                    }
                                });
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {

                    }
                });

    }

    /**
     * 有条件轮训 repeatWhen
     */
    public void repeatWhen(){

        getRetrofit().getCall()
                .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Void> observable) {
                        return observable.flatMap(new Func1<Void, Observable<?>>() {
                            @Override
                            public Observable<?> call(Void aVoid) {
                                if (i<=4){
                                    return Observable.just(1).delay(2000, TimeUnit.SECONDS);
                                }else {
                                   return Observable.error(new Throwable("repeat end"));
                                }
                            }
                        });
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Translation>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Translation translation) {
                        // e.接收服务器返回的数据
                        translation.show() ;
                        i++;
                    }
                });
    }

    /**
     * 网络请求出错重连 retry
     * Retry操作符不会将原始Observable的onError通知传递给观察者，它会订阅这个Observable，再给它一次机会无错误地完成它的数据序列。Retry总是传递onNext通知给观察者，由于重新订阅，可能会造成数据项重复，
     * 接受单个count参数的retry会最多重新订阅指定的次数，如果次数超了，它不会尝试再次订阅，它会把最新的一个onError通知传递给它的观察者。
     */
    public void retryConnect(){

        getRetrofit().getCall()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .retry(4)
                .subscribe(new Subscriber<Translation>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Translation translation) {

                    }
                });
    }

    /**
     * 当.repeat()接收到.onCompleted()事件后触发重订阅。
     * 当.retry()接收到.onError()事件后触发重订阅。
     * 如果你要实现一个延迟数秒的重订阅该如何去做？或者想通过观察错误来决定是否应该重订阅呢？这种情况下就需要.repeatWhen()和.retryWhen()的介入了，因为它们允许你为重试提供自定义逻辑。
     *
     *
     * retryWhen(Func1<? super Observable<? extends java.lang.Throwable>,? extends Observable<?>> notificationHandler)
     * 简化后，它包括三个部分：

     Func1像个工厂类，用来实现你自己的重试逻辑。
     输入的是一个Observable<Throwable>。
     输出的是一个Observable<?>。
     首先，让我们来看一下最后一部分。被返回的Observable<?>所要发送的事件决定了重订阅是否会发生。如果发送的是onCompleted或者onError事件，将不会触发重订阅。相对的，如果它发送onNext事件，则触发重订阅（不管onNext实际上是什么事件）。这就是为什么使用了通配符作为泛型类型：这仅仅是个通知（next, error或者completed），一个很重要的通知而已.
     */


    /**
     * 超时重连   retryWhen
     * retryWhen和retry类似，区别是，retryWhen将onError中的Throwable传递给一个函数，这个函数产生另一个Observable，retryWhen观察它的结果再决定是不是要重新订阅原始的Observable。如果这个Observable发射了一项数据，它就重新订阅，如果这个Observable发射的是onError通知，它就将这个通知传递给观察者然后终止。
     * 输入的Observable必须作为输出Observable的源。你必须对Observable<Throwable>做出反应，然后基于它发送事件；你不能只返回一个通用泛型流。
     */
    public void testRetryWhen(){

        getRetrofit().getCall()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Throwable> observable) {

                        //输入的Observable必须作为输出Observable的源。
                        // 你必须对Observable<Throwable>做出反应，然后基于它发送事件；你不能只返回一个通用泛型流。
                        return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                            @Override
                            public Observable<?> call(Throwable throwable) {
                                //重传3次后停止
                                if (i >3) {
                                    return Observable.error(throwable);
                                }else {
                                    i++;
                                    return Observable.just(null);
                                }
                            }
                        });
                    }
                })
                .subscribe(new Subscriber<Translation>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Translation translation) {

                    }
                });
    }

    /**
     * 网络嵌套调用:即在第1个网络请求成功后，继续再进行一次网络请求
     * flatMap
     */
    public void flatMapTest() {

        getRetrofit().getCall()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnNext(new Action1<Translation>() {
                    @Override
                    public void call(Translation translation) {
                        //第一次请求
                        translation.show();
                    }
                }).flatMap(new Func1<Translation, Observable<Translation>>() {
            @Override
            public Observable<Translation> call(Translation translation) {
                //第一次请求成功，第二次嵌套请求

                Observable<Translation> call2 = getRetrofit().getCall2();
                return call2;
            }
        }).subscribe(new Subscriber<Translation>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Translation o) {

            }
        });
    }

    /**
     * 按次序 有序发射Observaber
     */
    public void concatTest(){

        //1.先从内存中获取数据
        Observable<String> memoryObservable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {

                if (TextUtils.isEmpty(data)){
                    // 若无该数据，则直接发送结束事件
                    subscriber.onCompleted();
                }else {
                    // 若有该数据，则发送
                    subscriber.onNext("data from memory");
                }
            }
        });

        //2.再从硬盘中获取数据
        Observable<String> diskObservable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (TextUtils.isEmpty(data)){
                    // 若无该数据，则直接发送结束事件
                    subscriber.onCompleted();
                }else {
                    // 若有该数据，则发送
                    subscriber.onNext("data from disk");
                }
            }
        });

        //3.最后从网络中获取数据
        Observable<String> netObservable = getRetrofit().getData();

        /*
         * 通过concat（） 和 firstElement（）操作符实现缓存功能
         **/

        //(1). 通过concat（）合并memory、disk、network 3个被观察者的事件（即检查内存缓存、磁盘缓存 & 发送网络请求）
        //    并将它们按顺序串联成队列
        Observable.concat(memoryObservable,diskObservable,netObservable)
                //(2). 通过firstElement()，从串联队列中取出并发送第1个有效事件（Next事件），即依次判断检查memory、disk、network
                .first()
                .subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {

            }
        });
    }

    /**
     * 合并数据源
     * Merge（）例子 ：实现较为简单的从（网络 + 本地）获取数据 & 统一展示
     * Zip（）例子：结合Retrofit 与RxJava，实现较为复杂的合并2个网络请求向2个服务器获取数据 & 统一展示
     */
    public void mergeDataTest(){
        Observable<String> observable1 = Observable.just("数据1");

        Observable<String> observable2 = Observable.just("数据2");

        //merge没有进行真正的数据合并
        Observable.merge(observable1,observable2)
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                       // 最后接收合并事件后，统一展示
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String value) {
                        //多个数据源依次输出
                        Log.d(TAG, "数据源有： "+ value  );
                        result += value + "+";
                    }
                });

        //zip进行真正的数据合并,通过使用Zip（）对两个网络请求进行合并再发送
        Observable.zip(observable1, observable2, new Func2<String, String, Object>() {
            @Override
            public Object call(String s, String s2) {
                //将两个结果合并后发射
                return s+s2;
            }
        }).subscribe(new Subscriber<Object>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Object o) {

                //这里是合并后的数据
            }
        });
    }

    /**
     * 用户只需要操作该功能一次，防止多次点击操作
     * 使用了throttleFirst（）操作符，所以只会发送该段时间内的第1次点击事件
     */
    public void test(){

        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {

            }
        })
                //2s内只执行一次该操作
                .throttleFirst(2, TimeUnit.SECONDS)
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }

    /**
     * 联想搜索优化
     *
     * 实现联想搜索功能
     * 当用户输入一个字符，即可显示与改字符相关的搜索结果
     * 冲突：在用户搜索明确的情况下，可能会发送一些不必要的网络请求
     * 解决方案：通过制定时间过滤器条件 的过滤操作符（debounce）实现
     *
     * 1. 传入EditText控件，输入字符时都会发送数据事件（此处不会马上发送，因为使用了debounce（））
     * 2. 采用skip(1)原因：跳过 第1次请求 = 初始输入框的空字符状态
     */

    public void debounceTest(){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {

                    }
                })
                        .debounce(500, TimeUnit.MILLISECONDS).skip(1)
                        .subscribe(new Subscriber<String>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(String s) {

                            }
                        });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    EditText editText = null;





}

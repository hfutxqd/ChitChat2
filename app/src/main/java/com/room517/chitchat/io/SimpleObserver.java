package com.room517.chitchat.io;

import rx.Observer;

/**
 * Created by ywwynm on 2016/5/18.
 * 一个实现了{@link Observer}接口的Observer类，方便使用时仅选择需要的方法进行重载
 */
public class SimpleObserver<T> implements Observer<T> {

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onNext(T t) {

    }
}

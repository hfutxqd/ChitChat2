package com.room517.chitchat.helpers;

import com.room517.chitchat.io.SimpleObserver;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ywwynm on 2016/5/18.
 * RxJava/RxAndroid的辅助类
 */
public class RxHelper {

    public static <T> void ioMain(Observable<T> observableIO, SimpleObserver<T> observerMain) {
        observableIO.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerMain);
    }

}

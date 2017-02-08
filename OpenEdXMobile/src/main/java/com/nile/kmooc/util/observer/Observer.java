package com.nile.kmooc.util.observer;

import android.support.annotation.NonNull;

public interface Observer<T> {
    void onData(@NonNull T data);
    void onError(@NonNull Throwable error);
}



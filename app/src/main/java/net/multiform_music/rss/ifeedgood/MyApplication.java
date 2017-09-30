package net.multiform_music.rss.ifeedgood;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by michel.dio on 13/04/2017.
 *
 */



public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}


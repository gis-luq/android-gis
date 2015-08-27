package com.youngken.tool;

import android.app.Application;

/**
 * Created by Young Ken on 2015/8/19.
 */
public class ApplicationContext extends Application
{
    private static ApplicationContext instance;

    public static ApplicationContext getContext(){
        return instance;
    }


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance=this;
    }
}

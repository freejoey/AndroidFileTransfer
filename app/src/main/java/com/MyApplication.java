package com;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.aidl.NETInterface;
import com.aidl.NETService;

/**
 * Created by WangKui on 2015/6/7.
 */
public class MyApplication extends Application {
    private static NETInterface netService = null;
    private Context mContext;
    private static String myName = "Mat";

    @Override
    public void onCreate()
    {
        super.onCreate();
        mContext = this;
    }

    public static String getMyName() {
        return myName;
    }

    public static void setMyName(String myName) {
        myName = myName;
    }

    public static NETInterface getNetService() {
        return netService;
    }

    public static void setNetService(NETInterface netservice) {
        netService = netservice;
    }
}

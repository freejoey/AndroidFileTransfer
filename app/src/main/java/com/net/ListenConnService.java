package com.net;

import com.net.ServerThread.ListenServerState;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ListenConnService extends Service {
	private final String Tag = "ListenConnService";
	private ServerThread server = null;
	
	@Override  
    public IBinder onBind(Intent intent) {  
        return null;  
    }  
      
    @Override  
    public void onCreate() {  
        super.onCreate();  
        
        server = new ServerThread(this);
        server.setServerListener(new ListenServerState() {
			
			@Override
			public void onReady() {
				// TODO Auto-generated method stub
				Log.i(Tag, "���������service,  �ȴ�����...");
			}
			
			@Override
			public void onConnected(String addr) {
				// TODO Auto-generated method stub
				Log.i(Tag, "���յ�һ�����ӣ�" + addr);
			}
		});
        server.start();
    }  
      
    @Override  
    public void onStart(Intent intent, int startId) {  
        super.onStart(intent, startId);  
    }  
      
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
        return super.onStartCommand(intent, flags, startId);  
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	
    	if(server!=null)
    		server.stopThread();
    }
}

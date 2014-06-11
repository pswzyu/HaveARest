package com.example.havearest;


import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class DataCollectorService extends Service {

	private TextToSpeech mSpeech;
	private SensorManager sensorManager;
	private LocationManager locationManager;
	public static boolean should_collect = true;
	private DataCollectorThread mThread;
	
//	/**
//     * A binder that gives other components access to the speech capabilities provided by the
//     * service.
//     */
//    public class CollectorServiceBinder extends Binder {
//        public void resumeThread()
//        {
//        	
//        }
//        public void stopThread()
//        {
//        	
//        }
//        public boolean getStatus()
//        {
//        	return false;
//        }
//    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
    public void onCreate() {
        super.onCreate();
        // Even though the text-to-speech engine is only used in response to a menu action, we
        // initialize it when the application starts so that we avoid delays that could occur
        // if we waited until it was needed to start it up.
        mSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // Do nothing.
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        mThread = new DataCollectorThread(sensorManager, mSpeech);
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	
    	mThread.start();

        return START_NOT_STICKY;
    }

    @Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mThread.should_run = false;
	}
    
}

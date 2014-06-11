package com.example.havearest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class DataCollectorThread extends Thread implements SensorEventListener {

	public boolean should_run = true;
	private SensorManager sm;
	private TextToSpeech tts;
	private Sensor mSensorGravity;
	private Sensor mSensorLinearAcceleration;
	private int data_collected = 0;
	private String log_string;
	private ArrayDeque<float[]> gravity_his;
	private ArrayDeque<float[]> acc_his;
	private long last_remind_time = 0;
	
	private static final int COLLECT_COUNT = 4; // how many readings do we collect in every COLLECT_INTERVAL milliseconds
	private static final int COLLECT_INTERVAL = 30000; // in millisecond
	private static final int MIN_HISTORY_FOR_PROC = 40; // minimal data that we can start to process
	private static final int HISTORY_LENGTH = 80; // how many data we keep
	private static final int MIN_REMIND_INTERVAL = 600000; // in millisecond, the minimal interval between remindings
	private static final float REMIND_THRESHOLD = 10f; // the threshold for discriminating remind or not
	private static final float MIN_REMIND_THRESHOLD = 0.2f; // if you place your glass on the table, this will stop remind
	private static final float MAX_DISTANCE = 100;
	private static final String REMINDER_TEXT = "Have a rest for your neck please!"; // the string you want to hear
	
	DataCollectorThread(SensorManager p_sm, TextToSpeech p_tts)
	{
		sm = p_sm;
		tts = p_tts;
		log_string = new String();
		mSensorGravity = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorLinearAcceleration = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gravity_his = new ArrayDeque<float[]>(HISTORY_LENGTH);
        acc_his = new ArrayDeque<float[]>(HISTORY_LENGTH);
        
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		try {
            while(should_run) {
                Log.d("HAVEAREST", "TEST"+DataCollectorService.should_collect);
                log_string += "06-08 12:36:19.302: D/HAVEAREST(22408): TESTtrue\n";
                if (DataCollectorService.should_collect)
                {
                	sm.registerListener(this, mSensorGravity, SensorManager.SENSOR_DELAY_NORMAL);
                    sm.registerListener(this, mSensorLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
                }
                sleep(COLLECT_INTERVAL);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		

        Sensor sensor = event.sensor;
        int type = sensor.getType();
        float[] values = event.values;

        switch(type) {
            case Sensor.TYPE_GRAVITY:
                //Log.d("TAG", "Gravity:"+values[0]+","+values[1]+","+values[2]);
                log_string += "00-00 00:00:00.000: D/TAG(00000): "+"Gravity:"+values[0]+","+values[1]+","+values[2]+"\n";
                
                gravity_his.add(new float[]{values[0], values[1], values[2]});
                if (gravity_his.size() > HISTORY_LENGTH)
                {
                	gravity_his.pop();
                }
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
            	//Log.d("TAG", "LinearAcc:"+values[0]+","+values[1]+","+values[2]);
            	log_string += "00-00 00:00:00.000: D/TAG(00000): "+"LinearAcc:"+values[0]+","+values[1]+","+values[2]+"\n";
            	
            	acc_his.add(new float[]{values[0], values[1], values[2]});
            	if (acc_his.size() > HISTORY_LENGTH)
                {
                	acc_his.pop();
                }
                break;
            default:
                Log.w("TAG", "Unknown type: " + type);
        }
        
        data_collected ++;
		if ( data_collected == COLLECT_COUNT )
		{
			data_collected = 0;
			// TODO: stop collecting, unregister the listeners
			sm.unregisterListener(this);
			
			processData();
			
			writeToFile("log-relax", log_string);
			log_string="";
		}
		
	}
	public void processData()
	{
		if ( gravity_his.size() != acc_his.size() || acc_his.size() < MIN_HISTORY_FOR_PROC)
		{
			return;
		}
		Iterator<float[]> gravity_iter = gravity_his.iterator();
		Iterator<float[]> acc_iter = acc_his.iterator();
		
		float[] last_reading = gravity_iter.next();
		float gravity_square_sum = 0;
		while(gravity_iter.hasNext())
		{
			float[] reading = gravity_iter.next();
			gravity_square_sum += Math.min(Math.pow(reading[0]-last_reading[0],2) +
						Math.pow(reading[1]-last_reading[1],2) +
						Math.pow(reading[2]-last_reading[2],2), MAX_DISTANCE);
			last_reading = reading;
		}
		
		last_reading = acc_iter.next();
		float acc_square_sum = 0;
		while(acc_iter.hasNext())
		{
			float[] reading = acc_iter.next();
			acc_square_sum += Math.min(Math.pow(reading[0]-last_reading[0],2) +
						Math.pow(reading[1]-last_reading[1],2) +
						Math.pow(reading[2]-last_reading[2],2), MAX_DISTANCE);
			last_reading = reading;
		}
		
		float score = (acc_square_sum + gravity_square_sum) / acc_his.size();
		Log.d("score", "score:"+score+","+acc_his.size());
		writeToFile("log_dis", ""+acc_square_sum +","+ gravity_square_sum +"\n");
		
		if ( score > MIN_REMIND_THRESHOLD && score < REMIND_THRESHOLD
				&& last_remind_time + MIN_REMIND_INTERVAL < System.currentTimeMillis() )
		{
			last_remind_time = System.currentTimeMillis();
			tts.speak(REMINDER_TEXT, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	public void writeToFile(String fileName, String body)
    {
        FileOutputStream fos = null;

        try {
            final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/havearest/" );
        	//final File dir = new File("/mnt/shell/emulated/0/DCIM/Caemra/havearest/" );

            if (!dir.exists())
            {
                dir.mkdirs(); 
            }

            final File myFile = new File(dir, fileName + ".txt");
            //Log.d("filename", myFile.getAbsolutePath());

            if (!myFile.exists()) 
            {    
                myFile.createNewFile();
            } 

            fos = new FileOutputStream(myFile, true);

            fos.write(body.getBytes());
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}







/*

This app can be installed into your google glass. It will record your glasses' orientation and acceleration every a few seconds and can detect if you have kept your head steady for a long time. It will remind you that you should have a rest now.

This app is designed for us programmers or someone always stare on the screen or books for some reason to have a rest for your neck and shoulders.

The source code of the application is provided. You can customize the app as you wish. The app also has a activity in which you can enable or disable the reminder service.

*/
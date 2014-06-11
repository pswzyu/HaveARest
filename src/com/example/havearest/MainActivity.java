package com.example.havearest;

import java.util.ArrayList;
import java.util.List;

import com.google.android.glass.app.Card;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

public class MainActivity extends Activity {

	private final Handler mHandler = new Handler();

    private boolean mAttachedToWindow;
    private boolean mOptionsMenuOpen;
    
    private CardScrollAdapter mAdapter;
    private CardScrollView mCardScroller;
    
    private GestureDetector mGestureDetector;
    
    private TextView tv_status;

//    private ServiceConnection mConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            if (service instanceof DataCollectorService.CollectorServiceBinder) {
//            	mCollectorService = (DataCollectorService.CollectorServiceBinder) service;
////                openOptionsMenu();
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            // Do nothing.
//        }
//    };
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
	private GestureDetector createGestureDetector(final Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		// Create a base listener for generic gestures
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					// do something on tap
					return true;
				} else if (gesture == Gesture.TWO_TAP) {
					// do something on two finger tap
					if ( DataCollectorService.should_collect )
					{
						DataCollectorService.should_collect = false;
						tv_status.setText(R.string.status_stopped);
					}else{
						DataCollectorService.should_collect = true;
						tv_status.setText(R.string.status_running);
					}
					
					return true;
				} else if (gesture == Gesture.SWIPE_RIGHT) {
					// do something on right (forward) swipe
					return true;
				} else if (gesture == Gesture.SWIPE_LEFT) {
					// do something on left (backwards) swipe
					return true;
				}
				return false;
			}
		});
		gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
			@Override
			public void onFingerCountChanged(int previousCount, int currentCount) {
				// do something on finger count changes
			}
		});
		gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
			@Override
			public boolean onScroll(float displacement, float delta,
					float velocity) {
				// do something on scrolling
				return false;
			}
		});
		return gestureDetector;
	}
	
	@Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //bindService(new Intent(this, DataCollectorService.class), mConnection, 0);
        
        mGestureDetector = createGestureDetector(this);
        
        setContentView(R.layout.activity_main);
        tv_status = (TextView)findViewById(R.id.tv_status);
        
        if (!isMyServiceRunning(DataCollectorService.class))
        {
        	startService(new Intent(this, DataCollectorService.class));
        }
        if ( DataCollectorService.should_collect )
        {
        	tv_status.setText(R.string.status_running);
        }else{
        	tv_status.setText(R.string.status_stopped);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
//        openOptionsMenu();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
    }

//    @Override
//    public void openOptionsMenu() {
//        if (!mOptionsMenuOpen && mAttachedToWindow && mCollectorService != null) {
//            super.openOptionsMenu();
//        }
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_activity, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_main_activiry_resume:
//            	mCollectorService.resumeThread();
//                return true;
//            case R.id.menu_main_activiry_stop:
//                mCollectorService.stopThread();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
    
    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
//		mOptionsMenuOpen = false;
//		unbindService(mConnection);
	}
}

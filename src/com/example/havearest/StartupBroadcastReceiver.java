package com.example.havearest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		Intent startServiceIntent = new Intent(context, DataCollectorService.class);
        context.startService(startServiceIntent);
	}

}

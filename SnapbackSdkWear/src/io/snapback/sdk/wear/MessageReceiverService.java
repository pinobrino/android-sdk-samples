package io.snapback.sdk.wear;

import java.util.concurrent.CopyOnWriteArraySet;

import io.snapback.sdk.wear.shared.ClientPaths;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class MessageReceiverService extends WearableListenerService {
	private static final String TAG = MessageReceiverService.class.getSimpleName();

	private DeviceClient deviceClient;

	private SensorManager sensorManager;
	private CopyOnWriteArraySet<String> registeredSensors;
	private CopyOnWriteArraySet<String> requestedTriggers;

	private BroadcastReceiver receiver;

	@Override
	public void onCreate() {
		super.onCreate();

		// if previous run fails
		stopService(new Intent(this, SensorService.class));
		
		deviceClient = DeviceClient.getInstance(this);
		
		sensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
		
		registeredSensors = new CopyOnWriteArraySet<String>();
		requestedTriggers = new CopyOnWriteArraySet<String>();
		
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.NOTIFY_SENSOR_TRIGGERED_INTENT)) {
					String id = intent.getExtras().getString("id");
					
					requestedTriggers.remove(id);
					
					if(registeredSensors.isEmpty() && requestedTriggers.isEmpty()) {
						stopService(new Intent(MessageReceiverService.this, SensorService.class));
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.NOTIFY_SENSOR_TRIGGERED_INTENT);
		registerReceiver(receiver, filter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(receiver);
	}

	@Override
	public void onMessageReceived(MessageEvent messageEvent) {
		
		String path = messageEvent.getPath();
		
		Log.d(TAG, "Received message: " + path);

		if (path.equals(ClientPaths.SENSORS_LIST)) {
			deviceClient.sendSensorsList(sensorManager);
		}
		
		if (path.startsWith(ClientPaths.START_SENSOR_MEASUREMENT) || path.startsWith(ClientPaths.START_TRIGGER_MEASUREMENT)) {
			startService(new Intent(this, SensorService.class));
			
			String[] ss = path.split("/");
			String sType = ss[2];
			
			if(Util.getSensorFromStringId(sensorManager, sType) == null) {
				
				if(registeredSensors.isEmpty() && requestedTriggers.isEmpty()) {
					stopService(new Intent(this, SensorService.class));
				}
				
				return;
			}
			
			// allow service to register own receivers
			try {
				Thread.sleep(Constants.DEFAULT_SLEEP_MILLIS);
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		
		if (path.startsWith(ClientPaths.START_SENSOR_MEASUREMENT)) {
			String[] ss = path.split("/");
			String sType = ss[2];
			String sRateus = ss[3];
			
			// sensor already registered
			if(registeredSensors.contains(sType)) {
				Log.d(TAG, "broadcasting unregister sensor intent...");
				Intent unregisterSensorIntent = new Intent(Constants.UNREGISTER_SENSOR_INTENT);
				unregisterSensorIntent.putExtra("id", sType);
				sendBroadcast(unregisterSensorIntent);
			}
			else {
				registeredSensors.add(sType);
			}
			
			Log.d(TAG, "broadcasting register sensor intent...");
			Intent registerSensorIntent = new Intent(Constants.REGISTER_SENSOR_INTENT);
			registerSensorIntent.putExtra("id", sType);
			registerSensorIntent.putExtra("rate", sRateus);
			sendBroadcast(registerSensorIntent);
		}
		
		if (path.startsWith(ClientPaths.STOP_SENSOR_MEASUREMENT)) {
			String[] ss = path.split("/");
			String sType = ss[2];
			
			Intent unregisterSensorIntent = new Intent(Constants.UNREGISTER_SENSOR_INTENT);
			unregisterSensorIntent.putExtra("id", sType);
			sendBroadcast(unregisterSensorIntent);
			
			registeredSensors.remove(sType);
		}
		
		if(path.startsWith(ClientPaths.START_TRIGGER_MEASUREMENT)) {
			String[] ss = path.split("/");
			String sType = ss[2];
			
			// sensor already registered
			if(requestedTriggers.contains(sType)) {
				Log.d(TAG, "broadcasting cancel trigger intent...");
				Intent cancelTriggerIntent = new Intent(Constants.CANCEL_TRIGGER_SENSOR_INTENT);
				cancelTriggerIntent.putExtra("id", sType);
				sendBroadcast(cancelTriggerIntent);
			}
			else {
				requestedTriggers.add(sType);
			}
			
			Log.d(TAG, "broadcasting request trigger intent...");
			Intent requestTriggerIntent = new Intent(Constants.REQUEST_TRIGGER_SENSOR_INTENT);
			requestTriggerIntent.putExtra("id", sType);
			sendBroadcast(requestTriggerIntent);
		}
		
		if(path.startsWith(ClientPaths.STOP_TRIGGER_MEASUREMENT)) {
			String[] ss = path.split("/");
			String sType = ss[2];
			
			Log.d(TAG, "broadcasting cancel trigger intent...");
			Intent cancelTriggerIntent = new Intent(Constants.CANCEL_TRIGGER_SENSOR_INTENT);
			cancelTriggerIntent.putExtra("id", sType);
			sendBroadcast(cancelTriggerIntent);
			requestedTriggers.remove(sType);
		}
		
		if (path.startsWith(ClientPaths.STOP_SENSOR_MEASUREMENT) || path.startsWith(ClientPaths.STOP_TRIGGER_MEASUREMENT)) {
			if(registeredSensors.isEmpty() && requestedTriggers.isEmpty()) {
				stopService(new Intent(this, SensorService.class));
			}
		}
	}
}

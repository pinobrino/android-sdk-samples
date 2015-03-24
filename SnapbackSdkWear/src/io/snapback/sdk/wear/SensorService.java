package io.snapback.sdk.wear;

import java.util.concurrent.CopyOnWriteArraySet;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class SensorService extends Service implements SensorEventListener {
    private static final String TAG = SensorService.class.getSimpleName();

    private SensorManager mSensorManager;

    private DeviceClient client;

	private BroadcastReceiver receiver;
	
	private MyTriggerEventListener triggerEventListener;
	private CopyOnWriteArraySet<String> requestedTriggers;

	private SensorStringTypes sensorStringTypes;

    @Override
    public void onCreate() {
        super.onCreate();

        // register receiver asap
        receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Bundle extras = intent.getExtras();
				String id = extras.getString("id");
				
				if(action.equals(Constants.REGISTER_SENSOR_INTENT)) {
					Log.d(TAG, "registering sensor...");
					String rate = extras.getString("rate");
					startSensorMeasurement(id, Integer.parseInt(rate));
				}
				if(action.equals(Constants.UNREGISTER_SENSOR_INTENT)) {
					Log.d(TAG, "unregistering sensor...");
					stopSensorMeasurement(id);
				}
				if(action.equals(Constants.REQUEST_TRIGGER_SENSOR_INTENT)) {
					Log.d(TAG, "requesting trigger...");
					startTriggerMeasurement(id);
				}
				if(action.equals(Constants.CANCEL_TRIGGER_SENSOR_INTENT)) {
					Log.d(TAG, "canceling trigger...");
					stopTriggerMeasurement(id);
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.REGISTER_SENSOR_INTENT);
		filter.addAction(Constants.UNREGISTER_SENSOR_INTENT);
		filter.addAction(Constants.REQUEST_TRIGGER_SENSOR_INTENT);
		filter.addAction(Constants.CANCEL_TRIGGER_SENSOR_INTENT);
		registerReceiver(receiver, filter);
		
		mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
		
		triggerEventListener = new MyTriggerEventListener();
		requestedTriggers = new CopyOnWriteArraySet<String>();
		sensorStringTypes = new SensorStringTypes();
        
        client = DeviceClient.getInstance(this);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Snapback Sensor Service");
        builder.setContentText("Collecting sensor data..");
        builder.setSmallIcon(R.drawable.ic_launcher);

        startForeground(1, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopAllMeasurement();
        
        unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startSensorMeasurement(String id, int rate) {
    	Sensor s = Util.getSensorFromStringId(mSensorManager, id);
    	
    	if(s != null) {
    		mSensorManager.registerListener(this, s, rate);
    		printAddInfo(s);
    	}
    }
    
    private void stopSensorMeasurement(String id) {
    	Sensor s = Util.getSensorFromStringId(mSensorManager, id);
    	
    	if(s != null) {
    		mSensorManager.unregisterListener(this, s);
    	}
    }
    
    private void startTriggerMeasurement(String id) {
    	Sensor s = Util.getSensorFromStringId(mSensorManager, id);
    	
    	if(s != null) {
    		mSensorManager.requestTriggerSensor(triggerEventListener, s);
    		requestedTriggers.add(id);
    	}
    }
    
    private void stopTriggerMeasurement(String id) {
    	Sensor s = Util.getSensorFromStringId(mSensorManager, id);
    	
    	if(s != null) {
    		mSensorManager.cancelTriggerSensor(triggerEventListener, s);
    		requestedTriggers.remove(id);
    	}
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void printAddInfo(Sensor sensor) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        String s = sensor.getStringType() + " wake-up: " + sensor.isWakeUpSensor() + " - reporting mode: " + Util.getReportingModeString(sensor);

        Log.i(TAG, s);
    }

    private void stopAllMeasurement() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            
            for(String id : requestedTriggers) {
            	mSensorManager.cancelTriggerSensor(triggerEventListener, Util.getSensorFromStringId(mSensorManager, id));
            }
            requestedTriggers.clear();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
    public void onSensorChanged(SensorEvent event) {
    	String wakeUp = null;
    	String stringType = null;
    	String reportingMode = null;
    	
    	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    		Boolean b = event.sensor.isWakeUpSensor();
    		wakeUp = b.toString();
    		stringType = event.sensor.getStringType();
    		reportingMode = Util.getReportingModeString(event.sensor);
    	}
    	else {
    		stringType = sensorStringTypes.getStringType(event.sensor.getType());
    	}
    	
        client.sendSensorData(event.sensor.getType(), stringType, reportingMode, event.accuracy, event.timestamp, event.values, wakeUp, event.sensor.getName(), event.sensor.getVendor(), event.sensor.getVersion(), event.sensor.getPower());
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	// we don't care about this
    }
    
    private class MyTriggerEventListener extends TriggerEventListener {
    	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		@Override
		public void onTrigger(TriggerEvent event) {
			String wakeUp = null;
			String stringType = null;
	    	String reportingMode = null;
	    	
	    	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	    		Boolean b = event.sensor.isWakeUpSensor();
	    		wakeUp = b.toString();
	    		stringType = event.sensor.getStringType();
	    		reportingMode = Util.getReportingModeString(event.sensor);
	    	}
	    	else {
	    		stringType = sensorStringTypes.getStringType(event.sensor.getType());
	    	}
	    	
	    	requestedTriggers.remove(Util.getIdForSensor(event.sensor.getType(), wakeUp));
	    	client.sendTriggerData(event.sensor.getType(), stringType, reportingMode, event.timestamp, event.values, wakeUp, event.sensor.getName(), event.sensor.getVendor(), event.sensor.getVersion(), event.sensor.getPower());
		}
    }
}

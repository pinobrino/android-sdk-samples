package com.atooma.plugin.snapback;

import java.util.Timer;
import java.util.TimerTask;

import io.snapback.sdk.gesture.sequence.adapter.BlowSensorAdapter;
import io.snapback.sdk.gesture.wave.WaveGestureEvent;
import io.snapback.sdk.gesture.wave.WaveGestureHandler;
import io.snapback.sdk.gesture.wave.WaveGestureListener;
import io.snapback.sdk.motion.DegreeEvent;
import io.snapback.sdk.motion.InclinationDetector;
import io.snapback.sdk.motion.InclinationEvent;
import io.snapback.sdk.motion.InclinationEvent.InclinationType;
import io.snapback.sdk.motion.InclinationEventListener;
import io.snapback.plugin.util.Constants;
import io.snapback.plugin.util.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.atooma.plugin.IntentBasedTrigger;
import com.atooma.plugin.ParameterBundle;
import com.atooma.plugin.blow.R;

public class TR_BlowDetectionGestureBased extends IntentBasedTrigger
{
	private static final String LOG_TAG = TR_BlowDetectionGestureBased.class.getSimpleName();
	
	private static InclinationDetector inclinationDetector = null;
	private static int blow_rule_count = 0;
	private MyInclinationEventListener myInclinationEventListener;
	
	private WaveGestureHandler waveGestureHandlerBlow;
	private BlowSensorAdapter blowSensorAdapter;
	private MyWaveGestureListener myWaveGestureListener;
	
	private boolean blowDetected;
	
	public TR_BlowDetectionGestureBased(Context context, String id, int version)
	{
		super(context, id, version);
		
		Log.d(LOG_TAG, "constructor called (gesture blow rule count: " + blow_rule_count + ")");
	}

	@Override
	public void defineUI()
	{
		setIcon(R.drawable.sound_icon);
		setTitle(R.string.tr_sensor_name);
	}

	@Override
	// called when a rule is activated
	public String getIntentFilter() throws RemoteException
	{
		blow_rule_count++;
		
		blowDetected = false;
		
		Log.d(LOG_TAG, "getIntentFilter() called (gesture blow rule count: " + blow_rule_count + ")");
		
		if(inclinationDetector == null)
		{
			myInclinationEventListener = new MyInclinationEventListener();
			inclinationDetector = new InclinationDetector(getContext(), true, true);
			inclinationDetector.registerListener(myInclinationEventListener);
		}
		if(inclinationDetector.isPaused())
		{
			inclinationDetector.resume();
		}
		

		if(waveGestureHandlerBlow == null) {
			waveGestureHandlerBlow = new WaveGestureHandler();
			
			if(blowSensorAdapter == null) {
				blowSensorAdapter = new BlowSensorAdapter(waveGestureHandlerBlow, getContext());
			}
			
			waveGestureHandlerBlow.setSensorAdapters(blowSensorAdapter);
			
			myWaveGestureListener = new MyWaveGestureListener();
			waveGestureHandlerBlow.register(myWaveGestureListener);
		}

		
		return Constants.GESTURE_AND_BLOW_INTENT;
	}
	
	@Override
	public void onReceive(String ruleId, ParameterBundle parameters, Bundle bundleIntent)
	{
		if(waveGestureHandlerBlow.isStarted()) {
			waveGestureHandlerBlow.stop();
		}
		
		trigger(ruleId, parameters);
		
		if(inclinationDetector.isPaused()) {
			inclinationDetector.resume();
		}
	}

	@Override
	// called when a rule is deactivated
	public void onRevoke(String ruleId)
	{
		blow_rule_count--;
		
		Log.d(LOG_TAG, "onRevoke() called (gesture blow rule count: " + blow_rule_count + ")");
		
		if(blow_rule_count != 0)
		{
			return;
		}
		
		if(inclinationDetector != null)
		{
			if(!inclinationDetector.isPaused())
			{
				inclinationDetector.pause();
				inclinationDetector.unregisterListener(myInclinationEventListener);
			}
			inclinationDetector = null;
			
			Log.d(LOG_TAG, "InclinationDetector killed");
		}
		
		if(waveGestureHandlerBlow != null)
		{
			if(waveGestureHandlerBlow.isStarted()) {
				waveGestureHandlerBlow.stop();
			}
			
			waveGestureHandlerBlow.unregister(myWaveGestureListener);
			waveGestureHandlerBlow = null;
		}
	}
	
	private class MyInclinationEventListener implements InclinationEventListener {
		
		private static final int gestureTimeout = 1500;	// 1.5 secs
		private static final int blowTimeout = 4500;		// 4.5 secs
		private long lastVerticalTime;
		
		public MyInclinationEventListener() {
			lastVerticalTime = 0;
		}
		
		@Override
		public void onInclinationEvent(InclinationEvent inclinationEvent) {
			
			InclinationType type = inclinationEvent.getType();
			long tsNow = inclinationEvent.getTimestamp();
			
			if(type.equals(InclinationType.CUSTOM_VERTICAL_UP)) {
				Log.d(LOG_TAG, inclinationEvent.toString());
				
				lastVerticalTime = tsNow;
			}
			
			if(lastVerticalTime != 0 && type.equals(InclinationType.CUSTOM_HORIZONTAL_FACE_UP)) {
				Log.d(LOG_TAG, inclinationEvent.toString());
				
				long elapsed = tsNow - lastVerticalTime;
				
				if(elapsed <= gestureTimeout) {
					inclinationDetector.pause();
					
					blowDetected = false;
					
					waveGestureHandlerBlow.start();
					
					Timer timer = new Timer("BLOW TIMER");
					timer.schedule(new TimerTask() {
						
						@Override
						public void run() {
							if(waveGestureHandlerBlow.isStarted()) {
								waveGestureHandlerBlow.stop();
							}
							
							if(inclinationDetector.isPaused()) {
								inclinationDetector.resume();
							}
							
							Utils.pushToParse(getContext(), blowDetected);
						}
					}, blowTimeout);
				}
				
				lastVerticalTime = 0;
			}
		}

		@Override
		public void onInclinationDegreeEvent(DegreeEvent degreeEvent) {
			// DO NOTHING
		}
	}
	
	private class MyWaveGestureListener implements WaveGestureListener {
		
		@Override
		public void onEvent(WaveGestureEvent event)
		{
			String h = null;
			
			if(event.getHandler() == waveGestureHandlerBlow)
			{
				Log.d(LOG_TAG, "waveGestureHandlerBlow");
				h = "Blow handler";
			}
			
			String e = null;
			switch(event.getType())
			{
				case WaveGestureEvent.STEADY_WAVE_EVENT_TYPE:
					Log.d(LOG_TAG, "STEADY_WAVE_EVENT_TYPE");
					e = "STEADY_WAVE_EVENT_TYPE";
				break;
				
				case WaveGestureEvent.STEADY_REPEAT_WAVE_EVENT_TYPE:
					Log.d(LOG_TAG, "STEADY_REPEAT_WAVE_EVENT_TYPE");
					e = "STEADY_REPEAT_WAVE_EVENT_TYPE";
				break;
				
				case WaveGestureEvent.SINGLE_WAVE_EVENT_TYPE:
					Log.d(LOG_TAG, "SINGLE_WAVE_EVENT_TYPE");
					e = "SINGLE_WAVE_EVENT_TYPE";
				break;
				
				case WaveGestureEvent.SINGLE_STEADY_WAVE_EVENT_TYPE:
					Log.d(LOG_TAG, "SINGLE_STEADY_WAVE_EVENT_TYPE");
					e = "SINGLE_STEADY_WAVE_EVENT_TYPE";
				break;
				
				case WaveGestureEvent.SINGLE_STEADY_REPEAT_WAVE_EVENT_TYPE:
					Log.d(LOG_TAG, "SINGLE_STEADY_REPEAT_WAVE_EVENT_TYPE");
					e = "SINGLE_STEADY_REPEAT_WAVE_EVENT_TYPE";
				break;
				
				case WaveGestureEvent.DOUBLE_WAVE_EVENT_TYPE:
					Log.d(LOG_TAG, "DOUBLE_WAVE_EVENT_TYPE");
					e = "DOUBLE_WAVE_EVENT_TYPE";
				break;
				
				default:
					Log.d(LOG_TAG, "UNKNOWN EVENT TYPE");
				break;
			}
			
			// we need just a non null wave event
			if(e != null) {
				blowDetected = true;
				
				getContext().sendBroadcast(new Intent(Constants.GESTURE_AND_BLOW_INTENT));
			}
			else {
				blowDetected = false;
			}
			
			final String s = h + " " + e + " " + event.getTimestamp();
			Log.d(LOG_TAG, s);
		}
	}
}

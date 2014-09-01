package com.atooma.plugin.snapback;

import java.util.Timer;
import java.util.TimerTask;

import io.snapback.sdk.gesture.sequence.adapter.BlowSensorAdapter;
import io.snapback.sdk.gesture.wave.WaveGestureEvent;
import io.snapback.sdk.gesture.wave.WaveGestureHandler;
import io.snapback.sdk.gesture.wave.WaveGestureListener;
import android.content.Context;
import android.util.Log;

import com.atooma.plugin.ConditionChecker;
import com.atooma.plugin.ParameterBundle;
import com.atooma.plugin.blow.R;

public class CC_BlowDetection extends ConditionChecker implements WaveGestureListener
{
	private static final String LOG_TAG = CC_BlowDetection.class.getSimpleName();
	private static final int blowTimeout = 4500;	// 4.5 secs
	private WaveGestureHandler waveGestureHandlerBlow;
	private BlowSensorAdapter blowSensorAdapter;
	private boolean blowRecognized;
	
	private Object monitor;
	
	public CC_BlowDetection(Context context, String id, int version)
	{
		super(context, id, version);

		if(waveGestureHandlerBlow == null) {
			blowRecognized = false;
			monitor = new Object();
			
			waveGestureHandlerBlow = new WaveGestureHandler();
			
			if(blowSensorAdapter == null)
			{
				blowSensorAdapter = new BlowSensorAdapter(waveGestureHandlerBlow, getContext());
			}
			
			waveGestureHandlerBlow.setSensorAdapters(blowSensorAdapter);
			waveGestureHandlerBlow.register(this);
		}
	}

	@Override
	public void defineUI()
	{
		setIcon(R.drawable.sound_icon);
		setTitle(R.string.cc_blow_name);
	}

	@Override
	public boolean onInvoke(String ruleId, ParameterBundle parameters)
	{
		blowRecognized = false;
		
		waveGestureHandlerBlow.start();
		
		Timer timer = new Timer("BLOW TIMER");
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				synchronized (monitor) { monitor.notify(); }
			}
		}, blowTimeout);
		

		synchronized (monitor) {
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}
		
		if(waveGestureHandlerBlow.isStarted()) {
			waveGestureHandlerBlow.stop();
		}
		
		if(blowRecognized)
		{
			Log.d(LOG_TAG, "Blow recognized!");
			
			return true;
		}
		else
		{
			Log.d(LOG_TAG, "No blow recognized");
		}
		
		return false;
	}

	@Override
	public void onEvent(WaveGestureEvent event)
	{
		String h = null;
		
		if (event.getHandler() == waveGestureHandlerBlow)
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
			blowRecognized = true;
			
			synchronized (monitor) { monitor.notify(); }
		}
		else {
			blowRecognized = false;
		}
		
		final String s = h + " " + e + " " + event.getTimestamp();
		Log.d(LOG_TAG, s);
	}
}

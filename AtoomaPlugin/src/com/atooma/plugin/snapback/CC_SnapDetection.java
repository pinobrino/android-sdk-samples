package com.atooma.plugin.snapback;

import java.util.Timer;
import java.util.TimerTask;

import io.snapback.sdk.gesture.sequence.adapter.SnapSensorAdapter;
import io.snapback.sdk.gesture.wave.WaveGestureEvent;
import io.snapback.sdk.gesture.wave.WaveGestureHandler;
import io.snapback.sdk.gesture.wave.WaveGestureListener;
import android.content.Context;
import android.util.Log;

import com.atooma.plugin.ConditionChecker;
import com.atooma.plugin.ParameterBundle;
import com.atooma.plugin.blow.R;

public class CC_SnapDetection extends ConditionChecker implements WaveGestureListener
{
	private static final String LOG_TAG = CC_SnapDetection.class.getSimpleName();
	private static final int snapTimeout = 4500;	// 4.5 secs
	private WaveGestureHandler waveGestureHandlerSnap;
	private SnapSensorAdapter snapSensorAdapter;
	private boolean snapRecognized;
	
	private Object monitor;
	
	public CC_SnapDetection(Context context, String id, int version)
	{
		super(context, id, version);
		
		if(waveGestureHandlerSnap == null) {
			monitor = new Object();
			snapRecognized = false;
			
			waveGestureHandlerSnap = new WaveGestureHandler();
			
			if(snapSensorAdapter == null) {
				snapSensorAdapter = new SnapSensorAdapter(waveGestureHandlerSnap, getContext());	
			}
			
			waveGestureHandlerSnap.setSensorAdapters(snapSensorAdapter);
			waveGestureHandlerSnap.register(this);
		}
	}

	@Override
	public void defineUI()
	{
		setIcon(R.drawable.sound_icon);
		setTitle(R.string.cc_snap_name);
	}

	@Override
	public boolean onInvoke(String ruleId, ParameterBundle parameters)
	{
		snapRecognized = false;
		
		waveGestureHandlerSnap.start();
		
		Timer timer = new Timer("SNAP TIMER");
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				synchronized (monitor) { monitor.notify(); }
			}
		}, snapTimeout);
		

		synchronized (monitor) {
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}
		
		if(waveGestureHandlerSnap.isStarted()) {
			waveGestureHandlerSnap.stop();
		}
		
		
		if(snapRecognized)
		{
			Log.d(LOG_TAG, "Snap recognized!");
			
			return true;
		}
		else
		{
			Log.d(LOG_TAG, "No snap recognized");
		}
		
		return false;
	}

	@Override
	public void onEvent(WaveGestureEvent event)
	{
		String h = null;
		
		if (event.getHandler() == waveGestureHandlerSnap)
		{
			Log.d(LOG_TAG, "waveGestureHandlerSnap");
			h = "Snap handler";
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
			snapRecognized = true;
			
			synchronized (monitor) { monitor.notify(); }
		}
		else {
			snapRecognized = false;
		}
		
		final String s = h + " " + e + " " + event.getTimestamp();
		Log.d(LOG_TAG, s);
	}
}

package io.snapback.snap4magic;

import io.snapback.sdk.gesture.sequence.adapter.AmbientLightSensorAdapter;
import io.snapback.sdk.gesture.sequence.adapter.ProximitySensorAdapter;
import io.snapback.sdk.gesture.wave.WaveGestureEvent;
import io.snapback.sdk.gesture.wave.WaveGestureHandler;
import io.snapback.sdk.gesture.wave.WaveGestureListener;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class MainActivity extends Activity implements WaveGestureListener {

	private static final String LOG_TAG = "MainActivity";

	private ImageView image;
	private WaveGestureHandler waveGestureHandler;
	
	private ProximitySensorAdapter proximitySensorAdapter;
	private AmbientLightSensorAdapter ambientLightSensorAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

		Log.d(LOG_TAG, "onCreate");

		image = (ImageView) findViewById(R.id.image);

		waveGestureHandler = new WaveGestureHandler();
		proximitySensorAdapter = new ProximitySensorAdapter(waveGestureHandler, this);
		ambientLightSensorAdapter = new AmbientLightSensorAdapter(waveGestureHandler, this);
		waveGestureHandler.setSensorAdapters(proximitySensorAdapter, ambientLightSensorAdapter);
		waveGestureHandler.register(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume");

		waveGestureHandler.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOG_TAG, "onPause");

		waveGestureHandler.stop();
	}

	@Override
	public void onEvent(WaveGestureEvent event)
	{
		Log.d(LOG_TAG, event.toString());
		
		String h = null;
		
		if(event.getHandler() == waveGestureHandler)
		{
			Log.d(LOG_TAG, "waveGestureHandler");
			h = "waveGestureHandler";
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
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (image.getVisibility() == View.VISIBLE) {
							image.setVisibility(View.INVISIBLE);
						} else {
							image.setVisibility(View.VISIBLE);
						}
					}
				});
				
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
		
		final String s = h + " " + e + " " + event.getTimestamp();
		Log.d(LOG_TAG, s);
	}
}

package io.snapback.camlauncher;

import io.snapback.sdk.gesture.common.KeyEventDispatcher;
import io.snapback.sdk.gesture.sequence.adapter.HardKeySensorAdapter;
import io.snapback.sdk.gesture.sequence.wave.WaveGestureEvent;
import io.snapback.sdk.gesture.sequence.wave.WaveGestureHandler;
import io.snapback.sdk.gesture.sequence.wave.WaveGestureListener;
import io.snapback.sdk.support.motion.inclination.InclinationDetector;
import io.snapback.sdk.support.motion.inclination.InclinationEvent.InclinationType;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

public class MainActivity extends Activity implements WaveGestureListener {

	private final String LOG_TAG = "MainActivity";

	private Vibrator vibrator;
	
	private InclinationDetector inclinationDetector;
	private WaveGestureHandler waveGestureHandler;
	private HardKeySensorAdapter hardKeySensorAdapter;
	private int keyCode;
	private KeyEventDispatcher keyEventDispatcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		inclinationDetector = new InclinationDetector(this);
		
		//put here the key event you want
		//keyCode = KeyEvent.KEYCODE_FOCUS;
		keyCode = KeyEvent.KEYCODE_VOLUME_UP;
		
		waveGestureHandler = new WaveGestureHandler(this);
		waveGestureHandler.setUsageTrackingDetails("developer@snapback", "SnapbackCamLauncher");
		waveGestureHandler.register(this);
		hardKeySensorAdapter = new HardKeySensorAdapter(waveGestureHandler, keyCode);
		waveGestureHandler.setSensorAdapters(hardKeySensorAdapter);
		
		keyEventDispatcher = new KeyEventDispatcher();
		keyEventDispatcher.addAdapter(hardKeySensorAdapter);
		
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	protected void onResume() {
		inclinationDetector.resume();
		waveGestureHandler.start();

		super.onResume();
	}

	@Override
	protected void onPause() {
		inclinationDetector.pause();
		waveGestureHandler.stop();
		
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void startNewActivity(String packageName) {
		Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
		if (intent != null) {
			// we found the activity
			// now start the activity
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} else {
			// bring user to the market
			// or let them choose an app?
			intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(Uri.parse("market://details?id=" + packageName));
			startActivity(intent);
		}
	}

	@Override
	public void onEvent(WaveGestureEvent event) {
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
				
				vibrator.vibrate(300);
				InclinationType inclinationType = inclinationDetector.getCurrentInclinationType();
				
				Log.d(LOG_TAG, "inclination: " + inclinationType);

				if(inclinationType != null) {
					switch(inclinationType) {
					case VERTICAL_LEFT_SIDE:
					case VERTICAL_RIGHT_SIDE:
						startNewActivity("com.sec.android.app.camera");
						break;

					case VERTICAL_UP:
						startNewActivity("com.android.mms");
						break;

					case HORIZONTAL_FACE_UP:
						startNewActivity("com.google.android.gm");
						break;

					default:
						break;
					}
				}
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
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		
		if(keyEventDispatcher.dispatchKeyEvent(event))
		{
			return true;
		}
		
		return super.dispatchKeyEvent(event);
	}
}

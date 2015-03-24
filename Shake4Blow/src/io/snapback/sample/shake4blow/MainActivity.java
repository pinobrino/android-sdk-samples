package io.snapback.sample.shake4blow;

import io.snapback.sdk.gesture.sequence.adapter.BlowSensorAdapter;
import io.snapback.sdk.gesture.sequence.adapter.ShakeSensorAdapter;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureEvent;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureHandler;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureListener;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	private final static String SHK_STR = "Shake the device...";
	private final static String BLW_STR = "Blow on microphone...";
	private final static String BLW_STR_OK = "Blow recognized!";
	private final static long DEFAULT_DELAY = 2000;
	private final static long DEFAULT_VIBR_DURATION = 1000;
	
	private TextView txtw;
	private PulseGestureHandler pulseShake;
	private PulseGestureHandler pulseBlow;
	private Handler handler;
	private Vibrator v;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		txtw = (TextView)findViewById(R.id.text);
		
		pulseShake = new PulseGestureHandler(this);
		pulseShake.setUsageTrackingDetails("developer@snapback", "Shake4Blow");
		ShakeSensorAdapter shake = new ShakeSensorAdapter(pulseShake, this);
		pulseShake.setSensorAdapters(shake);
		pulseShake.register(new ShakeListener());
		
		pulseBlow = new PulseGestureHandler(this);
		pulseBlow.setUsageTrackingDetails("developer@snapback", "Shake4Blow");
		BlowSensorAdapter blow = new BlowSensorAdapter(pulseBlow, this);
		pulseBlow.setSensorAdapters(blow);
		pulseBlow.register(new BlowListener());
		
		handler = new Handler(Looper.getMainLooper());
		
		v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	private void changeText(final String txt) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				txtw.setText(txt);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(!pulseShake.isStarted()) {
			pulseShake.start();
		}
		
		changeText(SHK_STR);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(pulseShake.isStarted()) {
			pulseShake.stop();
		}
		
		if(pulseBlow.isStarted()) {
			pulseBlow.stop();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class ShakeListener implements PulseGestureListener {

		@Override
		public void onEvent(PulseGestureEvent event) {
			
			if(event.getType() == PulseGestureEvent.PULSE_HOLD_EVENT_TYPE) {
				if(!pulseBlow.isStarted()) {
					pulseBlow.start();
					changeText(BLW_STR);
					v.vibrate(DEFAULT_VIBR_DURATION / 2l);
				}
			}
		}
		
	}
	
	private class BlowListener implements PulseGestureListener {

		@Override
		public void onEvent(PulseGestureEvent event) {
			
			if(event.getType() == PulseGestureEvent.PULSE_START_EVENT_TYPE) {
				changeText(BLW_STR_OK);
				v.vibrate(DEFAULT_VIBR_DURATION);
			}
			
			if(event.getType() == PulseGestureEvent.PULSE_STOP_EVENT_TYPE) {
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						if(pulseBlow.isStarted()) {
							pulseBlow.stop();
						}
						
						changeText(SHK_STR);
					}
				}, DEFAULT_DELAY);
			}
		}
		
	}
}

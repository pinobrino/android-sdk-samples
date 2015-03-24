package io.snapback.sample.position4blow;

import io.snapback.sdk.gesture.sequence.adapter.BlowSensorAdapter;
import io.snapback.sdk.gesture.sequence.adapter.PositionSensorAdapter;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureEvent;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureHandler;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureListener;
import io.snapback.sdk.support.motion.inclination.InclinationEvent.InclinationType;
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

	private final static String POS_STR = "Move and put device in horizontal face up position (with the screen pointing the sky)...";
	private final static String BLW_STR = "Blow on microphone...";
	private final static String BLW_STR_OK = "Blow recognized!";
	private final static long DEFAULT_DELAY = 2000;
	private final static long DEFAULT_VIBR_DURATION = 1000;
	private final static double DEGREE_TOLERANCE = 15d;
	private final static double DEGREE_HYSTERESYS = 30d;
	
	private TextView txtw;
	private PulseGestureHandler pulsePos;
	private PulseGestureHandler pulseBlow;
	private Handler handler;
	private Vibrator v;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		txtw = (TextView)findViewById(R.id.text);
		
		pulsePos = new PulseGestureHandler(this);
		pulsePos.setUsageTrackingDetails("developer@snapback", "Position4Blow");
		PositionSensorAdapter position = new PositionSensorAdapter(pulsePos, this, InclinationType.HORIZONTAL_FACE_UP, false);
		position.setInclinationTypeDegreeTolerance(DEGREE_TOLERANCE, DEGREE_HYSTERESYS);
		pulsePos.setSensorAdapters(position);
		pulsePos.register(new PositionListener());
		
		pulseBlow = new PulseGestureHandler(this);
		pulseBlow.setUsageTrackingDetails("developer@snapback", "Position4Blow");
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
		
		if(!pulsePos.isStarted()) {
			pulsePos.start();
		}
		
		changeText(POS_STR);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(pulsePos.isStarted()) {
			pulsePos.stop();
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
	
	private class PositionListener implements PulseGestureListener {

		@Override
		public void onEvent(PulseGestureEvent event) {
			if(!pulseBlow.isStarted()) {
				pulseBlow.start();
				changeText(BLW_STR);
				v.vibrate(DEFAULT_VIBR_DURATION / 2l);
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
						
						changeText(POS_STR);
					}
				}, DEFAULT_DELAY);
			}
		}
		
	}
}

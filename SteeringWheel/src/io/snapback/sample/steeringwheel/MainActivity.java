package io.snapback.sample.steeringwheel;

import io.snapback.sdk.gesture.sequence.adapter.TouchSensorAdapter;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureEvent;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureHandler;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureListener;
import io.snapback.sdk.support.motion.steering.SteeringWheel;
import io.snapback.sdk.support.motion.steering.SteeringWheelEvent;
import io.snapback.sdk.support.motion.steering.SteeringWheelListener;
import io.snapback.sample.steeringwheel.R;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.widget.TextView;

public class MainActivity extends Activity implements SteeringWheelListener, PulseGestureListener {

	private TextView twIncl;
	private TextView twTouch;
	private SteeringWheel sw;
	private OrientationEventListener mOrientationEventListener;
	private PulseGestureHandler touchHandler;
	private TouchSensorAdapter touchAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		twIncl = (TextView)findViewById(R.id.textViewIncl);
		twTouch = (TextView)findViewById(R.id.textViewTouch);
		
		touchHandler = new PulseGestureHandler(this);
		touchHandler.setUsageTrackingDetails("developer@snapback", "SteeringWheel");
		touchAdapter = new TouchSensorAdapter(touchHandler, getWindow().getDecorView().getRootView());
		touchHandler.setSensorAdapters(touchAdapter);
		touchHandler.register(this);
		
		sw = new SteeringWheel(this);
		sw.registerListener(this);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(touchAdapter.dispatchTouchEvent(ev)) {
			return true;
		}
		
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (mOrientationEventListener == null) {
	        mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {

				@Override
	            public void onOrientationChanged(int orientation) {

	                if (orientation >= 315 || orientation < 45) {
	                	//currentOrientationDegree = 90;
	                }
	                else if (orientation < 315 && orientation >= 225) {
	                	//currentOrientationDegree = 0;
	                	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	                }
	                else if (orientation < 225 && orientation >= 135) {
	                	//currentOrientationDegree = 270;
	                }
	                else { // orientation <135 && orientation > 45
	                	//currentOrientationDegree = 180;
	                	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
	                }   
	            }
	        };
	    }
	    if (mOrientationEventListener.canDetectOrientation()) {
	        mOrientationEventListener.enable();
	    }
		
		sw.start();
		touchHandler.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mOrientationEventListener.disable();
		
		sw.start();
		touchHandler.stop();
	}

	@Override
	public void onSwerve(SteeringWheelEvent steeringWheelEvent) {
		
		//android.util.Log.d("----------", "" + steeringWheelEvent);
		
		final SteeringWheelEvent swe = steeringWheelEvent;
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				twIncl.setText("Steering degree: " + String.format("%,.3f", swe.getValue()));
			}
		});
	}

	@Override
	public void onEvent(PulseGestureEvent event) {
		
		String s = null;
		
		switch (event.getType()) {
			case PulseGestureEvent.PULSE_START_EVENT_TYPE:
				s = "START/PRESS";
			break;
			
			case PulseGestureEvent.PULSE_HOLD_EVENT_TYPE:
				s = "HOLD";
			break;
			
			case PulseGestureEvent.PULSE_STOP_EVENT_TYPE:
				s = "STOP/RELEASE";
			break;

		}
		
		final String sToPrint = s;
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				twTouch.setText("Touch event: " + sToPrint);
			}
		});
	}
}

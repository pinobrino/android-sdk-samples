package io.snapback.sample.rotationgamepad;

import io.snapback.sdk.gesture.common.BaseGestureEvent;
import io.snapback.sdk.gesture.common.BaseGestureHandler;
import io.snapback.sdk.gesture.hub.HubGestureEvent;
import io.snapback.sdk.gesture.hub.HubGestureHandler;
import io.snapback.sdk.gesture.hub.HubGestureListener;
import io.snapback.sdk.gesture.sequence.adapter.RotationSensorAdapter;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureEvent;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureHandler;
import io.snapback.sdk.support.motion.rotation.RotationDetector;
import io.snapback.sdk.support.motion.rotation.RotationEvent.RotationAxis;
import io.snapback.sdk.support.motion.rotation.RotationEvent.RotationType;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements HubGestureListener {

	private HubGestureHandler hubGestureHandler;
	private OrientationEventListener mOrientationEventListener;
	
	private PulseGestureHandler handlerLeft, handlerRight, handlerUp, handlerDown;
	private TextView tvDir;
	
	private boolean reverse;
	private Vibrator v;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		tvDir = (TextView)findViewById(R.id.textViewDir);
		
		RotationSensorAdapter adapterLeft, adapterRight, adapterUp, adapterDown;
		
		handlerLeft = new PulseGestureHandler(this);
		adapterLeft = new RotationSensorAdapter(handlerLeft, this, RotationAxis.X, RotationType.NEGATIVE);
		handlerLeft.setSensorAdapters(adapterLeft);
		
		handlerRight = new PulseGestureHandler(this);
		adapterRight = new RotationSensorAdapter(handlerRight, this, RotationAxis.X, RotationType.POSITIVE);
		handlerRight.setSensorAdapters(adapterRight);
		
		handlerUp = new PulseGestureHandler(this);
		adapterUp = new RotationSensorAdapter(handlerUp, this, RotationAxis.Y, RotationType.POSITIVE, RotationDetector.DEFAULT_SENSOR_DELAY_VALUE, 5.0f, RotationSensorAdapter.DEFAULT_SLOP_TIME_MS);
		handlerUp.setSensorAdapters(adapterUp);
		
		handlerDown = new PulseGestureHandler(this);
		adapterDown = new RotationSensorAdapter(handlerDown, this, RotationAxis.Y, RotationType.NEGATIVE, RotationDetector.DEFAULT_SENSOR_DELAY_VALUE, 5.0f, RotationSensorAdapter.DEFAULT_SLOP_TIME_MS);
		handlerDown.setSensorAdapters(adapterDown);
		
		hubGestureHandler = new HubGestureHandler(this);
		hubGestureHandler.setUsageTrackingDetails("developer@snapback", "RotationGamepad");
		hubGestureHandler.addHandlers(handlerLeft, handlerRight, handlerUp, handlerDown);
		hubGestureHandler.register(this);
		
		reverse = false;
		v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
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
	                	reverse = false;
	                }
	                else if (orientation < 225 && orientation >= 135) {
	                	//currentOrientationDegree = 270;
	                }
	                else { // orientation <135 && orientation > 45
	                	//currentOrientationDegree = 180;
	                	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
	                	reverse = true;
	                }   
	            }
	        };
	    }
	    if (mOrientationEventListener.canDetectOrientation()) {
	        mOrientationEventListener.enable();
	    }
	    
	    hubGestureHandler.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		hubGestureHandler.stop();
		
		mOrientationEventListener.disable();
	}

	@Override
	public void onEvent(HubGestureEvent event) {
		BaseGestureEvent innerEvent = event.getInnerEvent();
		
		BaseGestureHandler<?, ?> innerHandler = innerEvent.getHandler();
		int innerType = innerEvent.getType();
		
		String innerEventString = null;
		if(innerType == PulseGestureEvent.PULSE_START_EVENT_TYPE)
		{
			innerEventString = "START";
		}
		else if(innerType == PulseGestureEvent.PULSE_HOLD_EVENT_TYPE)
		{
			innerEventString = "HOLD";
		}
		else if(innerType == PulseGestureEvent.PULSE_STOP_EVENT_TYPE)
		{
			innerEventString = "STOP";
		}
		
		
		String innerHandlerString = null;
		if(innerHandler == handlerLeft) {
			if(!reverse) {
				innerHandlerString = "LEFT";
			}
			else {
				innerHandlerString = "RIGHT";
			}
		}
		else if(innerHandler == handlerRight) {
			if(!reverse) {
				innerHandlerString = "RIGHT";
			}
			else {
				innerHandlerString = "LEFT";
			}
		}
		else if(innerHandler == handlerUp) {
			if(!reverse) {
				innerHandlerString = "UP";
			}
			else {
				innerHandlerString = "DOWN";
			}
		}
		else if(innerHandler == handlerDown) {
			if(!reverse) {
				innerHandlerString = "DOWN";
			}
			else {
				innerHandlerString = "UP";
			}
		}
		
		final String dir = innerHandlerString;
		final long when = innerEvent.getTimestamp();
		
		if(innerEventString.equals("STOP")) {
			
			v.vibrate(75);
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					tvDir.setText("DIRECTION: " + dir + " " + when);
				}
			});
			
		}
	}
}

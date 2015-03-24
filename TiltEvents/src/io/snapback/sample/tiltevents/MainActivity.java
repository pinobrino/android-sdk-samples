package io.snapback.sample.tiltevents;

import io.snapback.sdk.gesture.common.BaseGestureEvent;
import io.snapback.sdk.gesture.common.BaseGestureHandler;
import io.snapback.sdk.gesture.hub.HubGestureEvent;
import io.snapback.sdk.gesture.hub.HubGestureHandler;
import io.snapback.sdk.gesture.hub.HubGestureListener;
import io.snapback.sdk.gesture.sequence.adapter.TiltSensorAdapter;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureEvent;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureHandler;
import io.snapback.sdk.support.motion.tilt.TiltDetector.TiltSensitivity;
import io.snapback.sdk.support.motion.tilt.TiltEvent.TiltType;
import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity implements HubGestureListener {

	private TextView tilts;
	private int leftcounter, rightcounter, backcounter, forwardcounter;
	
	private PulseGestureHandler leftTilt;
	private PulseGestureHandler rightTilt;
	private PulseGestureHandler backTilt;
	private PulseGestureHandler forwardTilt;	
	private HubGestureHandler hub;
	
	private Vibrator myVib;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		tilts = (TextView)findViewById(R.id.tilts);
		myVib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		
		leftcounter = 0; rightcounter = 0; backcounter = 0; forwardcounter = 0;

		
		leftTilt = new PulseGestureHandler(this);
		leftTilt.setUsageTrackingDetails("developer@snapback.io", "TiltEvents");
		TiltSensorAdapter left = new TiltSensorAdapter(leftTilt, this, TiltType.TILT_LEFT, TiltSensitivity.SLOW);
		leftTilt.setSensorAdapters(left);
		
		rightTilt = new PulseGestureHandler(this);
		rightTilt.setUsageTrackingDetails("developer@snapback.io", "TiltEvents");
		TiltSensorAdapter right = new TiltSensorAdapter(rightTilt, this, TiltType.TILT_RIGHT, TiltSensitivity.SLOW);
		rightTilt.setSensorAdapters(right);
		
		backTilt = new PulseGestureHandler(this);
		backTilt.setUsageTrackingDetails("developer@snapback.io", "TiltEvents");
		TiltSensorAdapter back = new TiltSensorAdapter(backTilt, this, TiltType.TILT_BACK, TiltSensitivity.SLOW);
		backTilt.setSensorAdapters(back);
		
		forwardTilt = new PulseGestureHandler(this);
		forwardTilt.setUsageTrackingDetails("developer@snapback.io", "TiltEvents");
		TiltSensorAdapter forward = new TiltSensorAdapter(forwardTilt, this, TiltType.TILT_FORWARD, TiltSensitivity.SLOW);
		forwardTilt.setSensorAdapters(forward);
		
		hub = new HubGestureHandler(this);
		hub.setUsageTrackingDetails("developer@snapback.io", "TiltEvents");
		hub.addHandlers(leftTilt, rightTilt, backTilt, forwardTilt);
		hub.register(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(!hub.isStarted()) {
			hub.start();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		if(hub.isStarted()) {
			hub.stop();
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

	@Override
	public void onEvent(HubGestureEvent event) {
		// First of all get the inner event which represents the real handler who notify the event
        BaseGestureEvent innerEvent = event.getInnerEvent();
        
        // Then get the handler and the event type
        BaseGestureHandler<?, ?> innerHandler = innerEvent.getHandler();
        int innerType = innerEvent.getType();

        if(innerHandler == leftTilt) {
//        	if(innerType== PulseGestureEvent.PULSE_START_EVENT_TYPE) {
//        	}
        	if(innerType == PulseGestureEvent.PULSE_STOP_EVENT_TYPE) {
        		leftcounter++;
        	}
        }
        else if(innerHandler == rightTilt) {
//        	if(innerType== PulseGestureEvent.PULSE_START_EVENT_TYPE) {
//        	}
        	if(innerType == PulseGestureEvent.PULSE_STOP_EVENT_TYPE) {
        		rightcounter++;
        	}
        }
        else if(innerHandler == backTilt) {
//        	if(innerType== PulseGestureEvent.PULSE_START_EVENT_TYPE) {
//        	}
        	if(innerType == PulseGestureEvent.PULSE_STOP_EVENT_TYPE) {
        		backcounter++;
        	}
        }
        else if(innerHandler == forwardTilt) {
//        	if(innerType== PulseGestureEvent.PULSE_START_EVENT_TYPE) {
//        	}
        	if(innerType == PulseGestureEvent.PULSE_STOP_EVENT_TYPE) {
        		forwardcounter++;
        	}
        }
        else {
        	return;
        }
        
        myVib.vibrate(40);
		
        runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tilts.setText("\n Left Counter: " + leftcounter +
						"\n Right counter: " + rightcounter +
						"\n Back counter: " + backcounter +
						"\n Front counter: " + forwardcounter);
			}
		});
	}
}

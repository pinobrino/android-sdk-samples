package io.snapback.sample.tiltcounter;

import io.snapback.sdk.support.motion.tilt.TiltDetector;
import io.snapback.sdk.support.motion.tilt.TiltDetector.TiltSensitivity;
import io.snapback.sdk.support.motion.tilt.TiltEvent;
import io.snapback.sdk.support.motion.tilt.TiltEvent.TiltType;
import io.snapback.sdk.support.motion.tilt.TiltEventListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity implements TiltEventListener {

	private TextView tilts;
	private int leftcounter, rightcounter, backcounter, forwardcounter;
	private TiltDetector leftTilt;
	private TiltDetector rightTilt;
	private TiltDetector backTilt;
	private TiltDetector forwardTilt;	
	private Vibrator myVib;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		tilts = (TextView)findViewById(R.id.tilts);
		myVib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		
		leftcounter = 0; rightcounter = 0; backcounter = 0; forwardcounter = 0;

		
		leftTilt = new TiltDetector(this, TiltType.TILT_LEFT, TiltSensitivity.NORMAL);

		rightTilt = new TiltDetector(this, TiltType.TILT_RIGHT, TiltSensitivity.NORMAL);

		backTilt = new TiltDetector(this, TiltType.TILT_BACK, TiltSensitivity.NORMAL);

		forwardTilt = new TiltDetector(this, TiltType.TILT_FORWARD, TiltSensitivity.NORMAL);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(leftTilt.isPaused()) {
			leftTilt.registerListener(this);
			leftTilt.resume();
		}
		
		if(rightTilt.isPaused()) {
			rightTilt.registerListener(this);
			rightTilt.resume();
		}
		
		if(backTilt.isPaused()) {
			backTilt.registerListener(this);
			backTilt.resume();
		}
		
		if(forwardTilt.isPaused()) {
			forwardTilt.registerListener(this);
			forwardTilt.resume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		if(!leftTilt.isPaused()) {
			leftTilt.pause();
			leftTilt.unregisterListener(this);
		}
		
		if(!rightTilt.isPaused()) {
			rightTilt.pause();
			rightTilt.unregisterListener(this);
		}
		
		if(!backTilt.isPaused()) {
			backTilt.pause();
			backTilt.unregisterListener(this);
		}
		
		if(!forwardTilt.isPaused()) {
			forwardTilt.pause();
			forwardTilt.unregisterListener(this);
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
	public void onTilt(TiltEvent tiltEvent) {
		
		switch (tiltEvent.getType()){
			case TILT_LEFT:
				leftcounter++;
			break;

			case TILT_RIGHT:
				rightcounter++;
			break;

			case TILT_FORWARD:
				forwardcounter++;
			break;

			case TILT_BACK:
				backcounter++;
			break;
			
			default :
				return;
		}
		
		myVib.vibrate(40);
		
		tilts.setText("\n Left Counter: " + leftcounter +
						"\n Right counter: " + rightcounter +
						"\n Back counter: " + backcounter +
						"\n Front counter: " + forwardcounter);
	}
}

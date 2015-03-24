package io.snapback.sample.orientedkeygamepad;

import java.util.Locale;

import io.snapback.sample.orientedkeygamepad.R;
import io.snapback.sdk.gesture.common.BaseGestureEvent;
import io.snapback.sdk.gesture.common.BaseGestureHandler;
import io.snapback.sdk.gesture.common.KeyEventDispatcher;
import io.snapback.sdk.gesture.hub.HubGestureEvent;
import io.snapback.sdk.gesture.hub.HubGestureHandler;
import io.snapback.sdk.gesture.hub.HubGestureListener;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureEvent;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureHandler;
import io.snapback.sdk.gesture.sequence.adapter.OrientedKeySensorAdapter;
import io.snapback.sdk.support.motion.inclination.InclinationEvent.InclinationType;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements HubGestureListener, OnInitListener {

	private final static String LOG_TAG = "OrientedKeyGamepad";
	
	private int MY_DATA_CHECK_CODE = 0;
	private TextToSpeech myTTS;
	
	private PulseGestureHandler pulseGestureHandlerOrientedKeyUp;
	private OrientedKeySensorAdapter orientedKeyUpSensorAdapterUp;
	private PulseGestureHandler pulseGestureHandlerOrientedKeyDown;
	private OrientedKeySensorAdapter orientedKeyUpSensorAdapterDown;
	private PulseGestureHandler pulseGestureHandlerOrientedKeyLeft;
	private OrientedKeySensorAdapter orientedKeyUpSensorAdapterLeft;
	private PulseGestureHandler pulseGestureHandlerOrientedKeyRight;
	private OrientedKeySensorAdapter orientedKeyUpSensorAdapterRight;
	private PulseGestureHandler pulseGestureHandlerOrientedKeyLeftReverse;
	private OrientedKeySensorAdapter orientedKeyUpSensorAdapterLeftReverse;
	private PulseGestureHandler pulseGestureHandlerOrientedKeyRightReverse;
	private OrientedKeySensorAdapter orientedKeyUpSensorAdapterRightReverse;
	private HubGestureHandler hubGestureHandler;
	
	private KeyEventDispatcher keyEventDispatcher;
	
	private TextView orientedKeyTxt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
		
		orientedKeyTxt = (TextView)findViewById(R.id.orientedKeyTxt);
		
		pulseGestureHandlerOrientedKeyUp = new PulseGestureHandler(this);
		pulseGestureHandlerOrientedKeyUp.setUsageTrackingDetails("developer@snapback", "OrientedKeyGamepad");
		orientedKeyUpSensorAdapterUp = new OrientedKeySensorAdapter(pulseGestureHandlerOrientedKeyUp, this, KeyEvent.KEYCODE_VOLUME_UP, InclinationType.VERTICAL_UP);
		pulseGestureHandlerOrientedKeyUp.setSensorAdapters(orientedKeyUpSensorAdapterUp);
		
		pulseGestureHandlerOrientedKeyDown = new PulseGestureHandler(this);
		pulseGestureHandlerOrientedKeyDown.setUsageTrackingDetails("developer@snapback", "OrientedKeyGamepad");
		orientedKeyUpSensorAdapterDown = new OrientedKeySensorAdapter(pulseGestureHandlerOrientedKeyDown, this, KeyEvent.KEYCODE_VOLUME_DOWN, InclinationType.VERTICAL_UP);
		pulseGestureHandlerOrientedKeyDown.setSensorAdapters(orientedKeyUpSensorAdapterDown);
		
		pulseGestureHandlerOrientedKeyLeft = new PulseGestureHandler(this);
		pulseGestureHandlerOrientedKeyLeft.setUsageTrackingDetails("developer@snapback", "OrientedKeyGamepad");
		orientedKeyUpSensorAdapterLeft = new OrientedKeySensorAdapter(pulseGestureHandlerOrientedKeyLeft, this, KeyEvent.KEYCODE_VOLUME_DOWN, InclinationType.HORIZONTAL_FACE_UP);
		pulseGestureHandlerOrientedKeyLeft.setSensorAdapters(orientedKeyUpSensorAdapterLeft);
		
		pulseGestureHandlerOrientedKeyLeftReverse = new PulseGestureHandler(this);
		pulseGestureHandlerOrientedKeyLeftReverse.setUsageTrackingDetails("developer@snapback", "OrientedKeyGamepad");
		orientedKeyUpSensorAdapterLeftReverse = new OrientedKeySensorAdapter(pulseGestureHandlerOrientedKeyLeftReverse, this, KeyEvent.KEYCODE_VOLUME_UP, InclinationType.HORIZONTAL_FACE_DOWN);
		pulseGestureHandlerOrientedKeyLeftReverse.setSensorAdapters(orientedKeyUpSensorAdapterLeftReverse);
		
		pulseGestureHandlerOrientedKeyRight = new PulseGestureHandler(this);
		pulseGestureHandlerOrientedKeyRight.setUsageTrackingDetails("developer@snapback", "OrientedKeyGamepad");
		orientedKeyUpSensorAdapterRight = new OrientedKeySensorAdapter(pulseGestureHandlerOrientedKeyRight, this, KeyEvent.KEYCODE_VOLUME_UP, InclinationType.HORIZONTAL_FACE_UP);
		pulseGestureHandlerOrientedKeyRight.setSensorAdapters(orientedKeyUpSensorAdapterRight);
		
		pulseGestureHandlerOrientedKeyRightReverse = new PulseGestureHandler(this);
		pulseGestureHandlerOrientedKeyRightReverse.setUsageTrackingDetails("developer@snapback", "OrientedKeyGamepad");
		orientedKeyUpSensorAdapterRightReverse = new OrientedKeySensorAdapter(pulseGestureHandlerOrientedKeyRightReverse, this, KeyEvent.KEYCODE_VOLUME_DOWN, InclinationType.HORIZONTAL_FACE_DOWN);
		pulseGestureHandlerOrientedKeyRightReverse.setSensorAdapters(orientedKeyUpSensorAdapterRightReverse);
		
		hubGestureHandler = new HubGestureHandler(this);
		hubGestureHandler.setUsageTrackingDetails("developer@snapback", "OrientedKeyGamepad");
		hubGestureHandler.addHandler(pulseGestureHandlerOrientedKeyUp);
		hubGestureHandler.addHandler(pulseGestureHandlerOrientedKeyDown);
		hubGestureHandler.addHandler(pulseGestureHandlerOrientedKeyLeft);
		hubGestureHandler.addHandler(pulseGestureHandlerOrientedKeyRight);
		hubGestureHandler.addHandler(pulseGestureHandlerOrientedKeyLeftReverse);
		hubGestureHandler.addHandler(pulseGestureHandlerOrientedKeyRightReverse);
		
		hubGestureHandler.register(this);
		
		keyEventDispatcher = new KeyEventDispatcher();
		keyEventDispatcher.addAdapters(orientedKeyUpSensorAdapterUp,
										orientedKeyUpSensorAdapterDown,
										orientedKeyUpSensorAdapterLeft,
										orientedKeyUpSensorAdapterRight,
										orientedKeyUpSensorAdapterLeftReverse,
										orientedKeyUpSensorAdapterRightReverse);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {      
				myTTS = new TextToSpeech(this, this);
			}
			else {
				Intent installTTSIntent = new Intent();
				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}
		}
	}

	@Override
	public void onInit(int initStatus) {
		if (initStatus == TextToSpeech.SUCCESS) {
			if(myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE) {
				myTTS.setLanguage(Locale.US);
			}
		}
		else if (initStatus == TextToSpeech.ERROR) {
		    Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		myTTS.shutdown();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		hubGestureHandler.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		hubGestureHandler.stop();
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
	public boolean dispatchKeyEvent(KeyEvent event)
	{

		if(keyEventDispatcher.dispatchKeyEvent(event))
		{
			return true;
		}
		
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public void onEvent(HubGestureEvent event) {
		String s = event.toString();
		Log.d(LOG_TAG, s);
		
		BaseGestureEvent innerEvent = event.getInnerEvent();
		String innerEventString = null;
		
		if(innerEvent.getType() == PulseGestureEvent.PULSE_START_EVENT_TYPE)
		{
			innerEventString = "START";
		}
		if(innerEvent.getType() == PulseGestureEvent.PULSE_HOLD_EVENT_TYPE)
		{
			innerEventString = "HOLD";
		}
		if(innerEvent.getType() == PulseGestureEvent.PULSE_STOP_EVENT_TYPE)
		{
			innerEventString = "STOP";
		}
		
		BaseGestureHandler<?, ?> innerHandler = innerEvent.getHandler();
		String ss = null;
		
		if(innerHandler == pulseGestureHandlerOrientedKeyUp)
		{
			Log.d(LOG_TAG, "--------------UP--------------");
			ss = "UP";
		}
		if(innerHandler == pulseGestureHandlerOrientedKeyDown)
		{
			Log.d(LOG_TAG, "--------------DOWN--------------");
			ss = "DOWN";
		}
		if(innerHandler == pulseGestureHandlerOrientedKeyLeft || innerHandler == pulseGestureHandlerOrientedKeyLeftReverse)
		{
			Log.d(LOG_TAG, "--------------LEFT--------------");
			ss = "LEFT";
		}
		if(innerHandler == pulseGestureHandlerOrientedKeyRight || innerHandler == pulseGestureHandlerOrientedKeyRightReverse)
		{
			Log.d(LOG_TAG, "--------------RIGHT--------------");
			ss = "RIGHT";
		}
		Log.d(LOG_TAG, "--------------" + innerEventString + "--------------");
		
		final String orientedKeyString = ss + " " +  innerEventString + " " + innerEvent.getTimestamp();
		
		final String speech = ss;
		final String direction = innerEventString;
		
		runOnUiThread(new Runnable()
		{
			  public void run()
			  {
				  orientedKeyTxt.setText(orientedKeyString);
				  
				  if(direction!= null && !direction.equalsIgnoreCase("STOP")) {
					  myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
				  }
			  }
		});
	}
}

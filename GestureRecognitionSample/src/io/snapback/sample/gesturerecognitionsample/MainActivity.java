package io.snapback.sample.gesturerecognitionsample;

import io.snapback.sdk.util.SerializationUtil;
import io.snapback.sdk.support.motion.gesture.GestureDetector;
import io.snapback.sdk.support.motion.gesture.GestureDetector.GestureTolerance;
import io.snapback.sdk.support.motion.gesture.GestureEvent;
import io.snapback.sdk.support.motion.gesture.GestureEvent.GestureEventType;
import io.snapback.sdk.support.motion.gesture.GestureEventListener;
import io.snapback.sdk.support.motion.gesture.GestureRecorder;
import io.snapback.sdk.support.motion.gesture.GestureSignal;
import io.snapback.sdk.support.motion.gesture.GestureSignalSerializable;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements GestureEventListener {

	private GestureRecorder recorder;
	private GestureDetector detector;
	private GestureSignal baseGesture;
	private TextView txt;
	private DecimalFormat df;
	private Button bt;
	private Vibrator v;
	
	private static final String FILENAME = "basegesture";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		recorder = new GestureRecorder(this);

		detector = new GestureDetector(GestureTolerance.LOW);
		detector.registerListener(this);
		
		txt = (TextView)findViewById(R.id.tv_info);
		df = new DecimalFormat("#.##");
		
		bt = (Button)findViewById(R.id.bt_reset);
		bt.setEnabled(false);
		bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bt.setEnabled(false);
				baseGesture = null;
				
				File f = new File(getFilesDir(), FILENAME);
				if(f.exists()) {
					f.delete();
				}
				
				txt.setText("No gesture");
			}
		});
		
		v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		
		
		File f = new File(getFilesDir(), FILENAME);
		if(f.exists()) {
			
			Log.d("onCreate", "loading " + f.getAbsolutePath() + "...");
			
			GestureSignalSerializable baseGestureSer = loadGesture(FILENAME);
			
			if(baseGestureSer != null) {
				detector.setBaseGestureSerialized(baseGestureSer);
				baseGesture = new GestureSignal(baseGestureSer);
				txt.setText("Base gesture loaded\n(duration: " + ((double)baseGesture.getDurationMillis() / 1000d) + " s)");
				bt.setEnabled(true);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		recorder.stopListening();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		recorder.startListening();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if(!recorder.isListening()) {
			return super.onTouchEvent(event);
		}
		
		int eventAction = event.getAction();

		switch (eventAction) {
		
			case MotionEvent.ACTION_DOWN:
				
				if(baseGesture == null) {
					txt.setText("Recording base gesture...");
					
				}
				else {
					txt.setText("Recording gesture to compare with...");
				}
				
				recorder.startRecording();
	
			break;
	
			case MotionEvent.ACTION_UP:
	
				if(baseGesture == null) {
					baseGesture = recorder.stopRecording();

					saveGesture(baseGesture.getGestureSignalToSerialize(), FILENAME);
					GestureSignalSerializable baseGestureSer = loadGesture(FILENAME);
					
					if(baseGestureSer != null) {
						detector.setBaseGestureSerialized(baseGestureSer);
						baseGesture = new GestureSignal(baseGestureSer);
						txt.setText("Base gesture recorded\n(duration: " + ((double)baseGesture.getDurationMillis() / 1000d) + " s)");
						bt.setEnabled(true);
					}
					else {
						txt.setText("Base gesture NOT recorded!");
					}
					
				}
				else {
					GestureSignal gestureToCompareWith = recorder.stopRecording();
					
					txt.setText("Comparing gestures...");
					detector.compareBaseGestureWith(gestureToCompareWith);
				}
				
			break;
	
			default:
			break;
		}

		return super.onTouchEvent(event);
	}
	
	private void saveGesture(GestureSignalSerializable baseGestureToSerialize, String fileName) {
		try {
			SerializationUtil.serialize(MainActivity.this, baseGestureToSerialize, fileName);
		} catch (IOException e) {
			Log.e("SerializationUtil", e.getMessage());
		}
	}
	
	private GestureSignalSerializable loadGesture(String fileName) {
		Object baseGestureSer = null;
		
		try {
			baseGestureSer = SerializationUtil.deserialize(MainActivity.this, fileName);
		} catch (ClassNotFoundException | IOException e) {
			Log.e("SerializationUtil", e.getMessage());
		}
		
		if(baseGestureSer != null) {
			return (GestureSignalSerializable)baseGestureSer;
		}
		
		return null;
	}

	@Override
	public void onGesture(GestureEvent gestureEvent) {
		
		GestureEventType gestureEventType = gestureEvent.getGestureEventType();
		
		if(gestureEventType.equals(GestureEventType.GESTURE_MATCH)) {
			v.vibrate(1000);
			txt.setText("Gestures match!\nSimilarity: " + df.format(gestureEvent.getSimilarity()) + "%\n(duration: " + ((double)gestureEvent.getDurationMillis() / 1000d) + " s)");
			ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
			toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150);
			toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150);
		}
		else if(gestureEventType.equals(GestureEventType.GESTURE_MISMATCH)) {
			v.vibrate(250);
			txt.setText("Gestures NOT match!\nSimilarity: " + df.format(gestureEvent.getSimilarity()) + "%\n(duration: " + ((double)gestureEvent.getDurationMillis() / 1000d) + " s)");
			ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
			toneG.startTone(ToneGenerator.TONE_SUP_ERROR, 500);
		}
		
	}
}

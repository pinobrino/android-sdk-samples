package com.atooma.plugin.snapback;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.atooma.plugin.ParameterBundle;
import com.atooma.plugin.Performer;
import com.atooma.plugin.blow.R;

public class PE_TorchOnOff extends Performer
{
	private static final String LOG_TAG = PE_TorchOnOff.class.getSimpleName();
	
	private static final String torch_off_intent = "com.atooma.plugin.snapback.PE_TorchOnOff.TORCH_INTENT";
	private static final int notify_id = 1;
	private Camera camera;
	private Parameters cParameters;
	private NotificationManager notificationManager;

	public PE_TorchOnOff(Context context, String id, int version)
	{
		super(context, id, version);
		
		BroadcastReceiver br = new BroadcastReceiver()
    	{
			@Override
			public void onReceive(Context arg0, Intent arg1)
			{
				if(arg1.getAction().equalsIgnoreCase(torch_off_intent))
				{
					turnOff();
					releaseCamera();
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(torch_off_intent);
		
		getContext().registerReceiver(br, filter);
	}

	@Override
	public ParameterBundle onInvoke(String arg0, ParameterBundle arg1) throws RemoteException
	{
		if(camera == null)
		{
			try
			{
				camera = Camera.open();
			}
			catch(RuntimeException e)
			{
				// if camera is already open
				Log.e(LOG_TAG, e.getMessage());
				Log.e(LOG_TAG, "The camera and flashlight are in use by another app");
				
				new Handler(getContext().getMainLooper()).post(new Runnable()
				{
					  public void run()
					  {
						  Toast.makeText(getContext(), "Snapback plugin: The camera and flashlight are in use by another app.", Toast.LENGTH_LONG).show();
					  }
				});
				
				return null;
			}
		}
		
	    cParameters = camera.getParameters();
	    
	    if(cParameters.getFlashMode().equalsIgnoreCase(Camera.Parameters.FLASH_MODE_TORCH))
	    {
	    	turnOff();
	    	releaseCamera();
	    	
	    	if(notificationManager != null)
	    	{
	    		notificationManager.cancel(notify_id);
	    	}
	    }
	    else
	    {
	    	turnOn();
	    	notification();
	    }
		
	    
		return null;
	}
	
	private void turnOn()
	{
		if(camera != null)
		{
			cParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			camera.setParameters(cParameters);
		}
	}
	
	private void turnOff()
	{
		if(camera != null)
		{
			cParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			camera.setParameters(cParameters);
		}
	}
	
	private void releaseCamera()
	{
		if(camera != null)
		{
			camera.release();
			camera = null;
		}
	}
	
	private void notification()
	{
		PendingIntent penIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(torch_off_intent), PendingIntent.FLAG_CANCEL_CURRENT);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext());
		mBuilder.setAutoCancel(true);
		mBuilder.setOngoing(true);
		mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
		mBuilder.setContentTitle("Torch on");
		mBuilder.setContentText("Tap to turn the torch off");
		mBuilder.setSmallIcon(R.drawable.torch_icon);
		mBuilder.setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.brand_icon));	
		mBuilder.setContentIntent(penIntent);

		notificationManager = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notify_id, mBuilder.build());
	}

	@Override
	public void defineUI()
	{
		setIcon(R.drawable.torch_icon);
		setTitle(R.string.pe_torch_name);
	}
}

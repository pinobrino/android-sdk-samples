package io.snapback.plugin.util;

import io.snapback.plugin.config.AssetsPropertyReader;
import io.snapback.plugin.data.ParseHandler;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

public class Utils
{
	public static int getScreenOrientationDegree(Context ctx, String LOG_TAG)
	{
		WindowManager wm = (WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE);
		
	    int rotation = wm.getDefaultDisplay().getRotation();
	    DisplayMetrics dm = new DisplayMetrics();
	    wm.getDefaultDisplay().getMetrics(dm);
	    int width = dm.widthPixels;
	    int height = dm.heightPixels;
	    int orientation;
	    // if the device's natural orientation is portrait:
	    if ((rotation == Surface.ROTATION_0
	            || rotation == Surface.ROTATION_180) && height > width ||
	        (rotation == Surface.ROTATION_90
	            || rotation == Surface.ROTATION_270) && width > height)
	    {
	        switch(rotation)
	        {
	            case Surface.ROTATION_0:
	                //orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	            	orientation = 0;
	                break;
	            case Surface.ROTATION_90:
	                //orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	            	orientation = 90;
	                break;
	            case Surface.ROTATION_180:
	                //orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
	            	orientation = 180;
	                break;
	            case Surface.ROTATION_270:
	                //orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	                orientation = 270;
	                break;
	            default:
	                Log.e(LOG_TAG, "Unknown screen orientation. Defaulting to portrait.");
	                //orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	                orientation = 0;
	                break;              
	        }
	    }
	    // if the device's natural orientation is landscape or if the device
	    // is square:
	    else
	    {
	        switch(rotation)
	        {
	            case Surface.ROTATION_0:
	                //orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	            	orientation = 90;
	                break;
	            case Surface.ROTATION_90:
	                //orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	            	orientation = 0;
	                break;
	            case Surface.ROTATION_180:
	                //orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	                orientation = 270;
	                break;
	            case Surface.ROTATION_270:
	                //orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
	            	orientation = 180;
	                break;
	            default:
	                Log.e(LOG_TAG, "Unknown screen orientation. Defaulting to landscape.");
	                //orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	                orientation = 90;
	                break;              
	        }
	    }

	    return orientation;
	}

	public static void pushToParse(Context ctx, boolean blowRecognized)
	{
		//TODO find other way to conditional compilation
		if(ParseConstants.USE_PARSE)
		{
			AssetsPropertyReader apr = AssetsPropertyReader.getInstance(ctx);
			String appId = apr.getParseAppID();
			String clientId = apr.getParseClientID();

			if(appId != null && appId.length() != 0 && !appId.equalsIgnoreCase("") &&
					clientId != null && clientId.length() != 0 && !clientId.equalsIgnoreCase("") )
			{
				final ParseHandler ph = ParseHandler.getInstance(ctx, ParseConstants.unique_id_value, appId, clientId);

				final Map<String, Number> dataMapToPush = new HashMap<String, Number>();

				if(blowRecognized)
				{
					dataMapToPush.put(ParseConstants.true_positive_key, 1);
					dataMapToPush.put(ParseConstants.false_positive_key, 0);
				}
				else
				{
					dataMapToPush.put(ParseConstants.true_positive_key, 0);
					dataMapToPush.put(ParseConstants.false_positive_key, 1);
				}

				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						ph.pushIncrements(ParseConstants.blow_trigger_stats_class_name, dataMapToPush);
					}
				}).start();
			}
		}
	}
}

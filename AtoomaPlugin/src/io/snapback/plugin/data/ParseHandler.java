package io.snapback.plugin.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class ParseHandler
{
	private static final String LOG_TAG = ParseHandler.class.getName();
	private final static String unique_id_key = "uniqueID";
	private final static String app_ver_code_key = "appVerCode";
	private int appVerCode;
	private final static String app_ver_name_key = "appVerName";
	private String appVerName;
	private static ParseHandler instance;
	
	// entry IDs
	private Map<String, String> objectIDs;
	private String uniqueDeviceID;
	private SharedPrefsHandler shPrefs;
	
	public static ParseHandler getInstance(Context ctx, String uniqueDeviceID, String appID, String clientID)
	{
		if(instance == null)
		{
			instance = new ParseHandler(ctx, uniqueDeviceID, appID, clientID);
		}
		
		return instance;
	}
	
	private ParseHandler(Context ctx, String uniqueDeviceID, String appID, String clientID)
	{
		Parse.enableLocalDatastore(ctx);
		Parse.initialize(ctx, appID, clientID);
		
		this.uniqueDeviceID = uniqueDeviceID;
		objectIDs = new HashMap<String, String>();
		shPrefs = new SharedPrefsHandler(ctx);
		
		try
		{
			appVerCode = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;
			appVerName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
			
			this.uniqueDeviceID = appVerCode + "_" + appVerName + "_" + uniqueDeviceID;
		}
		catch(NameNotFoundException e)
		{
			Log.e(LOG_TAG, "Error: " + e.getMessage());
			
			appVerCode = -1;
			appVerName = null;
		}
	}

	private boolean loadFromPrefs(String className)
	{
		String val = shPrefs.readString(className);
		
		if(val != null)
		{
			objectIDs.put(className, val);
			return true;
		}
		
		return false;
	}
	
	private void saveToPrefs(String className, String val)
	{
		shPrefs.write(className, val);
	}
	
	/**
	 * Returns an unique ID which identifies the user entry in the cloud.<br>
	 * If the returned value is null, means the current device doesn't push any data yet or the application version differs.
	 */
	private String retrieveObjectIDFromClassName(final String className)
	{
		if(!objectIDs.containsKey(className))
		{
			if(!loadFromPrefs(className))
			{
				ParseQuery<ParseObject> query = ParseQuery.getQuery(className);
				query.whereEqualTo(unique_id_key, uniqueDeviceID);
				
				try
				{
					List<ParseObject> objects = query.find();
					
					Log.d(LOG_TAG, "Retrieved " + objects.size() + " objects form class " + className);
					
					if(objects != null && objects.size() > 0)
					{
						ParseObject obj = objects.get(0);
						
						String id = obj.getObjectId();
						objectIDs.put(className, id);
						saveToPrefs(className, id);
					}
				}
				catch(ParseException e)
				{
					Log.d(LOG_TAG, "Error: " + e.getMessage());
				}
			}
		}
		
		return objectIDs.get(className);
	}
	
	/**
	 * Returns an unique ID which identifies the user entry in the cloud.<br>
	 * If the returned value is null, means the current device doesn't push any data yet or the application version differs.
	 */
	public String getObjectIDFromClassName(String className)
	{
		return retrieveObjectIDFromClassName(className);
	}
	
	/**
	 * Returns true if current user already have an entry into the cloud.
	 */
	public boolean isEntryPresentInClass(String className)
	{
		return (getObjectIDFromClassName(className) != null);
	}
	
	public void pushIncrements(final String className, final Map<String, Number> dataMapToPush)
	{
		final ParseObject pObj = new ParseObject(className);
		
		String objectID = getObjectIDFromClassName(className);
		
		if(objectID == null)
		{
			pObj.put(unique_id_key, uniqueDeviceID);
			
			pObj.put(app_ver_code_key, appVerCode);
			pObj.put(app_ver_name_key, appVerName);
			
			for(String dataKey : dataMapToPush.keySet())
			{
				pObj.increment(dataKey, dataMapToPush.get(dataKey));
			}
			
			// commit
			pObj.saveEventually(new SaveCallback()
			{
				@Override
				public void done(ParseException e)
				{
					if(e == null)
					{
						String val = pObj.getObjectId();
						objectIDs.put(className, val);
						saveToPrefs(className, val);
					}
				}
			});
		}
		else
		{
			// Retrieve the object by id
			ParseQuery<ParseObject> query = ParseQuery.getQuery(className);
			query.getInBackground(objectID, new GetCallback<ParseObject>()
			{
				public void done(ParseObject pObj, ParseException e)
				{
					if(e == null)
					{
						// Now let's update it with some new data.
						for(String dataKey : dataMapToPush.keySet())
						{
							pObj.increment(dataKey, dataMapToPush.get(dataKey));
						}
						
						// commit
						pObj.saveEventually();
					}
				}
			});
		}
	}

	/**
	 * Pushes new data or updates data already pushed in the past.
	 */
	public void pushData(final String className, final Map<String, Object> dataMapToPush)
	{
		String objectID = getObjectIDFromClassName(className);
		
		if(objectID == null)
		{
			final ParseObject pObj = new ParseObject(className);
			pObj.put(unique_id_key, uniqueDeviceID);
			
			pObj.put(app_ver_code_key, appVerCode);
			pObj.put(app_ver_name_key, appVerName);
			
			for(String dataKey : dataMapToPush.keySet())
			{
				pObj.put(dataKey, dataMapToPush.get(dataKey));
			}
			
			// commit
			pObj.saveEventually(new SaveCallback()
			{
				@Override
				public void done(ParseException e)
				{
					if(e == null)
					{
						String val = pObj.getObjectId();
						objectIDs.put(className, val);
						saveToPrefs(className, val);
					}
				}
			});
		}
		else
		{
			// Retrieve the object by id
			ParseQuery<ParseObject> query = ParseQuery.getQuery(className);
			query.getInBackground(objectID, new GetCallback<ParseObject>()
			{
				public void done(ParseObject pObj, ParseException e)
				{
					if(e == null)
					{
						// Now let's update it with some new data.
						for(String dataKey : dataMapToPush.keySet())
						{
							pObj.put(dataKey, dataMapToPush.get(dataKey));
						}
						
						// commit
						pObj.saveEventually();
					}
				}
			});
		}
	}
}

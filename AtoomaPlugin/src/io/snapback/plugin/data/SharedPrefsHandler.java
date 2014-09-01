package io.snapback.plugin.data;

import io.snapback.plugin.util.Constants;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class SharedPrefsHandler
{
	private SharedPreferences prefs;
	private static final String vers_key = "app_vers_key";

	public SharedPrefsHandler(Context ctx)
	{
		prefs = ctx.getSharedPreferences(Constants.preference_file_key, Context.MODE_PRIVATE);
		
		String last = prefs.getString(vers_key, null);
		int appVerCode = 0;
		String appVerName = "";
		
		try
		{
			appVerCode = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;
			appVerName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		}
		catch(NameNotFoundException e)
		{
			Log.e(SharedPrefsHandler.class.getSimpleName(), "Error: " + e.getMessage());
		}
		
		String curr = appVerCode + "_" + appVerName;
		
		if(last == null || !last.equalsIgnoreCase(curr))
		{
			clearAllData();
			write(vers_key, curr);
		}
	}
	
	public void clearAllData()
	{
		prefs.edit().clear();
		prefs.edit().commit();
	}
	
	/**
	 * 
	 * @param key String key
	 * @param val Integer, Double (will converted to Float), Float, String, Boolean
	 */
	public void write(String key, Object val)
	{
		if(key == null || val == null)
		{
			return;
		}
		
		Editor editor = null;
		
		if(val instanceof Long)
		{
			editor = prefs.edit();
			editor.putLong(key, (Long)val);
			editor.commit();
		}
		
		if(val instanceof Integer)
		{
			editor = prefs.edit();
			editor.putInt(key, (Integer)val);
			editor.commit();
		}
		
		if(val instanceof Double)
		{
			editor = prefs.edit();
			editor.putFloat(key, ((Double)val).floatValue());
			editor.commit();
		}
		
		if(val instanceof Float)
		{
			editor = prefs.edit();
			editor.putFloat(key, (Float)val);
			editor.commit();
		}
		
		if(val instanceof String)
		{
			editor = prefs.edit();
			editor.putString(key, (String)val);
			editor.commit();
		}
		
		if(val instanceof Boolean)
		{
			editor = prefs.edit();
			editor.putBoolean(key, (Boolean)val);
			editor.commit();
		}
	}
	
	public Float readFloat(String key)
	{
		if(key != null)
		{
			return prefs.getFloat(key, 0);
		}
		
		return null;
	}
	
	public Long readLong(String key)
	{
		if(key != null)
		{
			return prefs.getLong(key, 0);
		}
		
		return null;
	}
	
	public Integer readInteger(String key)
	{
		if(key != null)
		{
			return prefs.getInt(key, 0);
		}
		
		return null;
	}
	
	public Boolean readAsBoolean(String key)
	{
		if(key != null)
		{
			String s = readString(key);
			
			if(s != null)
			{
				return Boolean.parseBoolean(s);
			}
		}
		
		return null;
	}
	
	public Boolean readBooelan(String key)
	{
		if(key != null)
		{
			return prefs.getBoolean(key, false);
		}
		
		return null;
	}
	
	public String readString(String key)
	{
		if(key != null)
		{
			return prefs.getString(key, null);
		}
		
		return null;
	}
}

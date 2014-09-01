package io.snapback.plugin.config;

import io.snapback.plugin.util.ParseConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class AssetsPropertyReader
{
	private static final String DEFAULT_FILE_NAME = "config.properties";
	private static AssetsPropertyReader instance;
	private Properties properties;

	/**
	 * Convenient method useful where there is no context to provide.<br>
	 * WARNING: call this method when the class object is already instantiated.
	 */
	public static AssetsPropertyReader getLastInstance()
	{
		return instance;
	}
	
	public static AssetsPropertyReader getInstance(Context context)
	{
		return getInstance(context, DEFAULT_FILE_NAME);
	}
	
	public static AssetsPropertyReader getInstance(Context context, String fileName)
	{
		if(instance == null)
		{
			instance = new AssetsPropertyReader(context, fileName);
		}
		
		return instance;
	}
	
	private AssetsPropertyReader(Context context, String fileName)
	{
		properties = new Properties();
		
		try
		{
			/**
			 * getAssets() Return an AssetManager instance for your
			 * application's package. AssetManager Provides access to an
			 * application's raw asset files;
			 */
			AssetManager assetManager = context.getAssets();
			/**
			 * Open an asset using ACCESS_STREAMING mode. This
			 */
			InputStream inputStream = assetManager.open(fileName);
			/**
			 * Loads properties from the specified InputStream,
			 */
			properties.load(inputStream);

		}
		catch(IOException e)
		{
			Log.e("AssetsPropertyReader", e.toString());
		}
	}
	
	public String getValue(String key)
	{
		return properties.getProperty(key, null);
	}
	
	public String getParseAppID()
	{
		return getValue(ParseConstants.parse_app_id_key);
	}
	
	public String getParseClientID()
	{
		return getValue(ParseConstants.parse_client_id_key);
	}
}

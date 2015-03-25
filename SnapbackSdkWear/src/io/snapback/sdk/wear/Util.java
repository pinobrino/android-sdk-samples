package io.snapback.sdk.wear;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

public class Util {
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static Sensor getSensorFromStringId(SensorManager sensorManager, String id) {
    	Sensor ret = null;
    	
    	if(id != null) {
    		if(id.indexOf("-") != -1 ) {
    			String[] ss = id.split("-");
    			
    			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
    				ret = sensorManager.getDefaultSensor(Integer.parseInt(ss[0]));
    			}
    			else {
    				if(ss[1].equals("wkp")) {
    					ret = sensorManager.getDefaultSensor(Integer.parseInt(ss[0]), true);
    				}
    				else if(ss[1].equals("nwkp")) {
    					ret = sensorManager.getDefaultSensor(Integer.parseInt(ss[0]), false);
    				}
    				else {
    					ret = sensorManager.getDefaultSensor(Integer.parseInt(ss[0]));
    				}
    			}
    		}
    		else {
    			ret = sensorManager.getDefaultSensor(Integer.parseInt(id));
    		}
    	}
    	
    	return ret;
    }
	
	public static String getIdForSensor(int sensorType, Boolean wakeUp) {
		if(wakeUp == null) {
			return getIdForSensor(sensorType, (String)null);
		}
		
		return getIdForSensor(sensorType, Boolean.toString(wakeUp));
	}
	
	public static String getIdForSensor(int sensorType, String wakeUp) {
    	String ret = "" + sensorType;
    	
    	if(wakeUp != null) {
        	if(Boolean.valueOf(wakeUp)) {
        		ret += "-wkp";
        	}
        	else {
        		ret += "-nwkp";
        	}
    	}
    	
    	return ret;
    }
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static String getReportingModeString(Sensor sensor) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        
        String s = null;

        switch (sensor.getReportingMode()) {
            case Sensor.REPORTING_MODE_CONTINUOUS:
                s = "CONTINUOUS";
            break;
            case Sensor.REPORTING_MODE_ON_CHANGE:
                s = "ON_CHANGE";
            break;
            case Sensor.REPORTING_MODE_ONE_SHOT:
                s = "ONE_SHOT";
            break;
            case Sensor.REPORTING_MODE_SPECIAL_TRIGGER:
                s = "SPECIAL_TRIGGER";
            break;
        }
        
        return s;
    }
	
	public static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}

		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		}
		else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}
}

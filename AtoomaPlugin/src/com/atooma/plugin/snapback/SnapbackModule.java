package com.atooma.plugin.snapback;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.atooma.plugin.Module;
import com.atooma.plugin.blow.R;

public class SnapbackModule extends Module
{
	public SnapbackModule(Context context, String id, int version)
    {
        super(context, id, version);
    }
    
    public static final String MODULE_ID = "SnapbackModule";
    public static final int MODULE_VERSION = 1;

    @Override
    public void registerComponents()
    {
		registerTrigger(new TR_BlowDetectionGestureBased(getContext(), "Gesture+BlowDetection", 2));
		registerConditionChecker(new CC_BlowDetection(getContext(), "BlowDetection", 2));
		registerConditionChecker(new CC_SnapDetection(getContext(), "SnapDetection", 2));
		
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		String deviceName = manufacturer + " " + model;
		
		if(getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) &&
			!deviceName.equalsIgnoreCase("LGE Nexus 5") )
		{
			registerPerformer(new PE_TorchOnOff(getContext(), "TorchToggle", 1));
		}
    }
    
    @Override
    public void defineUI()
    {
        setIcon(R.drawable.brand_icon);
        setTitle(R.string.module_name);
    }
    
	@Override
	public void defineAuth()
	{
	}
	
	@Override
	public void clearCredentials()
	{
	}   
}
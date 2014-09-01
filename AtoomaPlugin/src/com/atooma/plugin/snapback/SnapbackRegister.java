package com.atooma.plugin.snapback;

import com.atooma.plugin.Module;
import com.atooma.sdk.RegisterService;

public class SnapbackRegister extends RegisterService
{
    @Override
    public Module getModuleInstance()
    {
        return new SnapbackModule(this, SnapbackModule.MODULE_ID, SnapbackModule.MODULE_VERSION);
    }
}

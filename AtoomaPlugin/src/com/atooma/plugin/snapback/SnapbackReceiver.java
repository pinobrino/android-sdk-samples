package com.atooma.plugin.snapback;

import com.atooma.sdk.AtoomaRegistrationReceiver;

public class SnapbackReceiver extends AtoomaRegistrationReceiver
{
    @Override
    public Class<SnapbackRegister> getRegisterServiceClass()
    {
        return SnapbackRegister.class;
    }
}

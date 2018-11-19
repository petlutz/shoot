package de.pgz.invoiceshoot.vr;

import com.jme3.app.VRConstants;
import com.jme3.input.vr.VRInputType;

public abstract class Constants {
	
//  Oculus config:
	public static final float FLOOR_Y = -1.3f;
	public static final VRInputType TRIGGERAXIS = VRInputType.OculusTriggerAxis;
	public static final int VRAPI = VRConstants.SETTING_VRAPI_OCULUSVR_VALUE;
	
//  OpenVR config:
//	public static final float FLOOR_Y = -0.5f;
//	public static final VRInputType TRIGGERAXIS = VRInputType.ViveTriggerAxis;
//	public static final int VRAPI = VRConstants.SETTING_VRAPI_OPENVR_VALUE;

	public static final float DROPZONE_XZ = 20;

	public static final float DROPZONE_Y = 20;

	public final static long DESTROY_TIME = 20000;
	
	public static final String MAT_LIGHTING = "Common/MatDefs/Light/Lighting.j3md";
}

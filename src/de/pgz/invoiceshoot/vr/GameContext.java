package de.pgz.invoiceshoot.vr;

import java.util.List;

import com.jme3.app.SimpleApplication;
import com.jme3.app.VRAppState;
import com.jme3.bullet.BulletAppState;

public interface GameContext {
	
	public VRAppState getVRAppState();
	
	public BulletAppState getBulletAppState();
	
	public SimpleApplication getApp();
	
	public List<Target> targets();
	
	public List<Updateable> updateables();
	
	public void addScore(long score);

}

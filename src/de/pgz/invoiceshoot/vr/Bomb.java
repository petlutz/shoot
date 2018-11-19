package de.pgz.invoiceshoot.vr;

import java.util.Date;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Bomb implements Target, Updateable {

	private GameContext ctx;
	
	private Node geo;

	private RigidBodyControl phy;

	private long creationTime = 0;
	
	private boolean dead = false;

	public Bomb(GameContext ctx) {
		this.ctx = ctx;
		
		geo = (Node) ctx.getApp().getAssetManager().loadModel("Models/bomb.blend");
		
		geo.setLocalScale(0.1f);
		geo.setLocalTranslation(Utility.getRandomFLoat(-Constants.DROPZONE_XZ, Constants.DROPZONE_XZ), Constants.DROPZONE_Y,
				Utility.getRandomFLoat(-Constants.DROPZONE_XZ, Constants.DROPZONE_XZ));
	 
		ctx.getApp().getRootNode().attachChild(geo);

		phy = new RigidBodyControl(new SphereCollisionShape(0.1f), 1.0f);
		
		ctx.getBulletAppState().getPhysicsSpace().add(phy);
	
		geo.addControl(phy);

		phy.setSleepingThresholds(0.0001f, 0.0001f);
		phy.applyTorque(new Vector3f(Utility.getRandomFLoat(-1, 1), Utility.getRandomFLoat(-1, 1), Utility.getRandomFLoat(-1, 1)));

		creationTime = new Date().getTime();

	}

	@Override
	public Spatial getSpatial() {
		return geo;
	}

	public void destroy() {
		phy.setEnabled(false);
		geo.removeControl(phy);
		geo.removeFromParent();
		ctx.getBulletAppState().getPhysicsSpace().remove(phy);
		dead = true;
	}

	@Override
	public boolean isDead() {
		return dead;
	}

	@Override
	public void update() {
		if (new Date().getTime() - creationTime > Constants.DESTROY_TIME) {
			destroy();
		} 
	}
	
	@Override
	public void hit() {
		destroy();
		ctx.updateables().add(new Explosion(ctx,geo.getWorldTranslation(), 5.0f, 1.0f));
		for (Target target: ctx.targets()) {
			if (!target.isDead()) {
				if (target.getSpatial().getWorldBound().distanceTo(geo.getWorldTranslation()) < 10.0f)  {
					target.hit();
				}
			}
		}
	}
}

package de.pgz.invoiceshoot.vr;

import java.util.Date;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

public class Invoice implements Target, Updateable {

	private static Texture invoiceTex;
	
	private GameContext ctx;
	
	private Geometry geo;

	private RigidBodyControl phy;

	private long creationTime = 0;
	
	private boolean dead = false;

	public Invoice(GameContext ctx) {
		this.ctx = ctx;
	
		geo = createGeometry();

		ctx.getApp().getRootNode().attachChild(geo);

		phy = new RigidBodyControl(1.0f);
		geo.addControl(phy);

		ctx.getBulletAppState().getPhysicsSpace().add(phy);
		phy.setSleepingThresholds(0.0001f, 0.0001f);
		// kartePhy.setMass(0.001f);
		// kartePhy.setGravity(new Vector3f(0.0f, GRAVITY, 0.0f) );
		phy.applyTorque(new Vector3f(Utility.getRandomFLoat(-1, 1), Utility.getRandomFLoat(-1, 1), Utility.getRandomFLoat(-1, 1)));

		creationTime = new Date().getTime();
		// if (lastLocation != null) {
		// Vector3f velocity = location.subtract(lastLocation).mult(15);
		// kartePhy.setLinearVelocity(velocity);
		// }
	}

	@Override
	public Spatial getSpatial() {
		return geo;
	}

	private Geometry createGeometry() {
		Material matKarte = new Material(ctx.getApp().getAssetManager(), Constants.MAT_LIGHTING);

		if (invoiceTex == null)
			invoiceTex = ctx.getApp().getAssetManager().loadTexture("Textures/rf.png");

		matKarte.setTexture("DiffuseMap", invoiceTex);

		Geometry geo = new Geometry("invoice", new Box(0.2f, 0.1f, 0.003f));
		geo.setMaterial(matKarte);
		geo.setShadowMode(RenderQueue.ShadowMode.Cast);

		geo.setLocalTranslation(Utility.getRandomFLoat(-Constants.DROPZONE_XZ, Constants.DROPZONE_XZ), Constants.DROPZONE_Y,
				Utility.getRandomFLoat(-Constants.DROPZONE_XZ, Constants.DROPZONE_XZ));

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
			ctx.addScore(-10);
		} 
	}

	@Override
	public void hit() {
		destroy();
		ctx.updateables().add(new Explosion(ctx, geo.getWorldTranslation()));
	}
}

package de.pgz.invoiceshoot.vr;

import java.util.Date;

import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

public class Gun implements Updateable {

	private GameContext ctx;
	
	private Node gunNode;

	private Node gunParentNode;
	
	private Node rayParentNode;
	
	private Geometry geoRay;

	private long shootTime = 0;

	private boolean rayVisible = false;
	
	private AudioNode audio;
	
	private Vector3f[] lastHandV = { null, null };

	public Gun( GameContext ctx )  {
		this.ctx = ctx;
		
		Material rayMat = new Material(ctx.getApp().getAssetManager(), Constants.MAT_LIGHTING);
		Texture tex = ctx.getApp().getAssetManager().loadTexture("Textures/laser.png");
		rayMat.setTexture("DiffuseMap", tex);		

		gunNode = (Node) ctx.getApp().getAssetManager().loadModel("Models/gun.blend");
		gunNode.setLocalScale(0.4f);
		gunNode.setLocalTranslation(new Vector3f(0f, -0.07f, -0.09f));
		gunNode.setLocalRotation(new Quaternion());
		gunNode.rotate(0.6f, 0.0f, 0);
		gunNode.setCullHint(CullHint.Dynamic); // make sure we see it

		geoRay = new Geometry("laserRay", new Box(new Vector3f(0.005f, 0.005f, 0.35f), new Vector3f(-0.005f, -0.005f, 100.0f)));
		geoRay.setMaterial(rayMat);
		geoRay.setShadowMode(RenderQueue.ShadowMode.Cast);
		geoRay.setLocalTranslation(0.0f, 0.067f, 0.0f);
		geoRay.setLocalRotation(new Quaternion());
		geoRay.rotate(0.6f, 0.0f, 0);
		
		audio = new AudioNode(ctx.getApp().getAssetManager(), "Sound/gunmono.wav", DataType.Buffer);
		audio.setLooping(false);  // activate continuous playing
		audio.setPositional(true);
		audio.setVolume(0.3f);
		audio.setRefDistance(0.2f);
//		audio.setMaxDistance(1f);
		audio.setDirectional(false);
		audio.setReverbEnabled(true);
			
		ctx.getApp().getRootNode().attachChild(audio);
	    
		gunParentNode = new Node();
		gunParentNode.attachChild(gunNode);
		
		rayParentNode = new Node();
		rayParentNode.attachChild(geoRay);
		
		ctx.getApp().getRootNode().attachChild(gunParentNode);
	    
	    
	}

	public void shoot(int index) {
		Quaternion q = ctx.getVRAppState().getVRinput().getFinalObserverRotation(index);
		Vector3f v = ctx.getVRAppState().getVRinput().getFinalObserverPosition(index);
		
		rayParentNode.setLocalTranslation(v);
		rayParentNode.setLocalRotation(q);
		
		audio.setLocalTranslation(v);
		audio.stop();
		audio.play();

		ctx.getApp().getRootNode().attachChild(rayParentNode);
		shootTime = new Date().getTime();
		rayVisible = true;

		for (Target target : ctx.targets()) {
			if (target.getSpatial().getWorldBound()
					.intersects(new Ray(geoRay.getWorldTranslation(), geoRay.getWorldRotation().mult(new Vector3f(0, 0, 1))))) {
				target.explode();
			}
		}

	}

	@Override
	public void update() {
		handleHandInput(1);
		if (rayVisible && new Date().getTime() - shootTime > 50) {
			rayVisible = false;
			rayParentNode.removeFromParent();
		}

	}

	private void handleHandInput(int index) {

		Quaternion q = ctx.getVRAppState().getVRinput().getFinalObserverRotation(index);
		Vector3f v = ctx.getVRAppState().getVRinput().getFinalObserverPosition(index);
		if (q != null && v != null) {

			gunParentNode.setLocalTranslation(v);	
			gunParentNode.setLocalRotation(q);

			if (ctx.getVRAppState().getVRinput().getAxis(index, Constants.TRIGGERAXIS).x >= 0.1f) {
				if (!buttonPressed[index]) {
					// addKarte(lastHandV[index], v, q);
					// vrAppState.getVRinput().triggerHapticPulse(index, 0.8f);
					shoot(index);
					buttonPressed[index] = true;
				}
				// System.out.println("KARTE ADDED");
			} else {
				buttonPressed[index] = false;
			}
			lastHandV[index] = v.clone();
	
			// print out all of the known information about the controllers here
			/*
			 * for(int i=0;i<VRInput.getRawControllerState(index).rAxis.length;i++) {
			 * VRControllerAxis_t cs = VRInput.getRawControllerState(index).rAxis[i];
			 * System.out.println("Controller#" + Integer.toString(index) + ", Axis#" +
			 * Integer.toString(i) + " X: " + Float.toString(cs.x) + ", Y: " +
			 * Float.toString(cs.y)); } System.out.println("Button press: " +
			 * Long.toString(VRInput.getRawControllerState(index).ulButtonPressed.longValue(
			 * )) + ", touch: " +
			 * Long.toString(VRInput.getRawControllerState(index).ulButtonTouched.longValue(
			 * )));
			 */

		} else {
			gunNode.setCullHint(CullHint.Always); // hide it
		}

	}

	@Override
	public boolean isDead() {
		return false;
	}
	
	boolean[] buttonPressed = { false, false };

}

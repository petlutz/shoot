package de.pgz.invoiceshoot.vr;

import java.util.Date;

import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioData.DataType;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

public class Explosion implements Updateable {

	private static Material matFlame;
	
	private long startTime;
	
	private ParticleEmitter fire;
	
	private boolean dead = false;
	
	private AudioNode audio;
	
	public Explosion(GameContext ctx, Vector3f position) {
		this(ctx, position, 0.5f, 0.5f);
	}
	
	public Explosion(GameContext ctx, Vector3f position, float size, float volume) {
		
		if (matFlame == null) {
			matFlame = new Material(ctx.getApp().getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
			matFlame.setTexture("Texture", ctx.getApp().getAssetManager().loadTexture("Effects/Explosion/flame.png"));
		}
		
		audio = new AudioNode(ctx.getApp().getAssetManager(), "Sound/explosion.wav", DataType.Buffer);
		audio.setLooping(false);  // activate continuous playing
		audio.setPositional(true);
		audio.setVolume(volume);
		audio.setRefDistance(3f);
//		audio.setMaxDistance(1f);
		audio.setDirectional(false);
		audio.setReverbEnabled(true);
		audio.setLocalTranslation(position);
		audio.play();
		ctx.getApp().getRootNode().attachChild(audio);
	    
		fire = new ParticleEmitter("Emitter", Type.Triangle, 30);
		fire.setMaterial(matFlame);
		fire.setParticlesPerSec(0f);
		fire.setImagesX(2);
		fire.setImagesY(2); // 2x2 texture animation
		fire.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f)); // red
		fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
		fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
		fire.setStartSize(size);
		fire.setEndSize(0.0f);
		fire.setGravity(0, 0, 0);
		fire.setLowLife(1f);
		fire.setHighLife(1f);
		fire.getParticleInfluencer().setVelocityVariation(0.3f);
		fire.setLocalTranslation(position);
		fire.emitAllParticles();
		ctx.getApp().getRootNode().attachChild(fire);
		startTime = new Date().getTime();
	}
	
	@Override
	public void update() {
		long lifeTime = new Date().getTime() - startTime;
		if (lifeTime > 1500) {
			fire.removeFromParent();
			audio.stop();
			audio.removeFromParent();
			dead = true;
		}
	}
	
	@Override
	public boolean isDead() {
		return dead;
	}
}

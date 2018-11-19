package de.pgz.invoiceshoot.vr;

import com.jme3.scene.Spatial;

public interface Target extends Updateable {

	public Spatial getSpatial();

	void explode();

	public void destroy();
}

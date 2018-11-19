package de.pgz.invoiceshoot.vr;

import java.util.Random;

public abstract class Utility {


	public static float getRandomFLoat(float low, float high) {
		Random r = new Random();
		return (r.nextFloat() * (high - low)) + low;
	}
	
}

package net.eagtek.metaballs.client.renderer;

import org.joml.Vector3f;

public class LightData {
	
	public static enum LightType {
		DIRECTION, POINT, SPOT
	}
	
	public LightType type;

	public float lightR = 1.0f;
	public float lightG = 1.0f;
	public float lightB = 1.0f;

	public Vector3f position = new Vector3f();
	public Vector3f direction = new Vector3f();
	public Vector3f halfVector = new Vector3f();
	
}

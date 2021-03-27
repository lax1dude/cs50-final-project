package net.eagtek.metaballs.client.renderer;

import java.util.LinkedList;

import org.joml.Matrix4f;

public class ShadowLightRenderer extends LightData {
	
	public final Matrix4f shadowMatrix = new Matrix4f();
	
	public int atlasLocation = -1;
	public LinkedList<ObjectRenderer> objectsInFrustum = null;
	
	public ShadowLightRenderer(LightType type, float emission, float pointsize, double lightX, double lightY, double lightZ) {
		super(type, emission, pointsize, lightX, lightY, lightZ);
	}

}

package net.eagtek.metaballs.client.renderer;

import java.util.LinkedList;

import org.joml.Matrix4f;

import gnu.trove.map.hash.TIntIntHashMap;

public class ShadowLightRenderer extends LightData {
	
	public final Matrix4f shadowMatrix = new Matrix4f();
	
	int atlasLocation = -1;
	LinkedList<ObjectRenderer> objectsInFrustum = null;
	
	TIntIntHashMap objectHashState = new TIntIntHashMap();
	
	public ShadowLightRenderer(LightType type, float emission, float pointsize, double lightX, double lightY, double lightZ, float drawPoint, float lensFlare) {
		super(type, emission, pointsize, lightX, lightY, lightZ, drawPoint, lensFlare);
	}

}

package net.eagtek.metaballs.client.renderer;

import java.util.LinkedList;

import org.joml.Matrix4f;

import net.eagtek.eagl.EaglFramebuffer;
import net.eagtek.eagl.EaglFramebuffer.DepthBufferType;

public class ShadowLightRenderer extends LightData {

	/**
	 * Remember to destroy the shadowMap
	 */
	public final EaglFramebuffer shadowMap;
	
	public final Matrix4f shadowMatrix = new Matrix4f();
	
	public LinkedList<ObjectRenderer> objectsInFrustum = null;
	
	/**
	 * Remember to destroy the shadowMap
	 */
	public ShadowLightRenderer(LightType type, float emission, float pointsize, double lightX, double lightY, double lightZ) {
		super(type, emission, pointsize, lightX, lightY, lightZ);
		this.shadowMap = new EaglFramebuffer(DepthBufferType.DEPTH24_TEXTURE);
	}

}

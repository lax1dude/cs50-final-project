package net.eagtek.metaballs.client.renderer;

import net.eagtek.eagl.EaglFramebuffer;
import net.eagtek.eagl.EaglFramebuffer.DepthBufferType;

public class ShadowLightRenderer extends LightData {

	public final EaglFramebuffer shadowMap;
	
	public ShadowLightRenderer(LightType type, float emission, float pointsize, double lightX, double lightY, double lightZ) {
		super(type, emission, pointsize, lightX, lightY, lightZ);
		this.shadowMap = new EaglFramebuffer(DepthBufferType.DEPTH24_TEXTURE);
	}

}

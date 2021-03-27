package net.eagtek.metaballs.client.renderer;

import org.joml.FrustumIntersection;

public abstract class ObjectRenderer {

	public abstract void renderGBuffer(GlobalRenderer globalRenderer);
	public abstract void renderReflectionMap(GlobalRenderer globalRenderer);
	public abstract void renderShadow(GlobalRenderer globalRenderer);
	public abstract void renderTransparent(GlobalRenderer globalRenderer);
	
	public abstract boolean isInFrustum(FrustumIntersection i);

}

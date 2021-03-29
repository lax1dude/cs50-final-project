package net.eagtek.metaballs.client.renderer;

import org.joml.FrustumIntersection;

public abstract class TerrainRenderer {

	public abstract void renderGBuffer(GlobalRenderer globalRenderer);
	public abstract void renderCubeMap(GlobalRenderer globalRenderer);
	public abstract void renderShadow(GlobalRenderer globalRenderer, int lod);
	public abstract void renderTransparent(GlobalRenderer globalRenderer);
	
	public abstract boolean isInFrustum(GlobalRenderer g);
	public abstract boolean isInFrustumWhenTransformed(GlobalRenderer i, FrustumIntersection s);
	
}

package net.eagtek.metaballs.client.renderer;

public abstract class TerrainRenderer {

	public abstract void renderGBuffer(GlobalRenderer globalRenderer);
	public abstract void renderCubeMap(GlobalRenderer globalRenderer);
	public abstract void renderShadow(GlobalRenderer globalRenderer, int lod);
	public abstract void renderTransparent(GlobalRenderer globalRenderer);
	
}

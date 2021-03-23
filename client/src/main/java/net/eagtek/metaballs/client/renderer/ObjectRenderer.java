package net.eagtek.metaballs.client.renderer;

public abstract class ObjectRenderer {

	public abstract void renderGBuffer(GlobalRenderer globalRenderer);
	public abstract void renderCubeMap(GlobalRenderer globalRenderer);
	public abstract void renderShadow(GlobalRenderer globalRenderer);
	public abstract void renderTransparent(GlobalRenderer globalRenderer);

}

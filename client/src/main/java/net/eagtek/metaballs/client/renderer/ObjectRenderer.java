package net.eagtek.metaballs.client.renderer;

import org.joml.FrustumIntersection;

public abstract class ObjectRenderer {
	
	private static int uidOffset = 0;
	
	public final int uid;
	public int trackingState = 0;
	
	public final RenderScene scene;
	
	public ObjectRenderer(RenderScene scene) {
		uid = uidOffset++;
		this.scene = scene;
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof ObjectRenderer) && ((ObjectRenderer)o).uid == uid;
	}
	
	@Override
	public int hashCode() {
		return uid;
	}
	
	public void markStateDirty() {
		++trackingState;
	}

	public abstract boolean shouldRenderPass(RenderPass pass);
	public abstract void renderPass(RenderPass pass, GlobalRenderer globalRenderer);

	public abstract boolean isInFrustum(GlobalRenderer i);
	public abstract boolean isInFrustumWhenTransformed(GlobalRenderer i, FrustumIntersection s);

}

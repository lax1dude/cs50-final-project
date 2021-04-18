package net.lax1dude.cs50_final_project.client.renderer;

import org.joml.FrustumIntersection;

import net.lax1dude.cs50_final_project.MathUtil;

public abstract class ObjectRenderer {
	
	private static int uidOffset = 1;
	
	public final int uid;
	public int trackingState = 1;
	
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

	public double posX = 0.0d;
	public double posY = 0.0d;
	public double posZ = 0.0d;

	public float scale = 1.0f;

	public float rotationX = 0.0f;
	public float rotationY = 0.0f;
	public float rotationZ = 0.0f;
	
	public ObjectRenderer setPosition(double X, double Y, double Z) {
		if(X != posX || Y != posY || Z != posZ) this.markStateDirty();
		posX = X;
		posY = Y;
		posZ = Z;
		return this;
	}
	
	public ObjectRenderer setRotation(float X, float Y, float Z) {
		if(X != rotationX || Y != rotationY || Z != rotationZ) this.markStateDirty();
		rotationX = X;
		rotationY = Y;
		rotationZ = Z;
		return this;
	}
	
	public void transform(GlobalRenderer globalRenderer) {
		globalRenderer.translateToWorldCoords(posX, posY, posZ);
		if(rotationY != 0.0f) globalRenderer.modelMatrix.rotateY(rotationY * MathUtil.toRadians);
		if(rotationX != 0.0f) globalRenderer.modelMatrix.rotateX(rotationX * MathUtil.toRadians);
		if(rotationZ != 0.0f) globalRenderer.modelMatrix.rotateZ(rotationZ * MathUtil.toRadians);
		if(scale != 1.0f) globalRenderer.modelMatrix.scale(scale);
	}
	
	public ObjectRenderer setScale(float f) {
		if(f != scale) this.markStateDirty();
		scale = f;
		return this;
	}

}

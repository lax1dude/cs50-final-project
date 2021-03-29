package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import org.apache.commons.lang3.ArrayUtils;
import org.joml.FrustumIntersection;

import net.eagtek.eagl.EaglVertexArray;
import net.eagtek.eagl.GLStateManager;
import net.eagtek.metaballs.MathUtil;

public class ModelTerrainRenderer extends TerrainRenderer {
	
	public final EaglVertexArray array;
	public final int texture2D;
	public final int drawmode;
	
	public int[] shadowMapLOD = new int[] { 0, 1, 2, 3 };
	
	public ModelTerrainRenderer(EaglVertexArray array3f4b2f, int texture2D, int drawmode) {
		this.array = array3f4b2f;
		this.texture2D = texture2D;
		this.drawmode = drawmode;
	}
	
	public ModelTerrainRenderer(EaglVertexArray array3f4b2f, int texture2D) {
		this(array3f4b2f, texture2D, GL_TRIANGLES);
	}
	
	public float ditherBlend;
	public float metallic;
	public float roughness;
	public float specular;
	public float ssr;
	public float emission;

	public double posX = 0.0d;
	public double posY = 0.0d;
	public double posZ = 0.0d;

	public float scale = 1.0f;

	public float rotationX = 0.0f;
	public float rotationY = 0.0f;
	public float rotationZ = 0.0f;
	
	public ModelTerrainRenderer setTransparency(float t) {
		ditherBlend = t;
		return this;
	}
	
	public ModelTerrainRenderer setShadowLOD(int[] lod) {
		shadowMapLOD = lod;
		return this;
	}
	
	public ModelTerrainRenderer setPosition(double X, double Y, double Z) {
		posX = X;
		posY = Y;
		posZ = Z;
		return this;
	}
	
	public ModelTerrainRenderer setRotation(float X, float Y, float Z) {
		rotationX = X;
		rotationY = Y;
		rotationZ = Z;
		return this;
	}
	
	public ModelTerrainRenderer setScale(float f) {
		scale = f;
		return this;
	}
	
	public ModelTerrainRenderer setMaterial(float ditherBlend, float metallic, float roughness, float specular, float ssr, float emission) {
		this.ditherBlend = ditherBlend;
		this.metallic = metallic;
		this.roughness = roughness;
		this.specular = specular;
		this.ssr = ssr;
		this.emission = emission;
		return this;
	}

	@Override
	public void renderGBuffer(GlobalRenderer globalRenderer) {
		globalRenderer.modelMatrix.pushMatrix();
		transform(globalRenderer);
		if(isInFrustum(globalRenderer)) {
			ProgramManager m = globalRenderer.progManager;
			m.gbuffer_3f_4b_2f_uniform.use();
			m.gbuffer_3f_4b_2f_uniform_ditherBlend.set1f(ditherBlend);
			m.gbuffer_3f_4b_2f_uniform_specular.set1f(specular);
			m.gbuffer_3f_4b_2f_uniform_metallic.set1f(metallic);
			m.gbuffer_3f_4b_2f_uniform_roughness.set1f(roughness);
			m.gbuffer_3f_4b_2f_uniform_ssr.set1f(ssr);
			m.gbuffer_3f_4b_2f_uniform_emission.set1f(emission);
			GLStateManager.bindTexture2D(texture2D);
			globalRenderer.updateMatrix(m.gbuffer_3f_4b_2f_uniform);
			array.drawAll(drawmode);
		}
		globalRenderer.modelMatrix.popMatrix();
	}

	@Override
	public void renderCubeMap(GlobalRenderer globalRenderer) {
		
	}

	@Override
	public void renderShadow(GlobalRenderer globalRenderer, int lod) {
		if(ArrayUtils.contains(shadowMapLOD, lod)) {
			globalRenderer.modelMatrix.pushMatrix();
			transform(globalRenderer);
			if(isInFrustum(globalRenderer)) {
				ProgramManager m = globalRenderer.progManager;
				m.shadow_3f_4b_2f.use();
				globalRenderer.updateMatrix(m.shadow_3f_4b_2f);
				array.drawAll(drawmode);
			}
			globalRenderer.modelMatrix.popMatrix();
		}
	}
	
	private void transform(GlobalRenderer globalRenderer) {
		globalRenderer.translateToWorldCoords(posX, posY, posZ);
		if(rotationY != 0.0f) globalRenderer.modelMatrix.rotateY(rotationY * MathUtil.toRadians);
		if(rotationX != 0.0f) globalRenderer.modelMatrix.rotateX(rotationX * MathUtil.toRadians);
		if(rotationZ != 0.0f) globalRenderer.modelMatrix.rotateZ(rotationZ * MathUtil.toRadians);
		if(scale != 1.0f) globalRenderer.modelMatrix.scale(scale);
	}

	@Override
	public void renderTransparent(GlobalRenderer globalRenderer) {
		
	}

	@Override
	public boolean isInFrustum(GlobalRenderer g) {
		return g.testBBFrustum(array.minX, array.minY, array.minZ, array.maxX, array.maxY, array.maxZ, g.modelMatrix, g.viewProjFustrum);
	}

	@Override
	public boolean isInFrustumWhenTransformed(GlobalRenderer i, FrustumIntersection s) {
		i.modelMatrix.pushMatrix();
		transform(i);
		boolean b = i.testBBFrustum(array.minX, array.minY, array.minZ, array.maxX, array.maxY, array.maxZ, i.modelMatrix, s);
		i.modelMatrix.popMatrix();
		return b;
	}

}

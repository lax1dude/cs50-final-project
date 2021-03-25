package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES20.GL_TRIANGLES;

import org.apache.commons.lang3.ArrayUtils;

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
		ProgramManager m = globalRenderer.progManager;
		m.gbuffer_3f_4b_2f_uniform.use();
		m.gbuffer_3f_4b_2f_uniform_ditherBlend.set1f(ditherBlend);
		m.gbuffer_3f_4b_2f_uniform_specular.set1f(specular);
		m.gbuffer_3f_4b_2f_uniform_metallic.set1f(metallic);
		m.gbuffer_3f_4b_2f_uniform_roughness.set1f(roughness);
		m.gbuffer_3f_4b_2f_uniform_ssr.set1f(ssr);
		m.gbuffer_3f_4b_2f_uniform_emission.set1f(emission);
		globalRenderer.modelMatrix.pushMatrix();
		globalRenderer.translateToWorldCoords(posX, posY, posZ);
		if(rotationY != 0.0f) globalRenderer.modelMatrix.rotate(rotationY * MathUtil.toRadians, 0.0f, 1.0f, 0.0f);
		if(rotationX != 0.0f) globalRenderer.modelMatrix.rotate(rotationX * MathUtil.toRadians, 1.0f, 0.0f, 0.0f);
		if(rotationZ != 0.0f) globalRenderer.modelMatrix.rotate(rotationZ * MathUtil.toRadians, 0.0f, 0.0f, 1.0f);
		if(scale != 1.0f) globalRenderer.modelMatrix.scale(scale);
		GLStateManager.bindTexture2D(texture2D);
		globalRenderer.updateMatrix(m.gbuffer_3f_4b_2f_uniform);
		array.drawAll(drawmode);
		globalRenderer.modelMatrix.popMatrix();
	}

	@Override
	public void renderCubeMap(GlobalRenderer globalRenderer) {
		
	}

	@Override
	public void renderShadow(GlobalRenderer globalRenderer, int lod) {
		if(ArrayUtils.contains(shadowMapLOD, lod)) {
			ProgramManager m = globalRenderer.progManager;
			m.shadow_3f_4b_2f.use();
			globalRenderer.modelMatrix.pushMatrix();
			globalRenderer.translateToWorldCoords(posX, posY, posZ);
			if(rotationY != 0.0f) globalRenderer.modelMatrix.rotate(rotationY * MathUtil.toRadians, 0.0f, 1.0f, 0.0f);
			if(rotationX != 0.0f) globalRenderer.modelMatrix.rotate(rotationX * MathUtil.toRadians, 1.0f, 0.0f, 0.0f);
			if(rotationZ != 0.0f) globalRenderer.modelMatrix.rotate(rotationZ * MathUtil.toRadians, 0.0f, 0.0f, 1.0f);
			if(scale != 1.0f) globalRenderer.modelMatrix.scale(scale);
			globalRenderer.updateMatrix(m.shadow_3f_4b_2f);
			array.drawAll(drawmode);
			globalRenderer.modelMatrix.popMatrix();
		}
	}

	@Override
	public void renderTransparent(GlobalRenderer globalRenderer) {
		
	}

}

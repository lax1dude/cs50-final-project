package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import net.eagtek.eagl.EaglVertexArray;
import net.eagtek.eagl.GLStateManager;
import net.eagtek.metaballs.MathUtil;

public class ModelObjectRenderer extends ObjectRenderer {
	
	public final EaglVertexArray array;
	public final int texture2D;
	public final int drawmode;
	
	public ModelObjectRenderer(EaglVertexArray array3f4b2f, int texture2D, int drawmode) {
		this.array = array3f4b2f;
		this.texture2D = texture2D;
		this.drawmode = drawmode;
	}
	
	public ModelObjectRenderer(EaglVertexArray array3f4b2f, int texture2D) {
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
	
	public ModelObjectRenderer setPosition(double X, double Y, double Z) {
		posX = X;
		posY = Y;
		posZ = Z;
		return this;
	}
	
	public ModelObjectRenderer setRotation(float X, float Y, float Z) {
		rotationX = X;
		rotationY = Y;
		rotationZ = Z;
		return this;
	}
	
	public ModelObjectRenderer setScale(float f) {
		scale = f;
		return this;
	}
	
	public ModelObjectRenderer setMaterial(float ditherBlend, float metallic, float roughness, float specular, float ssr, float emission) {
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
	public void renderReflectionMap(GlobalRenderer globalRenderer) {
		
	}

	@Override
	public void renderShadow(GlobalRenderer globalRenderer) {
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

	@Override
	public void renderTransparent(GlobalRenderer globalRenderer) {
		
	}

}

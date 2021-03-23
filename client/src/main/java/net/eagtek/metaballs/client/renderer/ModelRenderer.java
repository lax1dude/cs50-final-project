package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import net.eagtek.eagl.EaglVertexArray;
import net.eagtek.eagl.GLStateManager;

public class ModelRenderer extends ObjectRenderer {
	
	public final EaglVertexArray array;
	public final int texture2D;
	public final int drawmode;
	
	public ModelRenderer(EaglVertexArray array3f4b2f, int texture2D, int drawmode) {
		this.array = array3f4b2f;
		this.texture2D = texture2D;
		this.drawmode = drawmode;
	}
	
	public ModelRenderer(EaglVertexArray array3f4b2f, int texture2D) {
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
	
	public ModelRenderer setPosition(double X, double Y, double Z) {
		posX = X;
		posY = Y;
		posZ = Z;
		return this;
	}
	
	public ModelRenderer setMaterial(float ditherBlend, float metallic, float roughness, float specular, float ssr, float emission) {
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
		GLStateManager.bindTexture2D(texture2D);
		globalRenderer.updateMatrix(m.gbuffer_3f_4b_2f_uniform);
		array.drawAll(drawmode);
		globalRenderer.modelMatrix.popMatrix();
	}

	@Override
	public void renderCubeMap(GlobalRenderer globalRenderer) {
		
	}

	@Override
	public void renderShadow(GlobalRenderer globalRenderer) {
		
	}

	@Override
	public void renderTransparent(GlobalRenderer globalRenderer) {
		
	}

}

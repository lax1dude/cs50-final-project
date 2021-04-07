package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES31.*;

import org.apache.commons.lang3.ArrayUtils;
import org.joml.FrustumIntersection;

import net.eagtek.eagl.EaglVertexArray;
import net.eagtek.eagl.GLStateManager;
import net.eagtek.metaballs.MathUtil;

public class ModelObjectRenderer extends ObjectRenderer {
	
	public final EaglVertexArray array;
	public final int texture2D;
	public final int drawmode;
	
	public final RenderPass[] passes;

	public static final RenderPass[] passes_all_opaque = new RenderPass[] {
			RenderPass.G_BUFFER,
			RenderPass.CUBEMAP,
			RenderPass.LIGHT_SHADOW,
			RenderPass.REFLECTION,
			RenderPass.SHADOW_A,
			RenderPass.SHADOW_B,
			RenderPass.SHADOW_C,
			RenderPass.SHADOW_D
	};

	public static final RenderPass[] passes_small_object_opaque = new RenderPass[] {
			RenderPass.G_BUFFER,
			RenderPass.LIGHT_SHADOW,
			RenderPass.SHADOW_A,
			RenderPass.SHADOW_B
	};
	
	public ModelObjectRenderer(EaglVertexArray array3f4b2f, int texture2D, int drawmode, RenderPass[] passes, RenderScene renderScene) {
		super(renderScene);
		this.array = array3f4b2f;
		this.texture2D = texture2D;
		this.drawmode = drawmode;
		this.passes = passes;
	}
	
	public ModelObjectRenderer(EaglVertexArray array3f4b2f, int texture2D, RenderPass[] passes, RenderScene renderScene) {
		this(array3f4b2f, texture2D, GL_TRIANGLES, passes, renderScene);
	}
	
	public float ditherBlend;
	public float metallic;
	public float roughness;
	public float specular;
	public float ssr;
	public float emission;
	
	public boolean diffuseOverride = false;
	public float r = 0.0f;
	public float g = 0.0f;
	public float b = 0.0f;

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
	
	public ModelObjectRenderer setMaterialAndDiffuse(float diffuseR, float diffuseG, float diffuseB, float ditherBlend, float metallic, float roughness, float specular, float ssr, float emission) {
		return setDiffuse(diffuseR, diffuseG, diffuseB).setMaterial(ditherBlend, metallic, roughness, specular, ssr, emission);
	}

	public ModelObjectRenderer setDiffuse(float diffuseR, float diffuseG, float diffuseB) {
		this.diffuseOverride = true;
		this.r = diffuseR;
		this.g = diffuseG;
		this.b = diffuseB;
		return this;
	}

	@Override
	public boolean shouldRenderPass(RenderPass pass) {
		return ArrayUtils.contains(passes, pass);
	}

	@Override
	public void renderPass(RenderPass pass, GlobalRenderer globalRenderer) {
		switch(pass) {
		case G_BUFFER:
			renderGBuffer(globalRenderer);
			break;
		case SHADOW_A:
		case SHADOW_B:
		case SHADOW_C:
		case SHADOW_D:
		case LIGHT_SHADOW:
			renderShadow(globalRenderer);
			break;
		case REFLECTION:
			renderReflectionMap(globalRenderer);
		case CUBEMAP:
			renderCubeMap(globalRenderer);
		case TRANSPARENT:
			renderTransparent(globalRenderer);
		default:
			break;
		}
	}

	public void renderGBuffer(GlobalRenderer globalRenderer) {
		globalRenderer.modelMatrix.pushMatrix();
		transform(globalRenderer);
		if(isInFrustum(globalRenderer)) {
			ProgramManager m = globalRenderer.progManager;
			if(diffuseOverride) {
				m.gbuffer_3f_4b_uniform.use();
				m.gbuffer_3f_4b_uniform_ditherBlend.set1f(ditherBlend);
				m.gbuffer_3f_4b_uniform_specular.set1f(specular);
				m.gbuffer_3f_4b_uniform_metallic.set1f(metallic);
				m.gbuffer_3f_4b_uniform_roughness.set1f(roughness);
				m.gbuffer_3f_4b_uniform_ssr.set1f(ssr);
				m.gbuffer_3f_4b_uniform_emission.set1f(emission);
				m.gbuffer_3f_4b_uniform_diffuseColor.set3f(r, g, b);
				globalRenderer.updateMatrix(m.gbuffer_3f_4b_uniform);
			}else {
				m.gbuffer_3f_4b_2f_uniform.use();
				m.gbuffer_3f_4b_2f_uniform_ditherBlend.set1f(ditherBlend);
				m.gbuffer_3f_4b_2f_uniform_specular.set1f(specular);
				m.gbuffer_3f_4b_2f_uniform_metallic.set1f(metallic);
				m.gbuffer_3f_4b_2f_uniform_roughness.set1f(roughness);
				m.gbuffer_3f_4b_2f_uniform_ssr.set1f(ssr);
				m.gbuffer_3f_4b_2f_uniform_emission.set1f(emission);
				globalRenderer.updateMatrix(m.gbuffer_3f_4b_2f_uniform);
				GLStateManager.bindTexture2D(texture2D);
			}
			array.drawAll(drawmode);
		}
		globalRenderer.modelMatrix.popMatrix();
	}

	public void renderReflectionMap(GlobalRenderer globalRenderer) {
		
	}

	public void renderCubeMap(GlobalRenderer globalRenderer) {
		globalRenderer.modelMatrix.pushMatrix();
		transform(globalRenderer);
		if(isInFrustum(globalRenderer)) {
			ProgramManager m = globalRenderer.progManager;
			if(diffuseOverride) {
				m.cubemap_3f_4b_uniform.use();
				m.cubemap_3f_4b_uniform_specular.set1f(specular);
				m.cubemap_3f_4b_uniform_metallic.set1f(metallic);
				m.cubemap_3f_4b_uniform_roughness.set1f(roughness);
				m.cubemap_3f_4b_uniform_emission.set1f(emission);
				m.cubemap_3f_4b_uniform_shadowMatrix.setMatrix4f(globalRenderer.sunShadowProjViewA);
				m.cubemap_3f_4b_uniform_sunDirection.set3f(scene.sunDirection.x, scene.sunDirection.y, scene.sunDirection.z);
				m.cubemap_3f_4b_uniform_sunRGB.set3f(
						globalRenderer.colorTemperatures.getLinearR(scene.sunKelvin) * scene.sunBrightness * 0.1f,
						globalRenderer.colorTemperatures.getLinearG(scene.sunKelvin) * scene.sunBrightness * 0.1f,
						globalRenderer.colorTemperatures.getLinearB(scene.sunKelvin) * scene.sunBrightness * 0.1f
				);
				m.cubemap_3f_4b_uniform_diffuseColor.set3f(r, g, b);
				globalRenderer.updateMatrix(m.cubemap_3f_4b_uniform);
			}else {
				m.cubemap_3f_4b_2f_uniform.use();
				m.cubemap_3f_4b_2f_uniform_specular.set1f(specular);
				m.cubemap_3f_4b_2f_uniform_metallic.set1f(metallic);
				m.cubemap_3f_4b_2f_uniform_roughness.set1f(roughness);
				m.cubemap_3f_4b_2f_uniform_emission.set1f(emission);
				m.cubemap_3f_4b_2f_uniform_shadowMatrix.setMatrix4f(globalRenderer.sunShadowProjViewA);
				m.cubemap_3f_4b_2f_uniform_sunDirection.set3f(scene.sunDirection.x, scene.sunDirection.y, scene.sunDirection.z);
				m.cubemap_3f_4b_2f_uniform_sunRGB.set3f(
						globalRenderer.colorTemperatures.getLinearR(scene.sunKelvin) * scene.sunBrightness * 0.1f,
						globalRenderer.colorTemperatures.getLinearG(scene.sunKelvin) * scene.sunBrightness * 0.1f,
						globalRenderer.colorTemperatures.getLinearB(scene.sunKelvin) * scene.sunBrightness * 0.1f
				);
				globalRenderer.updateMatrix(m.cubemap_3f_4b_2f_uniform);
				GLStateManager.bindTexture2D(texture2D);
			}
			globalRenderer.sunShadowMap.bindDepthTexture(1);
			array.drawAll(drawmode);
		}
		globalRenderer.modelMatrix.popMatrix();
	}

	public void renderShadow(GlobalRenderer globalRenderer) {
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
	
	private void transform(GlobalRenderer globalRenderer) {
		globalRenderer.translateToWorldCoords(posX, posY, posZ);
		if(rotationY != 0.0f) globalRenderer.modelMatrix.rotateY(rotationY * MathUtil.toRadians);
		if(rotationX != 0.0f) globalRenderer.modelMatrix.rotateX(rotationX * MathUtil.toRadians);
		if(rotationZ != 0.0f) globalRenderer.modelMatrix.rotateZ(rotationZ * MathUtil.toRadians);
		if(scale != 1.0f) globalRenderer.modelMatrix.scale(scale);
	}
	
	public void renderTransparent(GlobalRenderer globalRenderer) {
		
	}

	@Override
	public boolean isInFrustum(GlobalRenderer i) {
		return i.testBBFrustum(array.minX, array.minY, array.minZ, array.maxX, array.maxY, array.maxZ, i.modelMatrix, i.viewProjFustrum);
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

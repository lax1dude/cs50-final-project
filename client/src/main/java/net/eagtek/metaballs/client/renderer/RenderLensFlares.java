package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES31.*;

import java.nio.FloatBuffer;
import java.util.Random;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import net.eagtek.eagl.EaglFramebuffer;
import net.eagtek.eagl.EaglFramebuffer.DepthBufferType;
import net.eagtek.eagl.EaglImage2D;
import net.eagtek.eagl.EaglIndexBuffer;
import net.eagtek.eagl.EaglTessellator;
import net.eagtek.eagl.EaglVertexArray;
import net.eagtek.eagl.EaglVertexBuffer;
import net.eagtek.eagl.GLDataType;
import net.eagtek.eagl.ResourceLoader;
import net.eagtek.metaballs.MathUtil;

public class RenderLensFlares {
	
	public final GlobalRenderer renderer;
	
	private final EaglTessellator tessellator;
	private final EaglVertexBuffer vertexBuffer;
	private final EaglIndexBuffer indexBuffer;
	private final EaglVertexArray vertexArray;
	private final Matrix4f transformationMatrix = new Matrix4f();
	private final Vector4f transformationVector = new Vector4f();
	private final EaglImage2D lensFlareTextures;
	private final EaglFramebuffer sunOcclusionTest;
	
	private final EaglVertexArray conditionalFlare;
	
	private static final float textureW = 512;
	private static final float textureH = 256;

	public float xPixelsInv = 1.0f;
	public float yPixelsInv = 1.0f; 
	
	public RenderLensFlares(GlobalRenderer g) {
		this.renderer = g;
		this.tessellator = new EaglTessellator(20, 1000, 4000);
		
		this.vertexBuffer = new EaglVertexBuffer();
		this.indexBuffer = new EaglIndexBuffer(GLDataType.SHORT_U);
		this.vertexArray = new EaglVertexArray(
			new EaglVertexBuffer[] { vertexBuffer }, new EaglVertexArray.VertexAttribPointer[] {
				EaglVertexArray.attrib(0, 0, 2, GLDataType.FLOAT, false, 20, 0),
				EaglVertexArray.attrib(0, 1, 2, GLDataType.FLOAT, false, 20, 8),
				EaglVertexArray.attrib(0, 2, 4, GLDataType.BYTE_U, true, 20, 16)
			}, indexBuffer
		);
		this.lensFlareTextures = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/textures/lensflares.png")).generateMipmap().filter(GL_LINEAR_MIPMAP_NEAREST, GL_LINEAR);
		this.sunOcclusionTest = new EaglFramebuffer(DepthBufferType.NONE, GL_R8);
		
		EaglVertexBuffer b = new EaglVertexBuffer();
		this.conditionalFlare = new EaglVertexArray(
			new EaglVertexBuffer[] { b }, new EaglVertexArray.VertexAttribPointer[] {
				EaglVertexArray.attrib(0, 0, 2, GLDataType.FLOAT, false, 8, 0)
			}
		);
		
		try(MemoryStack s = MemoryStack.stackPush()) {
			FloatBuffer f = s.mallocFloat(2 * 6);
			f.put(new float[] {
					-1.0f, -1.0f,
					 1.0f, -1.0f,
					 1.0f,  1.0f,
					-1.0f,  1.0f,
					-1.0f, -1.0f,
					 1.0f,  1.0f
			});
			f.flip();
			glBindBuffer(GL_ARRAY_BUFFER, b.glObject);
			glBufferData(GL_ARRAY_BUFFER, f, GL_STATIC_DRAW);
		}
	}
	
	/**
	 * Must have gbuffer depth texture bound to 0 and cloud texture a bound to 1 and cloud texture b bound to 2
	 * @param scene
	 */
	public void processOcclusion(RenderScene scene) {
		sunOcclusionTest.setSize(1, 1);
		sunOcclusionTest.bindFramebuffer();
		glViewport(0, 0, 1, 1);
		renderer.progManager.lens_flare_occlusion.use();
		renderer.progManager.lens_flare_occlusion_sunDirection.set3f(scene.sunDirection.x, scene.sunDirection.y, scene.sunDirection.z);
		renderer.progManager.lens_flare_occlusion_matrix_vp.setMatrix4f(renderer.viewProjMatrix);
		renderer.progManager.lens_flare_occlusion_cloudTextureBlend.set1f(renderer.cloudMapGenerator.blendAmount());
		renderer.quadArray.draw(GL_TRIANGLES, 0, 6);
	}

	private float sunClipX = 0.0f;
	private float sunClipY = 0.0f;
	
	private float sunR = 0.0f;
	private float sunG = 0.0f;
	private float sunB = 0.0f;
	
	private void placeFlare(int style, float dist, float size, float r, float g, float b, float a, float rotation) {
		float exposureModifier = 1.0f - (0.3f / renderer.exposure);
		if(exposureModifier < 0.2f) exposureModifier = 0.2f;
		if(exposureModifier > 1.0f) exposureModifier = 1.0f;
		pushQuad(sunClipX - sunClipX * dist, sunClipY - sunClipY * dist, size * exposureModifier, size * exposureModifier, 128 * style, 128, 128, 128, 0.0f, g * sunG, b * sunB, a, rotation + exposureModifier * 120.0f);
		pushQuad(sunClipX - sunClipX * (dist + 0.015f), sunClipY - sunClipY * (dist + 0.015f), size * exposureModifier, size * exposureModifier, 128 * style, 128, 128, 128, r * sunR, 0.0f, 0.0f, a, rotation + exposureModifier * 120.0f);
	}
	
	private void placeFlareNoAbberation(int style, float dist, float size, float r, float g, float b, float a, float rotation) {
		float exposureModifier = 1.0f - (0.3f / renderer.exposure);
		if(exposureModifier < 0.2f) exposureModifier = 0.2f;
		if(exposureModifier > 1.0f) exposureModifier = 1.0f;
		pushQuad(sunClipX - sunClipX * dist, sunClipY - sunClipY * dist, size * exposureModifier, size * exposureModifier, 128 * style, 128, 128, 128, r * sunR, g * sunG, b * sunB, a, rotation + exposureModifier * 120.0f);
	}
	
	private final Random rand = new Random();
	
	public void render(RenderScene scene) {
		
		float intensityF = (float)Math.sqrt(Math.max(scene.sunDirection.y + 0.1f, 0.0f));
		if(intensityF > 0.0f) {
			transformationVector.x = scene.sunDirection.x * 100.0f;
			transformationVector.y = scene.sunDirection.y * 100.0f;
			transformationVector.z = scene.sunDirection.z * 100.0f;
			transformationVector.w = 1.0f;
			
			renderer.viewProjMatrix.transform(transformationVector);
	
			transformationVector.x /= transformationVector.w;
			transformationVector.y /= transformationVector.w;
			transformationVector.z /= transformationVector.w;
			
			if(transformationVector.z >= -1.0f && transformationVector.z < 1.0f) {
				rand.setSeed("dick".hashCode());
				
				float x = sunClipX = (transformationVector.x) / xPixelsInv;
				float y = sunClipY = (transformationVector.y) / yPixelsInv;
				
				tessellator.reset();
				
				int kelvin = scene.sunKelvin;
				float r = sunR = renderer.colorTemperatures.getLinearR(kelvin);
				float g = sunG = renderer.colorTemperatures.getLinearG(kelvin);
				float b = sunB = renderer.colorTemperatures.getLinearB(kelvin);
				
				pushAlignedQuad(x, y, 0.5f, 0.5f, 384, 128, 127, 127, r, g, b, 1.0f, 0.0f);
				
				float streakIntensity = 0.2f;
				
				for(int i = 0; i < 8; ++i) {
					boolean tex2 = rand.nextBoolean();
					pushQuad(x, y, 0.1f, 0.25f * (float)(Math.sin((i * (360.0f / 8.0f) - 90.0f) * MathUtil.toRadians) + 2.0d), 0, (tex2 ? 1 : 49), 512, 48, r, g, b, streakIntensity, i * (360.0f / 16.0f));
				}
				
				kelvin = 20000;
				r = renderer.colorTemperatures.getLinearR(kelvin);
				g = renderer.colorTemperatures.getLinearG(kelvin);
				b = renderer.colorTemperatures.getLinearB(kelvin);
				
				r = 0.0f;
				g = 0.3f;
				b = 1.0f;

				for(int i = 0; i < 8; ++i) {
					boolean tex2 = rand.nextBoolean();
					pushQuad(x, y, 0.4f, 0.5f * (float)(Math.sin((i * (360.0f / 8.0f) - 90.0f) * MathUtil.toRadians) + 2.0d), 0, (tex2 ? 1 : 49), 512, 48, r, g, b, streakIntensity * 0.1f, i * (360.0f / 16.0f));
				}
				/*
				placeFlare(1, 0.5f, 0.15f,  0.7f, 0.9f, 0.2f,  0.2f, 0.0f);
				placeFlare(1, 0.52f, 0.15f,  0.7f, 0.9f, 0.2f,  0.2f, 0.0f);
				placeFlare(1, 0.7f, 0.15f,  0.7f, 0.9f, 0.2f,  0.2f, 0.0f);
				placeFlare(0, 0.8f, 0.25f,  0.3f, 0.9f, 0.2f,  0.2f, 0.0f);
				placeFlare(0, 0.9f, 0.2f,  0.2f, 0.3f, 0.9f,  1.0f, 0.0f);
				placeFlare(2, 1.1f, 1.0f,  0.3f, 0.9f, 0.2f,  0.1f, 0.0f);
				placeFlare(2, 1.3f, 0.3f,  0.3f, 0.9f, 0.2f,  1.0f, 0.0f);
				placeFlare(2, 1.5f, 0.15f,  0.3f, 0.9f, 0.2f,  1.0f, 0.0f);
				placeFlareNoAbberation(2, 1.6f, 0.15f,  0.1f, 0.1f, 0.9f,  1.0f, 0.0f);
				placeFlareNoAbberation(1, 1.7f, 0.6f,  0.1f, 0.1f, 0.9f,  1.0f, 0.0f);
				for(int i = 0; i < 15; ++i) {
					placeFlareNoAbberation(1, 1.0f + i * 0.0035f, 0.2f / (16 - i) + 0.01f, 0.7f, 0.7f, 0.2f, 0.5f, 0.0f);
				}
				for(int i = 0; i < 10; ++i) {
					placeFlareNoAbberation(1, 1.13f + i * 0.0035f, 0.2f / (11 - i) + 0.01f, 0.4f, 0.7f, 0.2f, 0.5f, 0.0f);
				}
				*/

				placeFlare(1, 0.2f, 0.1f,  0.5f, 0.9f, 0.2f,  0.2f, 0.0f);

				placeFlare(1, 0.3f, 0.1f,  0.5f, 0.9f, 0.2f,  0.2f, 0.0f);
				placeFlare(1, 0.35f, 0.15f,  0.5f, 0.9f, 0.7f,  0.2f, 0.0f);
				placeFlareNoAbberation(2, 0.4f, 0.4f,  0.3f, 0.9f, 0.7f,  0.05f, 0.0f);
				placeFlareNoAbberation(0, 0.5f, 0.2f,  0.3f, 0.9f, 0.7f,  0.03f, 0.0f);

				placeFlare(1, 1.0f, 0.15f,  0.5f, 0.9f, 0.7f,  0.1f, 0.0f);
				placeFlare(1, 1.04f, 0.15f,  0.5f, 0.5f, 0.7f,  0.1f, 0.0f);
				placeFlare(1, 1.07f, 0.1f,  0.7f, 0.7f, 0.7f,  0.2f, 0.0f);
				placeFlareNoAbberation(2, 1.11f, 0.1f,  0.2f, 0.2f, 0.7f,  0.05f, 0.0f);
				placeFlareNoAbberation(2, 1.11f, 0.3f,  0.2f, 0.7f, 0.2f,  0.05f, 0.0f);

				placeFlare(0, 1.25f, 0.2f,  0.4f, 0.7f, 0.2f,  0.02f, 0.0f);
				placeFlare(2, 1.22f, 0.1f,  0.3f, 0.7f, 0.7f,  0.05f, 0.0f);
				placeFlare(0, 1.27f, 0.1f,  0.5f, 0.7f, 0.5f,  0.15f, 0.0f);
				placeFlare(0, 1.30f, 0.08f,  0.7f, 0.7f, 0.7f,  0.15f, 0.0f);
				
				placeFlare(2, 1.45f, 0.3f,  0.3f, 0.7f, 0.2f,  0.02f, 0.0f);
				placeFlare(2, 1.55f, 0.1f,  0.3f, 0.7f, 0.7f,  0.05f, 0.0f);
				placeFlare(0, 1.59f, 0.1f,  0.5f, 0.7f, 0.5f,  0.15f, 0.0f);

				placeFlare(2, 2.0f, 0.3f,  0.3f, 0.7f, 0.2f,  0.05f, 0.0f);
				placeFlare(1, 1.98f, 0.2f,  0.3f, 0.7f, 0.2f,  0.05f, 0.0f);
				placeFlare(1, 2.02f, 0.2f,  0.3f, 0.7f, 0.2f,  0.05f, 0.0f);
				
				
				tessellator.uploadVertexes(vertexBuffer, false);
				tessellator.uploadIndexes(indexBuffer, false);
				
				renderer.progManager.lens_flare.use();
				renderer.progManager.lens_flare_intensity.set1f(0.1f * scene.sunBrightness * intensityF);
				lensFlareTextures.bind(0);
				sunOcclusionTest.bindColorTexture(0, 1);
				vertexArray.drawAll(GL_TRIANGLES);
			}
		}
	}
	
	private void pushAlignedQuad(float x, float y, float w, float h, int tx, int ty, int tw, int th, float r, float g, float b, float a, float rotation) {
		pushQuad(x, y, w, h, tx, ty, tw, th, r, g, b, a, rotation + (renderer.client.prevRenderYaw + (renderer.client.renderYaw - renderer.client.prevRenderYaw) * renderer.client.partialTicks) * 0.5f);
	}
	
	private void pushQuad(float x, float y, float w, float h, int tx, int ty, int tw, int th, float r, float g, float b, float a, float rotation) {
		
		transformationMatrix.identity().translate(x, y, 0.0f).rotate(rotation * MathUtil.toRadians, 0.0f, 0.0f, 1.0f);
		
		transformationVector.x = -w;
		transformationVector.y = -h;
		transformationVector.z = 0.0f;
		transformationVector.w = 1.0f;
		transformationMatrix.transform(transformationVector).mul(xPixelsInv, yPixelsInv, 1.0f, 1.0f);
		
		int va = tessellator.put_vec2f(transformationVector.x, transformationVector.y).put_vec2f(tx / textureW, 1.0f - (ty / textureH)).put_vec4b((byte)((int)(r * a * 255.0f)), (byte)((int)(g * a * 255.0f)), (byte)((int)(b * a * 255.0f)), (byte)0).endVertex();
		
		transformationVector.x = w;
		transformationVector.y = -h;
		transformationVector.z = 0.0f;
		transformationVector.w = 1.0f;
		transformationMatrix.transform(transformationVector).mul(xPixelsInv, yPixelsInv, 1.0f, 1.0f);
		
		int vb = tessellator.put_vec2f(transformationVector.x, transformationVector.y).put_vec2f((tx + tw) / textureW, 1.0f - (ty / textureH)).put_vec4b((byte)((int)(r * a * 255.0f)), (byte)((int)(g * a * 255.0f)), (byte)((int)(b * a * 255.0f)), (byte)0).endVertex();
		
		transformationVector.x = w;
		transformationVector.y = h;
		transformationVector.z = 0.0f;
		transformationVector.w = 1.0f;
		transformationMatrix.transform(transformationVector).mul(xPixelsInv, yPixelsInv, 1.0f, 1.0f);
		
		int vc = tessellator.put_vec2f(transformationVector.x, transformationVector.y).put_vec2f((tx + tw) / textureW, 1.0f - ((ty + th) / textureH)).put_vec4b((byte)((int)(r * a * 255.0f)), (byte)((int)(g * a * 255.0f)), (byte)((int)(b * a * 255.0f)), (byte)0).endVertex();

		transformationVector.x = -w;
		transformationVector.y = h;
		transformationVector.z = 0.0f;
		transformationVector.w = 1.0f;
		transformationMatrix.transform(transformationVector).mul(xPixelsInv, yPixelsInv, 1.0f, 1.0f);
		
		int vd = tessellator.put_vec2f(transformationVector.x, transformationVector.y).put_vec2f(tx / textureW, 1.0f - ((ty + th) / textureH)).put_vec4b((byte)((int)(r * a * 255.0f)), (byte)((int)(g * a * 255.0f)), (byte)((int)(b * a * 255.0f)), (byte)0).endVertex();
		
		tessellator.addToIndex(va).addToIndex(vb).addToIndex(vc);
		tessellator.addToIndex(va).addToIndex(vc).addToIndex(vd);
		
	}
	
	public void destroy() {
		this.tessellator.destroy();
		this.vertexArray.destroyWithBuffers();
		this.lensFlareTextures.destroy();
		this.sunOcclusionTest.destroy();
		this.conditionalFlare.destroy();
	}
	
	private static final Vector4f vec = new Vector4f();
	
	public void renderLightFlare(LightData r) {
		vec.x = (float)(r.lightX - renderer.renderPosX);
		vec.y = (float)(r.lightY - renderer.renderPosY);
		vec.z = (float)(r.lightZ - renderer.renderPosZ);
		vec.w = 0.0f;
		float dist = vec.length();
		if(dist < 100.0f) {
			vec.w = 1.0f;
			renderer.viewProjMatrix.transform(vec);
			vec.x /= vec.w;
			vec.y /= vec.w;
			vec.z /= vec.w;
			renderer.progManager.lens_flare_single.use();
			renderer.progManager.lens_flare_single_color.set3f(r.lightR * 0.2f * r.lensFlare, r.lightG * 0.5f * r.lensFlare, r.lightB * r.lensFlare);
			renderer.progManager.lens_flare_single_position.set3f(vec.x, vec.y, vec.z);
			renderer.progManager.lens_flare_single_size.set2f(r.drawPoint / (float)Math.min(Math.sqrt(dist * 0.1d), 15.0d) / renderer.displayW * 5.0f, 1.5f);
			renderer.progManager.lens_flare_single_flareTextureSelection.set1f(r.hashCode() % 2);
			lensFlareTextures.bind(0);
			renderer.gBuffer.bindDepthTexture(1);
			conditionalFlare.draw(GL_TRIANGLES, 0, 6);
		}
	}

}

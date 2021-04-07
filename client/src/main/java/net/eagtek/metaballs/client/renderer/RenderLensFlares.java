package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES31.*;

import org.joml.Matrix4f;
import org.joml.Vector4f;

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
	}
	
	public void render(RenderScene scene) {
		transformationVector.x = scene.sunDirection.x * 100.0f;
		transformationVector.y = scene.sunDirection.y * 100.0f;
		transformationVector.z = scene.sunDirection.z * 100.0f;
		transformationVector.w = 1.0f;
		
		renderer.viewProjMatrix.transform(transformationVector);

		transformationVector.x /= transformationVector.w;
		transformationVector.y /= transformationVector.w;
		transformationVector.z /= transformationVector.w;
		
		if(transformationVector.x >= -1.0f && transformationVector.x < 1.0f && transformationVector.y >= -1.0f && transformationVector.y < 1.0f && transformationVector.z >= -1.0f && transformationVector.z < 1.0f) {

			float x = (transformationVector.x) / xPixelsInv;
			float y = (transformationVector.y) / yPixelsInv;
			
			tessellator.reset();
			
			pushQuad(x, y, 1.0f, 0.05f, 0, 1, 512, 48, (byte)255, (byte)255, (byte)255, 45.0f);
			pushQuad(x, y, 1.0f, 0.05f, 0, 1, 512, 48, (byte)255, (byte)255, (byte)255, 135.0f);
			
			tessellator.uploadVertexes(vertexBuffer, false);
			tessellator.uploadIndexes(indexBuffer, false);
			
			renderer.progManager.lens_flare.use();
			renderer.progManager.lens_flare_intensity.set1f(5.0f);
			renderer.progManager.lens_flare_sunTexCoord.set2f(transformationVector.x, transformationVector.y);
			lensFlareTextures.bind(0);
			renderer.gBuffer.bindDepthTexture(1);
			vertexArray.drawAll(GL_TRIANGLES);
		}
	}
	
	private void pushQuad(float x, float y, float w, float h, int tx, int ty, int tw, int th, byte r, byte g, byte b, float rotation) {
		
		transformationMatrix.identity().translate(x, y, 0.0f).rotate(rotation * MathUtil.toRadians, 0.0f, 0.0f, 1.0f);
		
		transformationVector.x = -w;
		transformationVector.y = -h;
		transformationVector.z = 0.0f;
		transformationVector.w = 1.0f;
		transformationMatrix.transform(transformationVector).mul(xPixelsInv, yPixelsInv, 1.0f, 1.0f);
		
		int va = tessellator.put_vec2f(transformationVector.x, transformationVector.y).put_vec2f(tx / textureW, 1.0f - (ty / textureH)).put_vec4b(r, g, b, (byte)0).endVertex();
		
		transformationVector.x = w;
		transformationVector.y = -h;
		transformationVector.z = 0.0f;
		transformationVector.w = 1.0f;
		transformationMatrix.transform(transformationVector).mul(xPixelsInv, yPixelsInv, 1.0f, 1.0f);
		
		int vb = tessellator.put_vec2f(transformationVector.x, transformationVector.y).put_vec2f((tx + tw) / textureW, 1.0f - (ty / textureH)).put_vec4b(r, g, b, (byte)0).endVertex();
		
		transformationVector.x = w;
		transformationVector.y = h;
		transformationVector.z = 0.0f;
		transformationVector.w = 1.0f;
		transformationMatrix.transform(transformationVector).mul(xPixelsInv, yPixelsInv, 1.0f, 1.0f);
		
		int vc = tessellator.put_vec2f(transformationVector.x, transformationVector.y).put_vec2f((tx + tw) / textureW, 1.0f - ((ty + th) / textureH)).put_vec4b(r, g, b, (byte)0).endVertex();

		transformationVector.x = -w;
		transformationVector.y = h;
		transformationVector.z = 0.0f;
		transformationVector.w = 1.0f;
		transformationMatrix.transform(transformationVector).mul(xPixelsInv, yPixelsInv, 1.0f, 1.0f);
		
		int vd = tessellator.put_vec2f(transformationVector.x, transformationVector.y).put_vec2f(tx / textureW, 1.0f - ((ty + th) / textureH)).put_vec4b(r, g, b, (byte)0).endVertex();
		
		tessellator.addToIndex(va).addToIndex(vb).addToIndex(vc);
		tessellator.addToIndex(va).addToIndex(vc).addToIndex(vd);
		
	}
	
	public void destroy() {
		this.tessellator.destroy();
		this.vertexArray.destroyWithBuffers();
		this.lensFlareTextures.destroy();
	}

}

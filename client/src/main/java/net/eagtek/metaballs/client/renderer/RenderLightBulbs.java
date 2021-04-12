package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES31.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import net.eagtek.eagl.EaglImage2D;
import net.eagtek.eagl.GLStateManager;
import net.eagtek.eagl.ResourceLoader;

class RenderLightBulbs {
	
	public final GlobalRenderer renderer;
	
	private final int glVertexArray;
	private final int glVertexBuffer;
	private final int glInstancingBuffer;
	
	private final EaglImage2D lightBulbTexture;
	
	private static final int instanceBufferSize = 10000;
	
	private final ByteBuffer uploadBuffer = MemoryUtil.memAlloc(instanceBufferSize * 24);
	
	public final ArrayList<LightData> lensFlaresInFrustum = new ArrayList();
	
	public RenderLightBulbs(GlobalRenderer renderer) {
		this.renderer = renderer;
		this.glVertexArray = glGenVertexArrays();
		this.glVertexBuffer = glGenBuffers();
		this.glInstancingBuffer = glGenBuffers();
		
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
			glBindBuffer(GL_ARRAY_BUFFER, glVertexBuffer);
			glBufferData(GL_ARRAY_BUFFER, f, GL_STATIC_DRAW);
		}

		glBindBuffer(GL_ARRAY_BUFFER, glInstancingBuffer);
		glBufferData(GL_ARRAY_BUFFER, uploadBuffer, GL_STATIC_DRAW);
		
		GLStateManager.bindVertexArray(glVertexArray);
		glBindBuffer(GL_ARRAY_BUFFER, glVertexBuffer);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);
		glBindBuffer(GL_ARRAY_BUFFER, glInstancingBuffer);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 24, 0);
		glVertexAttribDivisor(1, 1);
		glEnableVertexAttribArray(2);
		glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, 24, 12);
		glVertexAttribDivisor(2, 1);
		glEnableVertexAttribArray(3);
		glVertexAttribPointer(3, 2, GL_FLOAT, false, 24, 16);
		glVertexAttribDivisor(3, 1);
		
		this.lightBulbTexture = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/textures/lights.png")).generateMipmap().filter(GL_NEAREST_MIPMAP_NEAREST, GL_LINEAR);
	}
	
	private static final Vector4f vec = new Vector4f();
	
	private void putLight(LightData r, Matrix4f vp) {
		vec.x = (float)(r.lightX - renderer.renderPosX);
		vec.y = (float)(r.lightY - renderer.renderPosY);
		vec.z = (float)(r.lightZ - renderer.renderPosZ);
		vec.w = 0.0f;
		float dist = vec.length();
		vec.w = 1.0f;
		vp.transform(vec);
		vec.x /= vec.w;
		vec.y /= vec.w;
		vec.z /= vec.w;
		uploadBuffer.putFloat(vec.x); //posX
		uploadBuffer.putFloat(vec.y); //posY
		uploadBuffer.putFloat(vec.z); //posZ
		uploadBuffer.put((byte)(int)(r.lightR * 255f)); //lightR
		uploadBuffer.put((byte)(int)(r.lightG * 255f)); //lightG
		uploadBuffer.put((byte)(int)(r.lightB * 255f)); //lightB
		uploadBuffer.put((byte)(((Double.hashCode(r.lightX) + Double.hashCode(r.lightY) + Double.hashCode(r.lightZ)) % 4) * 64)); //sprite offset
		uploadBuffer.putFloat(r.emission);  //emission
		uploadBuffer.putFloat(r.drawPoint / (float)Math.min(Math.sqrt(dist * 0.1d), 15.0d)); //pointsize
	}
	
	public void renderLightBulbs(RenderScene scene) {
		int renderedLights = 0;
		lensFlaresInFrustum.clear();
		Iterator<LightData> lightRenderers = scene.lightRenderers.iterator();
		while(lightRenderers.hasNext()) {
			if(renderedLights >= instanceBufferSize) {
				uploadBuffer.flip();
				drawInstancedArrays(renderedLights);
				renderedLights = 0;
				uploadBuffer.clear();
			}
			LightData r = lightRenderers.next();
			float x = (float)(r.lightX - renderer.renderPosX);
			float y = (float)(r.lightY - renderer.renderPosY);
			float z = (float)(r.lightZ - renderer.renderPosZ);
			if(r.drawPoint > 0.0f && renderer.viewProjFustrum.testPoint(x, y, z)) {
				putLight(r, renderer.viewProjMatrix);
				lensFlaresInFrustum.add(r);
				++renderedLights;
			}
		}
		
		Iterator<ShadowLightRenderer> lightRenderers2 = scene.shadowLightRenderers.iterator();
		while(lightRenderers2.hasNext()) {
			if(renderedLights >= instanceBufferSize) {
				uploadBuffer.flip();
				drawInstancedArrays(renderedLights);
				renderedLights = 0;
				uploadBuffer.clear();
			}
			LightData r = lightRenderers2.next();
			float x = (float)(r.lightX - renderer.renderPosX);
			float y = (float)(r.lightY - renderer.renderPosY);
			float z = (float)(r.lightZ - renderer.renderPosZ);
			if(r.drawPoint > 0.0f && renderer.viewProjFustrum.testPoint(x, y, z)) {
				putLight(r, renderer.viewProjMatrix);
				lensFlaresInFrustum.add(r);
				++renderedLights;
			}
		}
		
		if(renderedLights > 0) {
			uploadBuffer.flip();
			drawInstancedArrays(renderedLights);
			renderedLights = 0;
			uploadBuffer.clear();
		}
		
	}
	
	private void drawInstancedArrays(int count) {
		GLStateManager.bindVertexArray(glVertexArray);
		glBindBuffer(GL_ARRAY_BUFFER, glInstancingBuffer);
		glBufferData(GL_ARRAY_BUFFER, uploadBuffer, GL_STATIC_DRAW);
		renderer.progManager.light_bulb_renderer.use();
		renderer.progManager.light_bulb_renderer_aspectRatio.set2f(1.0f / renderer.displayW, 1.0f / renderer.displayH);
		lightBulbTexture.bind(0);
		glDrawArraysInstanced(GL_TRIANGLES, 0, 6, count);
	}
	
	public void destroy() {
		glDeleteBuffers(glVertexBuffer);
		glDeleteBuffers(glInstancingBuffer);
		glDeleteVertexArrays(glVertexArray);
		MemoryUtil.memFree(uploadBuffer);
		this.lightBulbTexture.destroy();
	}
	
}

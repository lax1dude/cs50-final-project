package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES20.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengles.GLES20.GL_CULL_FACE;
import static org.lwjgl.opengles.GLES20.GL_DEPTH_TEST;
import static org.lwjgl.opengles.GLES20.GL_NEAREST;
import static org.lwjgl.opengles.GLES20.glBindFramebuffer;
import static org.lwjgl.opengles.GLES20.glClear;
import static org.lwjgl.opengles.GLES20.glClearColor;
import static org.lwjgl.opengles.GLES20.glDisable;
import static org.lwjgl.opengles.GLES20.glViewport;
import static org.lwjgl.opengles.GLES30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengles.GLES30.GL_READ_FRAMEBUFFER;
import static org.lwjgl.opengles.GLES30.glBlitFramebuffer;
import static org.lwjgl.opengles.GLES31.*;

import java.nio.ByteBuffer;
import java.util.Iterator;

import net.eagtek.eagl.EaglFramebuffer;
import net.eagtek.eagl.GLStateManager;
import net.eagtek.metaballs.MathUtil;
import net.eagtek.metaballs.client.GameConfiguration;

class CubemapGenerator {
	
	public final GlobalRenderer renderer;
	
	private int currentFace = 0;

	public final int glObject;

	private final int glFramebuffer;
	private final int glRenderbuffer;

	private final EaglFramebuffer irradianceMapA;
	private final EaglFramebuffer irradianceMapB;
	private final EaglFramebuffer specularIBLBuffer;
	private final EaglFramebuffer specularIBLBlurA;
	private final EaglFramebuffer specularIBLBlurB;
	private boolean irradianceB = false;
	
	public CubemapGenerator(GlobalRenderer r) {
		this.renderer = r;
		this.glObject = glGenTextures();
		this.glFramebuffer = glGenFramebuffers();
		this.glRenderbuffer = glGenRenderbuffers();
		
		GLStateManager.bindCubemap2D(glObject, 0);
		
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		 
		for(int i = 0; i < 6; ++i) {
			glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB16F, GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution, 0, GL_RGB, GL_HALF_FLOAT, (ByteBuffer)null);
		}
		
		GLStateManager.bindFramebuffer(glFramebuffer);
		
		glBindRenderbuffer(GL_RENDERBUFFER, glRenderbuffer);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, glRenderbuffer);

		this.irradianceMapA = new EaglFramebuffer(EaglFramebuffer.DepthBufferType.NONE, GL_RGB16F);
		this.irradianceMapB = new EaglFramebuffer(EaglFramebuffer.DepthBufferType.NONE, GL_RGB16F);
		this.specularIBLBuffer = new EaglFramebuffer(EaglFramebuffer.DepthBufferType.NONE, GL_RGB16F);
		this.specularIBLBlurA = new EaglFramebuffer(EaglFramebuffer.DepthBufferType.NONE, GL_RGB16F);
		this.specularIBLBlurB = new EaglFramebuffer(EaglFramebuffer.DepthBufferType.NONE, GL_RGB16F);

		glViewport(0, 0, 32, 32);
		glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		
		this.irradianceMapA.setSize(32, 32);
		this.irradianceMapA.bindFramebuffer();
		glClear(GL_COLOR_BUFFER_BIT);
		
		this.irradianceMapB.setSize(32, 32);
		this.irradianceMapB.bindFramebuffer();
		glClear(GL_COLOR_BUFFER_BIT);
	}
	
	public void redrawCubemap(RenderScene scene) {
		if(GameConfiguration.cubeMapUpdateAllFaces) {
			redrawCubemapFace(scene, 0);
			redrawCubemapFace(scene, 1);
			redrawCubemapFace(scene, 2);
			redrawCubemapFace(scene, 3);
			redrawCubemapFace(scene, 4);
			redrawCubemapFace(scene, 5);
		}else {
			redrawCubemapFace(scene, currentFace++);
			if(currentFace > 5) currentFace = 0;
		}

		this.specularIBLBlurA.setSize(GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution / 2);
		this.specularIBLBlurB.setSize(GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution / 2);
		
		this.specularIBLBlurA.bindFramebuffer();
		glViewport(0, 0, GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution / 2);
		this.bindCubemap(0);
		this.renderer.progManager.specular_map_generate.use();
		this.renderer.quadArray.draw(GL_TRIANGLES, 0, 6);
		
		this.specularIBLBuffer.setSize(GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution * 2);
		
		for(int i = 0; i < 4; ++i) {
			
			this.specularIBLBlurB.bindFramebuffer();
			this.specularIBLBlurA.bindColorTexture(0, 0);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_REPEAT);
			this.renderer.progManager.specular_map_blur.use();
			this.renderer.progManager.specular_map_blur_screenSizeInv.set2f((float)(i*i + 1) / (GameConfiguration.cubeMapResolution), 0.0f);
			this.renderer.quadArray.draw(GL_TRIANGLES, 0, 6);
			
			this.specularIBLBlurA.bindFramebuffer();
			this.specularIBLBlurB.bindColorTexture(0, 0);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_REPEAT);
			this.renderer.progManager.specular_map_blur.use();
			this.renderer.progManager.specular_map_blur_screenSizeInv.set2f(0.0f, (float)(i*i + 1) / (GameConfiguration.cubeMapResolution / 2));
			this.renderer.quadArray.draw(GL_TRIANGLES, 0, 6);
			
			glBindFramebuffer(GL_READ_FRAMEBUFFER, specularIBLBlurA.glObject);
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, specularIBLBuffer.glObject);
			glBlitFramebuffer(0, 0, GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution / 2, 0, i * GameConfiguration.cubeMapResolution / 2, GameConfiguration.cubeMapResolution, (i + 1) * GameConfiguration.cubeMapResolution / 2, GL_COLOR_BUFFER_BIT, GL_NEAREST);
		}
	}
	
	public void updateIrradianceTexture() {
		if(irradianceB) {
			this.irradianceMapB.setSize(128, 64);
			this.irradianceMapB.bindFramebuffer();
			irradianceB = false;
		}else {
			this.irradianceMapA.setSize(128, 64);
			this.irradianceMapA.bindFramebuffer();
			irradianceB = true;
		}

		glViewport(0, 0, 128, 64);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
		
		renderer.progManager.irradiance_map_generate.use();
		renderer.quadArray.draw(GL_TRIANGLES, 0, 6);
	}
	
	public void bindIrradianceTextureA(int slot) {
		if(this.irradianceB) this.irradianceMapB.bindColorTexture(0, slot);
		else this.irradianceMapA.bindColorTexture(0, slot);
	}
	
	public void bindIrradianceTextureB(int slot) {
		if(this.irradianceB) this.irradianceMapA.bindColorTexture(0, slot);
		else this.irradianceMapB.bindColorTexture(0, slot);
	}
	
	public void bindSpecularIBLTexture(int slot) {
		this.specularIBLBuffer.bindColorTexture(0, slot);
	}
	
	private void rebindFramebufferAttachment(int face) {
		GLStateManager.bindFramebuffer(glFramebuffer);
		GLStateManager.bindCubemap2D(glObject, 0);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, glObject, 0);
		glDrawBuffers(GL_COLOR_ATTACHMENT0);
	}
	
	public void redrawCubemapFace(RenderScene scene, int face) {
		renderer.projMatrix.pushMatrix();
		renderer.cameraMatrix.pushMatrix();
		renderer.viewProjMatrix.pushMatrix();
		
		float pitch, yaw;
		switch (face) {
		case 0:
			pitch = 0;
			yaw = 90;
			break;
		case 1:
			pitch = 0;
			yaw = -90;
			break;
		case 2:
			pitch = -90;
			yaw = 180;
			break;
		case 3:
			pitch = 90;
			yaw = 180;
			break;
		case 4:
			pitch = 0;
			yaw = 180;
			break;
		case 5:
		default:
			pitch = 0;
			yaw = 0;
			break;
		}
		
		renderer.projMatrix.identity().scale(1.0f, 1.0f, -1.0f).perspective(90.0f * MathUtil.toRadians, 1.0f, 0.1f, GameConfiguration.farPlane / 8.0f);
		renderer.cameraMatrix.identity()
		.rotate(-pitch * MathUtil.toRadians, 1.0f, 0.0f, 0.0f)
		.rotate(-yaw * MathUtil.toRadians, 0.0f, 1.0f, 0.0f);
		
		renderer.cameraMatrix.mulLocal(renderer.projMatrix, renderer.viewProjMatrix);
		renderer.viewProjFustrum.set(renderer.viewProjMatrix);

		renderer.modelMatrix.pushMatrix().identity();
		
		rebindFramebufferAttachment(face);
		
		glViewport(0, 0, GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution);
		
		glStencilMask(0xFF);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepthf(0.0f);
		glClearStencil(0x00);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

		glEnable(GL_STENCIL_TEST);
		glStencilMask(0xFF);
		glStencilFunc(GL_ALWAYS, 0xFF, 0xFF);
		glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
		
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_GREATER);
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);

		Iterator<ObjectRenderer> objectRenderers = scene.objectRenderers.iterator();
		while(objectRenderers.hasNext()) {
			ObjectRenderer r = objectRenderers.next();
			if(r.shouldRenderPass(RenderPass.CUBEMAP)) {
				r.renderPass(RenderPass.CUBEMAP, renderer);
			}
		}
		
		renderer.renderSkyDome(scene, true);
		
		renderer.projMatrix.popMatrix();
		renderer.cameraMatrix.popMatrix();
		renderer.viewProjMatrix.popMatrix();
		renderer.modelMatrix.popMatrix();
		
		renderer.viewProjFustrum.set(renderer.viewProjMatrix);
		
	}
	
	public void bindCubemap(int slot) {
		GLStateManager.bindCubemap2D(glObject, slot);
	}
	
	public void destroy() {
		glDeleteTextures(glObject);
		glDeleteFramebuffers(glFramebuffer);
		glDeleteRenderbuffers(glRenderbuffer);
		this.irradianceMapA.destroy();
		this.irradianceMapB.destroy();
		this.specularIBLBuffer.destroy();
		this.specularIBLBlurA.destroy();
		this.specularIBLBlurB.destroy();
	}

}

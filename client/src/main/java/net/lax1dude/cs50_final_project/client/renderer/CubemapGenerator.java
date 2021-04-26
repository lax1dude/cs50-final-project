package net.lax1dude.cs50_final_project.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import java.nio.ByteBuffer;
import java.util.Iterator;

import net.lax1dude.cs50_final_project.MathUtil;
import net.lax1dude.cs50_final_project.client.GameConfiguration;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglFramebuffer;
import net.lax1dude.cs50_final_project.client.renderer.opengl.GLStateManager;

class CubemapGenerator {
	
	public final GlobalRenderer renderer;
	
	private int currentFace = 0;

	public final int glObject;

	private final int glFramebuffer;
	private final int glRenderbuffer;

	private final EaglFramebuffer irradianceMap;
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

		this.irradianceMap = new EaglFramebuffer(EaglFramebuffer.DepthBufferType.NONE, GL_RGB16F);
		this.specularIBLBuffer = new EaglFramebuffer(EaglFramebuffer.DepthBufferType.NONE, GL_RGB16F);
		this.specularIBLBlurA = new EaglFramebuffer(EaglFramebuffer.DepthBufferType.NONE, GL_RGB16F);
		this.specularIBLBlurB = new EaglFramebuffer(EaglFramebuffer.DepthBufferType.NONE, GL_RGB16F);

		glViewport(0, 0, 128, 128);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		this.irradianceMap.setSize(128, 128);
		this.irradianceMap.bindFramebuffer();
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
		
		this.bindCubemap(0);
		glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

		this.specularIBLBlurA.setSize(GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution / 2);
		this.specularIBLBlurB.setSize(GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution / 2);
		
		this.specularIBLBlurA.bindFramebuffer();
		glViewport(0, 0, GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution / 2);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		this.renderer.progManager.specular_map_generate.use();
		this.renderer.quadArray.draw(GL_TRIANGLES, 0, 6);
		
		this.specularIBLBuffer.setSize(GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution * 2);
		
		for(int i = 0; i < 4; ++i) {
			
			this.specularIBLBlurB.bindFramebuffer();
			this.specularIBLBlurA.bindColorTexture(0, 0);
			this.renderer.progManager.specular_map_blur.use();
			this.renderer.progManager.specular_map_blur_screenSizeInv.set2f((float)(i + 2) / (256), 0.0f);
			this.renderer.quadArray.draw(GL_TRIANGLES, 0, 6);
			
			this.specularIBLBlurA.bindFramebuffer();
			this.specularIBLBlurB.bindColorTexture(0, 0);
			this.renderer.progManager.specular_map_blur.use();
			this.renderer.progManager.specular_map_blur_screenSizeInv.set2f(0.0f, (float)(i + 2) / (256));
			this.renderer.quadArray.draw(GL_TRIANGLES, 0, 6);
			
			glBindFramebuffer(GL_READ_FRAMEBUFFER, specularIBLBlurA.glObject);
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, specularIBLBuffer.glObject);
			glBlitFramebuffer(0, 0, GameConfiguration.cubeMapResolution, GameConfiguration.cubeMapResolution / 2, 0, i * GameConfiguration.cubeMapResolution / 2, GameConfiguration.cubeMapResolution, (i + 1) * GameConfiguration.cubeMapResolution / 2, GL_COLOR_BUFFER_BIT, GL_NEAREST);
		}
	}
	
	public void updateIrradianceTexture() {
		irradianceB = !irradianceB;
		
		this.irradianceMap.setSize(64, 64);
		this.irradianceMap.bindFramebuffer();
			
		glViewport(0, irradianceB ? 32 : 0, 64, 32);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
		
		renderer.progManager.irradiance_map_generate.use();
		renderer.quadArray.draw(GL_TRIANGLES, 0, 6);
	}
	
	public void bindIrradianceTexture(int slot) {
		this.irradianceMap.bindColorTexture(0, slot);
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
		this.irradianceMap.destroy();
		this.specularIBLBuffer.destroy();
		this.specularIBLBlurA.destroy();
		this.specularIBLBlurB.destroy();
	}

}

package net.lax1dude.cs50_final_project.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import java.nio.ByteBuffer;

import org.lwjgl.opengles.EXTTextureFilterAnisotropic;

import net.lax1dude.cs50_final_project.client.GameClient;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglContext;
import net.lax1dude.cs50_final_project.client.renderer.opengl.GLStateManager;

public class MaterialTexture2D {
	
	public final int glObject;
	
	private boolean destroyed = false;
	
	public void bindTexture(int u) {
		GLStateManager.bindTexture2D(glObject, u);
	}
	
	public void destroy() {
		if(!destroyed && EaglContext.contextAvailable()) {
			glDeleteTextures(glObject);
			destroyed = true;
		}
	}
	
	public void finalize() {
		if(!destroyed && EaglContext.contextAvailable()) {
			GameClient.log.warn("MaterialTexture2D #{} leaked memory", glObject);
			glDeleteTextures(glObject);
			destroyed = true;
		}
	}
	
	public MaterialTexture2D filter(int min, int mag) {
		return filter(min, mag, 1.0f);
	}
	
	public MaterialTexture2D filter(int min, int mag, float anisotropy) {
		GLStateManager.bindTexture2D(glObject);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, min);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, mag);
		glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, Math.min(anisotropy, (float)EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
		return this;
	}
	
	public MaterialTexture2D wrap(int s, int t) {
		GLStateManager.bindTexture2D(glObject);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, s);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, t);
		return this;
	}
	
	public MaterialTexture2D wrap(int w) {
		return wrap(w, w);
	}
	
	public MaterialTexture2D generateMipmap() {
		GLStateManager.bindTexture2D(glObject);
		glGenerateMipmap(GL_TEXTURE_2D);
		return this;
	}
	
	public MaterialTexture2D(MaterialFile f) {
		this.glObject = glGenTextures();
		GLStateManager.bindTexture2D(glObject, 0);
		filter(GL_LINEAR, GL_LINEAR);
		wrap(GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, f.widthHeight * 3, f.widthHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer)null);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, f.widthHeight, f.widthHeight, GL_RGBA, GL_UNSIGNED_BYTE, f.diffuse);
		glTexSubImage2D(GL_TEXTURE_2D, 0, f.widthHeight, 0, f.widthHeight, f.widthHeight, GL_RGBA, GL_UNSIGNED_BYTE, f.normal);
		glTexSubImage2D(GL_TEXTURE_2D, 0, f.widthHeight * 2, 0, f.widthHeight, f.widthHeight, GL_RGBA, GL_UNSIGNED_BYTE, f.material);
	}
	
}

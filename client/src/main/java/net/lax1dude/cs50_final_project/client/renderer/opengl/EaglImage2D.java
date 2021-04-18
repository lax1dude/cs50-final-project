package net.lax1dude.cs50_final_project.client.renderer.opengl;

import static org.lwjgl.opengles.GLES30.*;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengles.EXTTextureFilterAnisotropic;
import org.lwjgl.system.MemoryUtil;

import net.lax1dude.cs50_final_project.client.GameClient;

public class EaglImage2D {
	
	public final int glObject;
	
	private boolean destroyed = false;
	
	public static EaglImage2D consumeStream(InputStream stream) {
		if(stream == null) return null;
		try {
			BufferedImage icon = ImageIO.read(stream);
			stream.close();
			return (new EaglImage2D()).uploadRGB(icon);
		}catch(Throwable tt) {
			GameClient.log.error("Could not load graphic", tt);
			return null;
		}
	}
	
	public EaglImage2D() {
		this.glObject = glGenTextures();
		GLStateManager.bindTexture2D(glObject);
		wrap(GL_REPEAT);
		filter(GL_NEAREST, GL_NEAREST);
	}
	
	
	public EaglImage2D uploadRGB(BufferedImage img, int x, int y, int w, int h) {
		
		ByteBuffer imgBuf = MemoryUtil.memAlloc(w * h * 3);
		
		int[] pixels = img.getRGB(x, y, w, h, null, 0, w);

		for(int y2 = 0; y2 < h; ++y2) {
			for(int x2 = 0; x2 < w; ++x2) {
				int idx = (h - y2 - 1) * w + x2;
				imgBuf.put((byte)(pixels[idx] >> 16));
				imgBuf.put((byte)(pixels[idx] >> 8));
				imgBuf.put((byte)(pixels[idx]));
			}
		}
		
		imgBuf.flip();

		GLStateManager.bindTexture2D(glObject);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, w, h, 0, GL_RGB, GL_UNSIGNED_BYTE, imgBuf);
		
		MemoryUtil.memFree(imgBuf);
		
		return this;
	}
	
	public EaglImage2D uploadRGB(BufferedImage img) {
		return uploadRGB(img, 0, 0, img.getWidth(), img.getHeight());
	}
	
	
	public EaglImage2D uploadRGBA(BufferedImage img, int x, int y, int w, int h) {
		
		ByteBuffer imgBuf = MemoryUtil.memAlloc(w * h * 4);
		int[] pixels = img.getRGB(x, y, w, h, null, 0, w);
		
		for(int y2 = 0; y2 < h; ++y2) {
			for(int x2 = 0; x2 < w; ++x2) {
				int idx = (h - y2 - 1) * w + x2;
				int alpha = (pixels[idx] >> 24) & 0xFF;
				imgBuf.put((byte)(((pixels[idx] >> 16) & 0xFF) * alpha / 256));
				imgBuf.put((byte)(((pixels[idx] >> 8) & 0xFF) * alpha / 256));
				imgBuf.put((byte)(((pixels[idx]) & 0xFF) * alpha / 256));
				imgBuf.put((byte)alpha);
			}
		}
		
		imgBuf.flip();

		GLStateManager.bindTexture2D(glObject);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, imgBuf);
		
		MemoryUtil.memFree(imgBuf);
			
		return this;
	}
	
	public EaglImage2D uploadRGBA(BufferedImage img) {
		return uploadRGBA(img, 0, 0, img.getWidth(), img.getHeight());
	}
	
	
	public EaglImage2D uploadRGBSub(BufferedImage img, int x, int y, int w, int h, int dx, int dy) {
		ByteBuffer imgBuf = MemoryUtil.memAlloc(w * h * 3);
		int[] pixels = img.getRGB(x, y, w, h, null, 0, w);
		
		for(int y2 = 0; y2 < h; ++y2) {
			for(int x2 = 0; x2 < w; ++x2) {
				int idx = (h - y2 - 1) * w + x2;
				imgBuf.put((byte)(pixels[idx] >> 16));
				imgBuf.put((byte)(pixels[idx] >> 8));
				imgBuf.put((byte)(pixels[idx]));
			}
		}
		
		imgBuf.flip();

		GLStateManager.bindTexture2D(glObject);
		glTexSubImage2D(GL_TEXTURE_2D, 0, dx, dy, w, h, GL_RGB, GL_UNSIGNED_BYTE, imgBuf);

		MemoryUtil.memFree(imgBuf);
		
		return this;
	}
	
	public EaglImage2D uploadRGBSub(BufferedImage img, int dx, int dy) {
		return uploadRGBSub(img, 0, 0, img.getWidth(), img.getHeight(), dx, dy);
	}
	
	
	public EaglImage2D uploadRGBASub(BufferedImage img, int x, int y, int w, int h, int dx, int dy) {
		ByteBuffer imgBuf = MemoryUtil.memAlloc(w * h * 3);
		int[] pixels = img.getRGB(x, y, w, h, null, 0, w);
		
		for(int y2 = 0; y2 < h; ++y2) {
			for(int x2 = 0; x2 < w; ++x2) {
				int idx = (h - y2 - 1) * w + x2;
				int alpha = (pixels[idx] >> 24) & 0xFF;
				imgBuf.put((byte)(((pixels[idx] >> 16) & 0xFF) * alpha / 256));
				imgBuf.put((byte)(((pixels[idx] >> 8) & 0xFF) * alpha / 256));
				imgBuf.put((byte)(((pixels[idx]) & 0xFF) * alpha / 256));
				imgBuf.put((byte)alpha);
			}
		}
		
		imgBuf.flip();

		GLStateManager.bindTexture2D(glObject);
		glTexSubImage2D(GL_TEXTURE_2D, 0, dx, dy, w, h, GL_RGBA, GL_UNSIGNED_BYTE, imgBuf);

		MemoryUtil.memFree(imgBuf);
		
		return this;
	}
	
	public EaglImage2D uploadRGBASub(BufferedImage img, int dx, int dy) {
		return uploadRGBASub(img, 0, 0, img.getWidth(), img.getHeight(), dx, dy);
	}
	
	
	public EaglImage2D generateMipmap() {
		GLStateManager.bindTexture2D(glObject);
		glGenerateMipmap(GL_TEXTURE_2D);
		return this;
	}
	
	public EaglImage2D filter(int min, int mag) {
		return filter(min, mag, 1.0f);
	}
	
	public EaglImage2D filter(int min, int mag, float anisotropy) {
		GLStateManager.bindTexture2D(glObject);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, min);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, mag);
		glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, Math.min(anisotropy, (float)EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
		return this;
	}
	
	public EaglImage2D wrap(int s, int t) {
		GLStateManager.bindTexture2D(glObject);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, s);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, t);
		return this;
	}
	
	public EaglImage2D wrap(int w) {
		return wrap(w, w);
	}
	
	public EaglImage2D bind() {
		GLStateManager.bindTexture2D(glObject);
		return this;
	}
	
	public EaglImage2D bind(int textureUnit) {
		GLStateManager.bindTexture2D(glObject, textureUnit);
		return this;
	}
	
	public void destroy() {
		if(!destroyed) {
			glDeleteTextures(glObject);
			destroyed = true;
		}
	}
	
	public void finalize() {
		if(!destroyed && EaglContext.contextAvailable()) {
			EaglContext.log.error("GL image #{} is leaking memory", glObject);
			glDeleteTextures(glObject);
			destroyed = true;
		}
	}
	
}

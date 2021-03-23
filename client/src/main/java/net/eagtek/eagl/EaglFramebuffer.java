package net.eagtek.eagl;

import static org.lwjgl.opengles.GLES30.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengles.EXTMultisampledRenderToTexture;
import org.lwjgl.system.MemoryStack;

public class EaglFramebuffer {
	
	public static enum DepthBufferType {
		NONE(0, 0, 0, 0, false),
		DEPTH16_RENDERBUFFER(GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT, GL_DEPTH_ATTACHMENT, true),
		DEPTH16_TEXTURE(GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT, GL_DEPTH_ATTACHMENT, false),
		DEPTH24_RENDERBUFFER(GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, GL_DEPTH_ATTACHMENT, true),
		DEPTH24_TEXTURE(GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, GL_DEPTH_ATTACHMENT, false),
		DEPTH24_STENCIL8_RENDERBUFFER(GL_DEPTH24_STENCIL8, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, GL_DEPTH_STENCIL_ATTACHMENT, true),
		DEPTH24_STENCIL8_TEXTURE(GL_DEPTH24_STENCIL8, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, GL_DEPTH_STENCIL_ATTACHMENT, false),
		STENCIL8_RENDERBUFFER(GL_STENCIL_INDEX8, GL_STENCIL, GL_UNSIGNED_BYTE, GL_STENCIL_ATTACHMENT, true);

		protected final int glEnumA;
		protected final int glEnumB;
		protected final int glEnumC;
		protected final int attachment;
		protected final boolean rbo;
		
		private DepthBufferType(int glEnumA, int glEnumB, int glEnumC, int attachment, boolean rbo) {
			this.glEnumA = glEnumA;
			this.glEnumB = glEnumB;
			this.glEnumC = glEnumC;
			this.attachment = attachment;
			this.rbo = rbo;
		}
	}

	public final int glObject;
	public final int[] colorAttachments;
	public final int[] colorAttachmentTypes;
	public final int depthBuffer;
	public final DepthBufferType depthBufferType;
	
	int samples = 1;
	
	private int w = 0;
	private int h = 0;

	public EaglFramebuffer(DepthBufferType depthStencil, int... targets) {
		this.glObject = glGenFramebuffers();
		depthBufferType = depthStencil;
		
		if(depthStencil == DepthBufferType.NONE) {
			this.depthBuffer = -1;
		}else if(depthStencil.rbo){
			this.depthBuffer = glGenRenderbuffers();
		}else {
			this.depthBuffer = glGenTextures();
		}
		
		colorAttachmentTypes = targets;
		colorAttachments = new int[targets.length];
		glGenTextures(colorAttachments);
	}
	
	public EaglFramebuffer linearTex() {
		for(int i = 0; i < colorAttachmentTypes.length; ++i) {
			GLStateManager.bindTexture2D(colorAttachments[i]);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);  
		}
		return this;
	}
	
	public EaglFramebuffer setSize(int w, int h, int samples) {
		if(this.w != w || this.h != h || this.samples != samples) {
			
			//if(glObject != -1) glDeleteFramebuffers(glObject);
			//glObject = glGenFramebuffers();
		
			this.w = w;
			this.h = h;
			this.samples = samples;
			
			GLStateManager.bindFramebuffer(glObject);
			
			if(samples > 1) {
				for(int i = 0; i < colorAttachmentTypes.length; ++i) {
					GLStateManager.bindTexture2D(colorAttachments[i]);
					glTexImage2D(GL_TEXTURE_2D, 0, colorAttachmentTypes[i], w, h, 0, getFormat(colorAttachmentTypes[i]), getDataType(colorAttachmentTypes[i]), (ByteBuffer)null);
					glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
					glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);  
					glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
					glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
					EXTMultisampledRenderToTexture.glFramebufferTexture2DMultisampleEXT(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, colorAttachments[i], 0, samples);
				}
				if(depthBuffer != -1) {
					if(depthBufferType.rbo){
						glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
						EXTMultisampledRenderToTexture.glRenderbufferStorageMultisampleEXT(GL_RENDERBUFFER, samples, depthBufferType.glEnumA, w, h);
						glFramebufferRenderbuffer(GL_FRAMEBUFFER, depthBufferType.attachment, GL_RENDERBUFFER, depthBuffer);
					}else {
						GLStateManager.bindTexture2D(depthBuffer);
						glTexImage2D(GL_TEXTURE_2D, 0, depthBufferType.glEnumA, w, h, 0, depthBufferType.glEnumB, depthBufferType.glEnumC, (ByteBuffer)null);
						glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
						glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);  
						glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
						glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
						EXTMultisampledRenderToTexture.glFramebufferTexture2DMultisampleEXT(GL_FRAMEBUFFER, depthBufferType.attachment, GL_TEXTURE_2D, depthBuffer, 0, samples);
					}
				}
			}else {
				for(int i = 0; i < colorAttachmentTypes.length; ++i) {
					GLStateManager.bindTexture2D(colorAttachments[i]);
					glTexImage2D(GL_TEXTURE_2D, 0, colorAttachmentTypes[i], w, h, 0, getFormat(colorAttachmentTypes[i]), getDataType(colorAttachmentTypes[i]), (ByteBuffer)null);
					glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
					glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);  
					glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
					glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
					glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, colorAttachments[i], 0);
				}
				if(depthBuffer != -1) {
					if(depthBufferType.rbo){
						glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
						glRenderbufferStorage(GL_RENDERBUFFER, depthBufferType.glEnumA, w, h);
						glFramebufferRenderbuffer(GL_FRAMEBUFFER, depthBufferType.attachment, GL_RENDERBUFFER, depthBuffer);
					}else {
						GLStateManager.bindTexture2D(depthBuffer);
						glTexImage2D(GL_TEXTURE_2D, 0, depthBufferType.glEnumA, w, h, 0, depthBufferType.glEnumB, depthBufferType.glEnumC, (ByteBuffer)null);
						glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
						glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);  
						glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
						glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
						glFramebufferTexture2D(GL_FRAMEBUFFER, depthBufferType.attachment, GL_TEXTURE_2D, depthBuffer, 0);
					}
				}
			}
		}
		
		GLStateManager.bindFramebuffer(0);
		
		return this;
	}
	
	public void bindFramebuffer() {
		GLStateManager.bindFramebuffer(glObject);
		try(MemoryStack s = MemoryStack.stackPush()) {
			IntBuffer up = s.mallocInt(colorAttachments.length);
			for(int i = 0; i < colorAttachments.length; ++i) {
				up.put(GL_COLOR_ATTACHMENT0 + i);
			}
			up.flip();
			glDrawBuffers(up);
		}
	}
	
	public void bindDepthTexture(int textureUnit) {
		if(depthBufferType.rbo == false) {
			GLStateManager.bindTexture2D(depthBuffer, textureUnit);
		}
	}
	
	public void bindColorTexture(int slot, int textureUnit) {
		GLStateManager.bindTexture2D(colorAttachments[slot], textureUnit);
	}
	
	public void bindDepthTexture() {
		bindDepthTexture(0);
	}
	
	public void bindColorTexture(int slot) {
		bindColorTexture(slot, 0);
	}

	public static int getFormat(int internalFormat) {
		switch(internalFormat) {
		case GL_R8: return GL_RED;
		case GL_R8_SNORM: return GL_RED;
		case GL_RG8: return GL_RG;
		case GL_RG8_SNORM: return GL_RG;
		case GL_RGB8: return GL_RGB;
		case GL_RGB8_SNORM: return GL_RGB;
		case GL_RGBA8: return GL_RGBA;
		case GL_RGBA8_SNORM: return GL_RGBA;
		case GL_SRGB8: return GL_RGB;
		case GL_SRGB8_ALPHA8: return GL_RGBA;
		case GL_R16F: return GL_RED;
		case GL_RG16F: return GL_RG;
		case GL_RGB16F: return GL_RGB;
		case GL_RGBA16F: return GL_RGBA;
		case GL_R32F: return GL_RED;
		case GL_RG32F: return GL_RG;
		case GL_RGB32F: return GL_RGB;
		case GL_RGBA32F: return GL_RGBA;
		case GL_R11F_G11F_B10F: return GL_RGB;
		case GL_RGB9_E5: return GL_RGB;
		case GL_R8I: return GL_RED_INTEGER;
		case GL_R8UI: return GL_RED_INTEGER;
		case GL_R16I: return GL_RED_INTEGER;
		case GL_R16UI: return GL_RED_INTEGER;
		case GL_R32I: return GL_RED_INTEGER;
		case GL_R32UI: return GL_RED_INTEGER;
		case GL_RG8I: return GL_RG_INTEGER;
		case GL_RG8UI: return GL_RG_INTEGER;
		case GL_RG16I: return GL_RG_INTEGER;
		case GL_RG16UI: return GL_RG_INTEGER;
		case GL_RG32I: return GL_RG_INTEGER;
		case GL_RG32UI: return GL_RG_INTEGER;
		case GL_RGB8I: return GL_RGB_INTEGER;
		case GL_RGB8UI: return GL_RGB_INTEGER;
		case GL_RGB16I: return GL_RGB_INTEGER;
		case GL_RGB16UI: return GL_RGB_INTEGER;
		case GL_RGB32I: return GL_RGB_INTEGER;
		case GL_RGB32UI: return GL_RGB_INTEGER;
		case GL_RGBA8I: return GL_RGBA_INTEGER;
		case GL_RGBA8UI: return GL_RGBA_INTEGER;
		case GL_RGBA16I: return GL_RGBA_INTEGER;
		case GL_RGBA16UI: return GL_RGBA_INTEGER;
		case GL_RGBA32I: return GL_RGBA_INTEGER;
		case GL_RGBA32UI: return GL_RGBA_INTEGER;
		default: return internalFormat;
		}
	}
	
	public static int getDataType(int internalFormat) {
		switch(internalFormat) {
		case GL_R8: return GL_UNSIGNED_BYTE;
		case GL_R8_SNORM: return GL_UNSIGNED_BYTE;
		case GL_RG8: return GL_UNSIGNED_BYTE;
		case GL_RG8_SNORM: return GL_UNSIGNED_BYTE;
		case GL_RGB8: return GL_UNSIGNED_BYTE;
		case GL_RGB8_SNORM: return GL_UNSIGNED_BYTE;
		case GL_RGBA8: return GL_UNSIGNED_BYTE;
		case GL_RGBA8_SNORM: return GL_UNSIGNED_BYTE;
		case GL_SRGB8: return GL_UNSIGNED_BYTE;
		case GL_SRGB8_ALPHA8: return GL_UNSIGNED_BYTE;
		case GL_R16F: return GL_HALF_FLOAT;
		case GL_RG16F: return GL_HALF_FLOAT;
		case GL_RGB16F: return GL_HALF_FLOAT;
		case GL_RGBA16F: return GL_HALF_FLOAT;
		case GL_R32F: return GL_FLOAT;
		case GL_RG32F: return GL_FLOAT;
		case GL_RGB32F: return GL_FLOAT;
		case GL_RGBA32F: return GL_FLOAT;
		case GL_R11F_G11F_B10F: return GL_UNSIGNED_INT_10F_11F_11F_REV;
		case GL_RGB9_E5: return GL_UNSIGNED_INT_5_9_9_9_REV;
		case GL_R8I: return GL_BYTE;
		case GL_R8UI: return GL_UNSIGNED_BYTE;
		case GL_R16I: return GL_SHORT;
		case GL_R16UI: return GL_UNSIGNED_SHORT;
		case GL_R32I: return GL_INT;
		case GL_R32UI: return GL_UNSIGNED_INT;
		case GL_RG8I: return GL_BYTE;
		case GL_RG8UI: return GL_UNSIGNED_BYTE;
		case GL_RG16I: return GL_SHORT;
		case GL_RG16UI: return GL_UNSIGNED_SHORT;
		case GL_RG32I: return GL_INT;
		case GL_RG32UI: return GL_UNSIGNED_INT;
		case GL_RGB8I: return GL_BYTE;
		case GL_RGB8UI: return GL_UNSIGNED_BYTE;
		case GL_RGB16I: return GL_SHORT;
		case GL_RGB16UI: return GL_UNSIGNED_SHORT;
		case GL_RGB32I: return GL_INT;
		case GL_RGB32UI: return GL_UNSIGNED_INT;
		case GL_RGBA8I: return GL_BYTE;
		case GL_RGBA8UI: return GL_UNSIGNED_BYTE;
		case GL_RGBA16I: return GL_SHORT;
		case GL_RGBA16UI: return GL_UNSIGNED_SHORT;
		case GL_RGBA32I: return GL_INT;
		case GL_RGBA32UI: return GL_UNSIGNED_INT;
		default: return GL_UNSIGNED_BYTE;
		}
	}

	public EaglFramebuffer setSize(int w, int h) {
		return setSize(w, h, 1);
	}
	
}

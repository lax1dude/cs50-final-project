package net.eagtek.eagl;

import static org.lwjgl.opengles.GLES30.*;

public class GLStateManager {

	private static int[] boundTexture2D = new int[32];
	private static int boundTextureUnit = GL_TEXTURE0;

	public static final void bindTexture2D(int tex) {
		bindTexture2D(tex, 0);
	}
	
	public static final void bindTexture2D(int tex, int slot) {
		if(boundTexture2D[slot] != tex) {
			if(boundTextureUnit != GL_TEXTURE0 + slot) glActiveTexture(boundTextureUnit = GL_TEXTURE0 + slot);
			glBindTexture(GL_TEXTURE_2D, boundTexture2D[slot] = tex);
		}
	}
	
	private static int boundProgram = -1;
	
	public static final void bindProgram(int prog) {
		if(boundProgram != prog) glUseProgram(boundProgram = prog);
	}
	
	private static int boundFramebuffer = -1;
	
	public static final void bindFramebuffer(int fbo) {
		if(boundFramebuffer != fbo) glBindFramebuffer(GL_FRAMEBUFFER, boundFramebuffer = fbo);
	}
	
	private static int boundVertexArray = -1;

	public static final void bindVertexArray(int vao) {
		if(boundVertexArray != vao) glBindVertexArray(boundVertexArray = vao);
	}
}

package net.lax1dude.cs50_final_project.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import net.lax1dude.cs50_final_project.client.renderer.opengl.GLStateManager;

public class RenderUtil {
	
	public static RenderUtil instance = null;
	
	public final GlobalRenderer renderer;
	
	private final int readFBO;
	private final int drawFBO;
	
	public RenderUtil(GlobalRenderer r) {
		this.renderer = r;
		this.readFBO = glGenFramebuffers();
		this.drawFBO = glGenFramebuffers();
	}
	
	public void copyTexture2D(int srcTex, int dstTex, int sx, int sy, int dx, int dy, int w, int h, int sLevel, int dLevel) {
		GLStateManager.bindFramebuffer(readFBO);
		GLStateManager.bindTexture2D(srcTex, 0);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, srcTex, sLevel);
		
		GLStateManager.bindFramebuffer(drawFBO);
		GLStateManager.bindTexture2D(dstTex, 0);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, dstTex, dLevel);
		
		glBindFramebuffer(GL_READ_FRAMEBUFFER, readFBO);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, drawFBO);
		glDrawBuffers(GL_COLOR_ATTACHMENT0);
		glBlitFramebuffer(sx, sy, sx+w, sy+h, dx, dy, dx+w, dy+h, GL_COLOR_BUFFER_BIT, GL_NEAREST);
	}

	public void destroy() {
		glDeleteFramebuffers(readFBO);
		glDeleteFramebuffers(drawFBO);
	}
	
}

package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES20.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengles.GLES20.glClear;
import static org.lwjgl.opengles.GLES20.glClearColor;
import static org.lwjgl.opengles.GLES20.glViewport;
import static org.lwjgl.opengles.GLES30.*;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import net.eagtek.eagl.EaglFramebuffer;
import net.eagtek.eagl.EaglFramebuffer.DepthBufferType;
import net.eagtek.eagl.EaglImage2D;
import net.eagtek.eagl.EaglIndexBuffer;
import net.eagtek.eagl.EaglProgram;
import net.eagtek.eagl.EaglTessellator;
import net.eagtek.eagl.EaglVertexArray;
import net.eagtek.eagl.EaglVertexBuffer;
import net.eagtek.eagl.GLDataType;
import net.eagtek.eagl.GLStateManager;
import net.eagtek.eagl.ResourceLoader;
import net.eagtek.metaballs.MathUtil;
import net.eagtek.metaballs.client.GameClient;

public class GlobalRenderer {
	
	public final GameClient client;

	public final Matrix4fStack modelMatrix = new Matrix4fStack(64);
	public final Matrix4f cameraMatrix = new Matrix4f();
	public final Matrix4f projMatrix = new Matrix4f();
	public final Matrix4f viewProjMatrix = new Matrix4f();
	public final Matrix4f multipliedMatrix = new Matrix4f();
	
	public final ProgramManager progManager;
	
	private final EaglVertexArray quadArray;
	private final EaglImage2D testGraphic;
	
	private final EaglFramebuffer mainFramebuffer;
	
	public GlobalRenderer(GameClient gameClient) {
		client = gameClient;
		progManager = new ProgramManager(this);
		
		// setup test quad =====================================================
		
		EaglTessellator t = new EaglTessellator(12 + 8, 4, 6);
		
		EaglVertexBuffer vbo = new EaglVertexBuffer();
		EaglIndexBuffer ibo = new EaglIndexBuffer(GLDataType.SHORT_U);
		quadArray = new EaglVertexArray(
				new EaglVertexBuffer[] { vbo },
				new EaglVertexArray.VertexAttribPointer[] {
						EaglVertexArray.attrib(0, 0, 3, GLDataType.FLOAT, false, 20, 0),
						EaglVertexArray.attrib(0, 1, 2, GLDataType.FLOAT, false, 20, 12)
				},
				ibo
		);
		
		int a = t.put_vec3f(-1.0f, -1.0f, 0.0f).put_vec2f(0.0f, 0.0f).endVertex();
		int b = t.put_vec3f( 1.0f, -1.0f, 0.0f).put_vec2f(1.0f, 0.0f).endVertex();
		int c = t.put_vec3f( 1.0f,  1.0f, 0.0f).put_vec2f(1.0f, 1.0f).endVertex();
		int d = t.put_vec3f(-1.0f,  1.0f, 0.0f).put_vec2f(0.0f, 1.0f).endVertex();
		
		t.addToIndex(a).addToIndex(b).addToIndex(c);
		t.addToIndex(a).addToIndex(c).addToIndex(d);
		
		t.uploadVertexes(vbo, true);
		t.uploadIndexes(ibo, true);
		t.destroy();
		
		//setup test texture ==================================================
		
		testGraphic = new EaglImage2D();
		
		BufferedImage icon64 = null;
		try {
			InputStream stream;
			stream = ResourceLoader.loadResource("metaballs/icon64.png");
			icon64 = ImageIO.read(stream);
			stream.close();
			testGraphic.uploadRGB(icon64);
		}catch(Throwable tt) {
			GameClient.log.error("Could not load test graphic", tt);
		}
		
		//setup framebuffer ==================================================
		
		mainFramebuffer = (new EaglFramebuffer(DepthBufferType.DEPTH24_STENCIL8_TEXTURE, GL_RGB8));
		
	}
	
	public void renderGame() {
		
		int w = client.context.getInnerWidth();
		int h = client.context.getInnerHeight();
		
		mainFramebuffer.setSize(w, h, 8);
		mainFramebuffer.bindFramebuffer();
		
		glViewport(0, 0, w, h);
		
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		
		projMatrix.setPerspective(100.0f * MathUtil.toRadians, (float)w / (float)h, 0.1f, 1024.0f);
		cameraMatrix.identity()
		.rotate(-(client.prevRenderPitch + (client.renderPitch - client.prevRenderPitch) * client.partialTicks) * MathUtil.toRadians, 1.0f, 0.0f, 0.0f)
		.rotate(-(client.prevRenderYaw + (client.renderYaw - client.prevRenderYaw) * client.partialTicks) * MathUtil.toRadians, 0.0f, 1.0f, 0.0f)
		.translate(
				(float)-(client.prevRenderX + (client.renderX - client.prevRenderX) * client.partialTicks),
				(float)-(client.prevRenderY + (client.renderY - client.prevRenderY) * client.partialTicks),
				(float)-(client.prevRenderZ + (client.renderZ - client.prevRenderZ) * client.partialTicks)
		);
		cameraMatrix.mulLocal(projMatrix, viewProjMatrix);
		
		modelMatrix.clear();
		modelMatrix.translate(0.0f, 0.0f, -5.0f);
		
		testGraphic.bind();
		updateMatrix(progManager.p3f2f_texture);
		progManager.p3f2f_texture.use();
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		GLStateManager.bindFramebuffer(0);

		glViewport(0, 0, w, h);
		
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		
		projMatrix.identity();
		cameraMatrix.identity();
		viewProjMatrix.identity();
		modelMatrix.clear();
		
		updateMatrix(progManager.p3f2f_texture);
		progManager.p3f2f_texture.use();
		mainFramebuffer.bindColorTexture(0);
		
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
	}
	
	public void updateMatrix(EaglProgram prog) {
		if(prog.matrix_m != null) prog.matrix_m.setMatrix4f(modelMatrix);
		if(prog.matrix_v != null) prog.matrix_v.setMatrix4f(cameraMatrix);
		if(prog.matrix_p != null) prog.matrix_p.setMatrix4f(projMatrix);
		if(prog.matrix_mvp != null) {
			prog.matrix_mvp.setMatrix4f(modelMatrix.mulLocal(viewProjMatrix, multipliedMatrix));
			if(prog.matrix_mvp_inv != null) {
				prog.matrix_mvp.setMatrix4f(multipliedMatrix.invert());
			}
		}else if(prog.matrix_mvp_inv != null) {
			prog.matrix_mvp.setMatrix4f(modelMatrix.mulLocal(viewProjMatrix, multipliedMatrix).invert());
		}
		if(prog.matrix_mv != null) {
			prog.matrix_mv.setMatrix4f(modelMatrix.mulLocal(cameraMatrix, multipliedMatrix));
		}else if(prog.matrix_mv_invtrans != null) {
			prog.matrix_mv_invtrans.setMatrix4f(modelMatrix.mulLocal(cameraMatrix, multipliedMatrix).invert().transpose());
		}
		if(prog.matrix_m_invtrans != null) {
			prog.matrix_m_invtrans.setMatrix4f(modelMatrix.invert(multipliedMatrix).transpose());
		}
	}
	
	public void destory() {
		quadArray.destroyWithBuffers();
	}

}

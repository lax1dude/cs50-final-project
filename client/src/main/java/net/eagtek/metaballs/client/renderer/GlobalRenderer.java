package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import java.io.InputStream;
import java.util.Iterator;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

import net.eagtek.eagl.EaglFramebuffer;
import net.eagtek.eagl.EaglFramebuffer.DepthBufferType;
import net.eagtek.eagl.EaglImage2D;
import net.eagtek.eagl.EaglModelLoader;
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

	private EaglVertexArray testModel = null;
	private ModelRenderer testModelRenderer = null;
	private final EaglImage2D testModelTexture;

	private final EaglFramebuffer gBuffer;
	private final EaglFramebuffer lightBuffer;
	private final EaglFramebuffer combinedBuffer;
	
	private long secondTimer = 0l;
	private int framesPassed = 0;
	private int prevFramesPassed = 0;
	
	public int getFramerate() {
		return prevFramesPassed;
	}
	
	public GlobalRenderer(GameClient gameClient) {
		client = gameClient;
		progManager = new ProgramManager(this);
		
		// setup test quad =====================================================
		
		EaglTessellator t = new EaglTessellator(20, 6, 0);
		EaglVertexBuffer vbo = new EaglVertexBuffer();
		quadArray = new EaglVertexArray(
			new EaglVertexBuffer[] { vbo }, new EaglVertexArray.VertexAttribPointer[] {
			EaglVertexArray.attrib(0, 0, 3, GLDataType.FLOAT, false, 20, 0), EaglVertexArray.attrib(0, 1, 2, GLDataType.FLOAT, false, 20, 12)
		});
		t.put_vec3f(-1.0f, -1.0f, 0.0f).put_vec2f(0.0f, 0.0f).endVertex();
		t.put_vec3f( 1.0f, -1.0f, 0.0f).put_vec2f(1.0f, 0.0f).endVertex();
		t.put_vec3f( 1.0f,  1.0f, 0.0f).put_vec2f(1.0f, 1.0f).endVertex();
		t.put_vec3f(-1.0f,  1.0f, 0.0f).put_vec2f(0.0f, 1.0f).endVertex();
		t.put_vec3f(-1.0f, -1.0f, 0.0f).put_vec2f(0.0f, 0.0f).endVertex();
		t.put_vec3f( 1.0f,  1.0f, 0.0f).put_vec2f(1.0f, 1.0f).endVertex();
		t.put_vec3f(-1.0f,  1.0f, 0.0f).put_vec2f(0.0f, 1.0f).endVertex();
		t.uploadVertexes(vbo, true);
		t.destroy();
		
		//load test model =====================================================
		
		try {
			InputStream stream;
			stream = ResourceLoader.loadResource("metaballs/models/testscene.mdl");
			testModel = EaglModelLoader.loadModel(stream);
			stream.close();
		}catch(Throwable tt) {
			GameClient.log.error("Could not load test graphic", tt);
		}
		
		//setup test texture ==================================================
		
		testGraphic = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/icon64.png"));
		testModelTexture = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/textures/longarms_texture.png"));
		
		client.getScene().objectRenderers.add(testModelRenderer = new ModelRenderer(testModel, testModelTexture.glObject).setMaterial(0.0f, 0.0f, 0.5f, 0.5f, 0.0f, 0.0f));
		client.getScene().sunDirection = new Vector3f(1.0f, -1.0f, 0.0f).normalize();
		
		//setup framebuffer ==================================================
		
		//gbuffer render targets
		// 0 - diffuseRGB, ditherBlend
		// 1 - metallic, roughness, specular, ssr
		// 2 - normalXYZ, emission
		// 3 - position

		gBuffer = new EaglFramebuffer(DepthBufferType.DEPTH24_STENCIL8_TEXTURE, GL_RGBA8, GL_RGBA8, GL_RGBA8, GL_RGB16F);
		lightBuffer = new EaglFramebuffer(DepthBufferType.DEPTH24_STENCIL8_RENDERBUFFER, GL_RGB16F, GL_RGB16F);
		
		combinedBuffer = new EaglFramebuffer(DepthBufferType.DEPTH24_STENCIL8_RENDERBUFFER, GL_RGB8);
		
	}
	
	public void renderGame(RenderScene scene) {
		
		int w = client.context.getInnerWidth();
		int h = client.context.getInnerHeight();
		
		gBuffer.setSize(w, h);
		gBuffer.bindFramebuffer();
		
		glViewport(0, 0, w, h);
		
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepthf(0.0f);
		glStencilMask(0xFF);
		glClearStencil(0x00);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		
		glStencilFunc(GL_ALWAYS, 0xFF, 0xFF);
		glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
		
		glEnable(GL_DEPTH_TEST);
		glDisable(GL_STENCIL_TEST);
		glDepthFunc(GL_GREATER);
		
		projMatrix.identity().scale(1.0f, 1.0f, -1.0f).perspective(100.0f * MathUtil.toRadians, (float)w / (float)h, 0.1f, 1024.0f);
		cameraMatrix.identity()
		.rotate(-(client.prevRenderPitch + (client.renderPitch - client.prevRenderPitch) * client.partialTicks) * MathUtil.toRadians, 1.0f, 0.0f, 0.0f)
		.rotate(-(client.prevRenderYaw + (client.renderYaw - client.prevRenderYaw) * client.partialTicks) * MathUtil.toRadians, 0.0f, 1.0f, 0.0f);
		
		cameraMatrix.mulLocal(projMatrix, viewProjMatrix);
		
		modelMatrix.clear();
/*
		Iterator<TerrainRenderer> terrainRenderers = scene.terrainRenderers.iterator();
		while(terrainRenderers.hasNext()) {
			TerrainRenderer r = terrainRenderers.next();
		}
*/
		testModelRenderer.setMaterial(0.0f, 0.0f, 0.4f, 0.2f, 0.0f, 0.0f);
		Iterator<ObjectRenderer> objectRenderers = scene.objectRenderers.iterator();
		while(objectRenderers.hasNext()) {
			ObjectRenderer r = objectRenderers.next();
			r.renderGBuffer(this);
		}

		lightBuffer.setSize(w, h);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer.glObject);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, lightBuffer.glObject);
		glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT, GL_NEAREST);
		lightBuffer.bindFramebuffer();
		
		projMatrix.identity();
		cameraMatrix.identity();
		viewProjMatrix.identity();
		modelMatrix.clear();

		glViewport(0, 0, w, h);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_DEPTH_TEST);

		glEnable(GL_STENCIL_TEST);
		glStencilMask(0xFF);
		glStencilFunc(GL_EQUAL, 0, 0xFF);
		glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
		
		progManager.light_sun.use();
		updateMatrix(progManager.light_sun);
		progManager.light_sun_color.set3f(1.0f, 1.0f, 1.0f);
		
		Vector3f sunDir = new Vector3f(0.5f, 1.0f, 0.0f).normalize();
		//Vector4f lookDir = cameraMatrix.transform(new Vector4f(0.0f, 0.0f, 1.0f, 1.0f)).normalize();
		
		progManager.light_sun_direction.set3f(sunDir.x, sunDir.y, sunDir.z);
		progManager.light_sun_color.set3f(1.0f, 1.0f, 1.0f);
		//progManager.light_sun_lookdirection.set3f(lookDir.x, lookDir.y, lookDir.z);
		
		gBuffer.bindColorTexture(1, 0);
		gBuffer.bindColorTexture(2, 1);
		gBuffer.bindColorTexture(3, 2);
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		Iterator<LightData> lightRenderers = scene.lightRenderers.iterator();
		while(lightRenderers.hasNext()) {
			LightData r = lightRenderers.next();
		}

		combinedBuffer.setSize(w, h);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer.glObject);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, combinedBuffer.glObject);
		glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT, GL_NEAREST);
		combinedBuffer.bindFramebuffer();
		
		glViewport(0, 0, w, h);
		glDisable(GL_DEPTH_TEST);
		
		glEnable(GL_STENCIL_TEST);
		glStencilMask(0x0);
		glStencilFunc(GL_EQUAL, 0, 0xFF);
		glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
		
		progManager.gbuffer_combined.use();
		updateMatrix(progManager.gbuffer_combined);
		gBuffer.bindColorTexture(0, 0);
		gBuffer.bindColorTexture(1, 1);
		gBuffer.bindColorTexture(2, 2);
		gBuffer.bindColorTexture(3, 3);
		lightBuffer.bindColorTexture(0, 4);
		lightBuffer.bindColorTexture(1, 5);
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		GLStateManager.bindFramebuffer(0);

		glViewport(0, 0, w, h);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_STENCIL_TEST);
		glStencilMask(0x0);
		
		// Edge sharpness: 8.0 (sharp, default) - 2.0 (soft)
	    // Edge threshold: 0.125 (softer, def) - 0.25 (sharper)
	    // 0.06 (faster, dark alias), 0.05 (def), 0.04 (slower, less dark alias)
		
		progManager.post_fxaa.use();
		updateMatrix(progManager.post_fxaa);
		progManager.post_fxaa_edgeSharpness.set1f(8.0f);
		progManager.post_fxaa_edgeThreshold.set1f(0.125f);
		progManager.post_fxaa_edgeThresholdMin.set1f(0.04f);
		progManager.post_fxaa_screenSize.set2f(w, h);
		
		combinedBuffer.bindColorTexture(0, 0);
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		++framesPassed;
		if(System.currentTimeMillis() - secondTimer >= 1000l) {
			secondTimer = System.currentTimeMillis();
			prevFramesPassed = framesPassed;
			framesPassed = 0;
			if(client.debugMode) {
				GameClient.log.debug("Framerate: {} ({}ms)", prevFramesPassed, 1000f / prevFramesPassed);
			}
		}
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
	
	public void translateToWorldCoords(double x, double y, double z) {
		modelMatrix.translate(
				(float)(x - (client.prevRenderX + (client.renderX - client.prevRenderX) * client.partialTicks)),
				(float)(y - (client.prevRenderY + (client.renderY - client.prevRenderY) * client.partialTicks)),
				(float)(z - (client.prevRenderZ + (client.renderZ - client.prevRenderZ) * client.partialTicks))
		);
	}
	
	public float toLocalX(double worldX) {
		return (float)(worldX - (client.prevRenderX + (client.renderX - client.prevRenderX) * client.partialTicks));
	}
	
	public float toLocalY(double worldY) {
		return (float)(worldY - (client.prevRenderY + (client.renderY - client.prevRenderY) * client.partialTicks));
	}
	
	public float toLocalZ(double worldZ) {
		return (float)(worldZ - (client.prevRenderZ + (client.renderZ - client.prevRenderZ) * client.partialTicks));
	}
	
	public void destory() {
		quadArray.destroyWithBuffers();
	}

}

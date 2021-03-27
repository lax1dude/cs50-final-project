package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.joml.FrustumIntersection;
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
import net.eagtek.metaballs.client.GameConfiguration;
import net.eagtek.metaballs.client.renderer.LightData.LightType;

public class GlobalRenderer {
	
	public final GameClient client;

	public final Matrix4fStack modelMatrix = new Matrix4fStack(64);
	public final Matrix4fStack cameraMatrix = new Matrix4fStack(4);
	public final Matrix4fStack projMatrix = new Matrix4fStack(4);
	public final Matrix4fStack viewProjMatrix = new Matrix4fStack(4);
	public final Matrix4f multipliedMatrix = new Matrix4f();

	public final Matrix4f sunShadowProjViewA = new Matrix4f();
	public final Matrix4f sunShadowProjViewB = new Matrix4f();
	public final Matrix4f sunShadowProjViewC = new Matrix4f();
	public final Matrix4f sunShadowProjViewD = new Matrix4f();
	
	public final FrustumIntersection viewProjFustrum = new FrustumIntersection();
	
	public final ProgramManager progManager;
	
	private final EaglVertexArray quadArray;
	private final EaglImage2D testGraphic;
	private final EaglImage2D bananaTexture;
	
	private ModelTerrainRenderer testModelRenderer = null;
	private ModelObjectRenderer longArmsRenderer = null;
	private ModelObjectRenderer bananaRenderer = null;
	private ModelObjectRenderer bananaRenderer2 = null;
	
	private final EaglImage2D testModelTexture;

	private EaglVertexArray lightSphere = null;
	private EaglVertexArray lightHemisphere = null;
	private EaglVertexArray lightCone = null;
	
	private ShadowLightRenderer lightTest = null;

	private final EaglFramebuffer gBuffer;
	private final EaglFramebuffer lightBuffer;
	private final EaglFramebuffer combinedBuffer;

	private final EaglFramebuffer sunShadowMapA;
	private final EaglFramebuffer sunShadowMapB;
	private final EaglFramebuffer sunShadowMapC;
	private final EaglFramebuffer sunShadowMapD;

	private final EaglFramebuffer sunShadowBuffer;
	//private final EaglFramebuffer sunShadowBuffer16th;
	//private final EaglFramebuffer sunShadowBlurred;

	private final EaglFramebuffer linearDepthBuffer;
	private final EaglFramebuffer ambientOcclusionBuffer;
	private final EaglFramebuffer ambientOcclusionBlur;
	
	private long secondTimer = 0l;
	private int framesPassed = 0;
	private int prevFramesPassed = 0;

	public double renderPosX;
	public double renderPosY;
	public double renderPosZ;
	
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
		
		//setup test texture ==================================================
		
		testGraphic = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/icon64.png"));
		testModelTexture = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/textures/longarms_texture.png"));
		bananaTexture = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/textures/banana_texture.png"));
		
		//load test model =====================================================
		
		try {
			InputStream stream;

			stream = ResourceLoader.loadResource("metaballs/models/testscene.mdl");
			client.getScene().terrainRenderers.add(testModelRenderer = new ModelTerrainRenderer(EaglModelLoader.loadModel(stream), testModelTexture.glObject));
			stream.close();
			
			stream = ResourceLoader.loadResource("metaballs/models/longarms.mdl");
			client.getScene().objectRenderers.add(longArmsRenderer = new ModelObjectRenderer(EaglModelLoader.loadModel(stream), testModelTexture.glObject));
			stream.close();
			
			stream = ResourceLoader.loadResource("metaballs/models/banana.mdl");
			client.getScene().objectRenderers.add(bananaRenderer = new ModelObjectRenderer(EaglModelLoader.loadModel(stream), bananaTexture.glObject));
			client.getScene().objectRenderers.add(bananaRenderer2 = new ModelObjectRenderer(bananaRenderer.array, bananaTexture.glObject));
			stream.close();
			
			stream = ResourceLoader.loadResource("metaballs/models/lightcone.mdl");
			lightCone = EaglModelLoader.loadModel(stream);
			stream.close();
			
			stream = ResourceLoader.loadResource("metaballs/models/lightsphere.mdl");
			lightSphere = EaglModelLoader.loadModel(stream);
			stream.close();
			
			stream = ResourceLoader.loadResource("metaballs/models/lighthemisphere.mdl");
			lightHemisphere = EaglModelLoader.loadModel(stream);
			stream.close();
			
		}catch(Throwable tt) {
			throw new RuntimeException("Could not load model files required for rendering", tt);
		}
		
		client.getScene().sunDirection = new Vector3f(1.0f, -1.0f, 0.0f).normalize();
		//client.getScene().lightRenderers.add(new LightData(LightType.POINT, 5.0f, 0.0f, 0.0d, 1.0d, 0.0d));
		
		Random r = new Random();
		for(int i = 0 ; i < 35; ++i) {
			client.getScene().lightRenderers.add(new LightData(LightType.POINT, r.nextInt(200), 0.0f, r.nextGaussian() * 20.0d, r.nextGaussian() * 3.0d + 4.0d, r.nextGaussian() * 20.0d).setRGB(r.nextFloat(), r.nextFloat(), r.nextFloat()).setDirection(0.0f, 1.0f, 0.0f));
		}
		
		client.getScene().shadowLightRenderers.add(lightTest = (ShadowLightRenderer) new ShadowLightRenderer(LightType.SPOT, 100.0f, 0.2f, 0.0d, 5.0d, 0.0d).setRGB(1.0f, 1.0f, 1.0f).setDirection(-1.0f, -1.0f, 0.0f).setSpotRadius(20.0f));
		
		//setup framebuffer ==================================================
		
		//gbuffer render targets
		// 0 - diffuseRGB, ditherBlend
		// 1 - metallic, roughness, specular, ssr
		// 2 - normalXYZ, emission
		// 3 - position

		gBuffer = new EaglFramebuffer(DepthBufferType.DEPTH24_STENCIL8_TEXTURE, GL_RGBA8, GL_RGBA8, GL_RGBA8, GL_RGB16F);
		lightBuffer = new EaglFramebuffer(gBuffer.depthBuffer, GL_RGB16F, GL_RGB16F);

		combinedBuffer = new EaglFramebuffer(gBuffer.depthBuffer, GL_RGB8);

		sunShadowMapA = new EaglFramebuffer(DepthBufferType.DEPTH24_TEXTURE);
		sunShadowMapB = new EaglFramebuffer(DepthBufferType.DEPTH24_TEXTURE);
		sunShadowMapC = new EaglFramebuffer(DepthBufferType.DEPTH24_TEXTURE);
		sunShadowMapD = new EaglFramebuffer(DepthBufferType.DEPTH24_TEXTURE);
		
		sunShadowBuffer = new EaglFramebuffer(gBuffer.depthBuffer, GL_R8);
		//sunShadowBuffer16th = new EaglFramebuffer(DepthBufferType.NONE, GL_R8);
		//sunShadowBlurred = new EaglFramebuffer(DepthBufferType.NONE, GL_R8);
		
		linearDepthBuffer = new EaglFramebuffer(DepthBufferType.NONE, GL_R32F);
		ambientOcclusionBuffer = new EaglFramebuffer(DepthBufferType.NONE, GL_R8);
		ambientOcclusionBlur = new EaglFramebuffer(DepthBufferType.NONE, GL_R8);
		
	}

	public static final Vector3f up = new Vector3f(0.0f, 0.0f, 1.0f);
	public static final Vector3f up2 = new Vector3f(0.0f, 1.0f, 0.0f);
	public static final Matrix4f matrixIdentity = new Matrix4f().identity();
	
	public void renderGame(RenderScene scene) {
		renderPosX = client.prevRenderX + (client.renderX - client.prevRenderX) * client.partialTicks;
		renderPosY = client.prevRenderY + (client.renderY - client.prevRenderY) * client.partialTicks;
		renderPosZ = client.prevRenderZ + (client.renderZ - client.prevRenderZ) * client.partialTicks;
		
		Vector3f sd = client.getScene().sunDirection;
		sd.set(0.0f, 1.0f, 0.0f).normalize();
		sd.rotateZ(50.0f * MathUtil.toRadians);
		//sd.rotateZ(((client.totalTicksF * 0.2f) % 360.0f) * MathUtil.toRadians);
		
		int w = client.context.getInnerWidth();
		int h = client.context.getInnerHeight();

		// ================================================= RENDER THE G BUFFER =======================================================
		
		gBuffer.setSize(w, h);
		gBuffer.bindFramebuffer();
		
		glViewport(0, 0, w, h);

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
		glDepthMask(true);
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		
		projMatrix.identity().scale(1.0f, 1.0f, -1.0f).perspective(100.0f * MathUtil.toRadians, (float)w / (float)h, 0.1f, GameConfiguration.farPlane);
		cameraMatrix.identity()
		.rotate(-(client.prevRenderPitch + (client.renderPitch - client.prevRenderPitch) * client.partialTicks) * MathUtil.toRadians, 1.0f, 0.0f, 0.0f)
		.rotate(-(client.prevRenderYaw + (client.renderYaw - client.prevRenderYaw) * client.partialTicks) * MathUtil.toRadians, 0.0f, 1.0f, 0.0f);
		
		cameraMatrix.mulLocal(projMatrix, viewProjMatrix);
		viewProjFustrum.set(viewProjMatrix);
		
		modelMatrix.clear();
		
		testModelRenderer.setMaterial(0.0f, 0.0f, 0.5f, 0.2f, 0.0f, 0.0f);
		
		longArmsRenderer.setMaterial(0.0f, 0.0f, 0.7f, 0.1f, 0.0f, 0.0f);
		longArmsRenderer.setPosition(0.0d, 0.0d, 0.0d).setRotation(0.0f, (client.totalTicksF * 2f) % 360.0f, 0.0f);
		
		bananaRenderer.setMaterial(0.0f, 0.0f, 0.6f, 0.2f, 0.0f, 0.0f);
		bananaRenderer.setPosition(3.0d, 0.2d, -5.0d).setRotation(0.0f, -90.0f, 0.0f);
		
		bananaRenderer2.setMaterial(0.0f, 0.0f, 0.6f, 0.2f, 0.0f, 0.0f);
		bananaRenderer2.setPosition(-22.0d, 4.0d, 13.0d).setRotation(150.0f, -160.0f, -75.0f).setScale(5.0f);
		
		Iterator<TerrainRenderer> terrainRenderers = scene.terrainRenderers.iterator();
		while(terrainRenderers.hasNext()) {
			TerrainRenderer r = terrainRenderers.next();
			if(r.isInFrustum(viewProjFustrum)) r.renderGBuffer(this);
		}
		
		Iterator<ObjectRenderer> objectRenderers = scene.objectRenderers.iterator();
		while(objectRenderers.hasNext()) {
			ObjectRenderer r = objectRenderers.next();
			if(r.isInFrustum(viewProjFustrum)) r.renderGBuffer(this);
		}
		
		glDisable(GL_STENCIL_TEST);
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_GREATER);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_FRONT);
		glDepthMask(true);

		// ================================================= RENDER SUN SHADOW MAPS =======================================================
		
		sunShadowMapA.setSize(GameConfiguration.sunShadowMapResolution, GameConfiguration.sunShadowMapResolution);
		sunShadowMapA.bindFramebuffer();
		glViewport(0, 0, GameConfiguration.sunShadowMapResolution, GameConfiguration.sunShadowMapResolution);

		projMatrix.pushMatrix();
		cameraMatrix.pushMatrix();
		viewProjMatrix.pushMatrix();
		modelMatrix.pushMatrix();
		modelMatrix.identity();
		
		cameraMatrix.identity().translate(0f, 0f, -GameConfiguration.sunShadowDistance).lookAlong(scene.sunDirection.mul(-1.0f, up2), up);
		
		projMatrix.identity().scale(1.0f, 1.0f, -1.0f).ortho(
				-GameConfiguration.sunShadowLODADistance, 
				GameConfiguration.sunShadowLODADistance, 
				-GameConfiguration.sunShadowLODADistance, 
				GameConfiguration.sunShadowLODADistance, 
				0f, 
				GameConfiguration.sunShadowDistance * 2.0f
		);
		cameraMatrix.mulLocal(projMatrix, viewProjMatrix);
		sunShadowProjViewA.set(viewProjMatrix);
		viewProjFustrum.set(viewProjMatrix);
		
		glClearDepthf(0.0f);
		glClear(GL_DEPTH_BUFFER_BIT);

		terrainRenderers = scene.terrainRenderers.iterator();
		while(terrainRenderers.hasNext()) {
			TerrainRenderer r = terrainRenderers.next();
			if(r.isInFrustum(viewProjFustrum)) r.renderShadow(this, 0);
		}
		
		objectRenderers = scene.objectRenderers.iterator();
		while(objectRenderers.hasNext()) {
			ObjectRenderer r = objectRenderers.next();
			if(r.isInFrustum(viewProjFustrum)) r.renderShadow(this);
		}
		
		sunShadowMapB.setSize(GameConfiguration.sunShadowMapResolution, GameConfiguration.sunShadowMapResolution);
		sunShadowMapB.bindFramebuffer();
		glViewport(0, 0, GameConfiguration.sunShadowMapResolution, GameConfiguration.sunShadowMapResolution);
		
		projMatrix.identity().scale(1.0f, 1.0f, -1.0f).ortho(
				-GameConfiguration.sunShadowLODBDistance, 
				GameConfiguration.sunShadowLODBDistance, 
				-GameConfiguration.sunShadowLODBDistance, 
				GameConfiguration.sunShadowLODBDistance, 
				0f, 
				GameConfiguration.sunShadowDistance * 2.0f
		);
		cameraMatrix.mulLocal(projMatrix, viewProjMatrix);
		sunShadowProjViewB.set(viewProjMatrix);
		viewProjFustrum.set(viewProjMatrix);
		
		glClearDepthf(0.0f);
		glClear(GL_DEPTH_BUFFER_BIT);
		
		terrainRenderers = scene.terrainRenderers.iterator();
		while(terrainRenderers.hasNext()) {
			TerrainRenderer r = terrainRenderers.next();
			if(r.isInFrustum(viewProjFustrum)) r.renderShadow(this, 1);
		}
		
		objectRenderers = scene.objectRenderers.iterator();
		while(objectRenderers.hasNext()) {
			ObjectRenderer r = objectRenderers.next();
			if(r.isInFrustum(viewProjFustrum)) r.renderShadow(this);
		}
		
		sunShadowMapC.setSize(GameConfiguration.sunShadowMapResolution, GameConfiguration.sunShadowMapResolution);
		sunShadowMapC.bindFramebuffer();
		glViewport(0, 0, GameConfiguration.sunShadowMapResolution, GameConfiguration.sunShadowMapResolution);
		
		projMatrix.identity().scale(1.0f, 1.0f, -1.0f).ortho(
				-GameConfiguration.sunShadowLODCDistance, 
				GameConfiguration.sunShadowLODCDistance, 
				-GameConfiguration.sunShadowLODCDistance, 
				GameConfiguration.sunShadowLODCDistance, 
				0f, 
				GameConfiguration.sunShadowDistance * 2.0f
		);
		cameraMatrix.mulLocal(projMatrix, viewProjMatrix);
		sunShadowProjViewC.set(viewProjMatrix);
		viewProjFustrum.set(viewProjMatrix);
		
		glClearDepthf(0.0f);
		glClear(GL_DEPTH_BUFFER_BIT);
		
		terrainRenderers = scene.terrainRenderers.iterator();
		while(terrainRenderers.hasNext()) {
			TerrainRenderer r = terrainRenderers.next();
			if(r.isInFrustum(viewProjFustrum)) r.renderShadow(this, 2);
		}
		
		sunShadowMapD.setSize(GameConfiguration.sunShadowMapResolution, GameConfiguration.sunShadowMapResolution);
		sunShadowMapD.bindFramebuffer();
		glViewport(0, 0, GameConfiguration.sunShadowMapResolution, GameConfiguration.sunShadowMapResolution);
		
		projMatrix.identity().scale(1.0f, 1.0f, -1.0f).ortho(
				-GameConfiguration.sunShadowLODDDistance, 
				GameConfiguration.sunShadowLODDDistance, 
				-GameConfiguration.sunShadowLODDDistance, 
				GameConfiguration.sunShadowLODDDistance, 
				0f, 
				GameConfiguration.sunShadowDistance * 2.0f
		);
		cameraMatrix.mulLocal(projMatrix, viewProjMatrix);
		sunShadowProjViewD.set(viewProjMatrix);
		viewProjFustrum.set(viewProjMatrix);
		
		glClearDepthf(0.0f);
		glClear(GL_DEPTH_BUFFER_BIT);
		
		terrainRenderers = scene.terrainRenderers.iterator();
		while(terrainRenderers.hasNext()) {
			TerrainRenderer r = terrainRenderers.next();
			if(r.isInFrustum(viewProjFustrum)) r.renderShadow(this, 3);
		}
		
		// ================================================= RENDER LIGHT SHADOW MAPS =======================================================

		
		lightTest.setDirection(-1.0f, -1.0f, 0.0f).setSpotRadius(30.0f);
		lightTest.pointsize = 30.0f;

		lightTest.lightX = 5.0d;
		lightTest.lightY = 8.0d;
		lightTest.lightZ = 1.0d;
		lightTest.emission = 100.0f;
		
		viewProjMatrix.popMatrix();
		viewProjFustrum.set(viewProjMatrix);
		viewProjMatrix.pushMatrix();
		
		double oldRPX = renderPosX;
		double oldRPY = renderPosY;
		double oldRPZ = renderPosZ;
		
		Iterator<ShadowLightRenderer> shadowLightRenderers = scene.shadowLightRenderers.iterator();
		FrustumIntersection i = new FrustumIntersection();
		while(shadowLightRenderers.hasNext()) {
			ShadowLightRenderer s = shadowLightRenderers.next();
			float x = (float)(s.lightX - oldRPX);
			float y = (float)(s.lightY - oldRPY);
			float z = (float)(s.lightZ - oldRPZ);
			renderPosX = s.lightX;
			renderPosY = s.lightY;
			renderPosZ = s.lightZ;
			float lightRadius = (float)Math.sqrt(s.emission) * 2.0f;
			if(viewProjFustrum.testSphere(x, y, z, lightRadius)) {
				s.objectsInFrustum = new LinkedList();
				cameraMatrix.identity().lookAlong(s.direction, up);
				projMatrix.identity().scale(1.0f, 1.0f, -1.0f).perspective(Math.min((s.type == LightData.LightType.SPOT ? s.spotRadius : 90.0f) * MathUtil.toRadians * 2.5f, (float)Math.PI - 0.001f), 1.0f, 0.1f, lightRadius * 2.0f);
				cameraMatrix.mulLocal(projMatrix, viewProjMatrix);
				s.shadowMatrix.set(viewProjMatrix);
				i.set(viewProjMatrix);
				objectRenderers = scene.objectRenderers.iterator();
				while(objectRenderers.hasNext()) {
					ObjectRenderer r = objectRenderers.next();
					if(r.isInFrustum(i)) {
						s.objectsInFrustum.add(r);
					}
				}
				if(s.objectsInFrustum.size() > 0) {
					s.shadowMap.setSize(GameConfiguration.lightShadowMapResolution, GameConfiguration.lightShadowMapResolution);
					s.shadowMap.bindFramebuffer();
					glClearDepthf(0.0f);
					glClear(GL_DEPTH_BUFFER_BIT);
					glViewport(0, 0, GameConfiguration.lightShadowMapResolution, GameConfiguration.lightShadowMapResolution);
					objectRenderers = s.objectsInFrustum.iterator();
					while(objectRenderers.hasNext()) {
						ObjectRenderer r = objectRenderers.next();
						r.renderShadow(this);
					}
				}
			}
		}
		
		renderPosX = oldRPX;
		renderPosY = oldRPY;
		renderPosZ = oldRPZ;
		
		projMatrix.popMatrix();
		cameraMatrix.popMatrix();
		viewProjMatrix.popMatrix();
		modelMatrix.popMatrix();
		
		viewProjFustrum.set(viewProjMatrix);

		// ================================================= RENDER SUN SHADOW BUFFER =======================================================
		
		sunShadowBuffer.setSize(w, h);
		sunShadowBuffer.bindFramebuffer();
		
		glEnable(GL_STENCIL_TEST);
		glStencilMask(0x0);
		glStencilFunc(GL_NOTEQUAL, 0, 0xFF);
		glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
		
		glViewport(0, 0, w, h);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
		glDepthMask(false);
		
		progManager.sunshadow_generate.use();
		progManager.sunshadow_generate_matrixA.setMatrix4f(sunShadowProjViewA);
		progManager.sunshadow_generate_matrixB.setMatrix4f(sunShadowProjViewB);
		progManager.sunshadow_generate_matrixC.setMatrix4f(sunShadowProjViewC);
		progManager.sunshadow_generate_matrixD.setMatrix4f(sunShadowProjViewD);
		progManager.sunshadow_generate_randTimer.set1f(client.totalTicksF % 100.0f);

		gBuffer.bindColorTexture(3, 0);
		sunShadowMapA.bindDepthTexture(1);
		sunShadowMapB.bindDepthTexture(2);
		sunShadowMapC.bindDepthTexture(3);
		sunShadowMapD.bindDepthTexture(4);
		gBuffer.bindColorTexture(2, 5);
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		glDisable(GL_STENCIL_TEST);

		// ================================================= RENDER LINEAR DEPTH BUFFER =======================================================

		linearDepthBuffer.setSize(w, h);
		linearDepthBuffer.bindFramebuffer();
		
		glViewport(0, 0, w, h);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
		glDepthMask(false);
		
		progManager.linearize_depth.use();
		progManager.linearize_depth_farPlane.set1f(GameConfiguration.farPlane);
		gBuffer.bindDepthTexture();
		quadArray.draw(GL_TRIANGLES, 0, 6);

		// ================================================= RENDER AMBIENT OCCLUSION =======================================================
		
		ambientOcclusionBuffer.setSize(w / 2, h / 2);
		ambientOcclusionBuffer.bindFramebuffer();

		glViewport(0, 0, w / 2, h / 2);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
		glDepthMask(false);
		
		progManager.ssao_generate.use();
		progManager.ssao_generate_randomTime.set1f(client.totalTicksF);
		progManager.ssao_generate_matrix_p_inv.setMatrix4f(projMatrix.invert(multipliedMatrix));
		progManager.ssao_generate_matrix_v_invtrans.setMatrix4f(cameraMatrix.invert(multipliedMatrix).transpose());
		updateMatrix(progManager.ssao_generate);
		gBuffer.bindColorTexture(2, 0);
		gBuffer.bindDepthTexture(1);
		quadArray.draw(GL_TRIANGLES, 0, 6);

		// ================================================= BLUR HORIZONTAL OCCLUSION =======================================================
		
		ambientOcclusionBlur.setSize(w / 2, h / 2);
		ambientOcclusionBlur.bindFramebuffer();

		glViewport(0, 0, w / 2, h / 2);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
		glDepthMask(false);
		
		progManager.ssao_blur.use();
		progManager.ssao_blur_blurDirection.set2f(4.0f / w, 0.0f);
		updateMatrix(progManager.ssao_blur);
		ambientOcclusionBuffer.bindColorTexture(0, 0);
		linearDepthBuffer.bindColorTexture(0, 1);
		quadArray.draw(GL_TRIANGLES, 0, 6);

		// ================================================= BLUR VERTICAL OCCLUSION =======================================================
		
		ambientOcclusionBuffer.setSize(w / 2, h / 2);
		ambientOcclusionBuffer.bindFramebuffer();

		glViewport(0, 0, w / 2, h / 2);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
		glDepthMask(false);
		
		progManager.ssao_blur.use();
		progManager.ssao_blur_blurDirection.set2f(0.0f, 4.0f / h);
		updateMatrix(progManager.ssao_blur);
		ambientOcclusionBlur.bindColorTexture(0, 0);
		linearDepthBuffer.bindColorTexture(0, 1);
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		// ================================================= RENDER LIGHT SOURCE DIFFUSE AND SPECULAR =======================================================
		
		lightBuffer.setSize(w, h);
		lightBuffer.bindFramebuffer();
		
		glViewport(0, 0, w, h);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		
		glEnable(GL_STENCIL_TEST);
		glStencilMask(0x0);
		glStencilFunc(GL_NOTEQUAL, 0, 0xFF);
		glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE);
		
		gBuffer.bindColorTexture(1, 0);
		gBuffer.bindColorTexture(2, 1);
		gBuffer.bindColorTexture(3, 2);

		glEnable(GL_CULL_FACE);
		glCullFace(GL_FRONT);
		
		Iterator<LightData> lightRenderers = scene.lightRenderers.iterator();
		while(lightRenderers.hasNext()) {
			LightData r = lightRenderers.next();
			float x = (float)(r.lightX - renderPosX);
			float y = (float)(r.lightY - renderPosY);
			float z = (float)(r.lightZ - renderPosZ);
			float lightRadius = (float)Math.sqrt(r.emission) * 2.0f;
			if(viewProjFustrum.testSphere(x, y, z, lightRadius)) {
				if(r.type == LightType.POINT) {
					modelMatrix.pushMatrix();
					translateToWorldCoords(r.lightX, r.lightY, r.lightZ);
					modelMatrix.scale(lightRadius);
					progManager.light_point.use();
					progManager.light_point_lightColor.set3f(r.lightR, r.lightG, r.lightB);
					progManager.light_point_lightPosition.set3f(x, y, z);
					progManager.light_point_screenSize.set2f(w, h);
					progManager.light_point_emission.set1f(r.emission);
					progManager.light_point_size.set1f(r.pointsize);
					updateMatrix(progManager.light_point);
					lightSphere.drawAll(GL_TRIANGLES);
					modelMatrix.popMatrix();
				}else if(r.type == LightType.SPOT) {
					modelMatrix.pushMatrix();
					translateToWorldCoords(r.lightX, r.lightY, r.lightZ);
					modelMatrix.rotateTowards(r.direction, up);
					modelMatrix.rotate(-90.0f * MathUtil.toRadians, 1.0f, 0.0f, 0.0f);
					modelMatrix.scale(1.0f, lightRadius, 1.0f);
					float spotRadius2 = (float)Math.sqrt(r.spotRadius * 5.0f);
					modelMatrix.scale(spotRadius2, 1.0f, spotRadius2);
					progManager.light_spot.use();
					progManager.light_spot_lightColor.set3f(r.lightR, r.lightG, r.lightB);
					progManager.light_spot_lightDirection.set3f(r.direction.x, r.direction.y, r.direction.z);
					progManager.light_spot_lightPosition.set3f(x, y, z);
					progManager.light_spot_screenSize.set2f(w, h);
					progManager.light_spot_radius.set1f(r.spotRadius / 180.0f);
					progManager.light_spot_emission.set1f(r.emission);
					progManager.light_spot_size.set1f(r.pointsize);
					updateMatrix(progManager.light_spot);
					lightCone.drawAll(GL_TRIANGLES);
					modelMatrix.popMatrix();
				}
			}
		}
		
		shadowLightRenderers = scene.shadowLightRenderers.iterator();
		while(shadowLightRenderers.hasNext()) {
			ShadowLightRenderer s = shadowLightRenderers.next();
			if(s.objectsInFrustum != null && s.objectsInFrustum.size() > 0) {
				LightData r = s;
				float x = (float)(r.lightX - renderPosX);
				float y = (float)(r.lightY - renderPosY);
				float z = (float)(r.lightZ - renderPosZ);
				float lightRadius = (float)Math.sqrt(r.emission) * 2.0f;
				if(viewProjFustrum.testSphere(x, y, z, lightRadius)) {
					if(r.type == LightType.POINT) {
						modelMatrix.pushMatrix();
						translateToWorldCoords(r.lightX, r.lightY, r.lightZ);
						modelMatrix.scale(lightRadius);
						progManager.light_point_shadowmap.use();
						progManager.light_point_shadowmap_lightColor.set3f(r.lightR, r.lightG, r.lightB);
						progManager.light_point_shadowmap_lightPosition.set3f(x, y, z);
						progManager.light_point_shadowmap_screenSize.set2f(w, h);
						progManager.light_point_shadowmap_emission.set1f(r.emission);
						progManager.light_point_shadowmap_size.set1f(r.pointsize);
						progManager.light_point_shadowmap_shadowMatrix.setMatrix4f(s.shadowMatrix);
						s.shadowMap.bindDepthTexture(3);
						updateMatrix(progManager.light_point_shadowmap);
						lightSphere.drawAll(GL_TRIANGLES);
						modelMatrix.popMatrix();
					}else if(r.type == LightType.SPOT) {
						modelMatrix.pushMatrix();
						translateToWorldCoords(r.lightX, r.lightY, r.lightZ);
						modelMatrix.rotateTowards(r.direction, up);
						modelMatrix.rotate(-90.0f * MathUtil.toRadians, 1.0f, 0.0f, 0.0f);
						modelMatrix.scale(1.0f, lightRadius, 1.0f);
						float spotRadius2 = (float)Math.sqrt(r.spotRadius * 5.0f);
						modelMatrix.scale(spotRadius2, 1.0f, spotRadius2);
						progManager.light_spot_shadowmap.use();
						progManager.light_spot_shadowmap_lightColor.set3f(r.lightR, r.lightG, r.lightB);
						progManager.light_spot_shadowmap_lightDirection.set3f(r.direction.x, r.direction.y, r.direction.z);
						progManager.light_spot_shadowmap_lightPosition.set3f(x, y, z);
						progManager.light_spot_shadowmap_screenSize.set2f(w, h);
						progManager.light_spot_shadowmap_radius.set1f(r.spotRadius / 180.0f);
						progManager.light_spot_shadowmap_emission.set1f(r.emission);
						progManager.light_spot_shadowmap_size.set1f(r.pointsize);
						progManager.light_spot_shadowmap_shadowMatrix.setMatrix4f(s.shadowMatrix);
						s.shadowMap.bindDepthTexture(3);
						updateMatrix(progManager.light_spot_shadowmap);
						lightCone.drawAll(GL_TRIANGLES);
						modelMatrix.popMatrix();
					}
				}
			}else {
				// copy from above ============================================
				LightData r = s;
				float x = (float)(r.lightX - renderPosX);
				float y = (float)(r.lightY - renderPosY);
				float z = (float)(r.lightZ - renderPosZ);
				float lightRadius = (float)Math.sqrt(r.emission) * 2.0f;
				if(viewProjFustrum.testSphere(x, y, z, lightRadius)) {
					if(r.type == LightType.POINT) {
						modelMatrix.pushMatrix();
						translateToWorldCoords(r.lightX, r.lightY, r.lightZ);
						modelMatrix.scale(lightRadius);
						progManager.light_point.use();
						progManager.light_point_lightColor.set3f(r.lightR, r.lightG, r.lightB);
						progManager.light_point_lightPosition.set3f(x, y, z);
						progManager.light_point_screenSize.set2f(w, h);
						progManager.light_point_emission.set1f(r.emission);
						progManager.light_point_size.set1f(r.pointsize);
						updateMatrix(progManager.light_point);
						lightSphere.drawAll(GL_TRIANGLES);
						modelMatrix.popMatrix();
					}else if(r.type == LightType.SPOT) {
						modelMatrix.pushMatrix();
						translateToWorldCoords(r.lightX, r.lightY, r.lightZ);
						modelMatrix.rotateTowards(r.direction, up);
						modelMatrix.rotate(-90.0f * MathUtil.toRadians, 1.0f, 0.0f, 0.0f);
						modelMatrix.scale(1.0f, lightRadius, 1.0f);
						float spotRadius2 = (float)Math.sqrt(r.spotRadius * 5.0f);
						modelMatrix.scale(spotRadius2, 1.0f, spotRadius2);
						progManager.light_spot.use();
						progManager.light_spot_lightColor.set3f(r.lightR, r.lightG, r.lightB);
						progManager.light_spot_lightDirection.set3f(r.direction.x, r.direction.y, r.direction.z);
						progManager.light_spot_lightPosition.set3f(x, y, z);
						progManager.light_spot_screenSize.set2f(w, h);
						progManager.light_spot_radius.set1f(r.spotRadius / 180.0f);
						progManager.light_spot_emission.set1f(r.emission);
						progManager.light_spot_size.set1f(r.pointsize);
						updateMatrix(progManager.light_spot);
						lightCone.drawAll(GL_TRIANGLES);
						modelMatrix.popMatrix();
					}
				}
			}
			s.objectsInFrustum = null;
		}
		
		glDisable(GL_CULL_FACE);
		
		projMatrix.identity();
		cameraMatrix.identity();
		viewProjMatrix.identity();
		modelMatrix.clear();
		
		// ================================================= RENDER SUN DIFFUSE AND SPECULAR =======================================================
		
		sunShadowBuffer.bindColorTexture(0, 3);
		ambientOcclusionBuffer.bindColorTexture(0, 4);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		progManager.light_sun.use();
		updateMatrix(progManager.light_sun);
		progManager.light_sun_color.set3f(1.0f, 1.0f, 1.0f);
		
		Vector3f sunDir = scene.sunDirection;
		//Vector4f lookDir = cameraMatrix.transform(new Vector4f(0.0f, 0.0f, 1.0f, 1.0f)).normalize();
		
		progManager.light_sun_direction.set3f(sunDir.x, sunDir.y, sunDir.z);
		progManager.light_sun_color.set3f(1.0f, 1.0f, 1.0f);
		//progManager.light_sun_lookdirection.set3f(lookDir.x, lookDir.y, lookDir.z);
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		glDisable(GL_BLEND);
		
		glCullFace(GL_BACK);

		// ================================================= COMBINE G BUFFERS =======================================================
		
		combinedBuffer.setSize(w, h);
		combinedBuffer.bindFramebuffer();
		
		glViewport(0, 0, w, h);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		
		progManager.gbuffer_combined.use();
		updateMatrix(progManager.gbuffer_combined);
		gBuffer.bindColorTexture(0, 0);
		gBuffer.bindColorTexture(1, 1);
		gBuffer.bindColorTexture(2, 2);
		gBuffer.bindColorTexture(3, 3);
		lightBuffer.bindColorTexture(0, 4);
		lightBuffer.bindColorTexture(1, 5);
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		// ================================================= RENDER FXAA =======================================================
		
		GLStateManager.bindFramebuffer(0);
		glDepthMask(true);

		glViewport(0, 0, w, h);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_STENCIL_TEST);
		glStencilMask(0x0);
		
		// Edge sharpness: 8.0 (sharp, default) - 2.0 (soft)
	    // Edge threshold: 0.125 (softer, def) - 0.25 (sharper)
	    // 0.06 (faster, dark alias), 0.05 (def), 0.04 (slower, less dark alias)
		
		progManager.post_fxaa.use();
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
				(float)(x - renderPosX),
				(float)(y - renderPosY),
				(float)(z - renderPosZ)
		);
	}
	/*
	public float toLocalX(double worldX) {
		return (float)(worldX - (client.prevRenderX + (client.renderX - client.prevRenderX) * client.partialTicks));
	}
	
	public float toLocalY(double worldY) {
		return (float)(worldY - (client.prevRenderY + (client.renderY - client.prevRenderY) * client.partialTicks));
	}
	
	public float toLocalZ(double worldZ) {
		return (float)(worldZ - (client.prevRenderZ + (client.renderZ - client.prevRenderZ) * client.partialTicks));
	}
	*/
	
	public void destory() {
		this.quadArray.destroyWithBuffers();
		this.combinedBuffer.destroy();
		this.gBuffer.destroy();
		this.lightBuffer.destroy();
		this.testGraphic.destroy();
		this.bananaTexture.destroy();
		this.testModelRenderer.array.destroyWithBuffers();
		this.bananaRenderer.array.destroyWithBuffers();
		this.lightCone.destroyWithBuffers();
		this.lightSphere.destroyWithBuffers();
		this.lightHemisphere.destroyWithBuffers();
		this.testModelTexture.destroy();
		this.sunShadowMapA.destroy();
		this.sunShadowMapB.destroy();
		this.sunShadowMapC.destroy();
		this.sunShadowMapD.destroy();
		this.sunShadowBuffer.destroy();
		this.lightTest.shadowMap.destroy();
		this.linearDepthBuffer.destroy();
		this.ambientOcclusionBuffer.destroy();
		this.ambientOcclusionBlur.destroy();
	}

}

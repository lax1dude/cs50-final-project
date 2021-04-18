package net.lax1dude.cs50_final_project.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import net.lax1dude.cs50_final_project.MathUtil;
import net.lax1dude.cs50_final_project.client.GameClient;
import net.lax1dude.cs50_final_project.client.GameConfiguration;
import net.lax1dude.cs50_final_project.client.renderer.LightData.LightType;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglFramebuffer;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglImage2D;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglModelLoader;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglProgram;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglTessellator;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglVertexArray;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglVertexBuffer;
import net.lax1dude.cs50_final_project.client.renderer.opengl.GLDataType;
import net.lax1dude.cs50_final_project.client.renderer.opengl.GLStateManager;
import net.lax1dude.cs50_final_project.client.renderer.opengl.ResourceLoader;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglFramebuffer.DepthBufferType;

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
	
	public FrustumIntersection viewProjFustrum = new FrustumIntersection();
	
	public final ProgramManager progManager;
	
	public final EaglVertexArray quadArray;
	
	private final EaglImage2D testGraphic;
	private final EaglImage2D bananaTexture;
	//private final EaglImage2D dirtTexture;
	private final EaglImage2D testModelTexture;
	private final EaglImage2D starsTexture;
	private final EaglImage2D moonsTexture;
	private final EaglImage2D brdfLUT;
	
	private ModelObjectRenderer testModelRenderer = null;
	private ModelObjectRenderer longArmsRenderer = null;
	private ModelObjectRenderer bananaRenderer = null;
	private ModelObjectRenderer bananaRenderer2 = null;

	private EaglVertexArray lightSphere = null;
	private EaglVertexArray lightHemisphere = null;
	private EaglVertexArray lightCone = null;

	private EaglVertexArray skyDome = null;
	private EaglVertexArray skyDomeSmall = null;
	private EaglVertexArray testSphere = null;
	private EaglVertexArray testMirror = null;
	
	private ShadowLightRenderer lightTest = null;

	public final EaglFramebuffer gBuffer;
	private final EaglFramebuffer lightBuffer;
	private final EaglFramebuffer combinedBuffer;

	public final EaglFramebuffer sunShadowMap;
	private final EaglFramebuffer sunShadowBuffer;
	
	private final EaglFramebuffer lightShadowMap;

	private final EaglFramebuffer linearDepthBuffer;
	private final EaglFramebuffer ambientOcclusionBuffer;
	private final EaglFramebuffer ambientOcclusionBlur;

	private final EaglFramebuffer postBufferA;
	private final EaglFramebuffer postBufferB;
	private final EaglFramebuffer postBufferC;
	private final EaglFramebuffer exposureCalcTexture;

	private final EaglFramebuffer previousFrame;
	private final EaglFramebuffer ssrBuffer;

	private final EaglFramebuffer waterRefractionTexture;
	private final EaglFramebuffer waterSSRBuffer;
	
	private final EaglFramebuffer opaqueDepthBuffer;

	private final EaglFramebuffer toneMapped;
	
	public final CloudMapGenerator cloudMapGenerator;
	public final CubemapGenerator cubemapGenerator;
	public final RenderLightBulbs lightBulbRenderer;
	public final RenderLensFlares lensFlareRenderer;
	public final UIRenderer uiRenderer;
	
	public final FontFile unicodeTextRenderer;
	
	public float exposure = 2.0f;
	public float targetExposure = 2.0f;
	
	private long secondTimer = 0l;
	private int framesPassed = 0;
	private int prevFramesPassed = 0;
	public int totalFrames = 0;
	public int totalTicks = 0;

	public double renderPosX;
	public double renderPosY;
	public double renderPosZ;

	public final ColorTemperature colorTemperatures;
	
	private final Random rand;
	private float grainStartRandom = 0.0f;
	private float grainEndRandom = 0.0f;
	
	private boolean nextTick = true;
	
	public int displayW;
	public int displayH;
	
	public final int[] queryObjectPool = new int[2048];
	private int queryObject = 0;

	private ArrayList<ObjectRenderer> sortedObjectRenderers = new ArrayList();
	private ArrayList<ObjectRenderer> sortedObjectRenderersWithTransparency = new ArrayList();
	
	public int getQuery() {
		if(queryObject >= queryObjectPool.length) queryObject = 0;
		return queryObjectPool[queryObject];
	}
	
	public int getFramerate() {
		return prevFramesPassed;
	}
	
	public GlobalRenderer(GameClient gameClient) {
		client = gameClient;
		
		uiRenderer = new UIRenderer(this.client);
		
		progManager = new ProgramManager(this);

		for(int i = 0; i < bbVertexes.length; ++i) {
			bbVertexes[i] = new Vector4f();
		}
		
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
		t.uploadVertexes(vbo, true);
		t.destroy();
		
		//setup test texture ==================================================
		
		testGraphic = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/icon64.png"));
		testModelTexture = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/textures/longarms_texture.png"));
		bananaTexture = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/textures/banana_texture.png"));
		//dirtTexture = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/textures/dirt1.jpg"));
		starsTexture = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/textures/stars.jpg")).generateMipmap().filter(GL_NEAREST_MIPMAP_LINEAR, GL_LINEAR);
		moonsTexture = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/textures/moons.jpg")).filter(GL_LINEAR, GL_LINEAR);
		brdfLUT = EaglImage2D.consumeStream(ResourceLoader.loadResource("metaballs/textures/ibl_brdf_lut.png")).filter(GL_LINEAR, GL_LINEAR);
		//dirtTexture.generateMipmap().filter(GL_LINEAR_MIPMAP_NEAREST, GL_LINEAR, EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
		
		
		
		this.rand = new Random("dgfgfgdfgaga".hashCode());
		
		try {
			InputStream stream;

		//load font renderer =================================================
			stream = ResourceLoader.loadResource("metaballs/fonts/unicode.eff");
			unicodeTextRenderer = new FontFile(stream);
			stream.close();
					
		//load test model =====================================================
			
			stream = ResourceLoader.loadResource("metaballs/models/testscene.mdl");
			client.getScene().objectRenderers.add(testModelRenderer = new ModelObjectRenderer(EaglModelLoader.loadModel(stream), testModelTexture.glObject, ModelObjectRenderer.passes_all_opaque, client.getScene()));
			stream.close();
			
			stream = ResourceLoader.loadResource("metaballs/models/longarms.mdl");
			client.getScene().objectRenderers.add(longArmsRenderer = new ModelObjectRenderer(EaglModelLoader.loadModel(stream), testModelTexture.glObject, ModelObjectRenderer.passes_all_opaque, client.getScene()));
			stream.close();
			
			stream = ResourceLoader.loadResource("metaballs/models/banana.mdl");
			client.getScene().objectRenderers.add(bananaRenderer = new ModelObjectRenderer(EaglModelLoader.loadModel(stream), bananaTexture.glObject, ModelObjectRenderer.passes_all_opaque, client.getScene()));
			client.getScene().objectRenderers.add(bananaRenderer2 = new ModelObjectRenderer(bananaRenderer.array, bananaTexture.glObject, ModelObjectRenderer.passes_small_object_opaque, client.getScene()));
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
			
			stream = ResourceLoader.loadResource("metaballs/models/skydome.mdl");
			skyDome = EaglModelLoader.loadModel(stream);
			stream.close();
			
			stream = ResourceLoader.loadResource("metaballs/models/skydome-reflect.mdl");
			skyDomeSmall = EaglModelLoader.loadModel(stream);
			stream.close();
			
			stream = ResourceLoader.loadResource("metaballs/models/sphere.mdl");
			testSphere = EaglModelLoader.loadModel(stream);
			stream.close();
			
			for(int i = 0 ; i < 10; ++i) {
				ModelObjectRenderer m = new ModelObjectRenderer(testSphere, 0, ModelObjectRenderer.passes_all_opaque, client.getScene()).setMaterialAndDiffuse(0.5f, 0.5f, 0.5f, rand.nextBoolean() ? 0.0f : 0.75f, 1.0f, 0.2f, 0.05f, 1.0f, 0.0f);
				m.setPosition(rand.nextFloat() * 40.0d - 20.0d, rand.nextFloat() * 0.5d + 0.5d, rand.nextFloat() * 40.0d - 20.0d);
				client.getScene().objectRenderers.add(m);
			}
			
			stream = ResourceLoader.loadResource("metaballs/models/mirror.mdl");
			testMirror = EaglModelLoader.loadModel(stream);
			stream.close();
			
			client.getScene().objectRenderers.add(new ModelObjectRenderer(testMirror, 0, ModelObjectRenderer.passes_small_object_opaque, client.getScene()).setMaterialAndDiffuse(0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.01f, 1.0f, 1.0f, 0.0f).setPosition(4.0d, 0.1d, 8.0d).setRotation(0.0f, 180.0f, 0.0f));
			client.getScene().objectRenderers.add(new ModelObjectRenderer(longArmsRenderer.array, longArmsRenderer.texture2D, ModelObjectRenderer.passes_small_object_opaque, client.getScene()).setMaterial(0.0f, 0.0f, 0.7f, 0.1f, 0.0f, 0.0f).setPosition(4.0d, 0.15d, 8.0d).setScale(0.3f).setRotation(0.0f, 130.0f, 0.0f));
			
		}catch(Throwable tt) {
			throw new RuntimeException("Could not load model files required for rendering", tt);
		}
		
		//load color temp table =====================================================
		
		colorTemperatures = new ColorTemperature(ResourceLoader.loadResourceBytes("metaballs/temperatures.lut"));
		
		client.getScene().sunDirection = new Vector3f(1.0f, -1.0f, 0.0f).normalize();
		//client.getScene().lightRenderers.add(new LightData(LightType.POINT, 5.0f, 0.0f, 0.0d, 1.0d, 0.0d));
		
		for(int i = 0 ; i < 35; ++i) {
			client.getScene().lightRenderers.add(new LightData(LightType.POINT, rand.nextInt(200), 0.0f, rand.nextGaussian() * 20.0d, rand.nextGaussian() * 1.0d + 2.0d, rand.nextGaussian() * 20.0d, 3.0f, 0.0f).setRGB(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()).setDirection(0.0f, 1.0f, 0.0f));
		}
		
		client.getScene().shadowLightRenderers.add(lightTest = (ShadowLightRenderer) new ShadowLightRenderer(LightType.SPOT, 100.0f, 0.2f, 0.0d, 5.0d, 0.0d, 1.0f, 0.2f).setRGB(1.0f, 1.0f, 1.0f).setDirection(-1.0f, -1.0f, 0.0f).setSpotRadius(20.0f));
		
		//setup framebuffer ==================================================
		
		//gbuffer render targets
		// 0 - diffuseRGB, ditherBlend
		// 1 - metallic, roughness, specular, ssr
		// 2 - normalXYZ, emission
		// 3 - position

		gBuffer = new EaglFramebuffer(DepthBufferType.DEPTH24_STENCIL8_TEXTURE, GL_RGBA8, GL_RGBA8, GL_RGBA8);
		lightBuffer = new EaglFramebuffer(gBuffer.depthBuffer, GL_RGB16F, GL_RGB16F);

		combinedBuffer = new EaglFramebuffer(gBuffer.depthBuffer, GL_RGB16F);

		sunShadowMap = new EaglFramebuffer(DepthBufferType.DEPTH24_TEXTURE);
		sunShadowBuffer = new EaglFramebuffer(gBuffer.depthBuffer, GL_R8);
		
		lightShadowMap = new EaglFramebuffer(DepthBufferType.DEPTH24_TEXTURE);
		
		linearDepthBuffer = new EaglFramebuffer(DepthBufferType.NONE, GL_R32F);
		ambientOcclusionBuffer = new EaglFramebuffer(DepthBufferType.NONE, GL_R8);
		ambientOcclusionBlur = new EaglFramebuffer(DepthBufferType.NONE, GL_R8);

		postBufferA = new EaglFramebuffer(DepthBufferType.NONE, GL_RGB16F);
		postBufferB = new EaglFramebuffer(DepthBufferType.NONE, GL_RGB16F);
		postBufferC = new EaglFramebuffer(DepthBufferType.NONE, GL_RGB16F);

		toneMapped = new EaglFramebuffer(DepthBufferType.NONE, GL_RGB);
		exposureCalcTexture = new EaglFramebuffer(DepthBufferType.NONE, GL_R32F);
		
		previousFrame = new EaglFramebuffer(DepthBufferType.NONE, GL_RGB16F);
		
		cloudMapGenerator = new CloudMapGenerator(this);
		cubemapGenerator = new CubemapGenerator(this);
		lightBulbRenderer = new RenderLightBulbs(this);
		lensFlareRenderer = new RenderLensFlares(this);
		
		opaqueDepthBuffer = new EaglFramebuffer(DepthBufferType.DEPTH24_STENCIL8_TEXTURE);
		ssrBuffer = new EaglFramebuffer(DepthBufferType.NONE, GL_RGBA16F);

		waterRefractionTexture = new EaglFramebuffer(DepthBufferType.NONE, GL_RGBA16F);
		waterSSRBuffer = new EaglFramebuffer(DepthBufferType.DEPTH24_STENCIL8_RENDERBUFFER, GL_RGBA16F);
		
		//glGenQueries(queryObjectPool);
	}

	public static final Vector3f up = new Vector3f(0.0f, 0.0f, 1.0f);
	public static final Vector3f up2 = new Vector3f(0.0f, 1.0f, 0.0f);
	public static final Matrix4f matrixIdentity = new Matrix4f().identity();
	
	private static final float lerp(float a, float b, float f){
	    return a + f * (b - a);
	}
	
	public void renderGame(RenderScene scene) {
		
		renderPosX = client.prevRenderX + (client.renderX - client.prevRenderX) * client.partialTicks;
		renderPosY = client.prevRenderY + (client.renderY - client.prevRenderY) * client.partialTicks;
		renderPosZ = client.prevRenderZ + (client.renderZ - client.prevRenderZ) * client.partialTicks;
		
		Vector3f sd = scene.sunDirection;
		sd.set(0.0f, 1.0f, 0.0f);
		sd.rotateZ((((scene.time + client.partialTicks) * 0.02f) % 360.0f) * MathUtil.toRadians);
		sd.rotateY(23.5f * MathUtil.toRadians);
		
		float timeOfDay = Math.max(sd.y, 0.0f);
		
		sd = scene.moonDirection;
		sd.set(0.0f, -1.0f, 0.0f);
		sd.rotateZ(((scene.time / 24000.0f / 27.0f * 360.0f) % 360.0f) * MathUtil.toRadians);
		sd.rotateY(5.0f * MathUtil.toRadians);
		sd.rotateZ((((scene.time + client.partialTicks) * 0.02f) % 360.0f) * MathUtil.toRadians);
		sd.rotateY(18.5f * MathUtil.toRadians);
		
		scene.skyBrightness = scene.enableSun ? timeOfDay * 2.0f : 0.0f;
		scene.sunBrightness = scene.enableSun ? 200.0f : 0.0f;
		
		scene.sunSize = 0.15f;
		
		scene.sunKelvin = (int) lerp(1500.0f, 4000.0f, Math.min(timeOfDay, 1.0f));
		
		scene.fogKelvin = 6000;
		
		scene.fogDensity = 0.005f;
		
		scene.cubemapSunBrightness = scene.sunDirection.y > 0.0f ? scene.sunBrightness : scene.moonBrightness;
		scene.cubemapSunKelvin = scene.sunDirection.y > 0.0f ? scene.sunKelvin : scene.moonKelvin;
		
		int w = displayW = client.context.getInnerWidth();
		int h = displayH = client.context.getInnerHeight();
		
		// ================================================ SORT OBJECT RENDERERS =====================================================
		
		sortedObjectRenderers.clear();
		sortedObjectRenderersWithTransparency.clear();
		
		Iterator<ObjectRenderer> objectRenderers = scene.objectRenderers.iterator();
		while(objectRenderers.hasNext()) {
			ObjectRenderer r = objectRenderers.next();
			if(r.shouldRenderPass(RenderPass.TRANSPARENT)) {
				sortedObjectRenderersWithTransparency.add(r);
			}else {
				sortedObjectRenderers.add(r);
			}
		}
		
		sortedObjectRenderers.sort(new Comparator<ObjectRenderer>() {
			@Override
			public int compare(ObjectRenderer o1, ObjectRenderer o2) {
				float o1X = (float) (o1.posX - renderPosX);
				float o1Y = (float) (o1.posY - renderPosY);
				float o1Z = (float) (o1.posZ - renderPosZ);
				float o2X = (float) (o2.posX - renderPosX);
				float o2Y = (float) (o2.posY - renderPosY);
				float o2Z = (float) (o2.posZ - renderPosZ);
				return (o1X*o1X + o1Y*o1Y + o1Z*o1Z > o2X*o2X + o2Y*o2Y + o2Z*o2Z) ? 1 : -1;
			}
		});
		
		sortedObjectRenderersWithTransparency.sort(new Comparator<ObjectRenderer>() {
			@Override
			public int compare(ObjectRenderer o1, ObjectRenderer o2) {
				float o1X = (float) (o1.posX - renderPosX);
				float o1Y = (float) (o1.posY - renderPosY);
				float o1Z = (float) (o1.posZ - renderPosZ);
				float o2X = (float) (o2.posX - renderPosX);
				float o2Y = (float) (o2.posY - renderPosY);
				float o2Z = (float) (o2.posZ - renderPosZ);
				return (o1X*o1X + o1Y*o1Y + o1Z*o1Z > o2X*o2X + o2Y*o2Y + o2Z*o2Z) ? -1 : 1;
			}
		});
		
		sortedObjectRenderers.addAll(0, sortedObjectRenderersWithTransparency);
		
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
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		
		projMatrix.identity().scale(1.0f, 1.0f, -1.0f).perspective(100.0f * MathUtil.toRadians, (float)w / (float)h, 0.1f, GameConfiguration.farPlane);
		cameraMatrix.identity()
		.rotate(-(client.prevRenderPitch + (client.renderPitch - client.prevRenderPitch) * client.partialTicks) * MathUtil.toRadians, 1.0f, 0.0f, 0.0f)
		.rotate(-(client.prevRenderYaw + (client.renderYaw - client.prevRenderYaw) * client.partialTicks) * MathUtil.toRadians, 0.0f, 1.0f, 0.0f);
		
		cameraMatrix.mulLocal(projMatrix, viewProjMatrix);
		viewProjFustrum.set(viewProjMatrix);
		
		modelMatrix.clear();
		
		testModelRenderer.setMaterial(0.0f, 1.0f, 0.2f, 0.5f, 0.0f, 0.0f);
		
		longArmsRenderer.setMaterial(0.0f, 0.0f, 0.7f, 0.1f, 0.0f, 0.0f);
		longArmsRenderer.setPosition(0.0d, 0.0d, 0.0d).setRotation(0.0f, (client.totalTicksF * 2f) % 360.0f, 0.0f);
		
		bananaRenderer.setMaterial(0.0f, 0.0f, 0.6f, 0.2f, 0.0f, 0.0f);
		bananaRenderer.setPosition(3.0d, 0.2d, -5.0d).setRotation(0.0f, -90.0f, 0.0f);
		
		bananaRenderer2.setMaterial(0.0f, 0.0f, 0.6f, 0.2f, 0.0f, 0.0f);
		bananaRenderer2.setPosition(-22.0d, 4.0d, 13.0d).setRotation(150.0f, -160.0f, -75.0f).setScale(5.0f);
		
		objectRenderers = sortedObjectRenderers.iterator();
		while(objectRenderers.hasNext()) {
			ObjectRenderer r = objectRenderers.next();
			if(r.shouldRenderPass(RenderPass.G_BUFFER)) {
				r.renderPass(RenderPass.G_BUFFER, this);
			}
		}
		
		glDisable(GL_STENCIL_TEST);
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_GREATER);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_FRONT);
		
		// =================================================== COPY DEPTH BUFFER ==========================================================
		
		opaqueDepthBuffer.setSize(w, h);

		glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer.glObject);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, opaqueDepthBuffer.glObject);
		glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT, GL_NEAREST);
		
		// ================================================= RENDER SUN SHADOW MAPS =======================================================
		
		sunShadowMap.setSize(GameConfiguration.sunShadowMapResolution * 4, GameConfiguration.sunShadowMapResolution);
		sunShadowMap.bindFramebuffer();
		
		glViewport(0, 0, GameConfiguration.sunShadowMapResolution * 4, GameConfiguration.sunShadowMapResolution);
		
		glClearDepthf(0.0f);
		glClear(GL_DEPTH_BUFFER_BIT);
		
		glViewport(0, 0, GameConfiguration.sunShadowMapResolution, GameConfiguration.sunShadowMapResolution);

		projMatrix.pushMatrix();
		cameraMatrix.pushMatrix();
		viewProjMatrix.pushMatrix();
		modelMatrix.pushMatrix();
		modelMatrix.identity();
		
		cameraMatrix.identity().translate(0f, 0f, -GameConfiguration.sunShadowDistance);
		if(scene.sunDirection.y > 0.0f) {
			cameraMatrix.lookAlong(scene.sunDirection.mul(-1.0f, up2), up);
		}else {
			cameraMatrix.lookAlong(scene.moonDirection.mul(-1.0f, up2), up);
		}
		
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

		if(scene.enableSun) {
			objectRenderers = sortedObjectRenderers.iterator();
			while(objectRenderers.hasNext()) {
				ObjectRenderer r = objectRenderers.next();
				if(r.shouldRenderPass(RenderPass.SHADOW_A)) {
					r.renderPass(RenderPass.SHADOW_A, this);
				}
			}
		}
		
		glViewport(GameConfiguration.sunShadowMapResolution * 1, 0, GameConfiguration.sunShadowMapResolution, GameConfiguration.sunShadowMapResolution);
		
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
		
		if(scene.enableSun) {
			objectRenderers = sortedObjectRenderers.iterator();
			while(objectRenderers.hasNext()) {
				ObjectRenderer r = objectRenderers.next();
				if(r.shouldRenderPass(RenderPass.SHADOW_B)) {
					r.renderPass(RenderPass.SHADOW_B, this);
				}
			}
		}
		
		glViewport(GameConfiguration.sunShadowMapResolution * 2, 0, GameConfiguration.sunShadowMapResolution, GameConfiguration.sunShadowMapResolution);
		
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
		
		if(scene.enableSun) {
			objectRenderers = sortedObjectRenderers.iterator();
			while(objectRenderers.hasNext()) {
				ObjectRenderer r = objectRenderers.next();
				if(r.shouldRenderPass(RenderPass.SHADOW_C)) {
					r.renderPass(RenderPass.SHADOW_C, this);
				}
			}
		}
		
		glViewport(GameConfiguration.sunShadowMapResolution * 3, 0, GameConfiguration.sunShadowMapResolution, GameConfiguration.sunShadowMapResolution);
		
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
		
		if(scene.enableSun) {
			objectRenderers = sortedObjectRenderers.iterator();
			while(objectRenderers.hasNext()) {
				ObjectRenderer r = objectRenderers.next();
				if(r.shouldRenderPass(RenderPass.SHADOW_D)) {
					r.renderPass(RenderPass.SHADOW_D, this);
				}
			}
		}
		
		// ================================================= RENDER LIGHT SHADOW MAPS =======================================================

		lightShadowMap.setSize(GameConfiguration.lightShadowMapResolution * 8, GameConfiguration.lightShadowMapResolution * 8);
		lightShadowMap.bindFramebuffer();
		
		int atlasLocation = 0;
		
		lightTest.setDirection(-1.0f, -1.0f, -0.5f).setSpotRadius(50.0f);
		lightTest.pointsize = 30.0f;

		lightTest.lightX = 3.0d;
		lightTest.lightY = 6.0d;
		lightTest.lightZ = 1.0d;
		lightTest.emission = 200.0f;
		
		viewProjMatrix.popMatrix();
		viewProjFustrum.set(viewProjMatrix);
		viewProjMatrix.pushMatrix();
		
		double oldRPX = renderPosX;
		double oldRPY = renderPosY;
		double oldRPZ = renderPosZ;
		
		Iterator<ShadowLightRenderer> shadowLightRenderers = scene.shadowLightRenderers.iterator();
		FrustumIntersection i = new FrustumIntersection();
		FrustumIntersection old = viewProjFustrum;
		while(shadowLightRenderers.hasNext()) {
			ShadowLightRenderer s = shadowLightRenderers.next();
			float x = (float)(s.lightX - oldRPX);
			float y = (float)(s.lightY - oldRPY);
			float z = (float)(s.lightZ - oldRPZ);
			renderPosX = s.lightX;
			renderPosY = s.lightY;
			renderPosZ = s.lightZ;
			float lightRadius = (float)Math.sqrt(s.emission) * 2.0f;
			if(viewProjFustrum.testSphere(x, y, z, lightRadius) && atlasLocation < 64) {
				s.objectsInFrustum = new LinkedList();
				cameraMatrix.identity().lookAlong(s.direction, up);
				projMatrix.identity().scale(1.0f, 1.0f, -1.0f).perspective(Math.min(s.spotRadius * MathUtil.toRadians * 2.25f, 50.0f * MathUtil.toRadians * 2.25f), 1.0f, 0.1f, lightRadius);
				cameraMatrix.mulLocal(projMatrix, viewProjMatrix);
				s.shadowMatrix.set(viewProjMatrix);
				i.set(viewProjMatrix);
				viewProjFustrum = i;
				objectRenderers = scene.objectRenderers.iterator();
				while(objectRenderers.hasNext()) {
					ObjectRenderer r = objectRenderers.next();
					if(r.shouldRenderPass(RenderPass.LIGHT_SHADOW) && r.isInFrustum(this)) {
						s.objectsInFrustum.add(r);
					}
				}
				if(s.objectsInFrustum.size() > 0 && atlasLocation < 64) {
					objectRenderers = s.objectsInFrustum.iterator();
					boolean outOfSync = false;
					if(rand.nextInt(60) == 0) {
						s.objectHashState.clear();
						outOfSync = true;
					}else {
						while(objectRenderers.hasNext()) {
							ObjectRenderer r = objectRenderers.next();
							int ts = s.objectHashState.get(r.uid);
							if(ts != r.trackingState || ts == 0) {
								outOfSync = true;
								break;
							}
						}
					}
					if(outOfSync) {
						s.atlasLocation = atlasLocation++;
						int xx = s.atlasLocation % 8;
						int yy = s.atlasLocation / 8;
						glViewport(xx * GameConfiguration.lightShadowMapResolution, yy * GameConfiguration.lightShadowMapResolution, GameConfiguration.lightShadowMapResolution, GameConfiguration.lightShadowMapResolution);
						
						glEnable(GL_SCISSOR_TEST);
						glScissor(xx * GameConfiguration.lightShadowMapResolution, yy * GameConfiguration.lightShadowMapResolution, GameConfiguration.lightShadowMapResolution, GameConfiguration.lightShadowMapResolution);
						glClearDepthf(0.0f);
						glClear(GL_DEPTH_BUFFER_BIT);
						glDisable(GL_SCISSOR_TEST);
						
						objectRenderers = s.objectsInFrustum.iterator();
						while(objectRenderers.hasNext()) {
							ObjectRenderer r = objectRenderers.next();
							r.renderPass(RenderPass.LIGHT_SHADOW, this);
							s.objectHashState.put(r.uid, r.trackingState);
						}
					}
				}
				viewProjFustrum = old;
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
		if(scene.enableSun) {
			progManager.sunshadow_generate.use();
			progManager.sunshadow_generate_matrixA.setMatrix4f(sunShadowProjViewA);
			progManager.sunshadow_generate_matrixB.setMatrix4f(sunShadowProjViewB);
			progManager.sunshadow_generate_matrixC.setMatrix4f(sunShadowProjViewC);
			progManager.sunshadow_generate_matrixD.setMatrix4f(sunShadowProjViewD);
			progManager.sunshadow_generate_randTimer.set1f(client.totalTicksF % 100.0f);
			progManager.sunshadow_generate_softShadow.set1i(GameConfiguration.enableSoftShadows ? 1 : 0);
			updateMatrix(progManager.sunshadow_generate);
			
			gBuffer.bindColorTexture(2, 0);
			opaqueDepthBuffer.bindDepthTexture(1);
			sunShadowMap.bindDepthTexture(2);
			quadArray.draw(GL_TRIANGLES, 0, 6);
		}
		
		//glDisable(GL_STENCIL_TEST);

		// ============================================= RENDER ENVIRONMENT CUBEMAP ===================================================
		
		cubemapGenerator.redrawCubemap(scene);
		
		// ============================================= RENDER LINEAR DEPTH BUFFER =======================================================

		linearDepthBuffer.setSize(w, h);
		linearDepthBuffer.bindFramebuffer();
		
		glViewport(0, 0, w, h);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
		
		progManager.linearize_depth.use();
		progManager.linearize_depth_farPlane.set1f(GameConfiguration.farPlane);
		opaqueDepthBuffer.bindDepthTexture();
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		if(GameConfiguration.enableAmbientOcclusion) {
			// ================================================= RENDER AMBIENT OCCLUSION =======================================================
			
			ambientOcclusionBuffer.setSize(w / 2, h / 2);
			ambientOcclusionBuffer.bindFramebuffer();
			
			glViewport(0, 0, w / 2, h / 2);
			glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			glClear(GL_COLOR_BUFFER_BIT);
			glDisable(GL_DEPTH_TEST);
			glDisable(GL_CULL_FACE);
			
			progManager.ssao_generate.use();
			progManager.ssao_generate_randomTime.set1f(client.totalTicksF);
			progManager.ssao_generate_matrix_p_inv.setMatrix4f(projMatrix.invert(multipliedMatrix));
			progManager.ssao_generate_matrix_v_invtrans.setMatrix4f(cameraMatrix.invert(multipliedMatrix).transpose());
			updateMatrix(progManager.ssao_generate);
			gBuffer.bindDepthTexture(1);
			gBuffer.bindColorTexture(2, 0);
			quadArray.draw(GL_TRIANGLES, 0, 6);
			
			// ================================================= BLUR HORIZONTAL OCCLUSION =======================================================
			
			ambientOcclusionBlur.setSize(w / 2, h / 2);
			ambientOcclusionBlur.bindFramebuffer();
			
			progManager.ssao_blur.use();
			progManager.ssao_blur_blurDirection.set2f(4.0f / w, 0.0f);
			updateMatrix(progManager.ssao_blur);
			linearDepthBuffer.bindColorTexture(0, 1);
			//gBuffer.bindDepthTexture(1);
			ambientOcclusionBuffer.bindColorTexture(0, 0);
			quadArray.draw(GL_TRIANGLES, 0, 6);
	
			// ================================================= BLUR VERTICAL OCCLUSION =======================================================
			
			ambientOcclusionBuffer.setSize(w / 2, h / 2);
			ambientOcclusionBuffer.bindFramebuffer();
			
			progManager.ssao_blur.use();
			progManager.ssao_blur_blurDirection.set2f(0.0f, 4.0f / h);
			updateMatrix(progManager.ssao_blur);
			ambientOcclusionBlur.bindColorTexture(0, 0);
			linearDepthBuffer.bindColorTexture(0, 1);
			//gBuffer.bindDepthTexture(1);
			quadArray.draw(GL_TRIANGLES, 0, 6);
			
		}else {
			ambientOcclusionBuffer.setSize(w / 2, h / 2);
			ambientOcclusionBuffer.bindFramebuffer();
			glViewport(0, 0, w / 2, h / 2);
			glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
			glClear(GL_COLOR_BUFFER_BIT);
		}
		
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
		opaqueDepthBuffer.bindDepthTexture(2);

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

		lightShadowMap.bindDepthTexture(3);
		
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
						progManager.light_point_shadowmap_shadowMapIndex.set1f(s.atlasLocation);
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
						progManager.light_spot_shadowmap_shadowMapIndex.set1f(s.atlasLocation);
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
		
		// ================================================= RENDER SUN DIFFUSE AND SPECULAR =======================================================
		int kelvin;
		if(scene.enableSun) {
			sunShadowBuffer.bindColorTexture(0, 3);
			progManager.light_sun.use();
			updateMatrix(progManager.light_sun);
			
			if(scene.sunDirection.y > 0.0f) {
				Vector3f sunDir = scene.sunDirection;
				progManager.light_sun_direction.set3f(sunDir.x, sunDir.y, sunDir.z);
				kelvin = scene.sunKelvin;
				progManager.light_sun_color.set3f(colorTemperatures.getLinearR(kelvin) * scene.sunBrightness * 0.1f, colorTemperatures.getLinearG(kelvin) * scene.sunBrightness * 0.1f, colorTemperatures.getLinearB(kelvin) * scene.sunBrightness * 0.1f);
			}else {
				Vector3f sunDir = scene.moonDirection;
				progManager.light_sun_direction.set3f(sunDir.x, sunDir.y, sunDir.z);
				kelvin = scene.moonKelvin;
				progManager.light_sun_color.set3f(colorTemperatures.getLinearR(kelvin) * scene.moonBrightness * 0.1f, colorTemperatures.getLinearG(kelvin) * scene.moonBrightness * 0.1f, colorTemperatures.getLinearB(kelvin) * scene.moonBrightness * 0.1f);
			}
			
			quadArray.draw(GL_TRIANGLES, 0, 6);
		}
		
		glDisable(GL_BLEND);
		glCullFace(GL_BACK);
		
		// =========================================== RENDER SCREEN SPACE REFLECTIONS ==============================================
		
		int div = GameConfiguration.ssrMapDivisor;
		ssrBuffer.setSize(w / div, h / div);
		ssrBuffer.bindFramebuffer();
		
		glViewport(0, 0, w / div, h / div);
		
		if(GameConfiguration.enableSSR) {

			gBuffer.bindColorTexture(1, 0);
			gBuffer.bindColorTexture(2, 1);
			gBuffer.bindDepthTexture(2);
			combinedBuffer.bindColorTexture(0, 3);
			progManager.ssr_generate.use();
			progManager.ssr_generate.getUniform("matrix_v_invtrans").setMatrix4f(cameraMatrix.invert(multipliedMatrix).transpose());
			updateMatrix(progManager.ssr_generate);
			quadArray.draw(GL_TRIANGLES, 0, 6);
			
		}else {
			glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			glClear(GL_COLOR_BUFFER_BIT);
		}

		// ================================================= COMBINE G BUFFERS =======================================================
		
		combinedBuffer.setSize(w, h);
		combinedBuffer.bindFramebuffer();
		
		glViewport(0, 0, w, h);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);

		ambientOcclusionBuffer.bindColorTexture(0, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		cubemapGenerator.bindIrradianceTextureA(0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		cubemapGenerator.bindIrradianceTextureB(0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		ssrBuffer.bindColorTexture(0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		cubemapGenerator.bindSpecularIBLTexture(0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		progManager.gbuffer_combined.use();
		updateMatrix(progManager.gbuffer_combined);
		gBuffer.bindColorTexture(0, 0);
		gBuffer.bindColorTexture(1, 1);
		gBuffer.bindColorTexture(2, 2);
		opaqueDepthBuffer.bindDepthTexture(3);
		lightBuffer.bindColorTexture(0, 4);
		lightBuffer.bindColorTexture(1, 5);
		ambientOcclusionBuffer.bindColorTexture(0, 6);
		cubemapGenerator.bindCubemap(7);
		cubemapGenerator.bindIrradianceTextureA(8);
		cubemapGenerator.bindIrradianceTextureB(9);
		ssrBuffer.bindColorTexture(0, 10);
		cubemapGenerator.bindSpecularIBLTexture(11);
		brdfLUT.bind(12);
		progManager.gbuffer_combined_irradianceMapBlend.set1f(((float)((this.totalTicks + client.partialTicks - 1) % 20.0f)) / 20.0f);
		progManager.gbuffer_combined_enableSSR.set1i(GameConfiguration.enableSSR ? 1 : 0);
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		ambientOcclusionBuffer.bindColorTexture(0, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		cubemapGenerator.bindIrradianceTextureA(0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		cubemapGenerator.bindIrradianceTextureB(0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		ssrBuffer.bindColorTexture(0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		cubemapGenerator.bindSpecularIBLTexture(0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		// =================================================== RENDER SKY =======================================================
		
		this.renderSkyDome(scene, false);
		
		// ================================================ RENDER LIGHT POINTS =======================================================
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE);
		
		glEnable(GL_DEPTH_TEST);
		glDisable(GL_STENCIL_TEST);
		glDepthMask(false);
		
		lightBulbRenderer.renderLightBulbs(scene);
		
		glDepthMask(true);
		glDisable(GL_BLEND);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
		
		if(GameConfiguration.enableSunlightVolumetric && scene.lightShafts && scene.enableSun) {
			
			// ================================================= RENDER LIGHT SHAFT MAP ================================================
			
			ambientOcclusionBuffer.setSize(w / 2, h / 2);
			ambientOcclusionBuffer.bindFramebuffer();
	
			glViewport(0, 0, w / 2, h / 2);
			glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			glClear(GL_COLOR_BUFFER_BIT);
			
			progManager.light_shaft_generate.use();
			progManager.light_shaft_generate_shadowMatrixA.setMatrix4f(sunShadowProjViewA);
			progManager.light_shaft_generate_shadowMatrixB.setMatrix4f(sunShadowProjViewB);
			//progManager.light_shaft_generate_matrix_v_inv.setMatrix4f(cameraMatrix.invert(multipliedMatrix));
			updateMatrix(progManager.light_shaft_generate);
			opaqueDepthBuffer.bindDepthTexture(0);
			sunShadowMap.bindDepthTexture(1);
			quadArray.draw(GL_TRIANGLES, 0, 6);
		}
		
		// ================================================= RENDER FOG OVERLAY =======================================================
		
		combinedBuffer.bindFramebuffer();
		
		glViewport(0, 0, w, h);
		
		glEnable(GL_STENCIL_TEST);
		glStencilMask(0x0);
		glStencilFunc(GL_NOTEQUAL, 0, 0xFF);
		glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		progManager.blend_atmosphere.use();
		progManager.blend_atmosphere_invTextureSize.set2f(2.0f / ((w / 2) * 2), 2.0f / ((h / 2) * 2));
		updateMatrix(progManager.blend_atmosphere);

		kelvin = scene.fogKelvin;
		float fogR = colorTemperatures.getLinearR(kelvin);
		float fogG = colorTemperatures.getLinearG(kelvin);
		float fogB = colorTemperatures.getLinearB(kelvin);
		progManager.blend_atmosphere_fogColor.set3f(fogR * scene.skyBrightness, fogG * scene.skyBrightness, fogB * scene.skyBrightness);
		
		kelvin = scene.sunKelvin;
		fogR = colorTemperatures.getLinearR(kelvin);
		fogG = colorTemperatures.getLinearG(kelvin);
		fogB = colorTemperatures.getLinearB(kelvin);
		progManager.blend_atmosphere_shaftColor.set3f(fogR * scene.skyBrightness, fogG * scene.skyBrightness, fogB * scene.skyBrightness);

		ambientOcclusionBuffer.bindColorTexture(0, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		progManager.blend_atmosphere_enableLightShafts.set1i((GameConfiguration.enableSunlightVolumetric && scene.lightShafts) ? 1 : 0);
		progManager.blend_atmosphere_fogDensity.set1f(scene.fogDensity);
		opaqueDepthBuffer.bindDepthTexture(0);
		ambientOcclusionBuffer.bindColorTexture(0, 1);
		quadArray.draw(GL_TRIANGLES, 0, 6);

		ambientOcclusionBuffer.bindColorTexture(0, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		glDisable(GL_BLEND);
		glDisable(GL_STENCIL_TEST);
		
		projMatrix.pushMatrix().identity();
		cameraMatrix.pushMatrix().identity();
		viewProjMatrix.pushMatrix().identity();
		modelMatrix.pushMatrix().identity();
		
		// ============================================ DITHER BLEND ==================================================
		
		postBufferA.setSize(w, h);
		postBufferA.bindFramebuffer();
		glViewport(0, 0, w, h);
		
		progManager.dither_blend.use();
		progManager.dither_blend_screenSizeInv.set2f(1.0f / w, 1.0f / h);
		combinedBuffer.bindColorTexture(0, 0);
		gBuffer.bindColorTexture(0, 1);
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		// =========================================== STORE FRAME FOR SSR ==================================================

		previousFrame.setSize(w, h);
		
		glBindFramebuffer(GL_READ_FRAMEBUFFER, postBufferA.glObject);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, previousFrame.glObject);
		glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL_COLOR_BUFFER_BIT, GL_NEAREST);
		
		// ============================================ RENDER LENS FLARES ==================================================

		
		combinedBuffer.bindFramebuffer();
		glViewport(0, 0, w, h);
		
		progManager.p3f2f_texture.use();
		updateMatrix(progManager.p3f2f_texture);
		postBufferA.bindColorTexture(0);
		quadArray.draw(GL_TRIANGLES, 0, 6);                 // copy dither-blended framebuffer back to main
		
		projMatrix.popMatrix();
		cameraMatrix.popMatrix();
		viewProjMatrix.popMatrix();
		modelMatrix.popMatrix();
		if(scene.enableSun) {
			lensFlareRenderer.xPixelsInv = 1.0f;
			lensFlareRenderer.yPixelsInv = (float)w / (float)h;
			
			opaqueDepthBuffer.bindDepthTexture(0);
			cloudMapGenerator.bindTextureA(1);
			cloudMapGenerator.bindTextureB(2);
			lensFlareRenderer.processOcclusion(scene);

			glEnable(GL_BLEND);
			glBlendFunc(GL_ONE, GL_ONE);
			
			combinedBuffer.bindFramebuffer();
			glViewport(0, 0, w, h);
			
			lensFlareRenderer.render(scene);
		}

		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE);
		
		lightRenderers = lightBulbRenderer.lensFlaresInFrustum.iterator();
		while(lightRenderers.hasNext()) {
			lensFlareRenderer.renderLightFlare(lightRenderers.next());
		}
		
		glDisable(GL_BLEND);

		projMatrix.identity();
		cameraMatrix.identity();
		viewProjMatrix.identity();
		modelMatrix.clear();
		
		// ================================================= DOWNSCALE =======================================================
		
		postBufferA.setSize(w, h);
		postBufferA.bindFramebuffer();
		
		glViewport(0, 0, w / 8 * 4, h / 8 * 4);
		
		progManager.p3f2f_texture.use();
		//modelMatrix.translate(1.0f / w, 1.0f / h, 0.0f);
		updateMatrix(progManager.p3f2f_texture);
		combinedBuffer.bindColorTexture(0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		quadArray.draw(GL_TRIANGLES, 0, 6);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		
		// ================================================= DOWNSCALE =======================================================
		
		postBufferB.setSize(w, h);
		postBufferB.bindFramebuffer();
		
		glViewport(0, 0, w / 8 * 2, h / 8 * 2);
		
		progManager.p3f2f_texture.use();
		modelMatrix.pushMatrix();
		modelMatrix.translate(1.0f, 1.0f, 0.0f);
		modelMatrix.scale(2.0f);
		updateMatrix(progManager.p3f2f_texture);
		postBufferA.bindColorTexture(0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		quadArray.draw(GL_TRIANGLES, 0, 6);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		modelMatrix.popMatrix();
		
		if(nextTick) {
			// ================================================= DOWNSCALE =======================================================
			
			postBufferC.setSize(w, h);
			postBufferC.bindFramebuffer();
			
			glViewport(0, 0, w / 8, h / 8);
			
			progManager.p3f2f_texture.use();
			modelMatrix.pushMatrix();
			modelMatrix.translate(3.0f, 3.0f, 0.0f);
			modelMatrix.scale(4.0f);
			updateMatrix(progManager.p3f2f_texture);
			postBufferB.bindColorTexture(0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			quadArray.draw(GL_TRIANGLES, 0, 6);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			modelMatrix.popMatrix();
			
			// ========================================= DOWNSCALE TO SINGLE PIXEL ==============================================
			
			exposureCalcTexture.setSize(1, 1);
			exposureCalcTexture.bindFramebuffer();
			
			glViewport(0, 0, 1, 1);
			
			progManager.post_downscale8th.use();
			progManager.post_downscale8th_textureSize.set2f(w, h);
			postBufferC.bindColorTexture(0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			quadArray.draw(GL_TRIANGLES, 0, 6);
			nextTick = false;
		}
		
		if(GameConfiguration.enableBloom) {
			// ========================================= VERT BLOOM ==============================================
			
			postBufferA.setSize(w, h);
			postBufferA.bindFramebuffer();
			glViewport(0, 0, w / 8 * 2, h / 8 * 2);
	
			progManager.post_bloom_h.use();
			progManager.post_bloom_h_screenSizeInv.set2f(0.0f, 1.0f / h);
			progManager.post_bloom_h_exposure.set1f(exposure);
			postBufferB.bindColorTexture(0);
			quadArray.draw(GL_TRIANGLES, 0, 6);
			
			// ========================================= HORZ BLOOM ==============================================
			
			postBufferC.setSize(w, h);
			postBufferC.bindFramebuffer();
			glViewport(0, 0, w / 8 * 2, h / 8 * 2);
			
			progManager.post_bloom.use();
			progManager.post_bloom_screenSizeInv.set2f(1.0f / w, 0.0f);
			progManager.post_bloom_offset.set2f(0.0f, 0.0f);
			progManager.post_bloom_scale.set1f(0.25f);
			postBufferA.bindColorTexture(0);
			quadArray.draw(GL_TRIANGLES, 0, 6);
			
			int blurLoops = w / 2000;
			
			for(int j = 0; j < blurLoops; ++j) {
				// ========================================= RIGHT BLOOM AGAIN ==============================================
				
				postBufferA.setSize(w, h);
				postBufferA.bindFramebuffer();
				glViewport(0, 0, w / 8 * 2, h / 8 * 2);
				
				postBufferC.bindColorTexture(0);
				progManager.post_bloom_screenSizeInv.set2f(1.0f / w, 1.0f / h);
				quadArray.draw(GL_TRIANGLES, 0, 6);
				
				// ========================================= LEFT BLOOM AGAIN ==============================================
				
				postBufferC.setSize(w, h);
				postBufferC.bindFramebuffer();
				glViewport(0, 0, w / 8 * 2, h / 8 * 2);
				
				postBufferA.bindColorTexture(0);
				progManager.post_bloom_screenSizeInv.set2f(-1.0f / w, 1.0f / h);
				quadArray.draw(GL_TRIANGLES, 0, 6);
			}
			
			
			// =========================================== DOWNSCALE ONCE AGAIN =================================================
			postBufferB.bindFramebuffer();
			glViewport(w / 4, 0, w / 8, h / 8);
			
			progManager.p3f2f_texture.use();
			modelMatrix.pushMatrix();
			modelMatrix.translate(3.0f, 3.0f, 0.0f);
			modelMatrix.scale(4.0f);
			updateMatrix(progManager.p3f2f_texture);
			postBufferC.bindColorTexture(0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			quadArray.draw(GL_TRIANGLES, 0, 6);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			modelMatrix.popMatrix();
			
			// ========================================= RIGHT BLOOM AGAIN ==============================================
			postBufferA.bindFramebuffer();
	
			progManager.post_bloom.use();
			progManager.post_bloom_screenSizeInv.set2f(2.0f / w, 2.0f / h);
			progManager.post_bloom_offset.set2f(0.25f, 0.0f);
			progManager.post_bloom_scale.set1f(0.125f);
			postBufferB.bindColorTexture(0);
			quadArray.draw(GL_TRIANGLES, 0, 6);
			
			// ========================================= LEFT BLOOM AGAIN ==============================================
			
			postBufferC.bindFramebuffer();
	
			progManager.post_bloom.use();
			progManager.post_bloom_screenSizeInv.set2f(-2.0f / w, 2.0f / h);
			postBufferA.bindColorTexture(0);
			quadArray.draw(GL_TRIANGLES, 0, 6);
			
			for(int j = 0; j < 3; ++j) {
				// ========================================= RIGHT BLOOM AGAIN ==============================================
				
				postBufferA.bindFramebuffer();
		
				progManager.post_bloom.use();
				progManager.post_bloom_screenSizeInv.set2f(4.0f / w, 4.0f / h);
				postBufferC.bindColorTexture(0);
				quadArray.draw(GL_TRIANGLES, 0, 6);
				
				// ========================================= LEFT BLOOM AGAIN ==============================================
				
				postBufferC.bindFramebuffer();
		
				progManager.post_bloom.use();
				progManager.post_bloom_screenSizeInv.set2f(-4.0f / w, 4.0f / h);
				postBufferA.bindColorTexture(0);
				quadArray.draw(GL_TRIANGLES, 0, 6);
			}
			
		}else {
			postBufferC.setSize(w, h);
			postBufferC.bindFramebuffer();
			glViewport(0, 0, w, h);
			glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			glClear(GL_COLOR_BUFFER_BIT);
		}
		
		// ================================================= BLOOM COMBINE LENS =======================================================

		postBufferA.setSize(w, h);
		postBufferA.bindFramebuffer();

		glViewport(0, 0, w, h);
		
		progManager.bloom_combine_lens.use();
		progManager.bloom_combine_lens_startRandom.set1f(grainStartRandom);
		progManager.bloom_combine_lens_endRandom.set1f(grainEndRandom);
		progManager.bloom_combine_lens_randomTransition.set1f(client.partialTicks);
		
		postBufferC.bindColorTexture(0, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		combinedBuffer.bindColorTexture(0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		postBufferC.bindColorTexture(0, 1);
		
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		postBufferC.bindColorTexture(0, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		combinedBuffer.bindColorTexture(0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		// ================================================= TONEMAP =======================================================

		toneMapped.setSize(w, h);
		toneMapped.bindFramebuffer();

		glViewport(0, 0, w, h);
		
		progManager.post_tonemap.use();
		progManager.post_tonemap_exposure.set1f(exposure);
		postBufferA.bindColorTexture(0);//TODO
		//unicodeTextRenderer.bindTexture();
		//cubemapGenerator.bindIrradianceTextureA(0);
		
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		// ================================================= RENDER FXAA =======================================================
		
		GLStateManager.bindFramebuffer(0);
		//glDepthMask(true);

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
		
		toneMapped.bindColorTexture(0, 0);
		quadArray.draw(GL_TRIANGLES, 0, 6);
		
		int w2 = w;
		int h2 = h;
		while(w2 > 2000) {
			w2 /= 2;
			h2 /= 2;
		}
		renderDebugOverlay(w2, h2);
		
		++framesPassed;
		++totalFrames;
		if(System.currentTimeMillis() - secondTimer >= 1000l) {
			secondTimer = System.currentTimeMillis();
			prevFramesPassed = framesPassed;
			framesPassed = 0;
			if(client.debugMode) {
				GameClient.log.debug("Framerate: {} ({}ms)", prevFramesPassed, 1000f / prevFramesPassed);
			}
		}
	}
	
	public void renderSkyDome(RenderScene scene, boolean lowPolySky) {
		if(scene.enableSun) {
			glEnable(GL_STENCIL_TEST);
			glStencilMask(0x0);
			glStencilFunc(GL_EQUAL, 0, 0xFF);
			glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
			
			glDisable(GL_DEPTH_TEST);
			
			progManager.sky.use();
			updateMatrix(progManager.sky);
			
			int kelvin = scene.sunKelvin;
			float scale = lowPolySky ? 0.0f : 10.0f;
			progManager.sky_sunColor.set3f(colorTemperatures.getLinearR(kelvin) * scene.sunBrightness * scale, colorTemperatures.getLinearG(kelvin) * scene.sunBrightness * scale, colorTemperatures.getLinearB(kelvin) * scene.sunBrightness * scale);
			
			scale = (float)Math.sqrt(Math.max(scene.sunDirection.y, 0.0f) + 0.01f) * 9.0f;
			kelvin = (int) lerp(scene.sunKelvin + 2000, 6000, Math.max(1.0f - scene.sunDirection.y, 0.0f));
			progManager.sky_cloudColor.set3f(colorTemperatures.getLinearR(kelvin) * scene.sunBrightness * scale, colorTemperatures.getLinearG(kelvin) * scene.sunBrightness * scale, colorTemperatures.getLinearB(kelvin) * scene.sunBrightness * scale);
			
			progManager.sky_sunDirection.set3f(scene.sunDirection.x, scene.sunDirection.y, scene.sunDirection.z);
			progManager.sky_sunSize.set1f(scene.sunSize);
			
			float altitude = (float) renderPosY;
			if(altitude > 100000.0f) altitude = 100000.0f;
			if(altitude < -1000.0f) altitude = -1000.0f;
			progManager.sky_altitude.set1f(altitude);
	
			this.cloudMapGenerator.bindTextureB(0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			this.cloudMapGenerator.bindTextureA(0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			this.cloudMapGenerator.bindTextureB(1);
			this.starsTexture.bind(2);
			progManager.sky_cloudTextureBlend.set1f(cloudMapGenerator.blendAmount());
			
			if(lowPolySky) {
				skyDomeSmall.drawAll(GL_TRIANGLES);
			}else {
				skyDome.drawAll(GL_TRIANGLES);
				
				modelMatrix.pushMatrix();
				
				glEnable(GL_BLEND);
				modelMatrix.rotate(180.0f * MathUtil.toRadians, 0.0f, 0.0f, 1.0f);
				modelMatrix.rotateTowards(scene.moonDirection.x, scene.moonDirection.y, scene.moonDirection.z, 0.0f, 0.0f, 1.0f);
				modelMatrix.translate(0.0f, 0.0f, 15.0f);
				
				if(scene.sunDirection.y > -0.1f) {
					
					glBlendFunc(GL_SRC_ALPHA, GL_ONE);
					
					moonsTexture.bind(0);
					progManager.moon_day.use();
					progManager.moon_day_moonColor.set3f(colorTemperatures.getLinearR(scene.moonKelvin) * scene.moonBrightness, colorTemperatures.getLinearG(scene.moonKelvin) * scene.moonBrightness, colorTemperatures.getLinearB(scene.moonKelvin) * scene.moonBrightness);
					int moonFace = ((scene.time / 33750) + 12) % 24;
					progManager.moon_day_moonTexXY.set2f((((moonFace % 6) * 155.0f) / 1024.0f), 1.0f - (((moonFace / 6 + 1) * 155.0f) / 1024.0f));
					updateMatrix(progManager.moon_day);
					quadArray.draw(GL_TRIANGLES, 0, 6);
					
				}else {
					
					glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
					
					moonsTexture.bind(0);
					cloudMapGenerator.bindTextureA(1);
					cloudMapGenerator.bindTextureB(2);
					progManager.moon_night.use();
					progManager.moon_night_moonColor.set3f(colorTemperatures.getLinearR(scene.moonKelvin) * scene.moonBrightness, colorTemperatures.getLinearG(scene.moonKelvin) * scene.moonBrightness, colorTemperatures.getLinearB(scene.moonKelvin) * scene.moonBrightness);
					int moonFace = ((scene.time / 33750) + 12) % 24;
					progManager.moon_night_moonTexXY.set2f((((moonFace % 6) * 155.0f) / 1024.0f), 1.0f - (((moonFace / 6 + 1) * 155.0f) / 1024.0f));
					progManager.moon_night_cloudTextureBlend.set1f(cloudMapGenerator.blendAmount());
					progManager.moon_night_cloudColor.set3f(colorTemperatures.getLinearR(kelvin) * scene.sunBrightness * scale, colorTemperatures.getLinearG(kelvin) * scene.sunBrightness * scale, colorTemperatures.getLinearB(kelvin) * scene.sunBrightness * scale);
					updateMatrix(progManager.moon_night);
					quadArray.draw(GL_TRIANGLES, 0, 6);
					
				}
				
				glDisable(GL_BLEND);
				
				modelMatrix.popMatrix();
			}
			
			glDisable(GL_STENCIL_TEST);
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
		if(prog.matrix_vp_inv != null) {
			prog.matrix_vp_inv.setMatrix4f(viewProjMatrix.invert(multipliedMatrix));
		}
		if(prog.matrix_p_inv != null) {
			prog.matrix_p_inv.setMatrix4f(projMatrix.invert(multipliedMatrix));
		}
		if(prog.matrix_v_inv != null) {
			prog.matrix_v_inv.setMatrix4f(cameraMatrix.invert(multipliedMatrix));
		}
	}
	
	private void renderDebugOverlay(int w, int h) {
		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
		
		
		modelMatrix.pushMatrix();
		modelMatrix.identity();
		modelMatrix.scale(1.0f, -1.0f, 0.0f);
		modelMatrix.translate(-1.0f, -1.0f, 0.0f);
		modelMatrix.scale(2.0f / w, 2.0f / h, 0.0f);
		
		uiRenderer.bindColorShader(unicodeTextRenderer, modelMatrix);
		
		StringBuilder debugInfo = new StringBuilder();
		debugInfo.append("Calder Young's CS50 Final Project, Physically Based Deffered Rendering\n");
		debugInfo.append("Press TAB to release mouse, ESC to exit\n");
		debugInfo.append("Use W,A,S,D Space and Shift to move\n");
		debugInfo.append("Resolution: ");
		debugInfo.append(client.context.getInnerWidth());
		debugInfo.append('x');
		debugInfo.append(client.context.getInnerHeight());
		debugInfo.append(" @ ");
		debugInfo.append(prevFramesPassed);
		debugInfo.append(" fps");
		
		String dbg = debugInfo.toString();
		
		uiRenderer.textBegin();
		uiRenderer.text(dbg, 2, 2, unicodeTextRenderer, 24, 0x777777);
		uiRenderer.text(dbg, 0, 0, unicodeTextRenderer, 24, 0xFFFFFF);
		
		String s = "Unicode Test: \u4F60\u597D\u6211\u53EB\u5361\u723E\u5FB7\u9019\u662F\u6211\u7684\u904A\u6232\u5F15\u64CE";
		uiRenderer.text(s, 2, h - 26 + 2, unicodeTextRenderer, 24, 0x777777);
		uiRenderer.text(s, 0, h - 26, unicodeTextRenderer, 24, 0xFFFFFF);
		uiRenderer.textDraw();
		
		modelMatrix.popMatrix();
		
		glDisable(GL_BLEND);
	}
	
	public void translateToWorldCoords(double x, double y, double z) {
		modelMatrix.translate(
				(float)(x - renderPosX),
				(float)(y - renderPosY),
				(float)(z - renderPosZ)
		);
	}
	
	public void tick() {
		
		grainEndRandom = grainStartRandom;
		grainStartRandom = rand.nextFloat();
		
		cloudMapGenerator.renderCloudMap(GameClient.instance.getScene());
		
		if((totalTicks + 10) % 20 == 0) {
			if(!nextTick) {
				
				exposureCalcTexture.setSize(1, 1);
				exposureCalcTexture.bindFramebuffer();
				
				glViewport(0, 0, 1, 1);
				
				float sceneBrightness = 1.0f;
				
				try(MemoryStack s = MemoryStack.stackPush()) {
					FloatBuffer buf = s.mallocFloat(1);
					exposureCalcTexture.bindColorTexture(0);
					glReadPixels(0, 0, 1, 1, GL_RED, GL_FLOAT, buf);
					sceneBrightness = buf.get(0);
				}
				
				nextTick = true;
				
				sceneBrightness += 0.1f;
				
				targetExposure = 1.0f / sceneBrightness;
			}
		}
		
		if(totalTicks % 20 == 0) {
			cubemapGenerator.updateIrradianceTexture();
		}
		
		if(targetExposure < 0.1f) targetExposure = 0.1f;
		
		exposure += (targetExposure - exposure) * 0.05f;
		
		++totalTicks;
	}
	
	private final Vector4f[] bbVertexes = new Vector4f[8];
	
	public boolean testBBFrustum(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Matrix4f modelMatrix, FrustumIntersection viewProjFustrum2) {

		bbVertexes[0].x = minX;
		bbVertexes[0].y = minY;
		bbVertexes[0].z = minZ;
		bbVertexes[0].w = 1.0f;
		
		bbVertexes[1].x = minX;
		bbVertexes[1].y = minY;
		bbVertexes[1].z = maxZ;
		bbVertexes[1].w = 1.0f;
		
		bbVertexes[2].x = maxX;
		bbVertexes[2].y = minY;
		bbVertexes[2].z = maxZ;
		bbVertexes[2].w = 1.0f;
		
		bbVertexes[3].x = maxX;
		bbVertexes[3].y = minY;
		bbVertexes[3].z = minZ;
		bbVertexes[3].w = 1.0f;
		
		bbVertexes[4].x = minX;
		bbVertexes[4].y = maxY;
		bbVertexes[4].z = minZ;
		bbVertexes[4].w = 1.0f;
		
		bbVertexes[5].x = minX;
		bbVertexes[5].y = maxY;
		bbVertexes[5].z = maxZ;
		bbVertexes[5].w = 1.0f;
		
		bbVertexes[6].x = maxX;
		bbVertexes[6].y = maxY;
		bbVertexes[6].z = maxZ;
		bbVertexes[6].w = 1.0f;
		
		bbVertexes[7].x = maxX;
		bbVertexes[7].y = maxY;
		bbVertexes[7].z = minZ;
		bbVertexes[7].w = 1.0f;
		
		modelMatrix.transform(bbVertexes[0]);
		modelMatrix.transform(bbVertexes[1]);
		modelMatrix.transform(bbVertexes[2]);
		modelMatrix.transform(bbVertexes[3]);
		modelMatrix.transform(bbVertexes[4]);
		modelMatrix.transform(bbVertexes[5]);
		modelMatrix.transform(bbVertexes[6]);
		modelMatrix.transform(bbVertexes[7]);

		float outMinX = 0.0f;
		float outMinY = 0.0f;
		float outMinZ = 0.0f;
		float outMaxX = 0.0f;
		float outMaxY = 0.0f;
		float outMaxZ = 0.0f;

		for(int i = 0; i < 8; ++i) {
			if(bbVertexes[i].x < outMinX || outMinX == 0.0f) outMinX = bbVertexes[i].x;
			if(bbVertexes[i].y < outMinY || outMinY == 0.0f) outMinY = bbVertexes[i].y;
			if(bbVertexes[i].z < outMinZ || outMinZ == 0.0f) outMinZ = bbVertexes[i].z;
			if(bbVertexes[i].x > outMaxX || outMaxX == 0.0f) outMaxX = bbVertexes[i].x;
			if(bbVertexes[i].y > outMaxY || outMaxY == 0.0f) outMaxY = bbVertexes[i].y;
			if(bbVertexes[i].z > outMaxZ || outMaxZ == 0.0f) outMaxZ = bbVertexes[i].z;
		}
		
		//System.out.println("[" + outMinX + ", " + outMinY + ", " + outMinZ + "] [" + outMaxX + ", " + outMaxY + ", " + outMaxZ + "]");
		
		return viewProjFustrum2.testAab(outMinX, outMinY, outMinZ, outMaxX, outMaxY, outMaxZ);
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
	
	public void destroy() {
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
		this.sunShadowMap.destroy();
		this.sunShadowBuffer.destroy();
		this.lightShadowMap.destroy();
		this.ambientOcclusionBuffer.destroy();
		this.ambientOcclusionBlur.destroy();
		this.linearDepthBuffer.destroy();
		this.postBufferA.destroy();
		this.postBufferB.destroy();
		this.postBufferC.destroy();
		this.toneMapped.destroy();
		this.exposureCalcTexture.destroy();
		this.skyDome.destroyWithBuffers();
		this.cloudMapGenerator.destroy();
		this.cubemapGenerator.destroy();
		this.skyDomeSmall.destroyWithBuffers();
		this.testSphere.destroyWithBuffers();
		this.lightBulbRenderer.destroy();
		this.lensFlareRenderer.destroy();
		//glDeleteQueries(queryObjectPool);
		this.starsTexture.destroy();
		this.moonsTexture.destroy();
		this.testMirror.destroy();
		this.previousFrame.destroy();
		this.ssrBuffer.destroy();
		this.brdfLUT.destroy();
		this.uiRenderer.destroy();
		this.waterRefractionTexture.destroy();
		this.waterSSRBuffer.destroy();
	}

}

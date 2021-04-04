package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES31.*;

import net.eagtek.eagl.EaglFramebuffer;
import net.eagtek.eagl.EaglFramebuffer.DepthBufferType;
import net.eagtek.metaballs.MathUtil;
import net.eagtek.metaballs.client.GameConfiguration;

public class CloudMapGenerator {
	
	public final GlobalRenderer renderer;
	public final EaglFramebuffer bufferA;
	public final EaglFramebuffer bufferB;
	public final EaglFramebuffer bufferC;
	
	private int tileID = 0;
	private static final int tilesSubdivisions = 4;
	
	private int bufferSwap = 0;

	private float cloudOffsetX;
	private float cloudOffsetZ;
	
	public CloudMapGenerator(GlobalRenderer renderer) {
		this.renderer = renderer;
		
		glViewport(0, 0, GameConfiguration.cloudMapResolution, GameConfiguration.cloudMapResolution);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		this.bufferA = new EaglFramebuffer(DepthBufferType.NONE, GL_R16F);
		
		bufferA.setSize(GameConfiguration.cloudMapResolution, GameConfiguration.cloudMapResolution);
		bufferA.bindFramebuffer();
		glClear(GL_COLOR_BUFFER_BIT);
		
		this.bufferB = new EaglFramebuffer(DepthBufferType.NONE, GL_R16F);
		
		bufferB.setSize(GameConfiguration.cloudMapResolution, GameConfiguration.cloudMapResolution);
		bufferB.bindFramebuffer();
		glClear(GL_COLOR_BUFFER_BIT);
		
		this.bufferC = new EaglFramebuffer(DepthBufferType.NONE, GL_R16F);
		
		bufferC.setSize(GameConfiguration.cloudMapResolution, GameConfiguration.cloudMapResolution);
		bufferC.bindFramebuffer();
		glClear(GL_COLOR_BUFFER_BIT);

		this.cloudOffsetX = MathUtil.random.nextFloat() * 20.0f;
		this.cloudOffsetZ = MathUtil.random.nextFloat() * 20.0f;
		
	}
	
	public void renderCloudMap(RenderScene scene) {
		EaglFramebuffer buffer = (bufferSwap == 0) ? bufferC : ((bufferSwap == 1) ? bufferA : bufferB);
		
		int size = (GameConfiguration.cloudMapResolution / tilesSubdivisions);

		int tileX = (tileID % tilesSubdivisions) * size;
		int tileY = (tileID / tilesSubdivisions) * size;
		++tileID;
		
		if(tileID > tilesSubdivisions * tilesSubdivisions) {
			cloudOffsetX += scene.windX;
			cloudOffsetZ += scene.windZ;
			tileID = 0;
			++bufferSwap;
			if(bufferSwap > 2) bufferSwap = 0;
		}
		
		buffer.setSize(GameConfiguration.cloudMapResolution, GameConfiguration.cloudMapResolution);
		buffer.bindFramebuffer();
		
		glViewport(0, 0, GameConfiguration.cloudMapResolution, GameConfiguration.cloudMapResolution);
		
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_STENCIL_TEST);
		glDisable(GL_CULL_FACE);
		
		glEnable(GL_SCISSOR_TEST);
		glScissor(tileX, tileY, size, size);
		
		if(scene.cloudDensity > 0.0f) {
			renderer.progManager.clouds_generate.use();
			renderer.progManager.clouds_generate_cloudDensity.set1f(scene.cloudDensity);
	
			float playerX = GameConfiguration.cloudsMoveWithPlayer ? (float)(renderer.renderPosX % 10000.0d) * 0.001f : 0.0f;
			float playerZ = GameConfiguration.cloudsMoveWithPlayer ? (float)(renderer.renderPosZ % 10000.0d) * 0.001f : 0.0f;
			renderer.progManager.clouds_generate_cloudOffset.set2f(cloudOffsetX - playerX, cloudOffsetZ - playerZ);
			
			renderer.progManager.clouds_generate_cloudMorph.set1f(GameConfiguration.cloudsMoveWithPlayer ? (float)(renderer.renderPosY % 10000.0d) * 0.001f : 0.0f);
			renderer.progManager.clouds_generate_sunPosition.set3f(scene.sunDirection.x, scene.sunDirection.y, scene.sunDirection.z);
			
			renderer.quadArray.draw(GL_TRIANGLES, 0, 6);
		}else {
			glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			glClear(GL_COLOR_BUFFER_BIT);
		}
		
		glDisable(GL_SCISSOR_TEST);
		
	}
	
	public void bindTextureA(int unit) {
		((bufferSwap == 0) ? bufferA : ((bufferSwap == 1) ? bufferB : bufferC)).bindColorTexture(0, unit);
	}
	
	public void bindTextureB(int unit) {
		((bufferSwap == 0) ? bufferB : ((bufferSwap == 1) ? bufferC : bufferA)).bindColorTexture(0, unit);
	}
	
	public float blendAmount() {
		return (float) (Math.tan(((tileID / (float)(tilesSubdivisions * tilesSubdivisions)) - 0.5f) * 1.8f) * 0.385f + 0.5f);
	}
	
	public void destroy() {
		this.bufferA.destroy();
		this.bufferB.destroy();
		this.bufferC.destroy();
	}
	
}

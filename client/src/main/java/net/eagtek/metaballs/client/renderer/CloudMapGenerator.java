package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import net.eagtek.eagl.EaglFramebuffer;
import net.eagtek.eagl.EaglFramebuffer.DepthBufferType;
import net.eagtek.metaballs.MathUtil;
import net.eagtek.metaballs.client.GameClient;
import net.eagtek.metaballs.client.GameConfiguration;

public class CloudMapGenerator {
	
	public final GlobalRenderer renderer;
	public final EaglFramebuffer bufferA;
	public final EaglFramebuffer bufferB;
	public final EaglFramebuffer bufferC;
	
	private int tileID = 0;
	private static final int tilesSubdivisions = 4;
	
	private int bufferSwap = 0;
	
	private int cloudOffset;
	
	public CloudMapGenerator(GlobalRenderer renderer) {
		this.renderer = renderer;
		
		glViewport(0, 0, GameConfiguration.cloudMapResolution, GameConfiguration.cloudMapResolution);
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
		
		this.bufferA = new EaglFramebuffer(DepthBufferType.NONE, GL_RG16F);
		
		bufferA.setSize(GameConfiguration.cloudMapResolution, GameConfiguration.cloudMapResolution);
		bufferA.bindFramebuffer();
		glClear(GL_COLOR_BUFFER_BIT);
		
		this.bufferB = new EaglFramebuffer(DepthBufferType.NONE, GL_RG16F);
		
		bufferB.setSize(GameConfiguration.cloudMapResolution, GameConfiguration.cloudMapResolution);
		bufferB.bindFramebuffer();
		glClear(GL_COLOR_BUFFER_BIT);
		
		this.bufferC = new EaglFramebuffer(DepthBufferType.NONE, GL_RG16F);
		
		bufferC.setSize(GameConfiguration.cloudMapResolution, GameConfiguration.cloudMapResolution);
		bufferC.bindFramebuffer();
		glClear(GL_COLOR_BUFFER_BIT);
		
		this.cloudOffset = MathUtil.random.nextInt(100);
		
	}
	
	public void renderCloudMap(RenderScene scene) {
		EaglFramebuffer buffer = (bufferSwap == 0) ? bufferC : ((bufferSwap == 1) ? bufferA : bufferB);
		
		int size = (GameConfiguration.cloudMapResolution / tilesSubdivisions);

		int tileX = (tileID % tilesSubdivisions) * size;
		int tileY = (tileID / tilesSubdivisions) * size;
		++tileID;
		
		if(tileID > tilesSubdivisions * tilesSubdivisions) {
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
		
		renderer.progManager.clouds_generate.use();
		renderer.progManager.clouds_generate_cloudDensity.set1f(0.3f);
		renderer.progManager.clouds_generate_cloudOffset.set2f(renderer.client.totalTicksF / 6400.0f + cloudOffset, renderer.client.totalTicksF / 3200.0f);
		renderer.progManager.clouds_generate_cloudMorph.set1f(renderer.client.totalTicksF / 1000000.0f);
		renderer.progManager.clouds_generate_sunPosition.set3f(scene.sunDirection.x, scene.sunDirection.y, scene.sunDirection.z);
		
		renderer.quadArray.draw(GL_TRIANGLES, 0, 6);
		
		glDisable(GL_SCISSOR_TEST);
		
	}
	
	public void bindTextureA(int unit) {
		((bufferSwap == 0) ? bufferA : ((bufferSwap == 1) ? bufferB : bufferC)).bindColorTexture(0, unit);
	}
	
	public void bindTextureB(int unit) {
		((bufferSwap == 0) ? bufferB : ((bufferSwap == 1) ? bufferC : bufferA)).bindColorTexture(0, unit);
	}
	
	public float blendAmount() {
		return tileID / (float)(tilesSubdivisions * tilesSubdivisions);
	}
	
	public void destroy() {
		this.bufferA.destroy();
		this.bufferB.destroy();
		this.bufferC.destroy();
	}
	
}

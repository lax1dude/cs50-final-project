package net.eagtek.metaballs.client;

//import static org.lwjgl.opengles.GLES30.*;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eagtek.eagl.EaglContext;
import net.eagtek.eagl.ResourceLoader;
import net.eagtek.eagl.EaglContext.ContextPlatform;
import net.eagtek.eagl.EaglContext.KeyboardEvent;
import net.eagtek.eagl.EaglContext.ToolkitPlatform;
import net.eagtek.metaballs.MathUtil;
import net.eagtek.metaballs.client.main.Main;
import net.eagtek.metaballs.client.renderer.GlobalRenderer;

public class GameClient {
	
	public static final Logger log = LoggerFactory.getLogger("GameClient");
	
	public static final GameClient instance = Main.createClientInstance();
	
	public final boolean debugMode;
	private boolean running = false;

	public final EaglContext context;
	
	private GlobalRenderer globalRenderer = null;

	public float partialTicks = 0.0f;
	public float totalTicksF = 0.0f;
	private int totalTicksI = 0;
	
	public GameClient(boolean debug) {
		debugMode = debug;
		log.info("debug mode: {}", debug);
		lastTick = System.nanoTime();
		context = new EaglContext(ToolkitPlatform.desktop, ContextPlatform.vulkan, GameConfiguration.gameName);
	}

	public void run() {
		running = true;
		
		try {
			startGame();
		}catch (Throwable t) {
			log.error("GAME CRASHED! -- During Initialization -- Stack Trace:", t);
			System.exit(-1);
		}
		
		try {
			while(running) {
				runGameLoop();
			}
		}catch(Throwable t) {
			log.error("GAME CRASHED! -- During Main Loop -- Stack Trace:", t);
			cleanup();
			System.exit(-1);
		}
		
		try {
			cleanup();
		}catch(Throwable t) {
			log.error("GAME CRASHED! -- During Shutdown -- Stack Trace:", t);
			System.exit(-1);
		}
	}
	
	public void shutdownGame() {
		running = false;
	}
	
	private void startGame() {
		
		context.create();
		
		try {
			InputStream stream;
			stream = ResourceLoader.loadResource("metaballs/icon64.png");
			BufferedImage icon64 = ImageIO.read(stream);
			stream.close();
			stream = ResourceLoader.loadResource("metaballs/icon32.png");
			BufferedImage icon32 = ImageIO.read(stream);
			stream.close();
			stream = ResourceLoader.loadResource("metaballs/icon16.png");
			BufferedImage icon16 = ImageIO.read(stream);
			stream.close();
			context.setIcons(new BufferedImage[] { icon64, icon32, icon16 });
		}catch(Throwable t) {
			log.error("Could not load icons!", t);
		}
		
		globalRenderer = new GlobalRenderer(this);

		int w = context.getInnerWidth();
		int h = context.getInnerHeight();
		context.setMouseGrabbed(true);
		context.setMousePos(w / 2, h / 2);
	}
	
	private long lastTick;
	
	private void runGameLoop() {
		
		context.pollEvents();
		
		if(context.closeRequested()) {
			shutdownGame();
			return;
		}
		
		long remainingticks = (System.nanoTime() - lastTick) / 50000000l;
		if(remainingticks > 100l) remainingticks = 100l;
		while(remainingticks > 0l) {
			runTick();
			lastTick = System.nanoTime();
			++totalTicksI;
			--remainingticks;
		}
		
		partialTicks = (float)((double)(System.nanoTime() - lastTick) / 50000000D);
		totalTicksF = totalTicksI + partialTicks;
		
		globalRenderer.renderGame();
		
		context.swapBuffers(false);
		
	}

	public float cameraAccelX = 0.0f;
	public float cameraAccelY = 0.0f;
	public float cameraAccelZ = 0.0f;
	
	public float renderPitch = 0.0f;
	public float renderYaw = 0.0f;
	
	public float prevRenderPitch = 0.0f;
	public float prevRenderYaw = 0.0f;
	
	public float cameraPitchAccel = 0.0f;
	public float cameraYawAccel = 0.0f;

	public double renderX = 0.0f;
	public double renderY = 0.0f;
	public double renderZ = 0.0f;
	
	public double prevRenderX = 0.0f;
	public double prevRenderY = 0.0f;
	public double prevRenderZ = 0.0f;
	
	private boolean isMouseGrabbed = true;
	
	private void runTick() {
		
		prevRenderX = renderX;
		prevRenderY = renderY;
		prevRenderZ = renderZ;
		prevRenderPitch = renderPitch;
		prevRenderYaw = renderYaw;
		
		int w = context.getInnerWidth();
		int h = context.getInnerHeight();
		
		KeyboardEvent e;
		while((e = context.keyboardNext()) != null) {
			if(e.pressed && !e.repeated) {
				if(e.code == context.KEY_TAB) {
					isMouseGrabbed = !isMouseGrabbed;
					context.setMouseGrabbed(isMouseGrabbed);
				}else if(e.code == context.KEY_ESCAPE) {
					shutdownGame();
				}
			}
		}
		
		if(isMouseGrabbed) {
			float panspeed = 0.1f;
			int x = context.mouseX();
			int y = context.mouseY();
			cameraYawAccel += ((w / 2) - x) * panspeed;
			cameraPitchAccel += ((h / 2) - y) * panspeed;
			context.setMousePos(w / 2, h / 2);
		}
		
		float movespeed = 0.2f;
		
		if(context.isKeyDown(context.KEY_W)) {
			cameraAccelX += MathUtil.RotationToX(renderYaw) * movespeed;
			cameraAccelZ += MathUtil.RotationToZ(renderYaw) * movespeed;
		}
		if(context.isKeyDown(context.KEY_S)) {
			cameraAccelX += MathUtil.RotationToX(180f + renderYaw) * movespeed;
			cameraAccelZ += MathUtil.RotationToZ(180f + renderYaw) * movespeed;
		}
		if(context.isKeyDown(context.KEY_A)) {
			cameraAccelX += MathUtil.RotationToX(90f + renderYaw) * movespeed;
			cameraAccelZ += MathUtil.RotationToZ(90f + renderYaw) * movespeed;
		}
		if(context.isKeyDown(context.KEY_D)) {
			cameraAccelX += MathUtil.RotationToX(270f + renderYaw) * movespeed;
			cameraAccelZ += MathUtil.RotationToZ(270f + renderYaw) * movespeed;
		}
		if(context.isKeyDown(context.KEY_SPACE)) {
			cameraAccelY += movespeed;
		}
		if(context.isKeyDown(context.KEY_LEFT_SHIFT)) {
			cameraAccelY -= movespeed;
		}
		
		if(renderYaw > 180F && prevRenderYaw > 180F) {
			renderYaw -= 360F;
			prevRenderYaw -= 360F;
		}
		if(renderYaw < -180F && prevRenderYaw < -180F) {
			renderYaw += 360F;
			prevRenderYaw += 360F;
		}
		
		if(renderPitch > 90F) renderPitch = 90F;
		if(renderPitch < -90F) renderPitch = -90F;

		renderX += cameraAccelX;
		renderY += cameraAccelY;
		renderZ += cameraAccelZ;
		
		float decell = 0.7f;
		cameraAccelX *= decell;
		cameraAccelY *= decell;
		cameraAccelZ *= decell;
		
		renderPitch += cameraPitchAccel;
		renderYaw += cameraYawAccel;
		
		float rdecell = 0.05f;
		cameraPitchAccel *= rdecell;
		cameraYawAccel *= rdecell;
		
	}
	
	private void cleanup() {
		globalRenderer.destory();
		context.destroy();
	}
}

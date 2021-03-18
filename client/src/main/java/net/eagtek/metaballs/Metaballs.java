package net.eagtek.metaballs;

import static org.lwjgl.opengles.GLES30.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eagtek.metaballs.ANGLEContext.ContextPlatform;
import net.eagtek.metaballs.ANGLEContext.ToolkitPlatform;

public class Metaballs {
	
	public static final Logger log = LoggerFactory.getLogger("Metaballs");
	
	public static void main(String[] args) {
		log.info("Starting Metaballs Demo...");
		
		ANGLEContext ctx = new ANGLEContext(ToolkitPlatform.desktop, ContextPlatform.vulkan, "Metaballs");
		ctx.create();
		
		while(!ctx.closeRequested()) {
			glClearColor(0.0f, 0.0f, 0.5f, 1.0f);
			glClear(GL_COLOR_BUFFER_BIT);
			ctx.swapBuffers(false);
			ctx.pollEvents();
		}
		
		ctx.destroy();
	}
}

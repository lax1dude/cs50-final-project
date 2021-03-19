package net.eagtek.metaballs;

import static org.lwjgl.opengles.GLES30.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

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
		try {
			ctx.setIcons(new BufferedImage[] {
					ImageIO.read(ResourceLoader.loadResource("icon64.png")),
					ImageIO.read(ResourceLoader.loadResource("icon32.png")),
					ImageIO.read(ResourceLoader.loadResource("icon16.png"))
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(!ctx.closeRequested()) {
			glClearColor(0.0f, 0.0f, 0.5f, 1.0f);
			glClear(GL_COLOR_BUFFER_BIT);
			ctx.swapBuffers(false);
			ctx.pollEvents();
		}
		
		ctx.destroy();
	}
}

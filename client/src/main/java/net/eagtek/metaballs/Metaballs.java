package net.eagtek.metaballs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eagtek.metaballs.ANGLEContext.ContextPlatform;
import net.eagtek.metaballs.ANGLEContext.ToolkitPlatform;

public class Metaballs {
	
	public static final Logger log = LoggerFactory.getLogger("Metaballs");
	
	public static void main(String[] args) {
		log.info("Starting Metaballs Demo...");
		
		ANGLEContext ctx = new ANGLEContext(ToolkitPlatform.desktop, ContextPlatform.d3d11, "Metaballs");
		ctx.create();
		
		Util.sleep(5000l);
		
		ctx.destroy();
	}
}

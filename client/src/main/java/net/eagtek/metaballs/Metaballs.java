package net.eagtek.metaballs;

import static org.lwjgl.opengles.GLES30.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eagtek.eagl.EaglContext;
import net.eagtek.eagl.ResourceLoader;
import net.eagtek.eagl.EaglContext.ContextPlatform;
import net.eagtek.eagl.EaglContext.ToolkitPlatform;
import net.eagtek.eagl.EaglProgram;
import net.eagtek.eagl.EaglShader;
import net.eagtek.eagl.EaglVertexArray;
import net.eagtek.eagl.EaglVertexBuffer;
import net.eagtek.eagl.GLDataType;

public class Metaballs {
	
	public static final Logger log = LoggerFactory.getLogger("Metaballs");
	
	public static void main(String[] args) {
		log.info("Starting Metaballs Demo...");
		
		EaglContext ctx = new EaglContext(ToolkitPlatform.desktop, ContextPlatform.vulkan, "Metaballs");
		ctx.create();
		
		try {
			ctx.setIcons(new BufferedImage[] {
					ImageIO.read(ResourceLoader.loadResource("metaballs/icon64.png")),
					ImageIO.read(ResourceLoader.loadResource("metaballs/icon32.png")),
					ImageIO.read(ResourceLoader.loadResource("metaballs/icon16.png"))
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		float[] verts = new float[] {
				-0.5f, -0.5f, 0.0f,
				 0.5f, -0.5f, 0.0f,
				 0.0f,  0.5f, 0.0f
		};
		
		ByteBuffer b = MemoryUtil.memAlloc(verts.length * 4);
		b.asFloatBuffer().put(verts);
		
		EaglVertexBuffer vbo = new EaglVertexBuffer().upload(b);
		
		EaglVertexArray vao = new EaglVertexArray(
				new EaglVertexBuffer[] { vbo },
				new EaglVertexArray.VertexAttribPointer[] {
						new EaglVertexArray.VertexAttribPointer(0, 0, 3, GLDataType.FLOAT, false, 12, 0)
				}
		);

		EaglShader vsh = new EaglShader(GL_VERTEX_SHADER).compile(ResourceLoader.loadResourceString("metaballs/shaders/test.vsh"), "test.vsh");
		EaglShader fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(ResourceLoader.loadResourceString("metaballs/shaders/test.fsh"), "test.fsh");
		EaglProgram prog = new EaglProgram().compile(vsh, fsh);
		
		prog.use();
		
		ctx.checkError("load");
		
		while(!ctx.closeRequested()) {
			glClearColor(0.0f, 0.0f, 0.5f, 1.0f);
			glClear(GL_COLOR_BUFFER_BIT);
			vao.draw(GL_TRIANGLES, 0, 3);
			ctx.swapBuffers(false);
			ctx.pollEvents();
		}
		
		ctx.destroy();
	}
}

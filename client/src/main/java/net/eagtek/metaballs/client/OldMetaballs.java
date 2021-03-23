package net.eagtek.metaballs.client;

import static org.lwjgl.opengles.GLES30.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eagtek.eagl.EaglContext;
import net.eagtek.eagl.EaglImage2D;
import net.eagtek.eagl.ResourceLoader;
import net.eagtek.eagl.EaglContext.ContextPlatform;
import net.eagtek.eagl.EaglContext.ToolkitPlatform;
import net.eagtek.eagl.EaglIndexBuffer;
import net.eagtek.eagl.EaglProgram;
import net.eagtek.eagl.EaglShader;
import net.eagtek.eagl.EaglTessellator;
import net.eagtek.eagl.EaglVertexArray;
import net.eagtek.eagl.EaglVertexBuffer;
import net.eagtek.eagl.GLDataType;

public class OldMetaballs {
	
	public static final Logger log = LoggerFactory.getLogger("Metaballs");
	
	public static void main(String[] args) throws IOException {
		log.info("Starting Metaballs Demo...");
		
		EaglContext ctx = new EaglContext(ToolkitPlatform.desktop, ContextPlatform.opengl, "Metaballs", 1);
		ctx.create();
		
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
		ctx.setIcons(new BufferedImage[] { icon64, icon32, icon16 });
		
		
		EaglVertexBuffer vbo = new EaglVertexBuffer();
		EaglIndexBuffer ibo = new EaglIndexBuffer(GLDataType.SHORT_U);
		
		EaglVertexArray vao = new EaglVertexArray(
				new EaglVertexBuffer[] { vbo },
				new EaglVertexArray.VertexAttribPointer[] {
						EaglVertexArray.attrib(0, 0, 3, GLDataType.FLOAT, false, 20, 0),
						EaglVertexArray.attrib(0, 1, 2, GLDataType.FLOAT, false, 20, 12)
				},
				ibo
		);
		
		
		EaglTessellator t = new EaglTessellator(12 + 8, 128, 128);

		int a = t.put_vec3f(-0.5f, -0.5f, 0.0f).put_vec2f(0.0f, 0.0f).endVertex();
		int b = t.put_vec3f(0.5f, -0.5f, 0.0f).put_vec2f(1.0f, 0.0f).endVertex();
		int c = t.put_vec3f(0.5f, 0.5f, 0.0f).put_vec2f(1.0f, 1.0f).endVertex();
		int d = t.put_vec3f(-0.5f, 0.5f, 0.0f).put_vec2f(0.0f, 1.0f).endVertex();

		t.addToIndex(a).addToIndex(b).addToIndex(c);
		t.addToIndex(a).addToIndex(c).addToIndex(d);
		
		t.uploadVertexes(vbo, true);
		t.uploadIndexes(ibo, true);
		

		EaglShader vsh = new EaglShader(GL_VERTEX_SHADER).compile(ResourceLoader.loadResourceString("metaballs/shaders/test.vsh"), "test.vsh");
		EaglShader fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(ResourceLoader.loadResourceString("metaballs/shaders/test.fsh"), "test.fsh");
		EaglProgram prog = new EaglProgram().compile(vsh, fsh);
		prog.use();
		
		glUniform1i(prog.getUniformLocation("tex"), 0);
		
		EaglImage2D img = new EaglImage2D().uploadRGBA(icon64);
		img.bind();
		
		while(!ctx.closeRequested()) {
			glViewport(0, 0, ctx.getInnerWidth(), ctx.getInnerHeight());
			
			glClearColor(0.0f, 0.0f, 0.5f, 1.0f);
			glClear(GL_COLOR_BUFFER_BIT);
			
			vao.draw(GL_TRIANGLES, 0, 6);
			
			ctx.swapBuffers(false);
			ctx.pollEvents();
		}
		
		ctx.destroy();
	}
}

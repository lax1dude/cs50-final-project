package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import net.eagtek.eagl.EaglProgram;
import net.eagtek.eagl.EaglShader;
import net.eagtek.eagl.ResourceLoader;

public class ProgramManager {
	
	public final EaglProgram p3f2f_texture;
	
	public final GlobalRenderer renderer;
	
	public ProgramManager(GlobalRenderer renderer) {
		this.renderer = renderer;
		
		String source; EaglShader vsh; EaglShader fsh;
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/p3f2f_texture.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "p3f2f_texture.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "p3f2f_texture.fsh");
		this.p3f2f_texture = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		p3f2f_texture.use();
		glUniform1i(p3f2f_texture.getUniformLocation("tex"), 0);
		
	}

}

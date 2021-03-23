package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES30.*;

import net.eagtek.eagl.EaglProgram;
import net.eagtek.eagl.EaglShader;
import net.eagtek.eagl.EaglUniform;
import net.eagtek.eagl.ResourceLoader;

public class ProgramManager {

	public EaglProgram p3f2f_texture;
	public EaglProgram p3f4b2f_texture;

	public EaglProgram gbuffer_3f_4b_2f_uniform;
	public EaglUniform gbuffer_3f_4b_2f_uniform_ditherBlend;
	public EaglUniform gbuffer_3f_4b_2f_uniform_metallic;
	public EaglUniform gbuffer_3f_4b_2f_uniform_roughness;
	public EaglUniform gbuffer_3f_4b_2f_uniform_specular;
	public EaglUniform gbuffer_3f_4b_2f_uniform_ssr;
	public EaglUniform gbuffer_3f_4b_2f_uniform_emission;

	public EaglProgram gbuffer_combined;

	public EaglProgram light_sun;
	public EaglUniform light_sun_color;
	public EaglUniform light_sun_direction;
	public EaglUniform light_sun_lookdirection;

	public EaglProgram post_fxaa;
	public EaglUniform post_fxaa_screenSize;
	public EaglUniform post_fxaa_edgeSharpness;
	public EaglUniform post_fxaa_edgeThreshold;
	public EaglUniform post_fxaa_edgeThresholdMin;
	
	public final GlobalRenderer renderer;
	
	public void refresh() {
		String source; EaglShader vsh; EaglShader fsh;
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/p3f2f_texture.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "p3f2f_texture.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "p3f2f_texture.fsh");
		this.p3f2f_texture = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		p3f2f_texture.getUniform("tex").set1i(0);
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/p3f4b2f_texture.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "p3f4b2f_texture.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "p3f4b2f_texture.fsh");
		this.p3f4b2f_texture = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		p3f4b2f_texture.getUniform("tex").set1i(0);
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/gbuffer_3f_4b_2f_uniform.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "gbuffer_3f_4b_2f_uniform.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "gbuffer_3f_4b_2f_uniform.fsh");
		this.gbuffer_3f_4b_2f_uniform = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		gbuffer_3f_4b_2f_uniform.getUniform("tex").set1i(0);
		gbuffer_3f_4b_2f_uniform_ditherBlend = gbuffer_3f_4b_2f_uniform.getUniform("ditherBlend");
		gbuffer_3f_4b_2f_uniform_metallic = gbuffer_3f_4b_2f_uniform.getUniform("metallic");
		gbuffer_3f_4b_2f_uniform_roughness = gbuffer_3f_4b_2f_uniform.getUniform("roughness");
		gbuffer_3f_4b_2f_uniform_specular = gbuffer_3f_4b_2f_uniform.getUniform("specular");
		gbuffer_3f_4b_2f_uniform_ssr = gbuffer_3f_4b_2f_uniform.getUniform("ssr");
		gbuffer_3f_4b_2f_uniform_emission = gbuffer_3f_4b_2f_uniform.getUniform("emission");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/gbuffer_combine.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "gbuffer_combine.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "gbuffer_combine.fsh");
		this.gbuffer_combined = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		gbuffer_combined.getUniform("diffuse").set1i(0);
		gbuffer_combined.getUniform("material").set1i(1);
		gbuffer_combined.getUniform("normal").set1i(2);
		gbuffer_combined.getUniform("position").set1i(3);
		gbuffer_combined.getUniform("lightDiffuse").set1i(4);
		gbuffer_combined.getUniform("lightSpecular").set1i(5);
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/light_sun.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "light_sun.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "light_sun.fsh");
		this.light_sun = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		light_sun.getUniform("material").set1i(0);
		light_sun.getUniform("normal").set1i(1);
		light_sun.getUniform("position").set1i(2);

		light_sun_color = light_sun.getUniform("sunRGB");
		light_sun_direction = light_sun.getUniform("sunDirection");
		light_sun_lookdirection = light_sun.getUniform("lookDirection");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/post_fxaa.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "post_fxaa.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "post_fxaa.fsh");
		this.post_fxaa = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		post_fxaa.getUniform("tex").set1i(0);
		
		this.post_fxaa_edgeSharpness = post_fxaa.getUniform("edgeSharpness");
		this.post_fxaa_edgeThreshold = post_fxaa.getUniform("edgeThreshold");
		this.post_fxaa_edgeThresholdMin = post_fxaa.getUniform("edgeThresholdMin");
		this.post_fxaa_screenSize = post_fxaa.getUniform("screenSize");
	}
	
	public ProgramManager(GlobalRenderer renderer) {
		this.renderer = renderer;
		refresh();
	}
	
	public void destroy() {
		p3f2f_texture.destroy();
		p3f4b2f_texture.destroy();
		gbuffer_3f_4b_2f_uniform.destroy();
		gbuffer_combined.destroy();
		light_sun.destroy();
	}

}

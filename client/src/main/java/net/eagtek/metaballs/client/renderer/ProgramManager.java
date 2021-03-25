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
	
	public EaglProgram light_point;
	public EaglUniform light_point_lightPosition;
	public EaglUniform light_point_lightColor;
	public EaglUniform light_point_screenSize;
	public EaglUniform light_point_emission;
	public EaglUniform light_point_size;
	
	public EaglProgram light_spot;
	public EaglUniform light_spot_lightPosition;
	public EaglUniform light_spot_lightDirection;
	public EaglUniform light_spot_lightColor;
	public EaglUniform light_spot_screenSize;
	public EaglUniform light_spot_emission;
	public EaglUniform light_spot_radius;
	public EaglUniform light_spot_size;

	public EaglProgram post_fxaa;
	public EaglUniform post_fxaa_screenSize;
	public EaglUniform post_fxaa_edgeSharpness;
	public EaglUniform post_fxaa_edgeThreshold;
	public EaglUniform post_fxaa_edgeThresholdMin;

	public EaglProgram shadow_3f_4b_2f;

	public EaglProgram sunshadow_generate;
	public EaglUniform sunshadow_generate_matrixA;
	public EaglUniform sunshadow_generate_matrixB;
	public EaglUniform sunshadow_generate_matrixC;
	public EaglUniform sunshadow_generate_matrixD;
	public EaglUniform sunshadow_generate_randTimer;
	
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
		light_sun.getUniform("sunShadow").set1i(3);
		//light_sun.getUniform("sunShadowDownscale").set1i(4);

		light_sun_color = light_sun.getUniform("sunRGB");
		light_sun_direction = light_sun.getUniform("sunDirection");
		light_sun_lookdirection = light_sun.getUniform("lookDirection");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/post_fxaa.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "post_fxaa.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "post_fxaa.fsh");
		this.post_fxaa = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		post_fxaa.getUniform("tex").set1i(0);
		
		post_fxaa_edgeSharpness = post_fxaa.getUniform("edgeSharpness");
		post_fxaa_edgeThreshold = post_fxaa.getUniform("edgeThreshold");
		post_fxaa_edgeThresholdMin = post_fxaa.getUniform("edgeThresholdMin");
		post_fxaa_screenSize = post_fxaa.getUniform("screenSize");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/light_point.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "light_point.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "light_point.fsh");
		this.light_point = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		light_point.getUniform("material").set1i(0);
		light_point.getUniform("normal").set1i(1);
		light_point.getUniform("position").set1i(2);
		
		light_point_lightPosition = light_point.getUniform("lightPosition");
		light_point_lightColor = light_point.getUniform("lightColor");
		light_point_screenSize = light_point.getUniform("screenSize");
		light_point_emission = light_point.getUniform("emission");
		light_point_size = light_point.getUniform("size");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/light_spot.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "light_spot.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "light_spot.fsh");
		this.light_spot = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		light_spot.getUniform("material").set1i(0);
		light_spot.getUniform("normal").set1i(1);
		light_spot.getUniform("position").set1i(2);

		light_spot_lightPosition = light_spot.getUniform("lightPosition");
		light_spot_lightDirection = light_spot.getUniform("lightDirection");
		light_spot_lightColor = light_spot.getUniform("lightColor");
		light_spot_screenSize = light_spot.getUniform("screenSize");
		light_spot_radius = light_spot.getUniform("radiusF");
		light_spot_emission = light_spot.getUniform("emission");
		light_spot_size = light_spot.getUniform("size");

		source = ResourceLoader.loadResourceString("metaballs/shaders/shadow_3f_4b_2f.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "shadow_3f_4b_2f.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "shadow_3f_4b_2f.fsh");
		this.shadow_3f_4b_2f = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/sunshadow_generate.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "sunshadow_generate.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "sunshadow_generate.fsh");
		this.sunshadow_generate = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();

		sunshadow_generate.getUniform("position").set1i(0);
		sunshadow_generate.getUniform("shadowMapA").set1i(1);
		sunshadow_generate.getUniform("shadowMapB").set1i(2);
		sunshadow_generate.getUniform("shadowMapC").set1i(3);
		sunshadow_generate.getUniform("shadowMapD").set1i(4);

		sunshadow_generate_matrixA = sunshadow_generate.getUniform("shadowMatrixA");
		sunshadow_generate_matrixB = sunshadow_generate.getUniform("shadowMatrixB");
		sunshadow_generate_matrixC = sunshadow_generate.getUniform("shadowMatrixC");
		sunshadow_generate_matrixD = sunshadow_generate.getUniform("shadowMatrixD");
		sunshadow_generate_randTimer = sunshadow_generate.getUniform("randTimer");
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
		post_fxaa.destroy();
		light_point.destroy();
		light_spot.destroy();
		sunshadow_generate.destroy();
	}

}

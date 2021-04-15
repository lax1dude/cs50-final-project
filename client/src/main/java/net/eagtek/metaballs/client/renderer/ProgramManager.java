package net.eagtek.metaballs.client.renderer;

import static org.lwjgl.opengles.GLES31.*;

import java.util.Random;

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

	public EaglProgram gbuffer_3f_4b_uniform;
	public EaglUniform gbuffer_3f_4b_uniform_ditherBlend;
	public EaglUniform gbuffer_3f_4b_uniform_metallic;
	public EaglUniform gbuffer_3f_4b_uniform_roughness;
	public EaglUniform gbuffer_3f_4b_uniform_specular;
	public EaglUniform gbuffer_3f_4b_uniform_ssr;
	public EaglUniform gbuffer_3f_4b_uniform_emission;
	public EaglUniform gbuffer_3f_4b_uniform_diffuseColor;

	public EaglProgram gbuffer_combined;
	public EaglUniform gbuffer_combined_irradianceMapBlend;
	public EaglUniform gbuffer_combined_enableSSR;

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
	public EaglUniform sunshadow_generate_softShadow;
	
	public EaglProgram light_point_shadowmap;
	public EaglUniform light_point_shadowmap_lightPosition;
	public EaglUniform light_point_shadowmap_lightColor;
	public EaglUniform light_point_shadowmap_screenSize;
	public EaglUniform light_point_shadowmap_emission;
	public EaglUniform light_point_shadowmap_size;
	public EaglUniform light_point_shadowmap_shadowMatrix;
	public EaglUniform light_point_shadowmap_shadowMapIndex;
	
	public EaglProgram light_spot_shadowmap;
	public EaglUniform light_spot_shadowmap_lightPosition;
	public EaglUniform light_spot_shadowmap_lightDirection;
	public EaglUniform light_spot_shadowmap_lightColor;
	public EaglUniform light_spot_shadowmap_screenSize;
	public EaglUniform light_spot_shadowmap_emission;
	public EaglUniform light_spot_shadowmap_radius;
	public EaglUniform light_spot_shadowmap_size;
	public EaglUniform light_spot_shadowmap_shadowMatrix;
	public EaglUniform light_spot_shadowmap_shadowMapIndex;

	public EaglProgram linearize_depth;
	public EaglUniform linearize_depth_farPlane;
	
	public EaglProgram ssao_generate;
	public EaglUniform ssao_generate_randomTime;
	public EaglUniform ssao_generate_matrix_v_invtrans;
	public EaglUniform ssao_generate_matrix_p_inv;
	
	public EaglProgram ssao_blur;
	public EaglUniform ssao_blur_blurDirection;

	public EaglProgram post_tonemap;
	public EaglUniform post_tonemap_exposure;
	
	public EaglProgram post_downscale8th;
	public EaglUniform post_downscale8th_textureSize;
	
	public final GlobalRenderer renderer;
	
	public EaglProgram post_bloom_h;
	public EaglProgram post_bloom;
	public EaglUniform post_bloom_h_screenSizeInv;
	public EaglUniform post_bloom_h_exposure;
	public EaglUniform post_bloom_screenSizeInv;
	public EaglUniform post_bloom_offset;
	public EaglUniform post_bloom_scale;
	
	public EaglProgram bloom_combine_lens;
	public EaglUniform bloom_combine_lens_startRandom;
	public EaglUniform bloom_combine_lens_endRandom;
	public EaglUniform bloom_combine_lens_randomTransition;
	
	public EaglProgram sky;
	public EaglUniform sky_sunColor;
	public EaglUniform sky_cloudColor;
	public EaglUniform sky_sunDirection;
	public EaglUniform sky_sunSize;
	public EaglUniform sky_altitude;
	public EaglUniform sky_cloudTextureBlend;
	
	public EaglProgram light_shaft_generate;
	public EaglUniform light_shaft_generate_shadowMatrixA;
	public EaglUniform light_shaft_generate_shadowMatrixB;
	public EaglUniform light_shaft_generate_matrix_v_inv;
	
	public EaglProgram blend_atmosphere;
	public EaglUniform blend_atmosphere_invTextureSize;
	public EaglUniform blend_atmosphere_fogColor;
	public EaglUniform blend_atmosphere_enableLightShafts;
	public EaglUniform blend_atmosphere_fogDensity;
	public EaglUniform blend_atmosphere_shaftColor;
	
	public EaglProgram position_to_view;
	public EaglProgram add_positions;
	
	public EaglProgram clouds_generate;
	public EaglUniform clouds_generate_cloudDensity;
	public EaglUniform clouds_generate_cloudOffset;
	public EaglUniform clouds_generate_cloudMorph;
	public EaglUniform clouds_generate_sunPosition;
	
	public EaglProgram cubemap_3f_4b_2f_uniform;
	public EaglUniform cubemap_3f_4b_2f_uniform_metallic;
	public EaglUniform cubemap_3f_4b_2f_uniform_roughness;
	public EaglUniform cubemap_3f_4b_2f_uniform_specular;
	public EaglUniform cubemap_3f_4b_2f_uniform_emission;
	public EaglUniform cubemap_3f_4b_2f_uniform_shadowMatrixA;
	public EaglUniform cubemap_3f_4b_2f_uniform_shadowMatrixB;
	public EaglUniform cubemap_3f_4b_2f_uniform_sunDirection;
	public EaglUniform cubemap_3f_4b_2f_uniform_sunRGB;
	
	public EaglProgram cubemap_3f_4b_uniform;
	public EaglUniform cubemap_3f_4b_uniform_metallic;
	public EaglUniform cubemap_3f_4b_uniform_roughness;
	public EaglUniform cubemap_3f_4b_uniform_specular;
	public EaglUniform cubemap_3f_4b_uniform_emission;
	public EaglUniform cubemap_3f_4b_uniform_shadowMatrixA;
	public EaglUniform cubemap_3f_4b_uniform_shadowMatrixB;
	public EaglUniform cubemap_3f_4b_uniform_sunDirection;
	public EaglUniform cubemap_3f_4b_uniform_sunRGB;
	public EaglUniform cubemap_3f_4b_uniform_diffuseColor;
	
	public EaglProgram irradiance_map_generate;
	
	public EaglProgram light_bulb_renderer;
	public EaglUniform light_bulb_renderer_aspectRatio;
	
	public EaglProgram lens_flare;
	public EaglUniform lens_flare_intensity;
	
	public EaglProgram lens_flare_occlusion;
	public EaglUniform lens_flare_occlusion_sunDirection;
	public EaglUniform lens_flare_occlusion_matrix_vp;
	public EaglUniform lens_flare_occlusion_cloudTextureBlend;
	
	public EaglProgram lens_flare_single;
	public EaglUniform lens_flare_single_position;
	public EaglUniform lens_flare_single_size;
	public EaglUniform lens_flare_single_color;
	public EaglUniform lens_flare_single_flareTextureSelection;
	
	public EaglProgram lens_flare_single_occlusion;
	public EaglUniform lens_flare_single_occlusion_position;
	public EaglUniform lens_flare_single_occlusion_size;
	
	public EaglProgram moon_day;
	public EaglUniform moon_day_moonColor;
	public EaglUniform moon_day_moonTexXY;
	
	public EaglProgram moon_night;
	public EaglUniform moon_night_moonColor;
	public EaglUniform moon_night_moonTexXY;
	public EaglUniform moon_night_cloudColor;
	public EaglUniform moon_night_cloudTextureBlend;
	
	public EaglProgram dither_blend;
	public EaglUniform dither_blend_screenSizeInv;
	
	public EaglProgram ssr_generate;
	
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
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/gbuffer_3f_4b_uniform.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "gbuffer_3f_4b_uniform.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "gbuffer_3f_4b_uniform.fsh");
		this.gbuffer_3f_4b_uniform = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		gbuffer_3f_4b_uniform.getUniform("tex").set1i(0);
		gbuffer_3f_4b_uniform_ditherBlend = gbuffer_3f_4b_uniform.getUniform("ditherBlend");
		gbuffer_3f_4b_uniform_metallic = gbuffer_3f_4b_uniform.getUniform("metallic");
		gbuffer_3f_4b_uniform_roughness = gbuffer_3f_4b_uniform.getUniform("roughness");
		gbuffer_3f_4b_uniform_specular = gbuffer_3f_4b_uniform.getUniform("specular");
		gbuffer_3f_4b_uniform_ssr = gbuffer_3f_4b_uniform.getUniform("ssr");
		gbuffer_3f_4b_uniform_emission = gbuffer_3f_4b_uniform.getUniform("emission");
		gbuffer_3f_4b_uniform_diffuseColor = gbuffer_3f_4b_uniform.getUniform("diffuseColor");
		
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
		gbuffer_combined.getUniform("ssaoBuffer").set1i(6);
		gbuffer_combined.getUniform("cubemap").set1i(7);
		gbuffer_combined.getUniform("irradianceMapA").set1i(8);
		gbuffer_combined.getUniform("irradianceMapB").set1i(9);
		gbuffer_combined.getUniform("ssrBuffer").set1i(10);

		gbuffer_combined_irradianceMapBlend = gbuffer_combined.getUniform("irradianceMapBlend");
		gbuffer_combined_enableSSR = gbuffer_combined.getUniform("enableSSR");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/light_sun.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "light_sun.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "light_sun.fsh");
		this.light_sun = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		light_sun.getUniform("material").set1i(0);
		light_sun.getUniform("normal").set1i(1);
		light_sun.getUniform("position").set1i(2);
		light_sun.getUniform("sunShadow").set1i(3);

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

		sunshadow_generate.getUniform("normal").set1i(0);
		sunshadow_generate.getUniform("position").set1i(1);
		sunshadow_generate.getUniform("shadowMap").set1i(2);

		sunshadow_generate_matrixA = sunshadow_generate.getUniform("shadowMatrixA");
		sunshadow_generate_matrixB = sunshadow_generate.getUniform("shadowMatrixB");
		sunshadow_generate_matrixC = sunshadow_generate.getUniform("shadowMatrixC");
		sunshadow_generate_matrixD = sunshadow_generate.getUniform("shadowMatrixD");
		sunshadow_generate_randTimer = sunshadow_generate.getUniform("randTimer");
		sunshadow_generate_softShadow = sunshadow_generate.getUniform("softShadow");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/light_point_shadowmap.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "light_point_shadowmap.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "light_point_shadowmap.fsh");
		this.light_point_shadowmap = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		light_point_shadowmap.getUniform("material").set1i(0);
		light_point_shadowmap.getUniform("normal").set1i(1);
		light_point_shadowmap.getUniform("position").set1i(2);
		light_point_shadowmap.getUniform("shadowMap").set1i(3);
		
		light_point_shadowmap_lightPosition = light_point_shadowmap.getUniform("lightPosition");
		light_point_shadowmap_lightColor = light_point_shadowmap.getUniform("lightColor");
		light_point_shadowmap_screenSize = light_point_shadowmap.getUniform("screenSize");
		light_point_shadowmap_emission = light_point_shadowmap.getUniform("emission");
		light_point_shadowmap_size = light_point_shadowmap.getUniform("size");
		light_point_shadowmap_shadowMatrix = light_point_shadowmap.getUniform("shadowMatrix");
		light_point_shadowmap_shadowMapIndex = light_point_shadowmap.getUniform("shadowMapIndex");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/light_spot_shadowmap.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "light_spot_shadowmap.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "light_spot_shadowmap.fsh");
		this.light_spot_shadowmap = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		light_spot_shadowmap.getUniform("material").set1i(0);
		light_spot_shadowmap.getUniform("normal").set1i(1);
		light_spot_shadowmap.getUniform("position").set1i(2);
		light_spot_shadowmap.getUniform("shadowMap").set1i(3);

		light_spot_shadowmap_lightPosition = light_spot_shadowmap.getUniform("lightPosition");
		light_spot_shadowmap_lightDirection = light_spot_shadowmap.getUniform("lightDirection");
		light_spot_shadowmap_lightColor = light_spot_shadowmap.getUniform("lightColor");
		light_spot_shadowmap_screenSize = light_spot_shadowmap.getUniform("screenSize");
		light_spot_shadowmap_radius = light_spot_shadowmap.getUniform("radiusF");
		light_spot_shadowmap_emission = light_spot_shadowmap.getUniform("emission");
		light_spot_shadowmap_size = light_spot_shadowmap.getUniform("size");
		light_spot_shadowmap_shadowMatrix = light_spot_shadowmap.getUniform("shadowMatrix");
		light_spot_shadowmap_shadowMapIndex = light_spot_shadowmap.getUniform("shadowMapIndex");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/linearize_depth.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "linearize_depth.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "linearize_depth.fsh");
		this.linearize_depth = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		linearize_depth.getUniform("tex").set1i(0);
		linearize_depth_farPlane = linearize_depth.getUniform("farPlane");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/ssao_generate.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "ssao_generate.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "ssao_generate.fsh");
		this.ssao_generate = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		ssao_generate.getUniform("normal").set1i(0);
		ssao_generate.getUniform("originalDepth").set1i(1);
		
		ssao_generate_randomTime = ssao_generate.getUniform("randomTime");
		ssao_generate_matrix_v_invtrans = ssao_generate.getUniform("matrix_v_invtrans");
		ssao_generate_matrix_p_inv = ssao_generate.getUniform("matrix_p_inv");
		
		Random r = new Random("444".hashCode());
		for(int i = 0; i < 32; i++) {
			
			float x = r.nextFloat() * 2.0f - 1.0f;
			float y = r.nextFloat() * 2.0f - 1.0f;
			float z = r.nextFloat();
			
			float s = lerp(0.5f, 0.9f, r.nextFloat());
			float hypot = (1.0f / (float) Math.sqrt(x*x + y*y + z*z)) * (float)Math.pow(s, 2.0d);
			x *= hypot;
			y *= hypot;
			z *= hypot;
			
			ssao_generate.getUniform("kernel[" + i + "]").set3f(x, y, z);
		}
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/ssao_blur.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "ssao_blur.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "ssao_blur.fsh");
		this.ssao_blur = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		ssao_blur.getUniform("ssaoBuffer").set1i(0);
		ssao_blur.getUniform("linearDepth").set1i(1);
		
		ssao_blur_blurDirection = ssao_blur.getUniform("blurDirection");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/post_tonemap.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "post_tonemap.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "post_tonemap.fsh");
		this.post_tonemap = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		post_tonemap.getUniform("tex").set1i(0);
		post_tonemap.getUniform("bloomTex").set1i(1);
		
		post_tonemap_exposure = post_tonemap.getUniform("exposure");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/post_downscale8th.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "post_downscale8th.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "post_downscale8th.fsh");
		this.post_downscale8th = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		post_downscale8th.getUniform("tex").set1i(0);
		
		post_downscale8th_textureSize = post_downscale8th.getUniform("textureSize");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/post_bloom_h.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "post_bloom_h.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "post_bloom_h.fsh");
		this.post_bloom_h = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		post_bloom_h.getUniform("tex").set1i(0);

		post_bloom_h_screenSizeInv = post_bloom_h.getUniform("screenSizeInv");
		post_bloom_h_exposure = post_bloom_h.getUniform("exposure");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/post_bloom.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "post_bloom.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "post_bloom.fsh");
		this.post_bloom = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		post_bloom.getUniform("tex").set1i(0);

		post_bloom_screenSizeInv = post_bloom.getUniform("screenSizeInv");
		post_bloom_offset = post_bloom.getUniform("offset");
		post_bloom_scale = post_bloom.getUniform("scale");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/bloom_combine_lens.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "bloom_combine_lens.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "bloom_combine_lens.fsh");
		this.bloom_combine_lens = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		bloom_combine_lens.getUniform("tex").set1i(0);
		bloom_combine_lens.getUniform("bloom").set1i(1);

		bloom_combine_lens_startRandom = bloom_combine_lens.getUniform("startRandom");
		bloom_combine_lens_endRandom = bloom_combine_lens.getUniform("endRandom");
		bloom_combine_lens_randomTransition = bloom_combine_lens.getUniform("randomTransition");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/sky.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "sky.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "sky.fsh");
		this.sky = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		sky.getUniform("cloudTextureA").set1i(0);
		sky.getUniform("cloudTextureB").set1i(1);
		sky.getUniform("starsTexture").set1i(2);

		sky_sunColor = sky.getUniform("sunColor");
		sky_cloudColor = sky.getUniform("cloudColor");
		sky_sunDirection = sky.getUniform("sunDirection");
		sky_sunSize = sky.getUniform("sunSize");
		sky_altitude = sky.getUniform("altitude");
		sky_cloudTextureBlend = sky.getUniform("cloudTextureBlend");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/light_shaft_generate.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "light_shaft_generate.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "light_shaft_generate.fsh");
		this.light_shaft_generate = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		light_shaft_generate.getUniform("positionTex").set1i(0);
		light_shaft_generate.getUniform("shadowMap").set1i(1);
		light_shaft_generate.getUniform("positionTex2").set1i(2);
		
		light_shaft_generate_matrix_v_inv = light_shaft_generate.getUniform("matrix_v_inv");
		light_shaft_generate_shadowMatrixA = light_shaft_generate.getUniform("shadowMatrixA");
		light_shaft_generate_shadowMatrixB = light_shaft_generate.getUniform("shadowMatrixB");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/blend_atmosphere.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "blend_atmosphere.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "blend_atmosphere.fsh");
		this.blend_atmosphere = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		blend_atmosphere.getUniform("positionTex").set1i(0);
		blend_atmosphere.getUniform("lightShaftTex").set1i(1);

		blend_atmosphere_invTextureSize = blend_atmosphere.getUniform("invTextureSize");
		blend_atmosphere_fogColor = blend_atmosphere.getUniform("fogColor");
		blend_atmosphere_enableLightShafts = blend_atmosphere.getUniform("enableLightShafts");
		blend_atmosphere_fogDensity = blend_atmosphere.getUniform("fogDensity");
		blend_atmosphere_shaftColor = blend_atmosphere.getUniform("shaftColor");

		source = ResourceLoader.loadResourceString("metaballs/shaders/position_to_view.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "position_to_view.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "position_to_view.fsh");
		this.position_to_view = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		position_to_view.getUniform("positionTex").set1i(0);

		source = ResourceLoader.loadResourceString("metaballs/shaders/add_positions.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "add_positions.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "add_positions.fsh");
		this.add_positions = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();

		source = ResourceLoader.loadResourceString("metaballs/shaders/clouds_generate.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "clouds_generate.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "clouds_generate.fsh");
		this.clouds_generate = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		clouds_generate_cloudDensity = clouds_generate.getUniform("cloudDensity");
		clouds_generate_cloudOffset = clouds_generate.getUniform("cloudOffset");
		clouds_generate_cloudMorph = clouds_generate.getUniform("cloudMorph");
		clouds_generate_sunPosition = clouds_generate.getUniform("sunPosition");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/cubemap_3f_4b_2f_uniform.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "cubemap_3f_4b_2f_uniform.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "cubemap_3f_4b_2f_uniform.fsh");
		this.cubemap_3f_4b_2f_uniform = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();

		cubemap_3f_4b_2f_uniform.getUniform("tex").set1i(0);
		cubemap_3f_4b_2f_uniform.getUniform("shadowMap").set1i(1);
		cubemap_3f_4b_2f_uniform_metallic = cubemap_3f_4b_2f_uniform.getUniform("metallic");
		cubemap_3f_4b_2f_uniform_roughness = cubemap_3f_4b_2f_uniform.getUniform("roughness");
		cubemap_3f_4b_2f_uniform_specular = cubemap_3f_4b_2f_uniform.getUniform("specular");
		cubemap_3f_4b_2f_uniform_emission = cubemap_3f_4b_2f_uniform.getUniform("emission");
		cubemap_3f_4b_2f_uniform_shadowMatrixA = cubemap_3f_4b_2f_uniform.getUniform("shadowMatrixA");
		cubemap_3f_4b_2f_uniform_shadowMatrixB = cubemap_3f_4b_2f_uniform.getUniform("shadowMatrixB");
		cubemap_3f_4b_2f_uniform_sunDirection = cubemap_3f_4b_2f_uniform.getUniform("sunDirection");
		cubemap_3f_4b_2f_uniform_sunRGB = cubemap_3f_4b_2f_uniform.getUniform("sunRGB");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/cubemap_3f_4b_uniform.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "cubemap_3f_4b_uniform.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "cubemap_3f_4b_uniform.fsh");
		this.cubemap_3f_4b_uniform = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();

		cubemap_3f_4b_uniform.getUniform("tex").set1i(0);
		cubemap_3f_4b_uniform.getUniform("shadowMap").set1i(1);
		cubemap_3f_4b_uniform_metallic = cubemap_3f_4b_uniform.getUniform("metallic");
		cubemap_3f_4b_uniform_roughness = cubemap_3f_4b_uniform.getUniform("roughness");
		cubemap_3f_4b_uniform_specular = cubemap_3f_4b_uniform.getUniform("specular");
		cubemap_3f_4b_uniform_emission = cubemap_3f_4b_uniform.getUniform("emission");
		cubemap_3f_4b_uniform_shadowMatrixA = cubemap_3f_4b_uniform.getUniform("shadowMatrixA");
		cubemap_3f_4b_uniform_shadowMatrixB = cubemap_3f_4b_uniform.getUniform("shadowMatrixB");
		cubemap_3f_4b_uniform_sunDirection = cubemap_3f_4b_uniform.getUniform("sunDirection");
		cubemap_3f_4b_uniform_sunRGB = cubemap_3f_4b_uniform.getUniform("sunRGB");
		cubemap_3f_4b_uniform_diffuseColor = cubemap_3f_4b_uniform.getUniform("diffuseColor");

		source = ResourceLoader.loadResourceString("metaballs/shaders/irradiance_map_generate.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "irradiance_map_generate.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "irradiance_map_generate.fsh");
		this.irradiance_map_generate = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		irradiance_map_generate.getUniform("cubeMap").set1i(0);
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/light_bulb_renderer.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "light_bulb_renderer.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "light_bulb_renderer.fsh");
		this.light_bulb_renderer = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();

		light_bulb_renderer.getUniform("lightBulbTexture").set1i(0);
		light_bulb_renderer_aspectRatio = light_bulb_renderer.getUniform("aspectRatio");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/lens_flare.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "lens_flare.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "lens_flare.fsh");
		this.lens_flare = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();

		lens_flare.getUniform("flareTexture").set1i(0);
		lens_flare.getUniform("sunOcclusionTexture").set1i(1);
		lens_flare_intensity = lens_flare.getUniform("intensity");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/lens_flare_occlusion.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "lens_flare_occlusion.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "lens_flare_occlusion.fsh");
		this.lens_flare_occlusion = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();

		lens_flare_occlusion.getUniform("depthTexture").set1i(0);
		lens_flare_occlusion.getUniform("cloudTextureA").set1i(1);
		lens_flare_occlusion.getUniform("cloudTextureB").set1i(2);
		lens_flare_occlusion_sunDirection = lens_flare_occlusion.getUniform("sunDirection");
		lens_flare_occlusion_matrix_vp = lens_flare_occlusion.getUniform("matrix_vp");
		lens_flare_occlusion_cloudTextureBlend = lens_flare_occlusion.getUniform("cloudTextureBlend");
		
		for(int i = 0; i < 32; ++i) {
			float x = (float) r.nextGaussian();
			float y = (float) r.nextGaussian();
			float z = (float) r.nextGaussian();
			float len = (float)Math.sqrt(x*x + y*y + z*z);
			lens_flare_occlusion.getUniform("sampleNoise["+i+"]").set4f(x, y, z, len);
		}
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/lens_flare_single.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "lens_flare_single.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "lens_flare_single.fsh");
		this.lens_flare_single = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();

		lens_flare_single.getUniform("flareTexture").set1i(0);
		lens_flare_single.getUniform("depthTexture").set1i(1);
		lens_flare_single_position = lens_flare_single.getUniform("position");
		lens_flare_single_size = lens_flare_single.getUniform("size");
		lens_flare_single_color = lens_flare_single.getUniform("color");
		lens_flare_single_flareTextureSelection = lens_flare_single.getUniform("flareTextureSelection");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/lens_flare_single_occlusion.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "lens_flare_single_occlusion.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "lens_flare_single_occlusion.fsh");
		this.lens_flare_single_occlusion = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		lens_flare_single_occlusion_position = lens_flare_single_occlusion.getUniform("position");
		lens_flare_single_occlusion_size = lens_flare_single_occlusion.getUniform("size");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/moon_day.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "moon_day.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "moon_day.fsh");
		this.moon_day = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();
		
		moon_day.getUniform("tex").set1i(0);
		moon_day_moonColor = moon_day.getUniform("moonColor");
		moon_day_moonTexXY = moon_day.getUniform("moonTexXY");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/moon_night.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "moon_night.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "moon_night.fsh");
		this.moon_night = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();

		moon_night.getUniform("tex").set1i(0);
		moon_night.getUniform("cloudTextureA").set1i(1);
		moon_night.getUniform("cloudTextureB").set1i(2);
		moon_night_moonColor = moon_night.getUniform("moonColor");
		moon_night_moonTexXY = moon_night.getUniform("moonTexXY");
		moon_night_cloudTextureBlend = moon_night.getUniform("cloudTextureBlend");
		moon_night_cloudColor = moon_night.getUniform("cloudColor");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/dither_blend.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "dither_blend.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "dither_blend.fsh");
		this.dither_blend = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();

		dither_blend.getUniform("tex").set1i(0);
		dither_blend.getUniform("diffuse").set1i(1);
		dither_blend_screenSizeInv = dither_blend.getUniform("screenSizeInv");
		
		source = ResourceLoader.loadResourceString("metaballs/shaders/ssr_generate.glsl");
		vsh = new EaglShader(GL_VERTEX_SHADER).compile(source, "ssr_generate.vsh");
		fsh = new EaglShader(GL_FRAGMENT_SHADER).compile(source, "ssr_generate.fsh");
		this.ssr_generate = new EaglProgram().compile(vsh, fsh); vsh.destroy(); fsh.destroy();

		ssr_generate.getUniform("material").set1i(0);
		ssr_generate.getUniform("normal").set1i(1);
		ssr_generate.getUniform("depth").set1i(2);
		ssr_generate.getUniform("prevFrame").set1i(3);
		
	}
	
	private static final float lerp(float a, float b, float f){
	    return a + f * (b - a);
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
		light_point_shadowmap.destroy();
		light_spot_shadowmap.destroy();
		linearize_depth.destroy();
		ssao_generate.destroy();
		ssao_blur.destroy();
		post_tonemap.destroy();
		post_downscale8th.destroy();
		post_bloom_h.destroy();
		post_bloom.destroy();
		bloom_combine_lens.destroy();
		sky.destroy();
		light_shaft_generate.destroy();
		blend_atmosphere.destroy();
		position_to_view.destroy();
		add_positions.destroy();
		clouds_generate.destroy();
		cubemap_3f_4b_2f_uniform.destroy();
		irradiance_map_generate.destroy();
		gbuffer_3f_4b_uniform.destroy();
		cubemap_3f_4b_uniform.destroy();
		light_bulb_renderer.destroy();
		lens_flare.destroy();
		lens_flare_occlusion.destroy();
		lens_flare_single.destroy();
		lens_flare_single_occlusion.destroy();
		moon_day.destroy();
		moon_night.destroy();
		dither_blend.destroy();
		ssr_generate.destroy();
	}

}

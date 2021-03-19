package net.eagtek.eagl;

import static org.lwjgl.opengles.GLES30.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.eagtek.metaballs.GameConfiguration;

public class EaglShader {
	
	public final int glObject;
	public final int type;
	
	private boolean destroyed = false;
	
	public EaglShader(int glType) {
		glObject = glCreateShader(glType);
		type = glType;
	}
	
	public EaglShader compile(String source, String identifier) {
		
		List<String> sourceLines = new LinkedList();
		
		sourceLines.addAll(Arrays.asList(source.replace('\r', ' ').split("\n")));
		sourceLines.add(0, GameConfiguration.glslVersion);
		
		String include = "";
		
		boolean precisionOverride = false;
		Iterator<String> i = sourceLines.iterator();
		while(i.hasNext()) {
			String s = i.next();
			if(s.startsWith("#include ")) {
				String[] ss = s.split(" ", 2);
				if(ss.length > 1) {
					String includeSource = ResourceLoader.loadResourceString("metaballs/shaders/" + ss[1]);
					if(includeSource != null) {
						include += (includeSource + "\n");
					}
				}
				i.remove();
			}else if(s.startsWith("precision ")) {
				precisionOverride = true;
			}
		}
		
		if(include.length() > 0) sourceLines.add(1, include);
		
		if(!precisionOverride) {
			sourceLines.addAll(1, Arrays.asList(new String[] {
					"precision " + (type == GL_FRAGMENT_SHADER ? GameConfiguration.glslFrag_FloatPrecision : GameConfiguration.glslVert_FloatPrecision) + " float;",
					"precision " + (type == GL_FRAGMENT_SHADER ? GameConfiguration.glslFrag_IntPrecision : GameConfiguration.glslVert_IntPrecision) + " int;",
					"precision " + (type == GL_FRAGMENT_SHADER ? GameConfiguration.glslFrag_SamplerPrecision : GameConfiguration.glslVert_SamplerPrecision) + " sampler2D;",
					"precision " + (type == GL_FRAGMENT_SHADER ? GameConfiguration.glslFrag_CubeSamplerPrecision : GameConfiguration.glslVert_CubeSamplerPrecision) + " samplerCube;"
			}));
		}
		
		source = StringUtils.join(sourceLines, '\n');
		
		glShaderSource(glObject, source);
		glCompileShader(glObject);
		if(glGetShaderi(glObject, GL_COMPILE_STATUS) == GL_FALSE) {
			EaglContext.log.error("Could not compile shader '{}' (#{})", identifier, glObject);
			for(String s : glGetShaderInfoLog(glObject).replace('\r', ' ').split("\n")) {
				EaglContext.log.error(" -- {}", s);
			}
			throw new IllegalArgumentException("Shader source invalid");
		}
		return this;
	}
	
	public void destroy() {
		if(!destroyed) {
			glDeleteShader(glObject);
			destroyed = true;
		}
	}
	
	public void finalize() {
		if(!destroyed && EaglContext.contextAvailable()) {
			EaglContext.log.warn("GL shader #{} leaked memory", glObject);
			glDeleteShader(glObject);
			destroyed = true;
		}
	}
	
}

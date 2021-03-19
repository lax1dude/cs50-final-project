package net.eagtek.eagl;

import static org.lwjgl.opengles.GLES30.*;

public class EaglProgram {
	
	public final int glObject;
	
	public boolean destroyed = false;
	
	public EaglProgram() {
		glObject = glCreateProgram();
	}
	
	public EaglProgram compile(EaglShader... shaders) {
		
		for(EaglShader s : shaders) {
			glAttachShader(glObject, s.glObject);
		}
		
		glLinkProgram(glObject);
		
		for(EaglShader s : shaders) {
			glDetachShader(glObject, s.glObject);
		}
		
		if(glGetProgrami(glObject, GL_LINK_STATUS) == GL_FALSE) {
			EaglContext.log.error("Could not link program #{}", glObject);
			for(String s : glGetProgramInfoLog(glObject).replace('\r', ' ').split("\n")) {
				EaglContext.log.error(" -- {}", s);
			}
			throw new IllegalArgumentException("Could not link program");
		}
		
		return this;
	}
	
	public void destroy() {
		if(!destroyed) {
			glDeleteProgram(glObject);
			destroyed = true;
		}
	}
	
	public void use() {
		glUseProgram(glObject);
	}
	
	public void finalize() {
		if(!destroyed && EaglContext.contextAvailable()) {
			EaglContext.log.warn("GL program #{} leaked memory", glObject);
			glDeleteProgram(glObject);
			destroyed = true;
		}
	}

}

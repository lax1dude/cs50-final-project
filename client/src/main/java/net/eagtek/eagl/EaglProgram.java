package net.eagtek.eagl;

import static org.lwjgl.opengles.GLES31.*;

import java.util.HashMap;

public class EaglProgram {
	
	public final int glObject;
	
	private boolean destroyed = false;
	
	private final HashMap<String,EaglUniform> uniforms;
	
	public EaglUniform matrix_m = null;
	public EaglUniform matrix_v = null;
	public EaglUniform matrix_p = null;
	public EaglUniform matrix_mvp = null;
	public EaglUniform matrix_mv = null;
	
	public EaglUniform matrix_mvp_inv = null;
	public EaglUniform matrix_vp_inv = null;
	public EaglUniform matrix_p_inv = null;
	public EaglUniform matrix_v_inv = null;

	public EaglUniform matrix_m_invtrans = null;
	public EaglUniform matrix_mv_invtrans = null;
	
	public EaglUniform shadowmatrix_near = null;
	public EaglUniform shadowmatrix_far = null;

	public EaglUniform shadowblur_near = null;
	
	public EaglProgram() {
		glObject = glCreateProgram();
		uniforms = new HashMap();
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
		
		this.matrix_m = getUniformOrNull("matrix_m");
		this.matrix_v = getUniformOrNull("matrix_v");
		this.matrix_p = getUniformOrNull("matrix_p");
		
		this.matrix_mvp = getUniformOrNull("matrix_mvp");
		this.matrix_mv = getUniformOrNull("matrix_mv");

		this.matrix_mvp_inv = getUniformOrNull("matrix_mvp_inv");
		this.matrix_vp_inv = getUniformOrNull("matrix_mvp_inv");
		this.matrix_p_inv = getUniformOrNull("matrix_p_inv");
		this.matrix_v_inv = getUniformOrNull("matrix_v_inv");

		this.matrix_m_invtrans = getUniformOrNull("matrix_m_invtrans");
		this.matrix_mv_invtrans = getUniformOrNull("matrix_mv_invtrans");

		this.shadowmatrix_near = getUniformOrNull("shadowmatrix_near");
		this.shadowmatrix_far = getUniformOrNull("shadowmatrix_far");
		
		this.shadowblur_near = getUniformOrNull("shadowblur_near");
		
		return this;
	}
	
	private EaglUniform getUniformOrNull(String string) {
		EaglUniform e = getUniform(string);
		if(e.glObject == -1) return null;
		return e;
	}

	public void destroy() {
		if(!destroyed) {
			glDeleteProgram(glObject);
			destroyed = true;
		}
	}
	
	public void use() {
		GLStateManager.bindProgram(glObject);
	}

	public int getUniformLocation(String name) {
		return getUniform(name).glObject;
	}
	
	public EaglUniform getUniform(String name) {
		EaglUniform e = uniforms.get(name);
		if(e == null) {
			int u = glGetUniformLocation(glObject, name);
			e = new EaglUniform(u, this);
			uniforms.put(name, e);
		}
		return e;
	}
	
	public void finalize() {
		if(!destroyed && EaglContext.contextAvailable()) {
			EaglContext.log.warn("GL program #{} leaked memory", glObject);
			glDeleteProgram(glObject);
			destroyed = true;
		}
	}

}

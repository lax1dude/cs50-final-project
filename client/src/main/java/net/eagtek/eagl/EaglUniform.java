package net.eagtek.eagl;

import static org.lwjgl.opengles.GLES30.*;

import java.nio.FloatBuffer;

import org.joml.*;
import org.lwjgl.system.MemoryUtil;

public class EaglUniform {
	
	private static final FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);
	
	public final int glObject;
	public final EaglProgram program;
	
	public EaglUniform(int loc, EaglProgram eaglProgram) {
		this.glObject = loc;
		this.program = eaglProgram;
	}

	public void setMatrix2f(Matrix2f mat) {
		if(this.glObject == -1) return;
		program.use();
		matrixBuffer.clear();
		mat.get(matrixBuffer);
		matrixBuffer.limit(2 * 2);
		glUniformMatrix2fv(glObject, false, matrixBuffer);
	}

	public void setMatrix3x2f(Matrix3x2f mat) {
		if(this.glObject == -1) return;
		program.use();
		matrixBuffer.clear();
		mat.get(matrixBuffer);
		matrixBuffer.limit(3 * 2);
		glUniformMatrix3x2fv(glObject, false, matrixBuffer);
	}

	public void setMatrix3f(Matrix3f mat) {
		if(this.glObject == -1) return;
		program.use();
		matrixBuffer.clear();
		mat.get(matrixBuffer);
		matrixBuffer.limit(3 * 3);
		glUniformMatrix3fv(glObject, false, matrixBuffer);
	}
	
	public void setMatrix4x3f(Matrix4x3f mat) {
		if(this.glObject == -1) return;
		program.use();
		matrixBuffer.clear();
		mat.get(matrixBuffer);
		matrixBuffer.limit(4 * 3);
		glUniformMatrix4x3fv(glObject, false, matrixBuffer);
		
	}
	
	public void setMatrix4f(Matrix4f mat) {
		if(this.glObject == -1) return;
		program.use();
		matrixBuffer.clear();
		mat.get(matrixBuffer);
		matrixBuffer.limit(4 * 4);
		glUniformMatrix4fv(glObject, false, matrixBuffer);
	}
	
	public void set1f(float p1) {
		if(this.glObject == -1) return;
		program.use();
		glUniform1f(glObject, p1);
	}
	
	public void set2f(float p1, float p2) {
		if(this.glObject == -1) return;
		program.use();
		glUniform2f(glObject, p1, p2);
	}
	
	public void set3f(float p1, float p2, float p3) {
		if(this.glObject == -1) return;
		program.use();
		glUniform3f(glObject, p1, p2, p3);
	}
	
	public void set4f(float p1, float p2, float p3, float p4) {
		if(this.glObject == -1) return;
		program.use();
		glUniform4f(glObject, p1, p2, p3, p4);
	}
	
	public void set1i(int p1) {
		if(this.glObject == -1) return;
		program.use();
		glUniform1i(glObject, p1);
	}
	
	public void set2i(int p1, int p2) {
		if(this.glObject == -1) return;
		program.use();
		glUniform2i(glObject, p1, p2);
	}
	
	public void set3i(int p1, int p2, int p3) {
		if(this.glObject == -1) return;
		program.use();
		glUniform3i(glObject, p1, p2, p3);
	}
	
	public void set4i(int p1, int p2, int p3, int p4) {
		if(this.glObject == -1) return;
		program.use();
		glUniform4i(glObject, p1, p2, p3, p4);
	}
	
	public void set1ui(int p1) {
		if(this.glObject == -1) return;
		program.use();
		glUniform1ui(glObject, p1);
	}
	
	public void set2ui(int p1, int p2) {
		if(this.glObject == -1) return;
		program.use();
		glUniform2ui(glObject, p1, p2);
	}
	
	public void set3ui(int p1, int p2, int p3) {
		if(this.glObject == -1) return;
		program.use();
		glUniform3ui(glObject, p1, p2, p3);
	}
	
	public void set4ui(int p1, int p2, int p3, int p4) {
		if(this.glObject == -1) return;
		program.use();
		glUniform4ui(glObject, p1, p2, p3, p4);
	}
}

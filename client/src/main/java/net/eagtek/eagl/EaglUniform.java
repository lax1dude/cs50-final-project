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
		program.use();
		matrixBuffer.clear();
		mat.get(matrixBuffer);
		matrixBuffer.limit(2 * 2);
		glUniformMatrix2fv(glObject, false, matrixBuffer);
	}

	public void setMatrix3x2f(Matrix3x2f mat) {
		program.use();
		matrixBuffer.clear();
		mat.get(matrixBuffer);
		matrixBuffer.limit(3 * 2);
		glUniformMatrix3x2fv(glObject, false, matrixBuffer);
	}

	public void setMatrix3f(Matrix3f mat) {
		program.use();
		matrixBuffer.clear();
		mat.get(matrixBuffer);
		matrixBuffer.limit(3 * 3);
		glUniformMatrix3fv(glObject, false, matrixBuffer);
	}
	
	public void setMatrix4x3f(Matrix4x3f mat) {
		program.use();
		matrixBuffer.clear();
		mat.get(matrixBuffer);
		matrixBuffer.limit(4 * 3);
		glUniformMatrix4x3fv(glObject, false, matrixBuffer);
		
	}
	
	public void setMatrix4f(Matrix4f mat) {
		program.use();
		matrixBuffer.clear();
		mat.get(matrixBuffer);
		matrixBuffer.limit(4 * 4);
		glUniformMatrix4fv(glObject, false, matrixBuffer);
	}

}

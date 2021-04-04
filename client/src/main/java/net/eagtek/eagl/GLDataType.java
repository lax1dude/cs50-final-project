package net.eagtek.eagl;

import static org.lwjgl.opengles.GLES31.*;

public enum GLDataType {

	FLOAT(GL_FLOAT, 4, false),
	HALF_FLOAT(GL_HALF_FLOAT, 2, false),
	INT(GL_INT, 4, false),
	INT_U(GL_UNSIGNED_INT, 4, true),
	SHORT(GL_SHORT, 2, false),
	SHORT_U(GL_UNSIGNED_SHORT, 2, true),
	BYTE(GL_BYTE, 1, false),
	BYTE_U(GL_UNSIGNED_BYTE, 1, true),
	INT_2_10_10_10_REV(GL_INT_2_10_10_10_REV, 4, false),
	INT_2_10_10_10_REV_U(GL_UNSIGNED_BYTE, 1, true);
	
	public final int glEnum;
	public final int bytesUsed;
	public final boolean unsigned;
	
	private GLDataType(int glEnum, int bytesUsed, boolean unsigned) {
		this.glEnum = glEnum;
		this.bytesUsed = bytesUsed;
		this.unsigned = unsigned;
	}
	

}

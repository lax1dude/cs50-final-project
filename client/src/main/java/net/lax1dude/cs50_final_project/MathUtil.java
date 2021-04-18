package net.lax1dude.cs50_final_project;

import java.nio.ByteBuffer;
import java.util.Random;

public class MathUtil {

	public static final float toRadians = 0.017453293F;
	public static final float toDegrees = 57.295777937F;

	public static final float[] SIN_TABLE = new float[65536];
	public static final float[] TAN_TABLE = new float[32768];
	public static final float[] ASIN_TABLE = new float[32768];
	public static final float[] ATAN_TABLE = new float[32768];
	public static final float PI_FLOAT = 3.141592654f;
	public static final Random random = new Random();
	
	static {
		float f = (float) ((2*PI_FLOAT) / 65536) ;

		for(int i = 0; i < 65536; i++) {
			SIN_TABLE[i] = (float) Math.sin(i * f);
		}
		
		for(int i = 0; i < 32768; i++) {
			TAN_TABLE[i] = (float) Math.tan(i * f);
		}
		
		float f1 = (float) (2f / 32768);
		
		for(int i = 0; i < 32768; i++) {
			ASIN_TABLE[i] = (float) Math.asin((i * f1) - 1f);
		}
		
		for(int i = 0; i < 32768; i++) {
			ATAN_TABLE[i] = (float) Math.atan((i * f1) - 1f);
		}
	}

	public static final float sin_deg(float angle) {
		return SIN_TABLE[(int)(angle * 182.044459008f) & 65535];
	}
	
	public static final float sin_rad(float angle) {
		return SIN_TABLE[(int)(angle * 10430.378344448f) & 65535];
	}
	
	public static final float cos_deg(float angle) {
		return SIN_TABLE[(int)((angle - 90f) * 182.044459008f) & 65535];
	}
	
	public static final float cos_rad(float angle) {
		return SIN_TABLE[(int)((angle - 1.570796327f) * 10430.378344448f) & 65535];
	}
	
	public static final float tan_deg(float angle) {
		return TAN_TABLE[(int)(angle * 182.044459008f) & 32767];
	}
	
	public static final float tan_rad(float angle) {
		return TAN_TABLE[(int)(angle * 10430.378344448f) & 32767];
	}
	

	public static final float asin_deg(float value) {
		int v = (int)((value + 1f) * 16384f);
		if(v < 0) v = 0;
		if(v > 32767) v = 32767;
		return ASIN_TABLE[v] * toDegrees;
	}
	
	public static final float asin_rad(float value) {
		int v = (int)((value + 1f) * 16384f);
		if(v < 0) v = 0;
		if(v > 32767) v = 32767;
		return ASIN_TABLE[v];
	}

	public static final float asin_deg_fast(float value) {
		return ASIN_TABLE[(int)((value + 1f) * 16384f)] * toDegrees;
	}
	
	public static final float asin_rad_fast(float value) {
		return ASIN_TABLE[(int)((value + 1f) * 16384f)];
	}
	

	public static final float acos_deg(float value) {
		int v = (int)((1f - value) * 16384f);
		if(v < 0) v = 0;
		if(v > 32767) v = 32767;
		return (ASIN_TABLE[v] + 1.570796327f) * toDegrees;
	}
	
	public static final float acos_rad(float value) {
		int v = (int)((1f - value) * 16384f);
		if(v < 0) v = 0;
		if(v > 32767) v = 32767;
		return ASIN_TABLE[v] + 1.570796327f;
	}

	public static final float acos_deg_fast(float value) {
		return (ASIN_TABLE[(int)((1f - value) * 16384f)] + 1.570796327f) * toDegrees;
	}

	public static final float acos_rad_fast(float value) {
		return ASIN_TABLE[(int)((1f - value) * 16384f)] + 1.570796327f;
	}
	

	public static final float atan_deg(float value) {
		int v = (int)((value + 1f) * 16384f);
		if(v < 0) v = 0;
		if(v > 32767) v = 32767;
		return ATAN_TABLE[v] * toDegrees;
	}
	
	public static final float atan_rad(float value) {
		int v = (int)((value + 1f) * 16384f);
		if(v < 0) v = 0;
		if(v > 32767) v = 32767;
		return ATAN_TABLE[v];
	}

	public static final float atan_deg_fast(float value) {
		return ATAN_TABLE[(int)((value + 1f) * 16384f)] * toDegrees;
	}
	
	public static final float atan_rad_fast(float value) {
		return ATAN_TABLE[(int)((value + 1f) * 16384f)];
	}
	

	public static final int floor(float in) {
		int xi = (int)in;
	    return in < xi ? xi - 1 : xi;
	}
	
	public static final int ciel(float in) {
		int xi = (int)in;
	    return in < xi ? xi : xi + 1;
	}

	public static final float DirectionToPitch(float y) {
		return asin_deg(-y);
	}
	
	public static final float DirectionToYaw(float x, float z) {
		return atan_deg(x/z);
	}

	public static final float RotationToX(float yaw) {
		return -sin_deg(yaw);
	}

	public static final float RotationToY(float pitch) {
		return sin_deg(pitch);
	}
	
	public static final float RotationToYV(float pitch) {
		return tan_deg(pitch);
	}

	public static final float RotationToZ(float yaw) {
		return cos_deg(yaw);
	}

	public static void printArray(int[] in, int rowlen) {
		System.out.println();
		for(int i = 0; i < in.length; i++) {
			if(i % rowlen == 0)
				System.out.println();
			System.out.print(in[i]);
			System.out.print(" ");
		}
		System.out.println();
	}
	
	public static void printArray(byte[] in, int rowlen) {
		System.out.println();
		for(int i = 0; i < in.length; i++) {
			if(i % rowlen == 0)
				System.out.println();
			System.out.print(in[i]);
			System.out.print(" ");
		}
		System.out.println();
	}
	
	public static void printArray(int[] in) {
		for(int i = 0; i < in.length; i++) {
			System.out.print(in[i]);
			System.out.print(" ");
		}
		System.out.println();
	}
	
	public static void printArray(byte[] in) {
		for(int i = 0; i < in.length; i++) {
			System.out.print(in[i]);
			System.out.print(" ");
		}
		System.out.println();
	}

	public static void printByteBuffer(ByteBuffer in) {
		for(int i = 0; i < in.limit(); i++) {
			System.out.print(in.get(i));
			System.out.print(" ");
		}
	}

	public static void printByteBuffer(ByteBuffer in, int rowlen) {
		for(int i = 0; i < in.limit(); i++) {
			if(i % rowlen == 0)
				System.out.println();
			System.out.print(in.get(i));
			System.out.print(" ");
		}
	}

	public static float wrapAngleTo180(float angle) {
		while(angle < -180F) {
			angle += 360F;
		}
		while(angle > 180F) {
			angle -= 360F;
		}
		return angle;
	}

	public static final float abs(float f) {
		return f < 0f ? -f : f;
	}
}

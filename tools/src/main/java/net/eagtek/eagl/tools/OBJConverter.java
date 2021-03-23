package net.eagtek.eagl.tools;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.lz4.LZ4;

public class OBJConverter {
	
	public static void convertModel(String in, boolean index, boolean texture, boolean normal, boolean compress, boolean index32, OutputStream out) throws IOException {
		String[] lines = in.split("\n");
		for(int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].replace("\r", "");
		}
		
		List<float[]> vertexes = new ArrayList<float[]>();
		List<byte[]> normals = new ArrayList<byte[]>();
		List<float[]> texcoords = new ArrayList<float[]>();
		List<int[][]> faces = new ArrayList<int[][]>();
		List<byte[]> vboentries = new ArrayList<byte[]>();
		List<byte[]> indexablevboentries = new ArrayList<byte[]>();
		List<Integer> indexbuffer = new ArrayList<Integer>();
		for(String ul : lines) {
			String[] l = ul.split(" ");
			if(l[0].equals("v")) {
				vertexes.add(new float[] {Float.parseFloat(l[1]), Float.parseFloat(l[2]), Float.parseFloat(l[3])});
			}
			if(l[0].equals("vn")) {
				normals.add(new byte[] {(byte)((int)(Float.parseFloat(l[1])*127f)), (byte)((int)(Float.parseFloat(l[2])*127f)), (byte)((int)(Float.parseFloat(l[3])*127f)), (byte)0});
			}
			if(l[0].equals("vt")) {
				texcoords.add(new float[] {Float.parseFloat(l[1]), Float.parseFloat(l[2])});
			}
			if(l[0].equals("f")) {
				if(texture) {
					String[] v1 = l[1].split("/");
					String[] v2 = l[2].split("/");
					String[] v3 = l[3].split("/");
					faces.add(new int[][] {
						{Integer.parseInt(v1[0]), Integer.parseInt(v1[1]), Integer.parseInt(v1[2])},
						{Integer.parseInt(v2[0]), Integer.parseInt(v2[1]), Integer.parseInt(v2[2])},
						{Integer.parseInt(v3[0]), Integer.parseInt(v3[1]), Integer.parseInt(v3[2])}
					});
				}else {
					String[] v1 = l[1].split("/");
					String[] v2 = l[2].split("/");
					String[] v3 = l[3].split("/");
					faces.add(new int[][] {
						{Integer.parseInt(v1[0]), Integer.parseInt(v1[2])},
						{Integer.parseInt(v2[0]), Integer.parseInt(v2[2])},
						{Integer.parseInt(v3[0]), Integer.parseInt(v3[2])}
					});
				}
			}
		}
		
		for(int[][] f : faces) {
			for(int i = 0; i < 3; i++) {
				byte[] b = new byte[24];
				
				float[] v = vertexes.get(f[i][0]-1);
				byte[] n = normals.get(f[i][2]-1);
				float[] t = texcoords.get(f[i][1]-1);
				
				int ix = Float.floatToRawIntBits(v[0]);
				int iy = Float.floatToRawIntBits(v[1]);
				int iz = Float.floatToRawIntBits(v[2]);
				int ix3 = Float.floatToRawIntBits(t[0]);
				int iy3 = Float.floatToRawIntBits(t[1]);
				
				int idx = 0;
				
				b[idx++] = (byte)(ix); b[idx++] = (byte)(ix >> 8); b[idx++] = (byte)(ix >> 16); b[idx++] = (byte)(ix >> 24);
				b[idx++] = (byte)(iy); b[idx++] = (byte)(iy >> 8); b[idx++] = (byte)(iy >> 16); b[idx++] = (byte)(iy >> 24);
				b[idx++] = (byte)(iz); b[idx++] = (byte)(iz >> 8); b[idx++] = (byte)(iz >> 16); b[idx++] = (byte)(iz >> 24);
				
				if(normal) {
					b[idx++] = n[0];
					b[idx++] = n[1];
					b[idx++] = n[2];
					b[idx++] = n[3];
				}
				
				if(texture) {
					b[idx++] = (byte)(ix3); b[idx++] = (byte)(ix3 >> 8); b[idx++] = (byte)(ix3 >> 16); b[idx++] = (byte)(ix3 >> 24);
					b[idx++] = (byte)(iy3); b[idx++] = (byte)(iy3 >> 8); b[idx++] = (byte)(iy3 >> 16); b[idx++] = (byte)(iy3 >> 24);
				}
				
				vboentries.add(b);
			}
		}
		
		if(index) {
			for(byte[] v : vboentries) {
				int l = indexablevboentries.size();
				boolean flag = true;
				for(int i = 0; i < l; i++) {
					if(Arrays.equals(v, indexablevboentries.get(i))) {
						indexbuffer.add(i);
						flag = false;
						break;
					}
				}
				if(flag) {
					indexbuffer.add(l);
					indexablevboentries.add(v);
				}
			}
		}
		

		DataOutputStream o = new DataOutputStream(out);
		o.write(0xEE);
		o.write(0xEE);
		
		index32 = index32 || (indexablevboentries != null && indexablevboentries.size() > 65536);
		
		int flags = 0;
		int componentLen = 12;
		if(index) flags |= 1;
		if(texture) {
			componentLen += 8;
			flags |= 2;
		}
		if(normal) {
			componentLen += 4;
			flags |= 4;
		}
		if(compress) flags |= 8;
		if(index32) flags |= 16;
		o.write(flags);
		
		o.writeUTF("\n\nthis file (c) "+DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDateTime.now())+" eagtek, all rights reserved\n\n");
		
		ByteArrayOutputStream data = new ByteArrayOutputStream(componentLen * (index ? indexablevboentries.size() : vboentries.size()) + (indexbuffer != null ? (indexbuffer.size() * (index32 ? 4 : 2)) : 0));
		if(index) {
			o.writeInt(indexablevboentries.size());
			o.writeInt(indexbuffer.size());
			for(byte[] b : indexablevboentries) {
				data.write(b, 0, componentLen);
			}
			for(int i : indexbuffer) {
				if(index32) {
					data.write((byte)i);
					data.write((byte)(i >> 8));
					data.write((byte)(i >> 16));
					data.write((byte)(i >> 24));
				}else {
					data.write((byte)i);
					data.write((byte)(i >> 8));
				}
			}
		}else {
			o.writeInt(vboentries.size());
			o.writeInt(0);
			for(byte[] b : vboentries) {
				data.write(b, 0, componentLen);
			}
		}
		
		byte[] toCompress = data.toByteArray();
		
		byte[] ret;
		if(compress) {
			ByteBuffer inn = MemoryUtil.memAlloc(toCompress.length);
			inn.put(toCompress);
			inn.flip();
			
			ByteBuffer outt = MemoryUtil.memAlloc(LZ4.LZ4_compressBound(toCompress.length));
			int written = LZ4.LZ4_compress_default(inn, outt);
			
			if(written == 0) {
				throw new RuntimeException("could not compress");
			}
			
			outt.limit(written);
			ret = new byte[outt.remaining()];
			
			outt.get(ret);
			
		}else {
			ret = toCompress;
		}
		

		o.writeInt(toCompress.length);
		o.writeInt(ret.length);
		o.write(ret);
		
		o.flush();
		
	}
	
}

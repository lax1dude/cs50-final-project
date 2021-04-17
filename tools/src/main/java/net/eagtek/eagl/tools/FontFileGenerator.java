package net.eagtek.eagl.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.lz4.LZ4;

public class FontFileGenerator {

	public static void generateFontFile(File in, File out, int size, boolean bold, boolean italic, boolean unicode) {
		try {
			Font f = new Font("Dialog", 0, size);//Font.createFont(Font.TRUETYPE_FONT, in);
			f.deriveFont((bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0), size);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(f);
			
			ArrayList<Integer> list = new ArrayList();
			for(int i = 0; i < (unicode ? 65536 : 256); ++i) {
				if(f.canDisplay(i)) {
					list.add(i);
				}
			}
			
			int divisions = 1;
			int size2 = size;
			while(size2 > 8) {
				size2 /= 2;
				++divisions;
			}
			
			FileOutputStream fos = new FileOutputStream(out);
			DataOutputStream out2 = new DataOutputStream(fos);
			out2.write('E');
			out2.write('F');
			out2.write('F');
			out2.write((byte)size);
			out2.write((byte)divisions);
			out2.writeInt(list.size());
			
			for(int i = 0; i < 65536; ++i) {
				out2.writeShort(list.indexOf(i));
			}
			
			BufferedImage page = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
			int imageSquareSize = (int)Math.ceil(Math.sqrt(list.size()));
			Graphics2D g = (Graphics2D) page.getGraphics();
			FontMetrics f2 = g.getFontMetrics(f);
			for(int i = 0; i < list.size(); ++i) {
				int ix = f2.stringWidth("" + ((char)list.get(i).intValue()));
				out2.write(ix);
			}
			g.dispose();
			
			size2 = size;
			//for(int i = 0; i < divisions; ++i) {
				out2.write(createPage(f, size2, unicode, list));
			//	size2 /= 2;
			//}
			
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	private static void LEWriteFloat(OutputStream o, float f) throws IOException {
		int ix = Float.floatToRawIntBits(f);
		o.write((byte)(ix)); o.write((byte)(ix >> 8)); o.write((byte)(ix >> 16)); o.write((byte)(ix >> 24));
	}
	*/
	private static byte[] createPage(Font f, int size, boolean unicode, ArrayList<Integer> list) {
		int imageSquareSize = (int)Math.ceil(Math.sqrt(list.size()));
		BufferedImage page = new BufferedImage(imageSquareSize * (size + 4), imageSquareSize * (size + 4), BufferedImage.TYPE_BYTE_GRAY);
		
		Graphics2D g = (Graphics2D) page.getGraphics();
		FontMetrics f2 = g.getFontMetrics(f);
		
		g.setFont(f);
		g.setColor(Color.WHITE);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		for(int i = 0; i < list.size(); ++i) {
			g.drawString("" + ((char)list.get(i).intValue()), (i % imageSquareSize) * (size + 4), (i / imageSquareSize + 1) * (size + 4) - f2.getDescent());
		}
		g.dispose();
		
		int numPixels = imageSquareSize * (size + 4) * imageSquareSize * (size + 4);
		int[] pixelsRetriv = page.getRGB(0, 0, imageSquareSize * (size + 4), imageSquareSize * (size + 4), new int[numPixels], 0, imageSquareSize * (size + 4));
		
		ByteBuffer in = MemoryUtil.memAlloc(numPixels);
		for(int i = 0; i < numPixels; ++i) {
			in.put((byte) (pixelsRetriv[i] & 255));
		};
		in.flip();
		
		ByteBuffer out = MemoryUtil.memAlloc(LZ4.LZ4_compressBound(numPixels));
		
		int written = LZ4.LZ4_compress_default(in, out);
		
		if(written <= 0) {
			throw new RuntimeException("could not compress");
		}
		out.rewind();
		out.limit(written);
		
		byte[] ret = new byte[out.remaining() + 6];
		ret[0] = (byte)((int)((imageSquareSize) * (size + 4)) >> 8);
		ret[1] = (byte)((int)((imageSquareSize) * (size + 4)));
		ret[2] = (byte)(out.remaining() >> 24);
		ret[3] = (byte)(out.remaining() >> 16);
		ret[4] = (byte)(out.remaining() >> 8);
		ret[5] = (byte)(out.remaining());
		out.get(ret, 6, out.remaining());
		
		MemoryUtil.memFree(in);
		MemoryUtil.memFree(out);
		
		return ret;
	}

}

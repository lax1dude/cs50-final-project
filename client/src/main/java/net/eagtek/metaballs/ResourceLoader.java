package net.eagtek.metaballs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class ResourceLoader {
	
	public static interface IResourceLoader {
		public InputStream loadResource(String name);
	}
	
	public static class ResourceLoaderFolder implements IResourceLoader {
		
		private final File rootDirectory;
		
		public ResourceLoaderFolder(File rootDirectory) {
			this.rootDirectory = rootDirectory;
		}
		
		@Override
		public InputStream loadResource(String name) {
			try {
				return new FileInputStream(new File(rootDirectory, name));
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		
	}
	
	public static class ResourceLoaderClasspath implements IResourceLoader {
		
		private final String prefix;
		
		public ResourceLoaderClasspath(String prefix) {
			if(!prefix.startsWith("/")) prefix = "/" + prefix;
			if(!prefix.endsWith("/")) prefix = prefix + "/";
			this.prefix = prefix;
		}

		@Override
		public InputStream loadResource(String name) {
			if(name.startsWith("/")) name = name.substring(1);
			return ResourceLoader.class.getResourceAsStream(prefix + name);
		}
		
	}

	private static final ArrayList<IResourceLoader> defaultLoaders = new ArrayList();
	private static final ArrayList<IResourceLoader> activeLoaders = new ArrayList();
	
	static {
		defaultLoaders.add(new ResourceLoaderFolder(new File("./assets_dev/")));
		defaultLoaders.add(new ResourceLoaderClasspath("/assets/"));
		resetLoaders();
	}
	
	public static void resetLoaders() {
		activeLoaders.clear();
		activeLoaders.addAll(defaultLoaders);
	}
	
	public static void registerLoader(IResourceLoader loader) {
		activeLoaders.add(0, loader);
	}
	
	public static InputStream loadResource(String name) {
		for(IResourceLoader loader : activeLoaders) {
			InputStream is = loader.loadResource(name);
			if(is != null) return is;
		}
		return null;
	}
	
	public static byte[] loadResourceBytes(String name) {
		try {
			InputStream is = loadResource(name);
			if(is == null) {
				return null;
			}else {
				return IOUtils.toByteArray(is);
			}
		} catch (IOException e) {
			return null;
		}
	}
	
	public static String loadResourceString(String name) {
		try {
			InputStream is = loadResource(name);
			if(is == null) {
				return null;
			}else {
				return IOUtils.toString(is, Charset.forName("UTF8"));
			}
		} catch (IOException e) {
			return null;
		}
	}
	
	public static List<String> loadResourceLines(String name) {
		try {
			InputStream is = loadResource(name);
			if(is == null) {
				return null;
			}else {
				return IOUtils.readLines(is, Charset.forName("UTF8"));
			}
		} catch (IOException e) {
			return null;
		}
	}
	
}

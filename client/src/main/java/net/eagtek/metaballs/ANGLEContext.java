package net.eagtek.metaballs;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLESCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.egl.EGL10.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeEGL.*;
import static org.lwjgl.opengles.GLES20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.egl.EGL;


public class ANGLEContext {
	
	public static final Logger log = LoggerFactory.getLogger("ANGLEContext");

	public static enum ToolkitPlatform {
		desktop;
	}
	
	public static enum ContextPlatform {
		angle(0x3202, "angle"), d3d11(0x3208, "d3d11"), d3d11on12(0x3488, "d3d11on12"), opengl(0x320D, "opengl"), opengles(0x320E, "opengles"), metal(0x3489, "metal"), vulkan(0x3450, "vulkan");
		
		protected final int eglEnum;
		protected final String id;
		
		private ContextPlatform(int eglEnum, String id) {
			this.eglEnum = eglEnum;
			this.id = id;
		}
		
	}

	private final ToolkitPlatform toolkit;
	private final ContextPlatform platform;
	private String title;
	
	private long glfw_windowHandle = 0l;
	
	private GLESCapabilities caps = null;
	
	public GLESCapabilities getGLESCapabilities() {
		return caps;
	}
	
	public ANGLEContext(ToolkitPlatform toolkit, ContextPlatform platform, String title) {
		this.toolkit = toolkit;
		this.platform = platform;
		this.title = title;
	}

	public void create() {
		if(toolkit == ToolkitPlatform.desktop) {
			createGLFW();
		}
	}

	private void createGLFW() {
		GLFWErrorCallback.createThrow().set();
		
		glfwInit();
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        
        glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);
        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_ES_API);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
        
        glfw_windowHandle = glfwCreateWindow(854, 480, title, NULL, NULL);
        
        long dpy = glfwGetEGLDisplay();
        
        int[] major = new int[] { 1 };
        int[] minor = new int[] { 1 };
        if(!eglInitialize(dpy, major, minor)) {
        	throw new RuntimeException("Could not initialize EGL");
        }
        
        EGL.createDisplayCapabilities(dpy, 1, 1);
        glfwMakeContextCurrent(glfw_windowHandle);
        
        caps = GLES.createCapabilities();

        log.info("GL_VENDOR: {}", glGetString(GL_VENDOR));
        log.info("GL_VERSION: {}", glGetString(GL_VERSION));
        log.info("GL_RENDERER: {}", glGetString(GL_RENDERER));
	}
	
	public void destroy() {
		if(toolkit == ToolkitPlatform.desktop) {
			destroyGLFW();
		}
	}

	private void destroyGLFW() {
		GLES.setCapabilities(null);
		glfwTerminate();
	}

}

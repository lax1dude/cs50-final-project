package net.eagtek.eagl;

import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowFocusCallbackI;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.opengles.GLDebugMessageKHRCallback;
import org.lwjgl.opengles.GLDebugMessageKHRCallbackI;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLESCapabilities;
import org.lwjgl.opengles.KHRDebug;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.list.linked.TCharLinkedList;

import static org.lwjgl.egl.EGL10.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeEGL.*;
import static org.lwjgl.opengles.GLES30.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

import org.lwjgl.PointerBuffer;
import org.lwjgl.egl.EGL;


public class EaglContext {
	
	public final int KEY_SPACE, KEY_APOSTROPHE, KEY_COMMA, KEY_MINUS, KEY_PERIOD, KEY_SLASH, KEY_0, KEY_1, KEY_2, KEY_3, KEY_4, KEY_5, KEY_6, KEY_7, KEY_8, KEY_9, KEY_SEMICOLON, KEY_EQUAL, KEY_A, KEY_B, KEY_C, KEY_D, KEY_E, KEY_F, KEY_G, KEY_H, KEY_I, KEY_J, KEY_K, KEY_L, KEY_M, KEY_N, KEY_O, KEY_P,
			KEY_Q, KEY_R, KEY_S, KEY_T, KEY_U, KEY_V, KEY_W, KEY_X, KEY_Y, KEY_Z, KEY_LEFT_BRACKET, KEY_BACKSLASH, KEY_RIGHT_BRACKET, KEY_GRAVE_ACCENT, KEY_WORLD_1, KEY_WORLD_2, KEY_ESCAPE, KEY_ENTER, KEY_TAB, KEY_BACKSPACE, KEY_INSERT, KEY_DELETE, KEY_RIGHT, KEY_LEFT, KEY_DOWN, KEY_UP, KEY_PAGE_UP,
			KEY_PAGE_DOWN, KEY_HOME, KEY_END, KEY_CAPS_LOCK, KEY_SCROLL_LOCK, KEY_NUM_LOCK, KEY_PRINT_SCREEN, KEY_PAUSE, KEY_F1, KEY_F2, KEY_F3, KEY_F4, KEY_F5, KEY_F6, KEY_F7, KEY_F8, KEY_F9, KEY_F10, KEY_F11, KEY_F12, KEY_F13, KEY_F14, KEY_F15, KEY_F16, KEY_F17, KEY_F18, KEY_F19, KEY_F20, KEY_F21,
			KEY_F22, KEY_F23, KEY_F24, KEY_F25, KEY_KP_0, KEY_KP_1, KEY_KP_2, KEY_KP_3, KEY_KP_4, KEY_KP_5, KEY_KP_6, KEY_KP_7, KEY_KP_8, KEY_KP_9, KEY_KP_DECIMAL, KEY_KP_DIVIDE, KEY_KP_MULTIPLY, KEY_KP_SUBTRACT, KEY_KP_ADD, KEY_KP_ENTER, KEY_KP_EQUAL, KEY_LEFT_SHIFT, KEY_LEFT_CONTROL, KEY_LEFT_ALT,
			KEY_LEFT_SUPER, KEY_RIGHT_SHIFT, KEY_RIGHT_CONTROL, KEY_RIGHT_ALT, KEY_RIGHT_SUPER, KEY_MENU, KEY_LAST;
	
	public static final Logger log = LoggerFactory.getLogger("EAGL");

	public static enum ToolkitPlatform {
		desktop;
	}
	
	public static enum ContextPlatform {
		angle(GLFW_ANGLE_PLATFORM_TYPE_NONE, "default"),
		d3d11(GLFW_ANGLE_PLATFORM_TYPE_D3D11, "d3d11"),
		opengl(GLFW_ANGLE_PLATFORM_TYPE_OPENGL, "opengl"),
		opengles(GLFW_ANGLE_PLATFORM_TYPE_OPENGLES, "opengles"),
		metal(GLFW_ANGLE_PLATFORM_TYPE_METAL, "metal"),
		vulkan(GLFW_ANGLE_PLATFORM_TYPE_VULKAN, "vulkan");
		
		protected final int eglEnum;
		protected final String id;
		
		private ContextPlatform(int eglEnum, String id) {
			this.eglEnum = eglEnum;
			this.id = id;
		}
		
	}

	public final ToolkitPlatform toolkit;
	public final ContextPlatform platform;
	private String title;
	public final boolean windowed;
	public final int resolutionDiv;
	private int monitorNum;

	private long glfw_windowHandle = 0l;
	private long glfw_eglHandle = 0l;
	
	private GLESCapabilities caps = null;
	
	public GLESCapabilities getGLESCapabilities() {
		return caps;
	}
	
	public boolean contextLost() {
		return glfwGetWindowAttrib(glfw_windowHandle, GLFW_ICONIFIED) == GLFW_TRUE;
	}

	public EaglContext(ToolkitPlatform toolkit, ContextPlatform platform, String title, int resolutionDiv) {
		this(toolkit, platform, title, true, resolutionDiv, 0);
	}
	
	public EaglContext(ToolkitPlatform toolkit, ContextPlatform platform, String title, int monitorNum, int resolutionDiv) {
		this(toolkit, platform, title, false, resolutionDiv, monitorNum);
	}
	
	private EaglContext(ToolkitPlatform toolkit, ContextPlatform platform, String title, boolean windowed, int resolutionDiv, int monitorNum) {
		this.toolkit = toolkit;
		this.platform = platform;
		this.title = title;
		this.windowed = windowed;
		this.resolutionDiv = resolutionDiv;
		this.monitorNum = monitorNum;
		
		if(toolkit == ToolkitPlatform.desktop) {
			KEY_SPACE = 32;
			KEY_APOSTROPHE = 39;
			KEY_COMMA = 44;
			KEY_MINUS = 45;
			KEY_PERIOD = 46;
			KEY_SLASH = 47;
			KEY_0 = 48;
			KEY_1 = 49;
			KEY_2 = 50;
			KEY_3 = 51;
			KEY_4 = 52;
			KEY_5 = 53;
			KEY_6 = 54;
			KEY_7 = 55;
			KEY_8 = 56;
			KEY_9 = 57;
			KEY_SEMICOLON = 59;
			KEY_EQUAL = 61;
			KEY_A = 65;
			KEY_B = 66;
			KEY_C = 67;
			KEY_D = 68;
			KEY_E = 69;
			KEY_F = 70;
			KEY_G = 71;
			KEY_H = 72;
			KEY_I = 73;
			KEY_J = 74;
			KEY_K = 75;
			KEY_L = 76;
			KEY_M = 77;
			KEY_N = 78;
			KEY_O = 79;
			KEY_P = 80;
			KEY_Q = 81;
			KEY_R = 82;
			KEY_S = 83;
			KEY_T = 84;
			KEY_U = 85;
			KEY_V = 86;
			KEY_W = 87;
			KEY_X = 88;
			KEY_Y = 89;
			KEY_Z = 90;
			KEY_LEFT_BRACKET = 91;
			KEY_BACKSLASH = 92;
			KEY_RIGHT_BRACKET = 93;
			KEY_GRAVE_ACCENT = 96;
			KEY_WORLD_1 = 161;
			KEY_WORLD_2 = 162;
			KEY_ESCAPE = 256;
			KEY_ENTER = 257;
			KEY_TAB = 258;
			KEY_BACKSPACE = 259;
			KEY_INSERT = 260;
			KEY_DELETE = 261;
			KEY_RIGHT = 262;
			KEY_LEFT = 263;
			KEY_DOWN = 264;
			KEY_UP = 265;
			KEY_PAGE_UP = 266;
			KEY_PAGE_DOWN = 267;
			KEY_HOME = 268;
			KEY_END = 269;
			KEY_CAPS_LOCK = 280;
			KEY_SCROLL_LOCK = 281;
			KEY_NUM_LOCK = 282;
			KEY_PRINT_SCREEN = 283;
			KEY_PAUSE = 284;
			KEY_F1 = 290;
			KEY_F2 = 291;
			KEY_F3 = 292;
			KEY_F4 = 293;
			KEY_F5 = 294;
			KEY_F6 = 295;
			KEY_F7 = 296;
			KEY_F8 = 297;
			KEY_F9 = 298;
			KEY_F10 = 299;
			KEY_F11 = 300;
			KEY_F12 = 301;
			KEY_F13 = 302;
			KEY_F14 = 303;
			KEY_F15 = 304;
			KEY_F16 = 305;
			KEY_F17 = 306;
			KEY_F18 = 307;
			KEY_F19 = 308;
			KEY_F20 = 309;
			KEY_F21 = 310;
			KEY_F22 = 311;
			KEY_F23 = 312;
			KEY_F24 = 313;
			KEY_F25 = 314;
			KEY_KP_0 = 320;
			KEY_KP_1 = 321;
			KEY_KP_2 = 322;
			KEY_KP_3 = 323;
			KEY_KP_4 = 324;
			KEY_KP_5 = 325;
			KEY_KP_6 = 326;
			KEY_KP_7 = 327;
			KEY_KP_8 = 328;
			KEY_KP_9 = 329;
			KEY_KP_DECIMAL = 330;
			KEY_KP_DIVIDE = 331;
			KEY_KP_MULTIPLY = 332;
			KEY_KP_SUBTRACT = 333;
			KEY_KP_ADD = 334;
			KEY_KP_ENTER = 335;
			KEY_KP_EQUAL = 336;
			KEY_LEFT_SHIFT = 340;
			KEY_LEFT_CONTROL = 341;
			KEY_LEFT_ALT = 342;
			KEY_LEFT_SUPER = 343;
			KEY_RIGHT_SHIFT = 344;
			KEY_RIGHT_CONTROL = 345;
			KEY_RIGHT_ALT = 346;
			KEY_RIGHT_SUPER = 347;
			KEY_MENU = 348;
			KEY_LAST = KEY_MENU;
		}else {
			KEY_SPACE = -1;
			KEY_APOSTROPHE = -1;
			KEY_COMMA = -1;
			KEY_MINUS = -1;
			KEY_PERIOD = -1;
			KEY_SLASH = -1;
			KEY_0 = -1;
			KEY_1 = -1;
			KEY_2 = -1;
			KEY_3 = -1;
			KEY_4 = -1;
			KEY_5 = -1;
			KEY_6 = -1;
			KEY_7 = -1;
			KEY_8 = -1;
			KEY_9 = -1;
			KEY_SEMICOLON = -1;
			KEY_EQUAL = -1;
			KEY_A = -1;
			KEY_B = -1;
			KEY_C = -1;
			KEY_D = -1;
			KEY_E = -1;
			KEY_F = -1;
			KEY_G = -1;
			KEY_H = -1;
			KEY_I = -1;
			KEY_J = -1;
			KEY_K = -1;
			KEY_L = -1;
			KEY_M = -1;
			KEY_N = -1;
			KEY_O = -1;
			KEY_P = -1;
			KEY_Q = -1;
			KEY_R = -1;
			KEY_S = -1;
			KEY_T = -1;
			KEY_U = -1;
			KEY_V = -1;
			KEY_W = -1;
			KEY_X = -1;
			KEY_Y = -1;
			KEY_Z = -1;
			KEY_LEFT_BRACKET = -1;
			KEY_BACKSLASH = -1;
			KEY_RIGHT_BRACKET = -1;
			KEY_GRAVE_ACCENT = -1;
			KEY_WORLD_1 = -1;
			KEY_WORLD_2 = -1;
			KEY_ESCAPE = -1;
			KEY_ENTER = -1;
			KEY_TAB = -1;
			KEY_BACKSPACE = -1;
			KEY_INSERT = -1;
			KEY_DELETE = -1;
			KEY_RIGHT = -1;
			KEY_LEFT = -1;
			KEY_DOWN = -1;
			KEY_UP = -1;
			KEY_PAGE_UP = -1;
			KEY_PAGE_DOWN = -1;
			KEY_HOME = -1;
			KEY_END = -1;
			KEY_CAPS_LOCK = -1;
			KEY_SCROLL_LOCK = -1;
			KEY_NUM_LOCK = -1;
			KEY_PRINT_SCREEN = -1;
			KEY_PAUSE = -1;
			KEY_F1 = -1;
			KEY_F2 = -1;
			KEY_F3 = -1;
			KEY_F4 = -1;
			KEY_F5 = -1;
			KEY_F6 = -1;
			KEY_F7 = -1;
			KEY_F8 = -1;
			KEY_F9 = -1;
			KEY_F10 = -1;
			KEY_F11 = -1;
			KEY_F12 = -1;
			KEY_F13 = -1;
			KEY_F14 = -1;
			KEY_F15 = -1;
			KEY_F16 = -1;
			KEY_F17 = -1;
			KEY_F18 = -1;
			KEY_F19 = -1;
			KEY_F20 = -1;
			KEY_F21 = -1;
			KEY_F22 = -1;
			KEY_F23 = -1;
			KEY_F24 = -1;
			KEY_F25 = -1;
			KEY_KP_0 = -1;
			KEY_KP_1 = -1;
			KEY_KP_2 = -1;
			KEY_KP_3 = -1;
			KEY_KP_4 = -1;
			KEY_KP_5 = -1;
			KEY_KP_6 = -1;
			KEY_KP_7 = -1;
			KEY_KP_8 = -1;
			KEY_KP_9 = -1;
			KEY_KP_DECIMAL = -1;
			KEY_KP_DIVIDE = -1;
			KEY_KP_MULTIPLY = -1;
			KEY_KP_SUBTRACT = -1;
			KEY_KP_ADD = -1;
			KEY_KP_ENTER = -1;
			KEY_KP_EQUAL = -1;
			KEY_LEFT_SHIFT = -1;
			KEY_LEFT_CONTROL = -1;
			KEY_LEFT_ALT = -1;
			KEY_LEFT_SUPER = -1;
			KEY_RIGHT_SHIFT = -1;
			KEY_RIGHT_CONTROL = -1;
			KEY_RIGHT_ALT = -1;
			KEY_RIGHT_SUPER = -1;
			KEY_MENU = -1;
			KEY_LAST = KEY_MENU;
		}
	}

	public void create() {
		if(toolkit == ToolkitPlatform.desktop) {
			createGLFW();
		}
		
		KHRDebug.glDebugMessageCallbackKHR(new GLDebugMessageKHRCallbackI() {
			
			@Override
			public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
				
				StringBuilder b = new StringBuilder();
				b.append("[KHR DEBUG #"); b.append(id); b.append("] ");
				
				switch(source) {
				case KHRDebug.GL_DEBUG_SOURCE_API_KHR: b.append("[API - "); break;
				case KHRDebug.GL_DEBUG_SOURCE_APPLICATION_KHR: b.append("[APPLICATION - "); break;
				case KHRDebug.GL_DEBUG_SOURCE_SHADER_COMPILER_KHR: b.append("[SHADER COMPILER - "); break;
				case KHRDebug.GL_DEBUG_SOURCE_THIRD_PARTY_KHR: b.append("[THIRD PARTY - "); break;
				case KHRDebug.GL_DEBUG_SOURCE_OTHER_KHR: default: b.append("[OTHER - "); break;
				}
				
				switch(type) {
				case KHRDebug.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_KHR: b.append("DEPRECATED BEHAVIOR] "); break;
				case KHRDebug.GL_DEBUG_TYPE_ERROR_KHR: b.append("ERROR] "); break;
				default:
				case KHRDebug.GL_DEBUG_TYPE_OTHER_KHR: b.append("OTHER] "); break;
				case KHRDebug.GL_DEBUG_TYPE_PERFORMANCE_KHR: b.append("PERFORMANCE] "); break;
				case KHRDebug.GL_DEBUG_TYPE_PORTABILITY_KHR: b.append("PORTABILITY] "); break;
				case KHRDebug.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_KHR: b.append("UNDEFINED BEHAVIOR] "); break;
				}
				
				switch(severity) {
				default:
				case KHRDebug.GL_DEBUG_SEVERITY_LOW_KHR: b.append("[LOW Severity] "); break;
				case KHRDebug.GL_DEBUG_SEVERITY_MEDIUM_KHR: b.append("[MEDIUM Severity] "); break;
				case KHRDebug.GL_DEBUG_SEVERITY_HIGH_KHR: b.append("[SEVERE] "); break;
				}
				
				b.append(GLDebugMessageKHRCallback.getMessage(length, message));
				log.error(b.toString());
				
				StackTraceElement[] ex = new RuntimeException().getStackTrace();
				for(int i = 0; i < ex.length; ++i) {
					log.error("    at {}", ex[i]);
				}
				//throw new RuntimeException();
			}
			
		}, 0l);

		glEnable(KHRDebug.GL_DEBUG_OUTPUT_KHR);
		glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS_KHR);
	}
	
	public void checkError(String location) {
		int error;
		while((error = glGetError()) != GL_NO_ERROR) {
			switch(error) {
			case GL_INVALID_ENUM:
				log.error("GL ERROR: GL_INVALID_ENUM @ {}", location);
				break;
			case GL_INVALID_VALUE:
				log.error("GL ERROR: GL_INVALID_VALUE @ {}", location);
				break;
			case GL_INVALID_OPERATION:
				log.error("GL ERROR: GL_INVALID_OPERATION @ {}", location);
				break;
			case GL_INVALID_FRAMEBUFFER_OPERATION:
				log.error("GL ERROR: GL_INVALID_FRAMEBUFFER_OPERATION @ {}", location);
				break;
			case GL_OUT_OF_MEMORY:
				log.error("GL ERROR: GL_OUT_OF_MEMORY @ {}", location);
				break;
			default:
				log.error("GL ERROR: UNKNOWN @ {}", location);
				break;
			}
			Thread.dumpStack();
		}
	}

	private void createGLFW() {
		GLFWErrorCallback.createThrow().set();
		
		glfwInitHint(GLFW_ANGLE_PLATFORM_TYPE, platform.eglEnum);
		glfwInit();
		
		log.info("GLFW Version: {}", glfwGetVersionString());
		
		PointerBuffer buf = glfwGetMonitors();
		
		if(monitorNum > buf.limit() - 1) {
			monitorNum = buf.limit() - 1;
		}
		
		GLFWVidMode v = glfwGetVideoMode(buf.get(monitorNum));

		int w = v.width() / resolutionDiv;
		int h = v.height() / resolutionDiv;

		int x = (v.width() - w) / 2;
		int y = (v.height() - h) / 2;
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

		glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);
		glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_ES_API);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);

		glfwWindowHint(GLFW_CENTER_CURSOR, GLFW_TRUE);
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
		
        glfw_windowHandle = glfwCreateWindow(w, h, title, windowed ? NULL : buf.get(monitorNum), NULL);
        glfw_eglHandle = glfwGetEGLDisplay();
        
        if(!windowed) glfwSetWindowPos(glfw_windowHandle, x, y);
        
        int[] major = new int[] { 1 };
        int[] minor = new int[] { 4 };
        if(!eglInitialize(glfw_eglHandle, major, minor)) {
        	throw new RuntimeException("Could not initialize EGL");
        }
        
        EGL.createDisplayCapabilities(glfw_eglHandle, major[0], minor[0]);
        glfwMakeContextCurrent(glfw_windowHandle);
        
        caps = GLES.createCapabilities();

        log.info("OpenGL Version: {}", glGetString(GL_VERSION));
        log.info("OpenGL Renderer: {}", glGetString(GL_RENDERER));

        glfwSetKeyCallback(glfw_windowHandle, new GLFWKeyCallbackI() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if(keyEvents.size() < 256 && !(!keyRepeat && action == GLFW_REPEAT)) {
					keyEvents.add(new KeyboardEvent(key, (action == GLFW_PRESS) || (action == GLFW_REPEAT), action == GLFW_REPEAT));
				}
			}
		});
        
        glfwSetCharCallback(glfw_windowHandle, new GLFWCharCallbackI() {
			@Override
			public void invoke(long window, int codepoint) {
				if(keyChars.size() < 256) {
					keyChars.addAll(Character.toChars(codepoint));
				}
			}
		});
        
        glfwSetCursorPosCallback(glfw_windowHandle, new GLFWCursorPosCallbackI() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
				EaglContext.this.mousex = (int)xpos;
				EaglContext.this.mousey = (int)ypos;
			}
		});
        
        glfwSetMouseButtonCallback(glfw_windowHandle, new GLFWMouseButtonCallbackI() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				if(mouseEvents.size() < 256) {
					mouseEvents.add(new MouseEvent(action == GLFW_PRESS, button, EaglContext.this.mousex, EaglContext.this.mousey, false, 0.0f, 0.0f));
				}
			}
		});
        
        glfwSetWindowFocusCallback(glfw_windowHandle, new GLFWWindowFocusCallbackI() {
			@Override
			public void invoke(long window, boolean focused) {
				EaglContext.this.focused = focused;
			}
		});
        
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
	
	public static boolean contextAvailable() {
		try {
			GLES.getCapabilities();
			return true;
		}catch(IllegalStateException e) {
			return false;
		}
	}
	
	public void setTitle(String s) {
		if(toolkit == ToolkitPlatform.desktop) {
			glfwSetWindowTitle(glfw_windowHandle, title = s);
		}
	}
	
	public void setSize(int w, int h) {
		if(toolkit == ToolkitPlatform.desktop) {
			glfwSetWindowSize(glfw_windowHandle, w, h);
		}
	}
	
	public int getOuterWidth() {
		if(toolkit == ToolkitPlatform.desktop) {
			int[] w = new int[1];
			int[] h = new int[1];
			glfwGetWindowSize(glfw_windowHandle, w, h);
			return w[0];
		}
		return 0;
	}
	
	public int getOuterHeight() {
		if(toolkit == ToolkitPlatform.desktop) {
			int[] w = new int[1];
			int[] h = new int[1];
			glfwGetWindowSize(glfw_windowHandle, w, h);
			return h[0];
		}
		return 0;
	}

	public int getInnerWidth() {
		if(toolkit == ToolkitPlatform.desktop) {
			int[] w = new int[1];
			int[] h = new int[1];
			glfwGetFramebufferSize(glfw_windowHandle, w, h);
			return w[0];
		}
		return 0;
	}
	
	public int getInnerHeight() {
		if(toolkit == ToolkitPlatform.desktop) {
			int[] w = new int[1];
			int[] h = new int[1];
			glfwGetFramebufferSize(glfw_windowHandle, w, h);
			return h[0];
		}
		return 0;
	}
	
	public String getClipboardString() {
		if(toolkit == ToolkitPlatform.desktop) {
			return glfwGetClipboardString(glfw_windowHandle);
		}
		return null;
	}
	
	public void setClipboardString(String s) {
		if(toolkit == ToolkitPlatform.desktop) {
			glfwSetClipboardString(glfw_windowHandle, s);
		}
	}
	
	public void setFullscreen(boolean fs) {
		throw new RuntimeException("not implemented");
	}
	
	public boolean getFullscreen() {
		throw new RuntimeException("not implemented");
	}
	
	public void setIcons(BufferedImage[] icons) {
		try {
			try (MemoryStack memorystack = MemoryStack.stackPush()) {
				Buffer buffer = GLFWImage.mallocStack(icons.length, memorystack);
				for(int i = 0; i < icons.length; ++i) {
					buffer.position(i);
					buffer.width(icons[i].getWidth());
					buffer.height(icons[i].getHeight());
					
					int[] image = icons[i].getRGB(0, 0, icons[i].getWidth(), icons[i].getHeight(), null, 0, icons[i].getWidth());
					
					for(int j = 0; j < image.length; ++j) {
						image[j] = image[j] << 8;
					}
					
					ByteBuffer imageBuffer = memorystack.malloc(image.length * 4).order(ByteOrder.BIG_ENDIAN);
					
					imageBuffer.asIntBuffer().put(image);
					
					buffer.pixels(imageBuffer);
				}
				glfwSetWindowIcon(glfw_windowHandle, buffer);
			}
		}catch(Throwable t) {
		}
	}
	
	public void swapBuffers(boolean sync) {
		if(toolkit == ToolkitPlatform.desktop) {
			glfwSwapInterval(sync ? 1 : 0);
			glfwSwapBuffers(glfw_windowHandle);
		}
	}
	
	public void pollEvents() {
		if(toolkit == ToolkitPlatform.desktop) {
			glfwPollEvents();
		}
	}
	
	public boolean closeRequested() {
		if(toolkit == ToolkitPlatform.desktop) {
			return glfwWindowShouldClose(glfw_windowHandle);
		}
		return false;
	}
	
	private boolean keyRepeat = true;
	
	public boolean getKeyRepeatEnabled() {
		return keyRepeat;
	}
	
	public void setKeyRepeatEnabled(boolean e) {
		keyRepeat = e;
	}

	private final TCharLinkedList keyChars = new TCharLinkedList();
	
	public char nextKeyboardChar() {
		if(keyChars.size() > 0) {
			char c = keyChars.get(0);
			keyChars.remove(0, 1);
			return c;
		}else {
			return '\0';
		}
	}
	
	public static class KeyboardEvent {
		
		public final int code;
		public final boolean pressed;
		public final boolean repeated;
		
		protected KeyboardEvent(int code, boolean pressed, boolean repeated) {
			this.code = code;
			this.pressed = pressed;
			this.repeated = repeated;
		}
		
	}
	
	private final LinkedList<KeyboardEvent> keyEvents = new LinkedList();

	public KeyboardEvent keyboardNext() {
		if(keyEvents.size() > 0) {
			return keyEvents.remove(0);
		}else {
			return null;
		}
	}
	
	public boolean isKeyDown(int key) {
		if(toolkit == ToolkitPlatform.desktop) {
			return glfwGetKey(glfw_windowHandle, key) == GLFW_PRESS;
		}
		return false;
	}
	
	public String getKeyName(int key) {
		if(toolkit == ToolkitPlatform.desktop) {
			return glfwGetKeyName(key, 0);
		}
		return null;
	}
	
	public static class MouseEvent {

		public final boolean pressed;
		public final int button;
		public final int x;
		public final int y;
		
		public final boolean dwheel;
		public final float dwheelx;
		public final float dwheely;
		
		protected MouseEvent(boolean pressed, int button, int x, int y, boolean dwheel, float dwheelx, float dwheely) {
			this.pressed = pressed;
			this.button = button;
			this.x = x;
			this.y = y;
			this.dwheel = dwheel;
			this.dwheelx = dwheelx;
			this.dwheely = dwheely;
		}
	}

	private final LinkedList<MouseEvent> mouseEvents = new LinkedList();
	
	public MouseEvent mouseNext() {
		if(mouseEvents.size() > 0) {
			return mouseEvents.remove(0);
		}else {
			return null;
		}
	}
	
	public boolean isMouseDown(int button) {
		if(toolkit == ToolkitPlatform.desktop) {
			return glfwGetMouseButton(glfw_windowHandle, button) == GLFW_PRESS;
		}
		return false;
	}

	public void setMouseGrabbed(boolean grabbed) {
		if(toolkit == ToolkitPlatform.desktop) {
			glfwSetInputMode(glfw_windowHandle, GLFW_CURSOR, grabbed ? GLFW_CURSOR_HIDDEN : GLFW_CURSOR_NORMAL);
		}
	}
	
	private boolean focused = true;
	
	public boolean windowHasFocus() {
		return focused;
	}
	
	public boolean getMouseGrabbed() {
		if(toolkit == ToolkitPlatform.desktop) {
			return glfwGetInputMode(glfw_windowHandle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED;
		}
		return false;
	}
	
	private int mousex = 0;
	private int mousey = 0;
	
	public int mouseX() {
		return mousex;
	}
	
	public int mouseY() {
		return mousey;
	}
	
	public void setMousePos(int x, int y) {
		if(toolkit == ToolkitPlatform.desktop) {
			glfwSetCursorPos(glfw_windowHandle, x, y);
		}
	}
	
	/*

	public static class ControllerEvent {
		
		public final boolean pressed;
		public final int button;
		
		protected ControllerEvent(boolean pressed, int button) {
			this.pressed = pressed;
			this.button = button;
		}
		
	}
	
	private final LinkedList<ControllerEvent> controllerEvents = new LinkedList();

	public ControllerEvent controllerNext() {
		if(controllerEvents.size() > 0) {
			return controllerEvents.remove(0);
		}else {
			return null;
		}
	}
	
	public boolean getControllerButtonDown(int button) {
		return false;
	}
	
	public float getControllerMainX() {
		return 0f;
	}
	
	public float getControllerMainY() {
		return 0f;
	}
	
	public float getControllerPovX() {
		return 0f;
	}
	
	public float getControllerPovY() {
		return 0f;
	}
	*/

}

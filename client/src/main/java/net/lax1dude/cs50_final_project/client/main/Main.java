package net.lax1dude.cs50_final_project.client.main;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.lax1dude.cs50_final_project.Util;
import net.lax1dude.cs50_final_project.client.GameClient;
import net.lax1dude.cs50_final_project.client.GameConfiguration;
import net.lax1dude.cs50_final_project.client.renderer.opengl.EaglContext;
import net.lax1dude.cs50_final_project.client.renderer.opengl.ResourceLoader;

public class Main {
	
	public static final Logger log = LoggerFactory.getLogger("GameClient");
	
	public static String[] cmdArgs = null;
	public static OptionSet cmdOpts = null;
	
	public static EaglContext.ContextPlatform platform = EaglContext.ContextPlatform.angle;
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		Thread.currentThread().setName("Client Thread");
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		log.info("Starting {} {}...", GameConfiguration.gameName, GameConfiguration.versionString);
		
		cmdArgs = args;
		System.setProperty("joml.fastmath", "true");
		System.setProperty("joml.sinLookup", "true");
		System.setProperty("joml.format", "false");
		
		OptionParser optionparser = new OptionParser();
		optionparser.accepts("debug");
		optionparser.accepts("renderdoc");
		OptionSpec<File> assets = optionparser.accepts("dev-assets").withOptionalArg().ofType(File.class);
		OptionSpec<String> plaf = optionparser.accepts("platform").withOptionalArg().defaultsTo("default");
		
		cmdOpts = optionparser.parse(cmdArgs);
		
		if(cmdOpts.has("renderdoc")) {
			LaunchDialog d = new LaunchDialog();
			d.setLocationRelativeTo(null);
			d.setVisible(true);
			while(d.isVisible()) {
				Util.sleep(100l);
			}
			d.dispose();
		}
		
		if(cmdOpts.has(assets)) {
			File f = assets.value(cmdOpts).getAbsoluteFile();
			ResourceLoader.registerLoader(new ResourceLoader.ResourceLoaderFolder(f));
			log.info("Additional assets directory: {}", f.getAbsolutePath());
		}
		
		for(EaglContext.ContextPlatform s : EaglContext.ContextPlatform.values()) {
			if(s.id.equals(plaf.value(cmdOpts))) {
				platform = s;
				break;
			}
		}
		
		GameClient.instance.run();
	}
	
	public static GameClient createClientInstance() {
		return new GameClient(cmdOpts.has("debug"));
	}
	
}

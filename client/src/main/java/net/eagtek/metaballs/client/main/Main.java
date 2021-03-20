package net.eagtek.metaballs.client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.eagtek.metaballs.client.GameClient;
import net.eagtek.metaballs.client.GameConfiguration;

public class Main {
	
	public static final Logger log = LoggerFactory.getLogger("GameClient");
	
	public static String[] cmdArgs = null;
	public static OptionSet cmdOpts = null;
	
	public static void main(String[] args) {
		Thread.currentThread().setName("Client Thread");
		
		log.info("Starting {} {}...", GameConfiguration.gameName, GameConfiguration.versionString);
		
		cmdArgs = args;
		System.setProperty("joml.fastmath", "true");
		System.setProperty("joml.sinLookup", "true");
		System.setProperty("joml.format", "false");
		
		OptionParser optionparser = new OptionParser();
		optionparser.accepts("debug");
		
		cmdOpts = optionparser.parse(cmdArgs);
		
		GameClient.instance.run();
	}
	
	public static GameClient createClientInstance() {
		return new GameClient(cmdOpts.has("debug"));
	}
	
}

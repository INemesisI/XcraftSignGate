package me.INemesisI.XcraftSignGate;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class XcraftSignGate extends JavaPlugin {
	 
    //ClassListeners
	private final Blocklistener blocklistener = new Blocklistener(this);
	private final Playerlistener playerlistener = new Playerlistener(this);
    //ClassListeners
	public GateHandler gateHandler = new GateHandler(this);
       
    public Logger log = Logger.getLogger("Minecraft");//Define your logger
 
    @Override
	public void onDisable() {
    	gateHandler.saveAll();
    	log.info("[" + getDescription().getName() + "] disabled!");
    }
 
    @Override
	public void onEnable() {
               
    PluginManager pm = this.getServer().getPluginManager();

    pm.registerEvent(Event.Type.SIGN_CHANGE, blocklistener, Event.Priority.Normal, this);
    pm.registerEvent(Event.Type.BLOCK_PLACE, blocklistener, Event.Priority.Normal, this);
    pm.registerEvent(Event.Type.REDSTONE_CHANGE, blocklistener, Event.Priority.Normal, this);
    pm.registerEvent(Event.Type.BLOCK_BREAK, blocklistener, Event.Priority.Normal, this);
    pm.registerEvent(Event.Type.PLAYER_INTERACT, playerlistener, Event.Priority.Normal, this);
    
    gateHandler.load();
    log.info("[" + getDescription().getName()+"] v"+getDescription().getVersion()+" by INemesisI loaded!");
    }

    public String getName() {
      	return ChatColor.DARK_GRAY + "[" + this.getDescription().getName() + "] " + getChatColor();
   	}
    
    public ChatColor getChatColor() {
    	return ChatColor.DARK_AQUA;
    }
    
    public String capitalize(String name) {
    	String save = name.substring(0, name.indexOf("["));
    	name = name.substring(name.indexOf("["), name.indexOf("]")+1);
    	String[] split = name.split(" ");
    	String string = "";
    	for (int i=0;i<split.length;i++) {
    		String character = ""+ split[i].charAt(0);
    		int num;
			if (character.equals("[") ){  num = 1;
			string += "[";
			}
    		else  num = 0;
			if (i > 0) string += " ";
    	string += Character.toUpperCase(split[i].charAt(num)) + split[i].substring(num+1, split[i].length());
    	}
		return save+string;
    
    }
}
package me.INemesisI.XcraftSignGate;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;


public class Playerlistener extends PlayerListener{
 
        //You HAVE to have this!
        public static XcraftSignGate plugin;
       
        public Playerlistener(XcraftSignGate instance) {
                plugin = instance;
        }
        //You HAVE to have this!
 
        @Override
		public void onPlayerInteract(PlayerInteractEvent event){	
        	if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        		Block block = event.getClickedBlock(); 
        		if (block.getState() instanceof Sign) {
        			Sign sign = (Sign) block.getState();
        			String[] lines = sign.getLines();
        			if ((lines[1].contains("[Gate]")) && event.getPlayer().hasPermission("XcraftSignGate.use")) {
            			Gate gate = plugin.gateHandler.getGate(block);
            			if (gate == null) {
            				event.getPlayer().sendMessage(plugin.getName() + "Dieses Gate wurde nicht geladen...");	
            				plugin.gateHandler.add(block);
            				return;
           				}
           				gate.toggle();
        			}
        		}
        	}
        }
}
package me.INemesisI.XcraftSignGate;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class Blocklistener extends BlockListener {
    public static XcraftSignGate plugin;
   
    public Blocklistener(XcraftSignGate instance) {
            plugin = instance;
    }
    //You HAVE to have this!
    
    @Override
	public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockAgainst();   
        if (block.getState() instanceof Sign) {
        	if (plugin.gateHandler.getGate(block) != null)
        		event.setCancelled(true);
        }
        
    }
    
    @Override
	public void onBlockBreak(BlockBreakEvent event) {
    	Block block = event.getBlock();   
        if (block.getState() instanceof Sign) {
        	Sign sign = (Sign) block.getState();
        	String[] lines = sign.getLines();
        	if (lines[1].toLowerCase().contains("[gate]") && plugin.gateHandler.getGate(block) != null) {
        		plugin.gateHandler.remove(block);
        		event.getPlayer().sendMessage(plugin.getName() + "Das Gate wurde gelöscht!");
        	}
        }
        if (block.getType().equals(Material.FENCE)) 
        	if (plugin.gateHandler.isBlockFromGate(block)) {
        		event.setCancelled(true);
        		event.getPlayer().sendMessage(plugin.getName() + "Du hast keine Rechte, Gates zu zerstören!");
        	}      	
    }
    
    @Override
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
    	Block block = event.getBlock();
    	if (block.getState() instanceof Sign) {
    		Gate gate = plugin.gateHandler.getGate(block);
        	if (gate != null) {

        		if ((block.isBlockIndirectlyPowered() || block.isBlockPowered()) && gate.isClosed()) {
	        		gate.toggle();
        		}
        		if(!block.isBlockIndirectlyPowered() && !block.isBlockPowered() && !gate.isClosed()) {
	        		gate.toggle();
        		}
        	}
        }
    }
    
    @Override
	public void onSignChange(SignChangeEvent event) {
    	if (event.getLine(1).toLowerCase().contains("[gate]")) {
    		if (!event.getPlayer().hasPermission("XcraftSign.gate.create")) {
    			event.getPlayer().sendMessage(plugin.getName() + "Du hast keine Rechte, Gates zu erstellen!");
        		event.setCancelled(true);
        		return;
        	}
    		event.setLine(1, plugin.capitalize(event.getLine(1)));
    		plugin.gateHandler.add(event.getBlock());
    		event.getPlayer().sendMessage(ChatColor.BLUE +plugin.getName() + "Das Gate wurde erstellt");
    	}
		else return;
    }
}

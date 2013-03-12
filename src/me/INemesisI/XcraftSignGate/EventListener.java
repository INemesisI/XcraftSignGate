package me.INemesisI.XcraftSignGate;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventListener implements Listener {
	public static XcraftSignGate plugin;

	public EventListener(XcraftSignGate instance) {
		plugin = instance;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		// prevent placing blocks when clicking a gate-sign!
		Block block = event.getBlockAgainst();
		if (block.getState() instanceof Sign) {
			if (plugin.gateHandler.getGate(block) != null)
				event.setCancelled(true);
		}

	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block.getState() instanceof Sign) {
			Sign sign = (Sign) block.getState();
			String[] lines = sign.getLines();
			if (lines[1].toLowerCase().equals("[Gate]") && plugin.gateHandler.getGate(block) != null) {
				plugin.gateHandler.remove(block);
				event.getPlayer().sendMessage(plugin.getCName() + "Das Gate wurde gelöscht!");
			}
		}
		if (block.getType().equals(Material.FENCE))
			if (plugin.gateHandler.isBlockFromGate(block)) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(plugin.getCName() + "Du hast keine Rechte, Gates zu zerstören!");
			}
	}

	@EventHandler
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		Block block = event.getBlock();
		if (block.getState() instanceof Sign) {
			Gate gate = plugin.gateHandler.getGate(block);
			if (gate != null) {
				if ((block.isBlockIndirectlyPowered() || block.isBlockPowered()) && gate.isClosed()) {
					gate.toggle();
				}
				if (!block.isBlockIndirectlyPowered() && !block.isBlockPowered() && !gate.isClosed()) {
					gate.toggle();
				}
			}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.getLine(1).toLowerCase().equals("[Gate]")) {
			if (!event.getPlayer().hasPermission("XcraftSignGate.create")) {
				event.getPlayer().sendMessage(plugin.getCName() + "Du hast keine Rechte, Gates zu erstellen!");
				event.setCancelled(true);
				return;
			}
			event.setLine(1, event.getLine(1).replace("g", "G"));
			plugin.gateHandler.add(event.getBlock());
			if (plugin.gateHandler.getGate(event.getBlock()) != null)
				event.getPlayer().sendMessage(plugin.getCName() + "Das Gate wurde erstellt");
			else {
				event.getPlayer().sendMessage(
						plugin.getCName() + ChatColor.RED + "Das Gate konnte nicht erstellt werden!");
				event.setCancelled(true);
			}
		} else
			return;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			if (block.getState() instanceof Sign) {
				Sign sign = (Sign) block.getState();
				String[] lines = sign.getLines();
				if ((lines[1].equals("[Gate]")) && event.getPlayer().hasPermission("XcraftSignGate.use")) {
					Gate gate = plugin.gateHandler.getGate(block);
					if (gate == null) {
						event.getPlayer().sendMessage(plugin.getCName() + "Dieses Gate wurde nicht geladen...");
						plugin.gateHandler.add(block);
						return;
					}
					gate.toggle();
				}
			}
		}
	}
}

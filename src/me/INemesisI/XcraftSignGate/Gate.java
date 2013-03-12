package me.INemesisI.XcraftSignGate;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class Gate {
	private int id;
	private ArrayList<Block> signs = new ArrayList<Block>(); // all signs for a fence
	private ArrayList<Block> fences = new ArrayList<Block>(); // all source fences
	private ArrayList<Block> antigrief = new ArrayList<Block>(); // fence blocks when closed
	private boolean closed;

	public Gate(int id, ArrayList<Block> signs, ArrayList<Block> fences, boolean closed) {
		this.setId(id);
		this.setSigns(signs);
		this.setFences(fences);
		this.setClosed(closed);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<Block> getSigns() {
		return signs;
	}

	public void setSigns(ArrayList<Block> signs) {
		this.signs = signs;
	}

	public ArrayList<Block> getFences() {
		return fences;
	}

	public void setFences(ArrayList<Block> fences) {
		this.fences = fences;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public ArrayList<Block> getAntigrief() {
		return antigrief;
	}

	public void toggle() {
		for (Block fence : fences) {
			for (int i = 1; i <= 15; i++) {
				Block relative = fence.getRelative(BlockFace.DOWN, i);
				Material type = relative.getType();
				if (closed && type.equals(Material.FENCE)) {
					relative.setType(Material.AIR);
					relative.setData((byte) 1);
				}
				if (!closed
						&& (type.equals(Material.AIR) || type.equals(Material.WATER) || type.equals(Material.LAVA)
								|| type.equals(Material.STATIONARY_LAVA) || type.equals(Material.STATIONARY_WATER))) {
					relative.setType(Material.FENCE);
					antigrief.add(relative);
				}
			}

		}
		if (isClosed()) {
			antigrief.clear();
			setClosed(false);
		} else
			setClosed(true);
	}
}

package me.INemesisI.XcraftSignGate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class GateHandler {
	private FileConfiguration config;
	private File file;
	private int nextid = 1;
	private ArrayList<Gate> gatelist = new ArrayList<Gate>();
	XcraftSignGate plugin;

	public GateHandler(XcraftSignGate instance) {
		plugin = instance;
	}

	public void load() {
		File folder = plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdirs();
			folder.setWritable(true);
			folder.setExecutable(true);
		}
		file = new File(plugin.getDataFolder(), "gates.yml");
		config = YamlConfiguration.loadConfiguration(file);
		for (String id : config.getKeys(false)) {
			ArrayList<Block> signs = new ArrayList<Block>();
			ArrayList<Block> fences = new ArrayList<Block>();
			boolean closed;
			List<String> temp;
			temp = config.getStringList(id + ".Signs");
			for (String s : temp) {
				Block b = StringToBlock(s);
				if (b != null) signs.add(b);
			}
			temp = config.getStringList(id + ".Fences");
			for (String s : temp) {
				Block b = StringToBlock(s);
				if (b != null) fences.add(b);
			}
			closed = config.getBoolean(id + ".Closed");
			int ID = Integer.parseInt(id);
			gatelist.add(new Gate(ID, signs, fences, closed));
			if (ID > nextid) nextid = ID;
		}
		plugin.log.info("[" + plugin.getDescription().getName() + "] " + gatelist.size() + " Gate(s) loaded!");
	}

	public void reload() {
		saveAll();
		gatelist.clear();
		load();
	}

	public void saveAll() {
		for (Gate gate : gatelist) {
			save(gate);
		}
		try {
			config.save(file);
		} catch (IOException e) {
		}
	}

	public void save(Gate gate) {
		config.set(gate.getId() + ".Signs", getStringList(gate.getSigns()));
		config.set(gate.getId() + ".Closed", gate.isClosed());
		config.set(gate.getId() + ".Fences", getStringList(gate.getFences()));
	}

	public void updateCFG(Gate gate) {
		config.set(gate.getId() + ".closed", gate.isClosed());
	}

	public void remove(Block block) {
		for (Gate gate : gatelist) {
			if (gate.getSigns().contains(block)) {
				gate.getSigns().remove(block);
				if (!gate.getSigns().isEmpty()) {
					save(gate);
				} else {
					if (gate.isClosed()) gate.toggle();
					gatelist.remove(gate);
					config.set(gate.getId() + "", null);
				}
				break;
			}
		}
	}

	public void add(Block block) {
		ArrayList<Block> fences = getFencesInRegion(block, 5);
		Block fence = getNearestBlock(block, fences);
		if (fence == null) return;
		BlockFace face = getBlockDirection(fence);
		if (face == null) return;
		fences = getGateBlocks(block, fence, face);
		for (Gate gate : gatelist) {
			if (listequals(gate.getFences(), fences)) {
				gate.getSigns().add(block);
				return;
			}
		}
		ArrayList<Block> signs = new ArrayList<Block>();
		signs.add(block);
		Gate gate = new Gate(nextid, signs, fences, false);
		nextid++;
		gatelist.add(gate);
		save(gate);
	}

	public ArrayList<Block> getGateBlocks(Block sign, Block block, BlockFace face) {
		Block save = block;
		BlockFace oppface = face.getOppositeFace();
		ArrayList<Block> blocks = new ArrayList<Block>();
		blocks.add(block);
		// for returned direction
		for (int i = 1; i <= 15; i++) {
			Block fence = getFenceInVertDirection(block.getRelative(face, 1), 1);
			if (fence == null) break;
			blocks.add(fence);
			block = fence;
		}
		block = save;
		// for opposite direction
		for (int i = 1; i <= 15; i++) {
			Block fence = getFenceInVertDirection(block.getRelative(oppface, 1), 1);
			if (fence == null) break;
			blocks.add(fence);
			block = fence;
		}
		return blocks;
	}

	public ArrayList<Block> getFencesInRegion(Block sign, int r) {

		int x = sign.getX();
		int y = sign.getY();
		int z = sign.getZ();
		ArrayList<Block> list = new ArrayList<Block>();
		for (int b = y + r; b >= y - r; b--) {
			for (int c = z - r; c <= z + r; c++) {
				for (int a = x - r; a <= x + r; a++) {
					Block block = sign.getWorld().getBlockAt(a, b, c);
					if (block.getType().equals(Material.FENCE)) {
						Material type = block.getRelative(BlockFace.DOWN).getType();
						if (type.equals(Material.AIR) || type.equals(Material.WATER) || type.equals(Material.LAVA) || type.equals(Material.STATIONARY_LAVA) || type.equals(Material.STATIONARY_WATER)) {
							list.add(block);
						}
					}
				}
			}
		}
		return list;
	}

	public BlockFace getBlockDirection(Block fence) {
		ArrayList<Block> blocks = getFencesInRegion(fence, 1);
		for (int i = 0; i < blocks.size(); i++) {
			if (blocks.get(i).getY() != fence.getY()) {
				Block block = blocks.get(i);
				blocks.set(i, fence.getWorld().getBlockAt(block.getX(), fence.getY(), block.getZ()));
			}
			BlockFace face = fence.getFace(blocks.get(i));
			if (!face.equals(BlockFace.SELF)) return face;
		}
		return null;
	}

	public Block getFenceInVertDirection(Block block, int vert) {
		for (int i = vert; i >= 0; i--) {
			Block checkdown = block.getRelative(BlockFace.DOWN, i);
			if (checkdown.getType().equals(Material.FENCE)) return checkdown;
		}
		for (int i = 1; i <= vert; i++) {

			Block checkup = block.getRelative(BlockFace.UP, i);
			if (checkup.getType().equals(Material.FENCE)) return checkup;
		}
		return null;

	}

	public Block getNearestBlock(Block sign, ArrayList<Block> blocks) {
		if (blocks.size() == 0) return null;
		if (blocks.size() == 1) return blocks.get(0);
		Block block = blocks.get(0);
		for (int i = 1; i < blocks.size(); i++) {
			Block check = blocks.get(i);
			int blockx = Math.abs(sign.getX() - block.getX());
			int blocky = Math.abs(sign.getY() - block.getY());
			int blockz = Math.abs(sign.getZ() - block.getZ());
			if (blockx == 0) blockx = 1;
			if (blocky == 0) blocky = 1;
			if (blockz == 0) blockz = 1;
			int region1 = blockx * blocky * blockz;
			int checkx = Math.abs(sign.getX() - check.getX());
			int checky = Math.abs(sign.getY() - check.getY());
			int checkz = Math.abs(sign.getZ() - check.getZ());
			if (checkx == 0) checkx = 1;
			if (checky == 0) checky = 1;
			if (checkz == 0) checkz = 1;
			int region2 = checkx * checky * checkz;
			if (region1 > region2) block = check;
		}
		return block;
	}

	public Block StringToBlock(String name) {
		String[] split = name.split(",");
		for (int a = 0; a < split.length; a++) {
		}
		int x = Integer.parseInt(split[0]);
		int y = Integer.parseInt(split[1]);
		int z = Integer.parseInt(split[2]);
		String w = split[3];
		World world = plugin.getServer().getWorld(w);
		if (world != null) return world.getBlockAt(x, y, z);
		else
			return null;
	}

	public String BlockToString(Block block) {
		return block.getX() + "," + block.getY() + "," + block.getZ() + "," + block.getWorld().getName();
	}

	private Object getStringList(ArrayList<Block> blist) {
		ArrayList<String> slist = new ArrayList<String>();
		for (Block block : blist) {
			slist.add(BlockToString(block));
		}
		return slist;
	}

	public boolean listequals(ArrayList<Block> list1, ArrayList<Block> list2) {
		for (Block block : list1) {
			list2.remove(block);
		}
		return list2.isEmpty();
	}

	public boolean isBlockFromGate(Block block) {
		for (Gate gate : gatelist) {
			if (gate.getFences().contains(block) || gate.getAntigrief().contains(block)) return true;
		}
		return false;
	}

	public Gate getGate(Block block) {
		for (Gate gate : gatelist) {
			if (gate.getSigns().contains(block)) return gate;
		}
		return null;
	}
}

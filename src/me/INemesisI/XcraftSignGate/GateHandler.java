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
	private final ArrayList<Gate> gatelist = new ArrayList<Gate>();
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
				Block b = this.StringToBlock(s);
				if (b != null) {
					signs.add(b);
				}
			}
			temp = config.getStringList(id + ".Fences");
			for (String s : temp) {
				Block b = this.StringToBlock(s);
				if (b != null) {
					fences.add(b);
				}
			}
			closed = config.getBoolean(id + ".Closed");
			int ID = Integer.parseInt(id);
			gatelist.add(new Gate(ID, signs, fences, closed));
			if (ID > nextid) {
				nextid = ID;
			}
		}
		plugin.log.info("[" + plugin.getDescription().getName() + "] " + gatelist.size() + " Gate(s) loaded!");
	}

	public void reload() {
		this.saveAll();
		gatelist.clear();
		this.load();
	}

	public void saveAll() {
		for (Gate gate : gatelist) {
			this.save(gate);
		}
		try {
			config.save(file);
		} catch (IOException e) {
		}
	}

	public void save(Gate gate) {
		config.set(gate.getId() + ".Signs", this.getStringList(gate.getSigns()));
		config.set(gate.getId() + ".Closed", gate.isClosed());
		config.set(gate.getId() + ".Fences", this.getStringList(gate.getFences()));
	}

	public boolean remove(Block block) {
		for (Gate gate : gatelist) {
			if (gate.getSigns().contains(block)) {
				gate.getSigns().remove(block);
				if (!gate.getSigns().isEmpty()) {
					this.save(gate);
				} else {
					if (gate.isClosed()) {
						gate.toggle();
					}
					gatelist.remove(gate);
					config.set(gate.getId() + "", null);
				}
				return true;
			}
		}
		return false;
	}

	public boolean add(Block block) {
		ArrayList<Block> fences = this.getFencesInRegion(block, 5);
		Block fence = this.getNearestBlock(block, fences);
		if (fence == null) {
			System.out.println("fence == null!");
			return false;
		}
		fence.setType(Material.BEDROCK);
		BlockFace face = this.getBlockDirection(fence);
		if (face == null) {
			System.out.println("face == null!");
			return false;
		}
		fences = this.getGateBlocks(block, fence, face);
		for (Gate gate : gatelist) {
			for (int i = 0; i < fences.size(); i++) {
				if (gate.getFences().contains(fences.get(i))) {
					gate.getSigns().add(block);
					return true;
				}
			}
		}
		ArrayList<Block> signs = new ArrayList<Block>();
		signs.add(block);
		Gate gate = new Gate(nextid++, signs, fences, false);
		gatelist.add(gate);
		this.save(gate);
		return true;
	}

	public ArrayList<Block> getGateBlocks(Block sign, Block block, BlockFace face) {
		Block save = block;
		BlockFace oppface = face.getOppositeFace();
		ArrayList<Block> blocks = new ArrayList<Block>();
		blocks.add(block);
		// for returned direction
		for (int i = 1; i <= 15; i++) {
			Block fence = this.getFenceInVertDirection(block.getRelative(face, i), 1);
			if (fence == null) {
				break;
			}
			blocks.add(fence);
			block = fence;
		}
		block = save;
		// for opposite direction
		for (int i = 1; i <= 15; i++) {
			Block fence = this.getFenceInVertDirection(block.getRelative(oppface, i), 1);
			if (fence == null) {
				break;
			}
			blocks.add(fence);
			block = fence;
		}
		return blocks;
	}

	public ArrayList<Block> getFencesInRegion(Block sign, int r) {
		ArrayList<Block> list = new ArrayList<Block>();
		for (int x = sign.getX() - r; x <= (sign.getX() + r); x++) {
			for (int z = sign.getZ() - r; z <= (sign.getZ() + r); z++) {
				for (int y = sign.getY() - r; y <= (sign.getY() + r); y++) {
					Block block = sign.getWorld().getBlockAt(x, y, z);
					if (block.getType().equals(Material.FENCE)) {
						Material type = block.getRelative(BlockFace.DOWN).getType();
						if (type.equals(Material.AIR) || type.equals(Material.WATER) || type.equals(Material.LAVA)
								|| type.equals(Material.STATIONARY_LAVA) || type.equals(Material.STATIONARY_WATER)) {
							list.add(block);
						}
					}
				}
			}
		}
		return list;
	}

	public BlockFace getBlockDirection(Block fence) {
		ArrayList<Block> blocks = this.getFencesInRegion(fence, 1);
		for (Block block : blocks) {
			if (block.getY() != fence.getY()) {
				block = block.getWorld().getBlockAt(block.getX(), fence.getY(), block.getZ());
			}
			BlockFace face = fence.getFace(block);
			System.out.println(face);
			if (face != BlockFace.SELF) {
				return face;
			}
		}
		return null;
	}

	public Block getFenceInVertDirection(Block block, int dist) {
		for (int i = 1; i <= dist; i++) {
			Block check = block.getRelative(BlockFace.DOWN, i);
			if (check.getType().equals(Material.FENCE)) {
				return check;
			}
			check = block.getRelative(BlockFace.UP, i);
			if (check.getType().equals(Material.FENCE)) {
				return check;
			}
		}
		return null;

	}

	public Block getNearestBlock(Block sign, ArrayList<Block> blocks) {
		if (blocks.size() == 0) {
			return null;
		}
		if (blocks.size() == 1) {
			return blocks.get(0);
		}
		int mindist = -1;
		Block mindistblock = null;
		for (Block block : blocks) {
			int dist = (int) Math.sqrt(Math.pow(Math.abs(sign.getX()) - Math.abs(block.getX()), 2)
					+ Math.pow(Math.abs(sign.getY()) - Math.abs(block.getY()), 2)
					+ Math.pow(Math.abs(sign.getZ()) - Math.abs(block.getZ()), 2));
			System.out.println("mindist: " + mindist + ", dist: " + dist);
			System.out.println(block.toString());
			if (mindist == -1 || dist < mindist) {
				mindist = dist;
				mindistblock = block;
			}
		}
		return mindistblock;
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
		if (world != null) {
			return world.getBlockAt(x, y, z);
		} else {
			return null;
		}
	}

	public String BlockToString(Block block) {
		return block.getX() + "," + block.getY() + "," + block.getZ() + "," + block.getWorld().getName();
	}

	private Object getStringList(ArrayList<Block> blist) {
		ArrayList<String> slist = new ArrayList<String>();
		for (Block block : blist) {
			slist.add(this.BlockToString(block));
		}
		return slist;
	}

	public boolean isBlockFromGate(Block block) {
		for (Gate gate : gatelist) {
			if (gate.getFences().contains(block) || gate.getAntigrief().contains(block)) {
				return true;
			}
		}
		return false;
	}

	public Gate getGate(Block block) {
		for (Gate gate : gatelist) {
			if (gate.getSigns().contains(block)) {
				return gate;
			}
		}
		return null;
	}
}

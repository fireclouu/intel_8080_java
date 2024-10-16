package com.fireclouu.intel8080emu.emulator;
import java.util.LinkedHashMap;

public class Guest {
	public class Media {
		public enum AUDIO {
			FIRE (0),
			PLAYER_EXPLODED (1),
			SHIP_INCOMING (2),
			ALIEN_MOVE_1 (3),
			ALIEN_MOVE_2 (3),
			ALIEN_MOVE_3 (3),
			ALIEN_MOVE_4 (3),
			ALIEN_KILLED (0),
			SHIP_HIT (1);

			private int id;
			AUDIO(int id) {
				this.id = id;
			}

			public int getId() {
				return id;
			}

			public void setId(int id) {
				this.id = id;
			}
		}
	}
	
	public class Display {
		public enum Orientation {
			DEFAULT, PORTRAIT, LANDSCAPE;
		}
		public static final int WIDTH = (32 * 8); // 256 (32 = byte per line; 8 = bit per byte)
		public static final int HEIGHT = (224);   // 224
	}
	
	public static LinkedHashMap<String, Integer> mapFileData;
	private final int MEMORY_MAP_ROM_MIN = 0x0000;
	private final int MEMORY_MAP_ROM_MAX = 0x1FFF;
	private final int MEMORY_MAP_RAM_MIN = 0x2000;
	private final int MEMORY_MAP_RAM_MAX = 0x23FF;
	private final int MEMORY_MAP_VRAM_MIN = 0x2400;
	private final int MEMORY_MAP_VRAM_MAX = 0x3FFF;
	private final int RAM_MIRROR = 0x4000;
	
	private final int SIZE_ROM = (MEMORY_MAP_ROM_MAX - MEMORY_MAP_ROM_MIN) + 1;
	private final int SIZE_RAM = (MEMORY_MAP_RAM_MAX - MEMORY_MAP_RAM_MIN) + 1;
	private final int SIZE_VRAM = (MEMORY_MAP_VRAM_MAX - MEMORY_MAP_VRAM_MIN) + 1;
	
    private final Cpu cpu;
    private final Mmu mmu;
	private final short[] memoryRom = new short[SIZE_ROM];
	private final short[] memoryRam = new short[SIZE_RAM];
	private final short[] memoryVram = new short[SIZE_VRAM];

    public Guest() {
		mapFileData = new LinkedHashMap<>();
		mapFileData.put("invaders.h", 0x0000);
		mapFileData.put("invaders.g", 0x0800);
		mapFileData.put("invaders.f", 0x1000);
		mapFileData.put("invaders.e", 0x1800);
		
        this.mmu = new Mmu(this);
        this.cpu = new Cpu(mmu);
    }

    public Cpu getCpu() {
        return this.cpu;
    }

    public Mmu getMmu() {
        return this.mmu;
    }
	
	public short getDataOnRom(int address) {
		return memoryRom[address];
	}
	
	public short getDataOnRam(int address) {
		return memoryRam[address];
	}
	
	public short getDataOnVram(int address) {
		return memoryVram[address];
	}
	
	public short[] getMemoryVram() {
		return memoryVram;
	}
	
	public void writeMemoryRom(int address, short value) {
		memoryRom[address] = value;
	}
	
	public void writeMemoryRam(int address, short value) {
		memoryRam[address] = value;
	}
	
	public void writeMemoryVram(int address, short value) {
		memoryVram[address] = value;
	}
}

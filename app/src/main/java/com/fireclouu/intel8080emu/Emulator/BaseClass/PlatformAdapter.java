package com.fireclouu.intel8080emu.Emulator.BaseClass;

import com.fireclouu.intel8080emu.Emulator.CpuComponents;
import com.fireclouu.intel8080emu.Emulator.Emulator;
import com.fireclouu.intel8080emu.R;

import java.io.IOException;
import java.io.InputStream;
import com.fireclouu.intel8080emu.Emulator.*;

public abstract class PlatformAdapter implements Runnable, ResourceAdapter
{
	private Thread master;

	protected Emulator emulator;
	protected CpuComponents cpu;
	protected DisplayAdapter display;

	public static String OUT_MSG = "System OK!";
	public static String[] BUILD_MSG;
	public static int MSG_COUNT = 0;
	
	// Stream file
	public abstract InputStream openFile(String romName);
	
	@Override
	public void run() {
		emulator.startEmulation(cpu, display, this);
	}
	
	public PlatformAdapter(DisplayAdapter display) {
		this.display = display;
	}
	
	public void setDisplay (DisplayAdapter display) {
		this.display = display;
	}
	
	// Main
	public void startOp() {
		// mmu inject
		Mmu.resource = this;
		// initial file check
		if (!isTestFile()) {
				if(!isAllFileOK()) {
				OUT_MSG = "Some files could be corrupted, or no files specified";
				System.out.println(OUT_MSG);
				return;
			}	
			// Start emulation
			startMain();
			return;
		}
		// test file
		startTest();
	}

	public void init() {
		// initialize cpu components
		cpu = new CpuComponents();
		// initialize emulator
		emulator = new Emulator();
		// media
		setEffectShipHit(R.raw.ship_hit);
		setEffectAlienKilled(R.raw.alien_killed);
		setEffectAlienMove(
			R.raw.enemy_move_1,
			R.raw.enemy_move_2,
			R.raw.enemy_move_3,
			R.raw.enemy_move_4
		);
		setEffectFire(R.raw.fire);
		setEffectPlayerExploded(R.raw.explosion);
		setEffectShipIncoming(R.raw.ship_incoming);
	}

	private void startMain() {
		// reset objects
		init();
		// load and start emulation
		if(loadSplitFiles() == 0) {
			master = new Thread(this);
			master.start();
		}
	}
	
	public static short[][] tf = new short[StringUtils.File.FILES.length][0x10_000];
	private void startTest() {
		// init
		init();
		// trigger debug
		StringUtils.Component.DEBUG = true;
		// testfiles container
		int counter = 0;
		for (String files : StringUtils.File.FILES)
		{
			tf[counter] = loadFile(files, 0x100, false);
			counter++;
		}
		
		// BUILD MSG
		BUILD_MSG = new String[65355];
		BUILD_MSG[0] = "";

		// reset objects
		init();

		// start emulation
		master = new Thread(this);
		master.start();
	}

	// check all files
	private boolean isAllFileOK() {
		for (String files : StringUtils.File.FILES) {
			if (!isAvailable(files)) return false;
		}
		
		return true;
	}
	// Read and buffer file
	public short[] loadFile(String filename, int addr, boolean sizeActual) {
		// holder
		short[] holder = null;
		short[] tmp = new short[0x10_000];
		// read file stream
		InputStream file = openFile(filename);
		// piece container
		short read;
		int counter = 0;
		try
		{
			while ((read = (short) file.read()) != -1) {
				tmp[addr++] = read;
				counter++;
			}
		} catch (IOException e) {
			OUT_MSG = filename + " cannot be read!";
			return null;
		}
		
		holder = new short[(sizeActual ? counter : StringUtils.Component.PROGRAM_LENGTH)];
		
		counter = 0;
		for (short tmp2 : tmp)
		{
			holder[counter++] = tmp2;
		}
		
		return holder;
	}
	
	// Read and buffer file
	public void loadFile(String filename, int addr) {
		// read file stream
		InputStream file = openFile(filename);
		// piece container
		short read;
		
		try
		{
			while ((read = (short) file.read()) != -1) {
				Mmu.writeMemory(cpu, addr++, read);
			}
		} catch (IOException e) {
			OUT_MSG = filename + " cannot be read!";
		}
	}
	
	private byte loadSplitFiles() {
		int counter = 0;
		for (String files : StringUtils.File.FILES) {
			if (isAvailable(files)) {
				loadFile(files, StringUtils.File.ROM_ADDRESS[counter++]);
			} else {
				System.out.println(OUT_MSG);
				return 1; // ERROR
			}
		}
		return 0; // SUCCESS
	}

	// File check
	private boolean isAvailable(String filename) {
		if (StringUtils.File.FILES.length == 0) {
			OUT_MSG = "No files specified.";
			return false;
		}
		if (StringUtils.File.ROM_ADDRESS.length == 0) {
			OUT_MSG = "File online, but no starting memory address specified.";
			return false;
		}
		if (StringUtils.File.ROM_ADDRESS.length != StringUtils.File.FILES.length) {
			OUT_MSG = "File online, but roms and memory address unaligned.";
			return false;
		}
		try
		{
			if (openFile(filename) == null) {
				OUT_MSG = "File \"" + filename + "\" could not be found.";
				return false;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		}
		OUT_MSG = "File online , loaded successfully!";
		return true;
	}

	private boolean isTestFile() {
		for(String name : StringUtils.File.FILES) {
			switch (name) {
				case "cpudiag.bin":
				case "8080EX1.COM":
				case "8080EXER.COM":
				case "CPUTEST.COM":
				case "8080EXM.COM":
				case "8080PRE.COM":
				case "TST8080.COM":
					return true;
			}
		}
		return false;
	}
	
	// Machine scenarios
	public void appPause() {
		Emulator.stateMaster = false;
	}
	
	public void setHighscore(int data) {
		
		int storedHiscore = getPrefs(StringUtils.ITEM_HISCORE);
		
		if (data > storedHiscore)
		{
			putPrefs(StringUtils.ITEM_HISCORE, data);
		}
	}

	public void appResume() {
		Emulator.stateMaster = true;
	}
}

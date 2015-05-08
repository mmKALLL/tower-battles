package mrtb;

import java.io.File;

/** 
 * An executable class that initializes the game, setting up the manager.
 * Run with the argument "-debug" to enable debug messages to the console.
 */

public class Launcher {
	public static void main(String[] args) throws Exception {
		// Ensure r/w access.
		if ((new File("profile.txt").canWrite() && new File("profile.txt").canRead())) {
			Manager.initialize(args);
		} else {
			throw new Exception("Tower Battles doesn't have read/write access or is missing some files. Please try running it as an administrator, grant the specified rights or try reinstalling the game.");
		}
	}
}

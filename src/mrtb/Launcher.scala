package mrtb

/**
 * An executable object that initializes the game, setting up the manager.
 */

object Launcher {
  // ensure r/w access
  def main(args: Array[String]) = {
    Manager.initialize
  }
  
}
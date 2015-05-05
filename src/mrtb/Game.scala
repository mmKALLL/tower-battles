package mrtb

import scala.swing.SimpleSwingApplication

/**
 * An executable object that initializes the game, setting up the interface and manager.
 */

object Game extends SimpleSwingApplication {
  
  Manager.initialize
  val interface = new mrtb.gui.GUI
  
  def main = super.main(null)
  
  def top = interface.top
  
}
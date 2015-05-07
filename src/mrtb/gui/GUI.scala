package mrtb.gui

import scala.swing.SimpleSwingApplication
import scala.swing.{MainFrame, FlowPanel, Button}
import java.awt.Dimension

/**
 * The GUI class. Initially I attempted to set it up using Scage, but due to an
 * illegal inheritance related to self-types and scage's very poor documentation, I had to give up
 * on that after battling with it for almost a whole day. My next attempt was at using Slick and
 * lwjgl 3.0, but I was unable to run anything using them - I had lots of trouble setting the build path
 * as well. It might be a Java/Scala issue, or perhaps I was using incompatible versions. In the end, I decided
 * to cut my losses short and get on with Scala Swing, abandoning most of the visual stylishness I had
 * planned to create. Perhaps it was naïve from me to expect being able to set up a gfx library within
 * one or two hours.
 * 
 * As for the implementation here, the game's GUI essentially consists of two screens - the main menu
 * and the actual gameplay. The panel that represents them is the singleton object called GameScreen, and
 * the various UI elements are painted on the panel. Due to time restrictions, there might be some discrepancy
 * between Scala and Java style implementations regarding Swing and the event handling. I aimed to create a
 * completely graphical UI, with as little reliance on Swing's components as possible. I know that this has
 * a lot of downsides, but feel the most comfortable implementing such an interface, and it's definitely
 * the most powerful option available, if a bit resource-intensive. (Relying on Graphics2D might be pushing
 * it performance-wise, but at this point I feel like it's justifiable.)
 * 
 * If there was more time, I'd create an abstract class that would make implementing other UIs easier.
 * Right now, extensibility by modifying or adding code is not one of my top priorities, although
 * I certainly intend to keep the system flexible to changes and resilient to errors.
 */

abstract class SwingInterface extends MainFrame {
  def enterMenu
  def enterGame
  def update = this.repaint()
}

class GUI (width: Int, height: Int) extends MainFrame {
  
  // Initialization
  this.preferredSize = new Dimension(width + 6, height + 28) //padding to conform with borders on Win7
  this.contents = GameScreen
  this.resizable = false
  this.title = "Tower Battles ver 0.2.05"
//  this.centerOnScreen  aaa
  enterMenu
  this.repaint()
  this.visible = true
  
  // Various functions
  def top: MainFrame = this
  def enterMenu = GameScreen.enterMenu
  def enterGame = GameScreen.enterGame
  def update = this.repaint()
  
  // Event handling
  this.listenTo(GameScreen)
  
}

object GUI {
  val manager = mrtb.Manager
  // A factory method for creating windows of various resolutions.
//  def apply(width: Int, height: Int) = ???
}
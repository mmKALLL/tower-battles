package mrtb.gui

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import scala.swing.Panel
import java.awt.Dimension
import scala.swing.event._
import scala.swing.Button
import java.awt.BasicStroke
import java.awt.Font

/**
 * A Panel that uses Graphics2D to paint the wanted images etc.
 *
 * There are essentially two states - "menu" and "game". Depending on the state,
 * the effects by user actions as well as the things drawn on screen differ.
 *
 * The aim is to provide a completely graphical custom UI with custom elements; however -
 * while creating handlers for those custom elements would be the ideal situation, I have
 * decided to settle with sub-optimal, less elegant choices. This is because I don't feel like
 * the UI is complex enough that handling events in a simple way like this is "too" inelegant
 * considering the scope of the project. For version 1.1 or 1.2, I hope to create proper and automated
 * methods of creating various interface elements, in order to enable not only custom maps but
 * a modifiable UI as well.
 *
 * Pattern matching is used a lot.
 */

object GameScreen extends Panel {

  // Internal values.
  private var state = "menu"
  def enterMenu = state = "menu"
  def enterGame = state = "game"

  private val tileSize = GUI.manager.TILESIZE
  private val gridWidth = GUI.manager.GRIDSIZE._1
  private val gridHeight = GUI.manager.GRIDSIZE._2

  // Initialization
  preferredSize = new Dimension(800, 480)
  repaint()
  this.visible = true

  override def paintComponent(g: Graphics2D) = {
    clear(g)

    state match {
      case "menu" => {
        // White areas are drawn first, then other backgrounds, then text, then outlines.
        g.setColor(new Color(255, 255, 255))
        g.fillRect(15, 30, 80, 50)
        g.setColor(new Color(0, 0, 0))
        g.drawRect(15, 30, 80, 50)
        g.drawString("click here", 30, 50)
        g.drawString("for game", 35, 65)
      }

      case "game" => {
        // First, the various background areas of major UI features are colored light grey.
        g.setColor(new Color(245, 245, 245))
        g.fillRect(tileSize, tileSize, tileSize * gridWidth, tileSize * gridHeight)
        g.fillRect(size.width - 150, 30, 120, 70)		// Stat HUD
        g.fillRect(size.width - 150, 120, 120, 200)		// Tower selector
        g.fillRect(size.width - 150, 340, 120, 110)		// Selection info
        g.fillRect(tileSize, 410, tileSize * gridWidth, 30) // Wave info
        
        // Then, entry and exit points for the enemies are added.
        g.setColor(new Color(255, 140, 140))
        g.fillRect(tileSize, tileSize * (gridHeight / 2 + 1), tileSize, tileSize)
        g.setColor(new Color(140, 255, 140))
        g.fillRect(tileSize * gridWidth, tileSize * (gridHeight / 2 + 1), tileSize, tileSize)
        
        // The game grid itself is squared in.
        g.setColor(new Color(215, 215, 215))
        for (x <- 1 until GUI.manager.GRIDSIZE._1) {
          g.drawLine(tileSize * (x + 1), tileSize, tileSize * (x + 1), tileSize * (gridHeight + 1))
        }
        for (y <- 1 until GUI.manager.GRIDSIZE._2) {
          g.drawLine(tileSize, tileSize * (y + 1), tileSize * (gridWidth + 1), tileSize * (y + 1))
        }
        
        // Borders are added to the major UI elements.
        g.setColor(new Color(45, 45, 45))
        g.setStroke(new BasicStroke(2))
        g.drawRect(tileSize, tileSize, tileSize * gridWidth, tileSize * gridHeight)
        g.drawRect(size.width - 150, 30, 120, 70)
        g.drawRect(size.width - 150, 120, 120, 200)
        g.drawRect(size.width - 150, 340, 120, 110)
        g.drawRect(tileSize, 410, tileSize * gridWidth, 30)
        
        // Text is added
        g.setColor(new Color(0, 0, 0))
        g.setFont(new Font("SansSerif", Font.PLAIN, 14))
        g.drawString(GUI.manager.currentStage.lives.toString, size.width - 140, 35)
        
      }

      case _ => throw new Exception("Exception 0001 - GUI component \"GameScreen\" has illegal state.")
    }

  }

  def clear(g: Graphics2D) = {
    g.setColor(new Color(230, 255, 255))
    g.fillRect(0, 0, size.width, size.height)
  }
  
  
/* An example of a class that defines a custom component; it should be modified with
 * more parameters to determine custom background color or image. As of now, it is
 * unimplemented, but creating classes to represent components like this and automating
 * much of their workings was my vision. */
  class CustomButton(x: Int, y: Int, width: Int, height: Int, text: String, g: Graphics2D) {
    ???
    def wasClicked(event: MouseClicked): Boolean = ???
  }

  // The event handlers; the most important ones are without a doubt MouseClicked, 
  // MouseMoved and KeyTyped. Pattern matching is used to determine type and results.
  this.listenTo(mouse.clicks, mouse.moves, keys)
  
  this.reactions += {
    case b: MouseClicked => {
      state match {
        case "menu" => {
          println("reacted to MouseClicked in-menu; " + b)
          if (b.point.x < 100 && b.point.y < 120) {
        	GUI.manager.loadStage("test")
          }
        }

        case "game" => {
          println("reacted to MouseClicked in-game; " + b)
        }

        case _ => throw new Exception("Exception 0001 - GUI component \"GameScreen\" has illegal state.")
      }
     repaint()
    }
  }

}
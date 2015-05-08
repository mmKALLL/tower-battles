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
import java.awt.FontMetrics
import javax.swing.Timer
import java.awt.event.ActionListener
import java.awt.image.BufferedImage

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
 * considering the scope of the project. For version 1.2, I hope to create proper and automated
 * methods of creating various interface elements, in order to enable not only custom maps but
 * a modifiable UI as well.
 *
 * Pattern matching is used a lot. Crude benchmarking showed that on my subpar hardware the drawing was done
 * within 1 ms without objects on the field, and in 2-3 ms with them. Since this seems to be by far the most
 * resource-intensive part of the software, I'm delighted with the results. Running the game very smoothly even
 * on a limited system such as the OpenPandora should be well within realistic limits.
 */

object GameScreen extends Panel {

  // Internal values.
  private var state = "menu"
  def enterMenu = state = "menu"
  def enterGame = state = "game"

  private val tileSize = GUI.manager.TILESIZE
  private val gridWidth = GUI.manager.GRIDSIZE._1
  private val gridHeight = GUI.manager.GRIDSIZE._2

  private val fm_SSP14 = this.peer.getFontMetrics(new Font("SansSerif", Font.PLAIN, 14))
  private val fm_SSB14 = this.peer.getFontMetrics(new Font("SansSerif", Font.BOLD, 14))
  //  private val bg = new BufferedImage(800, 480, BufferedImage.TYPE_INT_RGB)
  //  private val g2 = bg.createGraphics()
  //  this.drawBackground
  private var renderTimeTotal = 0L
  private var frames = 0

  // Initialization
  preferredSize = new Dimension(800, 480)
  repaint()
  this.visible = true
  

  override def paintComponent(g: Graphics2D) = {
    val start = System.nanoTime()
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
        g.fillRect(tileSize, tileSize, tileSize * gridWidth, tileSize * gridHeight) // Game area
        g.fillRect(size.width - 150, 32, 120, 70) // Stat HUD
        g.fillRect(size.width - 150, 122, 120, 200) // Tower selector
        g.fillRect(size.width - 150, 342, 120, 110) // Selection info
        g.fillRect(tileSize, tileSize * (gridHeight + 1) + 20, tileSize * gridWidth, 46) // Wave info

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
        g.drawRect(size.width - 150, 32, 120, 70)
        g.drawRect(size.width - 150, 122, 120, 200)
        g.drawRect(size.width - 150, 342, 120, 110)
        g.drawRect(tileSize, tileSize * (gridHeight + 1) + 20, tileSize * gridWidth, 46)

        // Text is added
        g.setColor(new Color(0, 0, 0))
        g.setFont(new Font("SansSerif", Font.BOLD, 14))
        g.drawString("Lives:", size.width - 135, 49)
        g.drawString("Gold:", size.width - 135, 64)
        g.drawString("Score:", size.width - 135, 79)
        g.drawString("Time:", size.width - 135, 94)

        // Right alignment using the FontMetrics object
        g.setFont(new Font("SansSerif", Font.PLAIN, 14))
        g.drawString(GUI.manager.currentStage.lives.toString, size.width - 50 - fm_SSP14.stringWidth(GUI.manager.currentStage.lives.toString), 49)
        g.drawString(GUI.manager.currentStage.gold.toString, size.width - 50 - fm_SSP14.stringWidth(GUI.manager.currentStage.gold.toString), 64)
        g.drawString(GUI.manager.currentStage.score.toString, size.width - 50 - fm_SSP14.stringWidth(GUI.manager.currentStage.score.toString), 79)
        g.drawString(GUI.manager.parsePhaseTime, size.width - 50 - fm_SSP14.stringWidth(GUI.manager.parsePhaseTime), 94)

        // The game field is drawn
        //todo
        
      }

      case _ => throw new Exception("Exception 0001 - GUI component \"GameScreen\" has illegal state.")
    }
    
    frames += 1
    if (frames % (GUI.manager.FPS * 10) == 0 && GUI.manager.debug)
      println("Rendered a total of " + frames + " frames with an average of " + renderTimeTotal / frames / 1000 + "µs per frame at " + GUI.manager.FPS + " FPS.")
    renderTimeTotal += System.nanoTime() - start
  }

  def clear(g: Graphics2D) = {
    g.setColor(new Color(230, 255, 255))
    g.fillRect(0, 0, size.width, size.height)
  }

  /* An example of a class that defines a custom component; it should be modified with
 * more parameters to determine custom background color and image. As of now, it is
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
    case b: MouseReleased => {
      state match {
        case "menu" => {
          if (GUI.manager.debug) println("reacted to MouseReleased in-menu; " + b)
          if (b.point.x < 95 && b.point.y < 80) {
            GUI.manager.loadStage("Test Map")
            if (GUI.manager.debug) println("opening stage \"" + GUI.manager.currentStage.name + "\", proceeding to game...")
          }
        }

        case "game" => {
          GUI.manager.currentStage.lives -= 1 //aaa
        }

        case _ => throw new Exception("Exception 0001 - GUI component \"GameScreen\" has illegal state.")
      }
      repaint()
    }
  }

}
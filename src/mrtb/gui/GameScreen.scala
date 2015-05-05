package mrtb.gui

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import scala.swing.Panel
import java.awt.Dimension
import scala.swing.event._

/**
 * A Panel that uses Graphics2D to paint the wanted images etc.
 *
 */

object GameScreen extends Panel {

  private var state = "menu"
  def enterMenu = state = "menu"
  def enterGame = state = "game"
  preferredSize = new Dimension(800, 480)
  repaint()
  this.visible = true

  override def paintComponent(g: Graphics2D) = {
    clear(g)
    
    if (state == "menu") {
      g.setColor(new Color(255, 255, 255))
      g.fillRect(15, 30, 80, 50)
      g.setColor(new Color(0, 0, 0))
      g.drawRect(15, 30, 80, 50)
      g.drawString("click here", 30, 50)
      g.drawString("for game", 35, 65)
    } 
    
    else if (state == "game") {
      for (x <- 0 until GUI.manager.GRIDSIZE._1; y <- 0 until GUI.manager.GRIDSIZE._2)
      g.setColor(new Color(255, 255, 255))
      g.fillRect(15, 30, 80, 50)
      g.setColor(new Color(0, 0, 0))
      g.drawRect(15, 30, 80, 50)
      g.drawString("game", 30, 50)
    }
  }

  def clear(g: Graphics2D) = {
    g.setColor(new Color(230, 255, 255))
    g.fillRect(0, 0, size.width, size.height)
  }

  def drawButton(x: Int, y: Int, width: Int, height: Int, text: String, g: Graphics2D) {
    ???
  }
  
  reactions += {
    case b: ButtonClicked => {
      state match {
        case "menu" => {
          
        }
        
        case "game" => {
          
        }
        
        case _ => throw new Exception("GUI component \"GameScreen\" has illegal state")
      }
    }
    
    
  }

}
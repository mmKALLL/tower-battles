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
import scala.swing.Dialog
import mrtb._
import scala.swing.Publisher
import scala.collection.mutable.Queue
import java.awt.event.MouseListener

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
 * Pattern matching is used a lot. Crude benchmarking showed that on my subpar hardware the
 * drawing was often done in ~1 ms. Since this seems to be by far the most resource-intensive
 * part of the software, I'm delighted with the results. Running the game very smoothly even
 * on a limited system such as the OpenPandora should be well within realistic limits.
 */

object GameScreen extends Panel {

  // Some private variables for convenience.
  private var dialogOpen = false
  private val tileSize = GUI.manager.TILESIZE
  private val gridWidth = GUI.manager.GRIDSIZE._1
  private val gridHeight = GUI.manager.GRIDSIZE._2

  private var towers: Array[Tower] = null
  private var selection = -1
  private var point = new java.awt.Point(0, 0)

  // Three queues that handle the shooting effects; first parameter is width, the others are x1, y1, x2, y2
  private var oneFrameShots: Queue[(Int, Int, Int, Int, Int)] = Queue()
  private var twoFrameShots: Queue[(Int, Int, Int, Int, Int)] = Queue()
  var threeFrameShots: Queue[(Int, Int, Int, Int, Int)] = Queue()

  private val fm_SSP14 = this.peer.getFontMetrics(new Font("SansSerif", Font.PLAIN, 14))
  private val fm_SSB14 = this.peer.getFontMetrics(new Font("SansSerif", Font.BOLD, 14))
  private val as = 0

  // A class used for dialog drawing outside of repaints.
  class DialogDrawEvent extends Event

  // Used for benchmarking the repaint.
  private var renderTimeTotal = 0L
  private var frames = 0

  // Initialization
  preferredSize = new Dimension(800, 480)
  repaint()
  this.visible = true

  // The main painting method, a lot of things are drawn directly on the Graphics2D object.
  override def paintComponent(g: Graphics2D) = {
    val start = System.nanoTime()
    clear(g)

    GUI.manager.gameState.take(4) match {
      case "menu" => {
        // Backgrounds are drawn first, then outlines, then text.
        g.setFont(new Font("SansSerif", Font.PLAIN, 14))
        for (x <- GUI.manager.stagelist.keys.zipWithIndex) {
          g.setColor(new Color(255, 255, 255))
          g.fillRect(200, 10 + 40 * x._2, 200, 30)
          g.setColor(new Color(0, 0, 0))
          g.drawRect(200, 10 + 40 * x._2, 200, 30)
          g.drawString(x._1, 215, 28 + 40 * x._2)
        }
      }

      case "over" => {
        Manager.gameState = "end"
        g.setColor(new Color(230, 255, 255))
        g.fillRect(0, 0, size.width, size.height)
        // For some reason, this doesn't work properly. \\\\\\TODO\\\\\\ aaa
        publish(new DialogDrawEvent)
        Dialog.showMessage(this, "You lost!", "Game over! Score: " + GUI.manager.currentStage.score)
        println("Game ended, exiting...")
        System.exit(0)
      }

      case "end" =>

      case "beat" => {
        //todo
      }

      case "game" => {

        // First, the various background areas of major UI features are colored light grey.
        g.setColor(new Color(245, 245, 245))
        g.fillRect(tileSize, tileSize, tileSize * gridWidth, tileSize * gridHeight) // Game area
        g.fillRect(size.width - 150, 32, 120, 70) // Stat HUD
        g.fillRect(size.width - 150, 122, 120, 200) // Tower selector
        g.fillRect(size.width - 150, 342, 120, 110) // Selection info
        g.fillRect(tileSize, tileSize * (gridHeight + 1) + 20, tileSize * gridWidth, 46) // Wave info

        // The game grid itself is squared in, along with tower selector.
        g.setColor(new Color(215, 215, 215))
        g.setStroke(new BasicStroke(1))
        for (x <- 1 until GUI.manager.GRIDSIZE._1) {
          g.drawLine(tileSize * (x + 1), tileSize, tileSize * (x + 1), tileSize * (gridHeight + 1))
        }
        for (y <- 1 until GUI.manager.GRIDSIZE._2) {
          g.drawLine(tileSize, tileSize * (y + 1), tileSize * (gridWidth + 1), tileSize * (y + 1))
        }
        g.drawLine(size.width - 110, 122, size.width - 110, 322)
        g.drawLine(size.width - 70, 122, size.width - 70, 322)
        for (y <- 1 to 4)
          g.drawLine(size.width - 150, 122 + 40 * y, size.width - 30, 122 + 40 * y)

        // Then, the entry and exit points for the enemies are colored.
        g.setColor(new Color(255, 140, 140))
        g.fillRect(tileSize, tileSize * (gridHeight / 2 + 1), tileSize, tileSize)
        g.setColor(new Color(140, 255, 140))
        g.fillRect(tileSize * gridWidth, tileSize * (gridHeight / 2 + 1), tileSize, tileSize)

        // Borders are added to the major UI elements.
        g.setColor(new Color(45, 45, 45))
        g.setStroke(new BasicStroke(2))
        g.drawRect(tileSize - 1, tileSize - 1, tileSize * gridWidth + 2, tileSize * gridHeight + 2)
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
        Manager.currentStage.getCurrentWave.enemyList.foreach(
          a => if (a._1 <= 0) g.drawImage(a._2.image, null, a._2.x + Manager.TILESIZE - a._2.image.getWidth() / 2, a._2.y + Manager.TILESIZE - a._2.image.getHeight() / 2))
        Manager.currentStage.tiles.flatten.foreach(
          a => if (!a.isEmpty) g.drawImage(a.getTower.image, null, a.leftEdge._1 + Manager.TILESIZE + 1, a.upperEdge._2 + Manager.TILESIZE + 1))

        // The UI elements are drawn
        // First, the towers in the selector:
        for (x <- 0 until Math.min(towers.size, 15)) {
          g.drawImage(towers(x).image, null, size.width - 150 + (x % 3 * 40) + 6, x / 3 * 40 + 126)
        }

        // Then the selected tower is drawn
        if (selection >= 0 && point.x > 32 && point.x < (Manager.GRIDSIZE._1 + 1) * Manager.TILESIZE && point.y > 32 && point.y < (Manager.GRIDSIZE._2 + 1) * Manager.TILESIZE) {
          g.drawImage(towers(selection).image, null, point.x - point.x % 32 + 1, point.y - point.y % 32 + 1)
          g.setColor(new Color(255, 40, 40))
          g.drawOval(point.x - point.x % 32 + 16 - towers(selection).range, point.y - point.y % 32 + 16 - towers(selection).range, 2 * towers(selection).range, 2 * towers(selection).range)
          g.setColor(new Color(255, 40, 40))
        }

        // The wave display
        Manager.currentStage.waves.take(5).foreach(a => {
          g.setColor(new Color(0, 0, 0))
          g.setColor(a.descriptionC)
          // TODO:qas
        })

        // The shots are drawn and displayed for three frames; a very... crude... solution, but it works. 
        var temp = (0, 0, 0, 0, 0)
        g.setColor(new Color(165, 85, 5))
        if (oneFrameShots.length > 1)
          for (x <- oneFrameShots) {
            temp = oneFrameShots.dequeue
            g.setStroke(new BasicStroke(temp._1))
            g.drawLine(temp._2, temp._3, temp._4, temp._5)
          }
        if (twoFrameShots.length > 1)
          for (x <- twoFrameShots) {
            temp = twoFrameShots.dequeue
            oneFrameShots.enqueue(temp)
            g.setStroke(new BasicStroke(temp._1))
            g.drawLine(temp._2, temp._3, temp._4, temp._5)
          }
        if (threeFrameShots.length > 1)
          for (x <- threeFrameShots) {
            temp = threeFrameShots.dequeue
            twoFrameShots.enqueue(temp)
            g.setStroke(new BasicStroke(temp._1))
            g.drawLine(temp._2, temp._3, temp._4, temp._5)
          }

        g.setColor(new Color(0, 0, 0))
        g.setStroke(new BasicStroke(1))

      }

      case _ => throw new Exception("Exception 0001 - GUI component \"GameScreen\" has illegal state.")
    }

    frames += 1
    if (frames % (GUI.manager.FPS * 10) == 0 && GUI.manager.debug)
      println("Rendered a total of " + frames + " frames with an average of " + renderTimeTotal / frames / 1000 + "µs per frame at " + GUI.manager.FPS + " FPS.")
    renderTimeTotal += System.nanoTime() - start
  }

  def clear(g: Graphics2D) = {
    g.setStroke(new BasicStroke(1))
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
  this.listenTo(mouse.clicks, mouse.moves, keys, this)

  this.reactions += {
    case b: MouseReleased => {
      GUI.manager.gameState match {
        case "menu" => {
          if (GUI.manager.debug) println("reacted to MouseReleased in-menu; " + b)
          if (b.point.x > 200 && b.point.x < 400 && b.point.y < 40 * GUI.manager.stagelist.size && b.point.y > 10) {
            try {
              GUI.manager.loadStage(GUI.manager.stagelist.keys.take(((b.point.y + 10) / 40) + 1).last)
              towers = Manager.currentStage.availableTowers.toArray
            } catch {
              case e: IllegalArgumentException =>
                Dialog.showMessage(this, "You tried to open a stage that is incompatible with the game! Parser is version 1.0.", "Error!"); e.printStackTrace()
              case e: NoWavesDefinedException =>
                Dialog.showMessage(this, "You tried to open a stage that contains no compatible waves! Parser is version 1.0.", "Error!"); e.printStackTrace()
              case e: Throwable => Dialog.showMessage(this, "You tried to open a stage, but there was an unknown error! Parser is version 1.0.\n" + e, "Error!"); e.printStackTrace()
            }
            if (GUI.manager.debug) println("opening stage \"" + GUI.manager.currentStage + "\", proceeding to game...")
          }
        }

        case "game_setup" => {
          // Right click removes tower selection
          if (b.peer.getButton() == java.awt.event.MouseEvent.BUTTON2 || b.peer.getButton() == java.awt.event.MouseEvent.BUTTON3)
            selection = -1
          // If the tower selector is clicked, select the one
          else if (b.point.x > size.width - 150 && b.point.x < size.width - 30 && b.point.y > 122 && b.point.y < 322) {
            this.selection = (b.point.x - (size.width - 150)) / 40 + 3 * ((b.point.y - 122) / 40)
            if (selection >= towers.length)
              selection = -1

            // If the game field is clicked
          } else if (b.point.x > 32 && b.point.x < (Manager.GRIDSIZE._1 + 1) * Manager.TILESIZE && b.point.y > 32 && b.point.y < (Manager.GRIDSIZE._2 + 1) * Manager.TILESIZE) {
            if (selection >= 0) {
              if (Manager.currentStage.placeTower(towers(selection), (b.point.x) / Manager.TILESIZE, (b.point.y) / Manager.TILESIZE, false))
                Manager.currentStage.placeTower(towers(selection), (b.point.x) / Manager.TILESIZE, (b.point.y) / Manager.TILESIZE, true)
            } else if (false) {
              // todo: tower selection and upgrade
            }
          } else if (false) {
            // TODO: add skip to wave-button, speed-up, and other UI goodies
          }
        }

        case "game_wave" => {

        }

        case "over" =>

        case "end" =>

        case "beat" =>

        case _ => throw new Exception("Exception 0001 - GUI component \"GameScreen\" has illegal state.")
      }
      repaint()
    }

    case m: MouseMoved => {
      point = m.point
    }

    case k: KeyTyped => {
      selection = -1
    }

    case d: DialogDrawEvent => //todo
  }

}
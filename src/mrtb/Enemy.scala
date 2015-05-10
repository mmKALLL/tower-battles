package mrtb

import scala.collection.mutable.Map
import java.awt.image.BufferedImage
import java.io.FileReader
import java.io.BufferedReader
import java.io.File
import javax.imageio.ImageIO
import java.io.IOException

// The abstract class defines the methods that have to be implemented in all
// enemy classes. The various classes might have individual solutions to pathfinding etc.
class Enemy(id: String, image: BufferedImage, var HP: Int, speed: Int, damage: Int = 1, goldgain: Int, scoregain: Int, var types: String = "normal") {

  // A percentage of slowness that is used to calculate the true speed.
  // Negative values hasten, positive values slow down.
  var slow: Int = 0

  var nextTile: Tile = null
  
  def update = {
    // movespeed = speed * (1 - (slowed / 100))
    ???
  }

  def findPath(tiles: Array[Array[Tile]]): Unit = {
    if (types == "normal") {
      Enemy.findShortestPath(tiles)
    } else {
      if (Manager.debug) println("unknown type in enemy \"" + id + "\", assuming it to be \"normal\"")
      types = "normal"
      findPath(tiles)
    }
  }

}

/**
 * The companion object provides some helper functions for using external
 * files to load and create enemies.
 */
object Enemy {

  var shortestPath: Array[Tile] = null
  
  // The pathfinding algorithm. Entry and exit points are assumed to be static,
  // but I plan to make that definable in v1.1, and mid-wave tower placement possible in v1.2.
  def findShortestPath(tiles: Array[Array[Tile]]) = {
    ???
  }
  
  
  def loadEnemy(id: String): Enemy = {
    if (Manager.enemylist.contains(id))
      Manager.enemylist(id)
    else {
      if (Manager.debug) println("enemy with id \"" + id + "\" was not found, ignoring")
      null
    }
  }

  // The method to parse the enemies.txt file; essentially the same as the Tower parser.
  def loadEnemies: Map[String, Enemy] = {
    var enemies = Map[String, Enemy]()
    val r = new BufferedReader(new FileReader(new File("enemies.txt")))
    var s = r.readLine()
    var version = "0"

    while (s != null) {
      // When a block is found, check whether it's an enemy or version definition, otherwise unknown.
      if (s.trim().take(1) == "!") {
        s.trim match {
          case "!defineunit" => {
            var temp = Map[String, String]()
            s = r.readLine()
            while (s != null && s.trim().take(1) != "!") {
              if (s.split("=").length == 2 && s.trim().take(1) != "#")
                temp += s.split("=")(0).trim.toLowerCase() -> s.split("=")(1).trim
              s = r.readLine()
            }

            // If the enemy definition was valid, a new enemy object is created and added to the map.
            // id: String, image: BufferedImage, var HP: Int, speed: Int, goldgain: Int, scoregain: Int, var types: String = "normal"
            if (temp.contains("id") && temp.contains("image") && temp.contains("hp") && temp.contains("speed")
              && temp.contains("damage") && temp.contains("goldgain") && temp.contains("scoregain"))
              try {
                if (temp.contains("type"))
                  enemies += temp("id") -> new Enemy(temp("id"), ImageIO.read(new File(".\\images\\" + temp("image"))),
                    Integer.parseInt(temp("hp")), Integer.parseInt(temp("speed")), Integer.parseInt(temp("damage")), Integer.parseInt(temp("goldgain")),
                    Integer.parseInt(temp("scoregain")), temp("type"))
                else
                  enemies += temp("id") -> new Enemy(temp("id"), ImageIO.read(new File(".\\images\\" + temp("image"))),
                    Integer.parseInt(temp("hp")), Integer.parseInt(temp("speed")), Integer.parseInt(temp("damage")), Integer.parseInt(temp("goldgain")),
                    Integer.parseInt(temp("scoregain")))
              } catch {
                case i: IOException => println("caught IOException; the image at .\\images\\" + temp("image") + ", defined for enemy \"" + temp("id") + "\" could not be read")
                case t: Throwable => println("caught an unexpected exception when processing enemy \"" + temp("id") + "\"; " + t)
              }
            else if (Manager.debug) println("the enemy with id \"" + temp("id") + "\" did not have enough parameters defined; please check the syntax")
          }

          case _ => {
            if (version == "0") {
              if (s.contains("1.0")) version = "1.0"
            } else if (Manager.debug) println("unknown block detected in enemies.txt on line\n\"" + s + "\";\nparser is version " + Manager.VERSION + ".")
            s = r.readLine()
          }
        }
      } // If no new block was found, read the next line.
      else s = r.readLine()
    }

    enemies

  }


}
package mrtb

import scala.collection.mutable.Map
import java.awt.image.BufferedImage
import java.io.FileReader
import java.io.BufferedReader
import java.io.File
import javax.imageio.ImageIO
import java.io.IOException
import scala.collection.mutable.Buffer
import scala.collection.mutable.Map
import scala.collection.mutable.PriorityQueue

// The abstract class defines the methods that have to be implemented in all
// enemy classes. The various classes might have individual solutions to pathfinding etc.
class Enemy(id: String, image: BufferedImage, var HP: Int, speed: Int, damage: Int = 1, goldgain: Int, scoregain: Int, var types: String = "normal") {

  // A percentage of slowness that is used to calculate the true speed.
  // Negative values hasten, positive values slow down.
  var slow: Int = 0

  // An integer denoting the position in the shortestPath array (below).
  var currentTile: Int = 0

  var x: Int = 0
  var y: Int = 0

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

  // The pathfinding algorithm, using A*. Entry and exit points are assumed to be static,
  // but I plan to make that definable in v1.1, and mid-wave tower placement possible in v1.2.
  // The return value denotes whether a path is available.
  def findShortestPath(tiles: Array[Array[Tile]]): Boolean = {
    val goal = tiles(Manager.GRIDSIZE._1 - 1)(Manager.GRIDSIZE._2 / 2 + 1)
    var done = false

    // The heuristic is based on Euclidean distance.
    // An improved heuristic would be to check for surrounding tiles' towers and
    // multiply the distance with the path's dangerousness.
    def getHeuristic(a: Tile): Double = Math.sqrt((a.getX - goal.getX) * (a.getX - goal.getX)) + Math.sqrt((a.getY - goal.getY) * (a.getY - goal.getY))
    // The 1.3 version
    //Math.abs(Manager.GRIDSIZE._1 * 1.0 - a.getX * 1.0) + Math.abs(Manager.GRIDSIZE._2 * 1.0 - a.getY * 1.0) - 
    //Math.min(Math.abs(Manager.GRIDSIZE._1 * 1.0 - a.getX * 1.0), Math.abs(Manager.GRIDSIZE._2 * 1.0 - a.getY * 1.0)) * 0.3

    // A map containing a reference to the best distance, heuristic distance, and parent tile.
    var weights = Map[Tile, (Double, Double, Tile)]()
    tiles.flatten.foreach(a => weights += a -> (99999.9, getHeuristic(a), null))

    // Open is a priority queue consisting of the tiles currently under consideration, ordered by best distance + heuristic.
    var open = PriorityQueue[Tile](tiles(0)(Manager.GRIDSIZE._2 / 2 + 1))(Ordering.by[Tile, Double](a => weights(a)._1 + weights(a)._2))
    var closed = Buffer[Tile]() // lookup unordered is O(n) (!!!) aaa

    var current = open.dequeue

    def getNeighbors(in: Tile): Array[Tile] = {
      // Check for the edge tiles
      if (in.getX == 1 || in.getX == Manager.GRIDSIZE._1 || in.getY == 1 || in.getY == Manager.GRIDSIZE._2) {
        (in.getX, in.getY) match {
          case (1, 1) => Array(tiles(in.getX)(in.getY - 1), tiles(in.getX - 1)(in.getY))
          case (1, Manager.GRIDSIZE._2) => Array(tiles(in.getX)(in.getY - 1), tiles(in.getX - 1)(in.getY - 2))
          case (Manager.GRIDSIZE._1, 1) => Array(tiles(in.getX - 1)(in.getY - 2), tiles(in.getX - 2)(in.getY - 1))
          case (Manager.GRIDSIZE._1, Manager.GRIDSIZE._2) => Array(tiles(in.getX - 1)(in.getY - 2), tiles(in.getX - 2)(in.getY - 1))
          case (1, _) => Array(tiles(in.getX)(in.getY - 1), tiles(in.getX - 1)(in.getY - 2), tiles(in.getX - 1)(in.getY))
          case (Manager.GRIDSIZE._1, _) => Array(tiles(in.getX - 1)(in.getY - 2), tiles(in.getX - 1)(in.getY), tiles(in.getX - 2)(in.getY - 1))
          case (_, 1) => Array(tiles(in.getX)(in.getY - 1), tiles(in.getX - 1)(in.getY), tiles(in.getX - 2)(in.getY - 1))
          case (_, Manager.GRIDSIZE._2) => Array(tiles(in.getX)(in.getY - 1), tiles(in.getX - 1)(in.getY - 2), tiles(in.getX - 2)(in.getY - 1))
        }
      } else Array(tiles(in.getX)(in.getY - 1), tiles(in.getX - 1)(in.getY - 2), tiles(in.getX - 1)(in.getY), tiles(in.getX - 2)(in.getY - 1))
    }

    // A method for getting the real distance between two adjacent nodes.
    def getRealDistance(x1: Int, y1: Int, x2: Int, y2: Int) = {
      (x1 - x2, y1 - y2) match {
        case _ => // Unimplemented; the graph structure needs heavy reworking in order to get subtile diagonal movement to work properly.
      }
    }

    // The current tile is removed from the queue, then its adjacent tiles are added to consideration.
    while (!done) {
      if (current == goal)
        done = true
      else if (open.isEmpty)
        return false
      else {
        closed += current
        for (x <- getNeighbors(current)) {
          if (closed.contains(x) && weights(current)._1 + 1 < weights(x)._1) {
            weights(x) = (weights(current)._1 + 1, weights(x)._2, current)
          } else if (open.exists(x == _) && weights(current)._1 + 1 < weights(x)._1) {
            weights(x) = (weights(current)._1 + 1, weights(x)._2, current)
          } else if (!closed.contains(x) && !open.exists(x == _)) {
            weights(x) = (weights(current)._1 + 1, weights(x)._2, current)
            open += x
          }
        }
      }

      println(weights)
      current = open.dequeue
    }

    current = goal

    var result = Buffer[Tile](goal)

    while (current != tiles(0)(Manager.GRIDSIZE._2 / 2 + 1)) {
      result += weights(current)._3
      current = weights(current)._3
    }

    shortestPath = result.toArray.reverse

    true
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
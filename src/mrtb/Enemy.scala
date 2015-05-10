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
class Enemy(val id: String, val image: BufferedImage, var HP: Int, val speed: Int, val damage: Int = 1, val goldgain: Int, val scoregain: Int, var types: String = "normal") {

  // A percentage of slowness that is used to calculate the true speed.
  // Negative values hasten, positive values slow down.
  var slow: Int = 0

  // An integer denoting the position in the shortestPath array (below).
  var currentTile: Int = 0
  var nextTile: Int = 1

  var x: Int = Manager.TILESIZE / 2
  var y: Int = Manager.TILESIZE * (Manager.GRIDSIZE._2 / 2) + Manager.TILESIZE / 2

  // Handles movement on a pixel scale
  def update = {
    if (slow > 0)
      slow -= 1
    if (HP <= 0)
      Manager.destroy(this)
    var movement = Math.min(32.0, Math.max(1.43, speed.toDouble * (1 - (slow / 100)) / 10))
    while (movement > 0) {
      if (currentTile == Enemy.shortestPath.length - 1) {
        movement = 0
        Manager.destroy(this)
      } else {

        val a = Enemy.shortestPath(currentTile)
        val b = Enemy.shortestPath(nextTile)

        // Check the direction and update position accordingly. The enemies move from the center of a tile edge to the next.
        (b.getX - a.getX, b.getY - a.getY) match {
          case (1, 0) => {
            val distance = Math.sqrt(Math.pow(a.rightEdge._1 - x, 2) + Math.pow(a.rightEdge._2 - y, 2))
            if (movement > distance) {
              movement -= distance.toInt
              x = a.rightEdge._1
              y = a.rightEdge._2
              currentTile += 1
              nextTile += 1
            } else {
              x += ((a.rightEdge._1 - x) / distance * movement).toInt
              y += ((a.rightEdge._2 - y) / distance * movement).toInt
              movement = 0
            }
          }

          case (-1, 0) => {
            val distance = Math.sqrt(Math.pow(a.leftEdge._1 - x, 2) + Math.pow(a.leftEdge._2 - y, 2))
            if (movement > distance) {
              movement -= distance.toInt
              x = a.leftEdge._1
              y = a.leftEdge._2
              currentTile += 1
              nextTile += 1
            } else {
              x += ((a.leftEdge._1 - x) / distance * movement).toInt
              y += ((a.leftEdge._2 - y) / distance * movement).toInt
              movement = 0
            }
          }

          case (0, 1) => {
            val distance = Math.sqrt(Math.pow(a.lowerEdge._1 - x, 2) + Math.pow(a.lowerEdge._2 - y, 2))
            if (movement > distance) {
              movement -= distance.toInt
              x = a.lowerEdge._1
              y = a.lowerEdge._2
              currentTile += 1
              nextTile += 1
            } else {
              x += ((a.lowerEdge._1 - x) / distance * movement).toInt
              y += ((a.lowerEdge._2 - y) / distance * movement).toInt
              movement = 0
            }
          }

          case (0, -1) => {
            val distance = Math.sqrt(Math.pow(a.upperEdge._1 - x, 2) + Math.pow(a.upperEdge._2 - y, 2))
            if (movement > distance) {
              movement -= distance.toInt
              x = a.upperEdge._1
              y = a.upperEdge._2
              currentTile += 1
              nextTile += 1
            } else {
              x += ((a.upperEdge._1 - x) / distance * movement * 1.2).toInt
              y += ((a.upperEdge._2 - y) / distance * movement * 1.2).toInt
              movement = 0
            }
          }
        }
      }
    }
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
    val goal = tiles(Manager.GRIDSIZE._1 - 1)(Manager.GRIDSIZE._2 / 2)
    var done = false

    // The heuristic is based on Euclidean distance.
    // An improved heuristic would be to check for surrounding tiles' towers and
    // multiply the distance with the path's dangerousness.
    def getHeuristic(a: Tile): Double = Math.sqrt((a.getX - goal.getX) * (a.getX - goal.getX)) + ((a.getY - goal.getY) * (a.getY - goal.getY))
    // The 1.3 advanced version
    //Math.abs(Manager.GRIDSIZE._1 * 1.0 - a.getX * 1.0) + Math.abs(Manager.GRIDSIZE._2 * 1.0 - a.getY * 1.0) - 
    //Math.min(Math.abs(Manager.GRIDSIZE._1 * 1.0 - a.getX * 1.0), Math.abs(Manager.GRIDSIZE._2 * 1.0 - a.getY * 1.0)) * 0.3

    // A map containing a reference to the best distance, heuristic distance, and parent tile.
    var weights = Map[Tile, (Double, Double, Tile)]()
    tiles.flatten.foreach(a => weights += a -> (99999.9, getHeuristic(a), null))

    // Open is a priority queue consisting of the tiles currently under consideration, ordered by best distance + heuristic.
    var open = PriorityQueue[Tile](tiles(0)(5))(Ordering[Double].on(a => - weights(a)._1 - weights(a)._2))
    var closed = Buffer[Tile]() // lookup unordered is O(n) (!!!) aaa

    var current = open.dequeue

    def getNeighbors(in: Tile): Array[Tile] = {
      // Check for the edge tiles
      (in.getX, in.getY) match {
        case (1, 1) => Array(tiles(in.getX)(in.getY - 1), tiles(in.getX - 1)(in.getY))
        case (1, Manager.GRIDSIZE._2) => Array(tiles(in.getX)(in.getY - 1), tiles(in.getX - 1)(in.getY - 2))
        case (Manager.GRIDSIZE._1, 1) => Array(tiles(in.getX - 1)(in.getY), tiles(in.getX - 2)(in.getY - 1))
        case (Manager.GRIDSIZE._1, Manager.GRIDSIZE._2) => Array(tiles(in.getX - 1)(in.getY - 2), tiles(in.getX - 2)(in.getY - 1))
        case (1, _) => Array(tiles(in.getX)(in.getY - 1), tiles(in.getX - 1)(in.getY - 2), tiles(in.getX - 1)(in.getY))
        case (Manager.GRIDSIZE._1, _) => Array(tiles(in.getX - 1)(in.getY - 2), tiles(in.getX - 1)(in.getY), tiles(in.getX - 2)(in.getY - 1))
        case (_, 1) => Array(tiles(in.getX)(in.getY - 1), tiles(in.getX - 1)(in.getY), tiles(in.getX - 2)(in.getY - 1))
        case (_, Manager.GRIDSIZE._2) => Array(tiles(in.getX)(in.getY - 1), tiles(in.getX - 1)(in.getY - 2), tiles(in.getX - 2)(in.getY - 1))
        case _ => Array(tiles(in.getX)(in.getY - 1), tiles(in.getX - 1)(in.getY - 2), tiles(in.getX - 1)(in.getY), tiles(in.getX - 2)(in.getY - 1))
      }
    }

    // A method for getting the real distance between two adjacent nodes.
    def getRealDistance(x1: Int, y1: Int, x2: Int, y2: Int) = {
      (x1 - x2, y1 - y2) match {
        case _ => // Unimplemented; the graph structure would need heavy reworking, and easier to implement in the enemy itself.
      }
    }

    // The current tile is removed from the queue, then its adjacent tiles are added to consideration.
    while (!done) {
      if (current == goal)
        done = true
      else if (open.isEmpty && !closed.isEmpty)
        return false
      else {
        closed += current
        for (x <- getNeighbors(current).filter(_.isEmpty)) {
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

      current = open.dequeue
    }

    // After finding the path, process it
    current = goal
    var result = Buffer[Tile](goal)
    while (current != tiles(0)(5)) {
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

  // The apply function copies an enemy.
  def apply(in: Enemy): Enemy = new Enemy(in.id, in.image, in.HP, in.speed, in.damage, in.goldgain, in.scoregain, in.types)

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
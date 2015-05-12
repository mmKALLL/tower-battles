package mrtb

import scala.collection.mutable.Map
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileReader
import java.io.BufferedReader
import javax.imageio.ImageIO
import java.io.IOException

/**
 * A class that represents a single tower.
 * @cost The tower's placement cost.
 * @speed The reload delay of a tower, measured in frames.
 * @damage The tower's damage per shot.
 * @range The circular range of this tower from its epicenter, measured in pixels.
 * @upgradesTo The id of the tower that this one upgrades into, null if final upgrade.
 * @special The special ability of this tower; 0 indicates no special.
 */

class Tower(val id: String, val name: String, val image: BufferedImage, val cost: Int, val speed: Int, val damage: Int, val range: Int, val upgradesTo: String, val special: String = "") {

  var x = 0
  var y = 0
  var reload = speed
  def getCost: Int = cost

  def shoot: Boolean = {
    if (special == "aoe") {
      Manager.currentStage.waves.head.enemyList.foreach(a => {
        if (Math.sqrt(Math.pow(a._2.x - x, 2) + Math.pow(a._2.y - y, 2)) < range * 1.5 && a._1 <= 0) {
          a._2.HP -= damage
          reload = speed
        }
      })
    } else if (special.take(4) == "slow") {
      Manager.currentStage.waves.head.enemyList.filter(a => Math.sqrt(Math.pow(a._2.x - x, 2) + Math.pow(a._2.y - y, 2)) < range + 16 && a._1 <= 0).headOption match {
        case a: Some[(Int, Enemy)] => a.get._2.HP -= damage; a.get._2.slow = special.drop(4).toInt; reload = speed
        case None => 
      }
    } else {
      Manager.currentStage.waves.head.enemyList.filter(a => Math.sqrt(Math.pow(a._2.x - x, 2) + Math.pow(a._2.y - y, 2)) < range + 16 && a._1 <= 0).headOption match {
        case a: Some[(Int, Enemy)] => a.get._2.HP -= damage; reload = speed
        case None => 
      }
    }
    false
  }

  def isUpgradeable: Boolean = upgradesTo != null
  def upgradeCost: Int = if (isUpgradeable) Tower.loadTower(upgradesTo).getCost - cost else -1
  def upgrade = {
    ???
  }
  
  def setCoordinates(pos: Tile) = {
    x = pos.center._1
    y = pos.center._2
  }

  def update = {
    if (this.reload > 0)
      this.reload -= 1
    else shoot
  }

}

/**
 * The Tower object provides methods for keeping track of and parsing tower files.
 */

object Tower {

  // Loads a single tower with the specified id; returns null (!!) if not found.
  def loadTower(id: String): Tower = {
    if (Manager.towerlist.contains(id)) {
      Manager.towerlist(id)
    }
      else {
      if (Manager.debug) println("tower with id \"" + id + "\" was not found")
      Manager.stageOK = false
      null
    }
  }

  // Parses towers.txt for all the tower definitions available.
  def loadTowers: Map[String, Tower] = {
    var towers = Map[String, Tower]()
    val r = new BufferedReader(new FileReader(new File("towers.txt")))
    var s = r.readLine()
    var version = "0"

    while (s != null) {
      // When a block is found, check whether it's a tower or version definition, otherwise unknown.
      if (s.trim().take(1) == "!") {
        s.trim match {
          case "!definetower" => {
            var temp = Map[String, String]()
            s = r.readLine()
            while (s != null && s.trim().take(1) != "!") {
              if (s.split("=").length == 2 && s.trim().take(1) != "#")
                temp += s.split("=")(0).trim.toLowerCase() -> s.split("=")(1).trim
              s = r.readLine()
            }

            // If the tower definition was valid, a new tower object is created and added to the map.
            if (temp.contains("id") && temp.contains("name") && temp.contains("image") && temp.contains("cost") && temp.contains("speed")
              && temp.contains("damage") && temp.contains("range") && temp.contains("upgrade") && temp.contains("special"))
              try {
                towers += temp("id") -> new Tower(temp("id"), temp("name"), ImageIO.read(new File(".\\images\\" + temp("image"))),
                  Integer.parseInt(temp("cost")), Integer.parseInt(temp("speed")), Integer.parseInt(temp("damage")),
                  Integer.parseInt(temp("range")), temp("upgrade"), temp("special"))
              } catch {
                case i: IOException => println("caught IOException; the image at .\\images\\" + temp("image") + ", defined for tower \"" + temp("id") + "\" could not be read")
                case t: Throwable => println("caught an unexpected exception when processing tower \"" + temp("id") + "\"; " + t)
              }
            else if (Manager.debug) println("the tower with id \"" + temp("id") + "\" did not have enough parameters defined; please check the syntax")
          }

          case _ => {
            if (version == "0") {
              if (s.contains("1.0")) version = "1.0"
            } else if (Manager.debug) println("unknown block detected in towers.txt on line\n\"" + s + "\";\nparser is version " + Manager.VERSION + ".")
            s = r.readLine()
          }
        }
      } // If no new block was found, read the next line.
      else s = r.readLine()
    }

    towers
  }

}


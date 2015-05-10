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

class Tower(val name: String, val image: BufferedImage, val cost: Int, val speed: Int, val damage: Int, val range: Int, val upgradesTo: String, val special: String = "") {

  def getCost: Int = cost

  def isUpgradeable: Boolean = upgradesTo != null
  def upgradeCost: Int = if (isUpgradeable) Tower.loadTower(upgradesTo).getCost - cost else -1

}

/**
 * The Tower object provides methods for keeping track of and parsing tower files.
 */

object Tower {

  // Loads a single tower with the specified id; returns null (!!) if not found.
  def loadTower(id: String): Tower = {
    if (Manager.towerlist.contains(id))
      Manager.towerlist(id)
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
                towers += temp("id") -> new Tower(temp("name"), ImageIO.read(new File(".\\images\\" + temp("image"))),
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


package mrtb

import java.io.File
import scala.collection.mutable.Buffer
import scala.collection.mutable.Map
import java.io.BufferedReader
import java.io.FileReader

/**
 * Represents a single game. The enemy waves and other info are read from external
 * files, and this class keeps track of the playfield's state as well as structures
 * containing the relevant enemies, waves and towers.
 */

class Stage {
  // Some variables are created to hold the relevant game-state information.
  // They are initialized here and overwritten by values specified in the stage file.
  var name = "unknown"
  var maker = "unknown"
  var lives = 10
  var betweenWaves = true // A variable for future use.
  var gold = 0
  var score = 0
  var phaseStart = System.currentTimeMillis
  var phaseTime = 2
  var timeLeft = (phaseTime * 1000 + phaseStart - System.currentTimeMillis).toInt / 1000

  var waves = Buffer[Wave]()
  var availableTowers = Buffer[Tower]()
  var mostRecentTower: Tower = null
  var towers = Buffer[Tower]()
  val tiles = Array.tabulate[Tile](Manager.GRIDSIZE._1, Manager.GRIDSIZE._2)((a, b) => new Tile(a + 1, b + 1))

  def setWaves(in: Buffer[Wave]) = waves = in
  def setTowers(in: Buffer[Tower]) = availableTowers = in
  def getCurrentWave: Wave = waves.head

  def placeTower(in: Tower, x: Int, y: Int, finalize: Boolean): Boolean = {
    val tower = new Tower(in.id, in.name, in.image, in.cost, in.speed, in.damage, in.range, in.upgradesTo, in.special: String)

    if (gold >= tower.cost && tiles(x - 1)(y - 1).getTower == null && !(y == 6 && (x == 1 || x == 18))) {
      gold -= tower.cost
      val tile = tiles(x - 1)(y - 1)
      tile.setTower(tower)
      if (Enemy.findShortestPath(tiles)) {
        if (!finalize) {
          gold += tower.cost
          tile.setTower(null)
          Enemy.findShortestPath(tiles)
          true
        } else {
          tile.getTower.setCoordinates(tile)
          towers += tiles(x - 1)(y - 1).getTower
          mostRecentTower = tiles(x - 1)(y - 1).getTower
          true
        }
      } else {
        gold += tower.cost
        tile.setTower(null)
        Enemy.findShortestPath(tiles)
        false
      }
    } else false
  }

  def startWave = {
    score += 3 * timeLeft
    Enemy.findShortestPath(tiles)
    phaseStart = System.currentTimeMillis
    phaseTime = waves.head.enemyList.last._1 / Manager.FPS
    Manager.gameState = "game_wave"
  }

  def nextWave = {
    mrtb.gui.GameScreen.clearShots // direct access like this is not advised, but...
    if (waves.length > 1) {
      waves = waves.drop(1)
      phaseStart = System.currentTimeMillis
      phaseTime = waves.head.buildphase
      timeLeft = (phaseTime * 1000 + phaseStart - System.currentTimeMillis).toInt / 1000
      gold += waves.head.goldbonus
      lives += waves.head.lifebonus
      Manager.gameState = "game_setup"
    } else {
      println("You beat the game! Score: " + score)
      Manager.gameState = "end"
    }
  }

  def loseLife(damage: Int) = {
    lives -= damage
    if (lives <= 0) {
      Manager.gameState = "over"
    }
  }

  def update = {
    if (waves.head.enemyList.isEmpty && Manager.gameState == "game_wave") {
      nextWave
    } else if (timeLeft <= 0 && Manager.gameState == "game_setup") {
      startWave
    } else if (Manager.gameState == "game_wave") {
      timeLeft = (System.currentTimeMillis - phaseStart).toInt / 1000
      if (!waves.head.enemyList.isEmpty) {
        waves.head.update
        towers.foreach(_.update)
      }
    } else {
      timeLeft = (phaseTime * 1000 + phaseStart - System.currentTimeMillis).toInt / 1000
    }
  }
}

/**
 * The singleton object Stage provides functions for reading and verifying stage data from external files.
 * The main focus for version 1.1 would be to improve the leniency and tolerance with the file parsing.
 *
 * The NoWavesException is thrown when the stage file contains no compatible enemy wave definitions.
 */
class NoWavesDefinedException(message: String = "") extends Exception(message)

object Stage {

  // load the stage list when initialized; return has name mapped to a tuple containing the file reference and info fields.
  def listStages(directory: String): Map[String, (File, Array[String])] = {
    val result = Map[String, (File, Array[String])]()

    // The stage directory and its subdirectories are filtered for all text files.
    // Those text files are then checked for validity and loaded.
    if (new File(directory).exists()) {
      for (
        x <- new File(directory).listFiles() // Files in the stage directory are listed,
          .filter(_.isDirectory()).map(_.listFiles).flatten // filtered for directories, then mapped for their contents,
          .filter(a => a.getName().takeRight(4) == ".txt" && a.isFile) // and finally filtered to look for text files.
          ++ new File(directory).listFiles() // The base directory's contents are then concatenated to the result.
          .filter(a => a.getName().takeRight(4) == ".txt" && a.isFile)
      ) {
        if (isValidStage(x)) {
          val temp = getStageInfo(x)
          result += temp(0) -> (x, temp.drop(1))
        } else if (Manager.debug) println(x.getName() + " is not a valid stage")
      }
    } else {
      if (Manager.debug) println("The \'stages\' directory doesn't exist!!!")
      throw new Exception("The stage directory doesn't exist. Exiting...")
    }

    result
  }

  // A method to parse and load a single stage file, returning a Stage object.
  // This should only be called from Manager using a verified stage after the user has selected it.
  // The code is not very pretty nor elegant, but file parsing sometimes causes that to happen...
  def createStage(name: String, stageFile: File): Stage = {
    val waves = Buffer[Wave]()
    val towers = Buffer[Tower]()
    val result = new Stage()
    val r = new BufferedReader(new FileReader(stageFile))
    var s = r.readLine()
    var version = "0"

    // While not at end of file, read a line and see whether it starts a block; if so, process the text.
    try {
      while (s != null) {
        if (s.trim.take(1) == "!") {
          s.trim match {

            case "!info" => s = {
              result.name = this.getStageInfo(stageFile)(0)
              r.readLine()
            }

            case "!availabletowers" => {
              s = r.readLine()
              while (s != null && s.trim.take(1) != "!") {
                if (s.trim.take(1) != "#" && !s.isEmpty) {
                  for (x <- s.split(",").map(_.trim)) {
                    val tower = Tower.loadTower(x)
                    if (tower != null) towers += tower
                  }
                }
                s = r.readLine()
              }
            }

            case "!wave" => {
              s = r.readLine()
              val res = new Wave(waves.size + 1)
              while (s != null && s.trim.take(1) != "!") {
                if (s.trim.take(1) != "#" && !s.isEmpty()) {
                  if (s.split("=").length == 2) {
                    s.split("=")(0) match {
                      case "buildphase" => try {
                        res.setBuildPhase(s.split("=")(1).trim.toInt)
                      } catch { case _: Throwable => if (Manager.debug) println("buildphase in wave #" + waves.length + 1 + " is not a number, ignoring") }
                      case "type" => res.setType(s.split("=")(1).trim)
                      case "goldbonus" => res.setGoldBonus(s.split("=")(1).trim.toInt)
                      case "lifebonus" => res.setLifeBonus(s.split("=")(1).trim.toInt)
                      case _ => if (Manager.debug) println("unknown value defined in wave #" + waves.length + 1 + ", ignoring")
                    }
                  } else {
                    val temp = s.split(',').map(_.trim)
                    if (temp.length == 4) {
                      val enemy = Enemy.loadEnemy(temp(0))
                      if (enemy != null) {
                        try {
                          for (x <- 1 to temp(1).toInt) {
                            res.addEnemy(enemy, temp(3).toInt + temp(2).toInt * x)
                          }
                        } catch { case _: Throwable => if (Manager.debug) println("enemy define in wave #" + waves.length + 1 + "is invalid") }

                      } else if (Manager.debug) println("an invalid enemy series defined in wave #" + waves.length + 1 + ", ignoring")
                    } else if (Manager.debug) println("an invalid enemy series defined in wave #" + waves.length + 1 + ", ignoring")
                  }
                }
                s = r.readLine()
              }
              waves += res
            }

            case "!highscores" => {
              //            High scores are not yet implemented.
              s = r.readLine()
            }

            case _ => {
              if (version == "0") {
                if (s.contains("1.0")) version = "1.0"
              } else if (Manager.debug) println("unknown block detected; parser version " + Manager.VERSION + ".")
              s = r.readLine()
            }
          }

        } else s = r.readLine()
      }

      if (version != "1.0")
        throw new IllegalArgumentException("You tried to load a map file that is incompatible with the parser, version " + Manager.VERSION + ".")

      if (waves.length == 0)
        throw new NoWavesDefinedException("You tried to load a map file with no waves defined.")
    
    } catch {
      case _: Throwable => Manager.stageOK = false
    }
    // If parsing is done, proceed to modify the result before returning it.
    //todo: ...
    if (Manager.debug) println("waves found in map " + result.name + ": " + waves)
    waves.foreach(_.sortEnemies)
    result.setWaves(waves)
    result.gold = waves(0).goldbonus
    result.lives = waves(0).lifebonus
    result.phaseTime = waves(0).buildphase
    result.setTowers(towers)
    result
  }

  // A method to check the validness of a stage file.
  def isValidStage(in: File): Boolean = {
    //todo
    if (Manager.debug)
      if (Manager.stageOK)
        println(in.getName + " seems like a valid stage")
      else
        println(in.getName + " is not a valid stage!!!")
    Manager.stageOK

  }

  // A method to parse the stage file for the info. Returns an array with the info fields;
  // result(0) is stage name; result(1) is creator name; result(2) is description
  def getStageInfo(in: File): Array[String] = {
    val r = new BufferedReader(new FileReader(in))
    var s = r.readLine()
    var result = Array[String](in.getName().dropRight(4), "unknown", "-")

    while (s != null) {
      if (s.trim.take(5) == "!info") {

        do {
          s = r.readLine
          if (s.split('=').length == 2)
            s.split('=')(0).trim match {
              case "name" => result(0) = s.split('=')(1).trim()
              case "creator" => result(1) = s.split('=')(1).trim()
              case "description" => result(2) = s.split('=')(1).trim()
              case _ => if (Manager.debug) println("unknown variable found from info block in file " + in)
            }
        } while (!s.trim.startsWith("!") && s != null)

        // There is only one !info block so the result is returned here.
        return result
      }
      s = r.readLine
    }
    result
  }

}
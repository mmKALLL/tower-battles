package mrtb

import java.io.File
import scala.collection.mutable.Buffer

/**
 * Represents a single game. The enemy waves and other info are read from external
 * files, and this class keeps track of the playfield's state as well as structures
 * containing the relevant enemies and towers.
 */

class Stage(waves: Buffer[Wave]) {
  // Some variables are created to hold the relevant game-state information.
  // They are initialized here and overwritten by values specified in the stage file.
  var lives = 10
  var currentWave = 1
  var betweenWaves = true
  var gold = 200
  var score = 0
  var phaseStart = System.currentTimeMillis
  var phaseTime = 65
  var timeLeft = (phaseTime * 1000 + phaseStart - System.currentTimeMillis) / 1000
  val tiles = Array.ofDim[Tile](Manager.GRIDSIZE._1, Manager.GRIDSIZE._2)
  
  def update = {
    timeLeft = (phaseTime * 1000 + phaseStart - System.currentTimeMillis) / 1000
    if (timeLeft <= 0) {
      
    }
  }
}


/**
 * The singleton object Stage provides functions for reading and verifying stage data from external files.
 */

object Stage {

  // load the stage list when initialized
  def listStages(directory: String): Buffer[String] = {
    val result = Buffer[String]()

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
        if (isValidStage(x.getName)) {
          result += x.getName().dropRight(4)
          println(x.getName + " recognized as a valid stage")
        } else println(x.getName() + " is not a valid stage") // aaa
      }
    } else {
      println("The \'stages\' directory doesn't exist!!!") // aaa
    }

    result
  }

  // A method to parse and load a single stage file, returning a Stage object.
  def createStage(id: String, directory: String): Stage = {
    val stageFile = new File(directory + id)
    if (stageFile.getName() == "test.txt") {
      new Stage(null)
    } else {
      new Stage(null) //todo
    }
    
  }

  // A method to check the validness of a stage file.
  def isValidStage(id: String): Boolean = {
    //todo
    true
  }

}
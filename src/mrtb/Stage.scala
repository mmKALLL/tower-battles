package mrtb

import java.io.File
import scala.collection.mutable.Buffer
import scala.collection.mutable.Map
import java.io.BufferedReader
import java.io.FileReader

/**
 * Represents a single game. The enemy waves and other info are read from external
 * files, and this class keeps track of the playfield's state as well as structures
 * containing the relevant enemies and towers.
 */

class Stage(waves: Buffer[Wave]) {
  // Some variables are created to hold the relevant game-state information.
  // They are initialized here and overwritten by values specified in the stage file.
  var name = "test"
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
 * The main focus for version 1.1 would be to improve the leniency and tolerance with the file parsing.
 */

object Stage {

  // load the stage list when initialized
  def listStages(directory: String): Map[String, File] = {
    val result = Map[String, File]()

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
          result += getStageName(x) -> x
        } else if (Manager.debug) println(x.getName() + " is not a valid stage") // aaa
      }
    } else {
      if (Manager.debug) println("The \'stages\' directory doesn't exist!!!") // aaa
      throw new Exception("The stage directory doesn't exist. Exiting...")
    }

    result
  }

  // A method to parse and load a single stage file, returning a Stage object.
  // This should only be called from Manager using a list of verified stages.
  def createStage(stageFile: File): Stage = {
    //normal=235,235,235; slow=235,235,10; mixed=0,220,0
    new Stage(null) //todo

  }

  // A method to check the validness of a stage file.
  def isValidStage(in: File): Boolean = {
    //todo
    if (Manager.debug)
      println(in.getName + " recognized as a valid stage")
    true

  }

  // A method to parse the stage file for the name. Returns file name without extension if no name is specified.
  def getStageName(in: File): String = {
    var fr = new FileReader(in)
    val r = new BufferedReader(fr)
    var s = r.readLine()
    while (s != null) {
      if (s.trim.take(5) == "!info") {
        do {
          s = r.readLine
          if (s.trim.take(4) == "name") {
            if (Manager.debug)
              println("checked stage name " + s.split('=')(1).trim() + " from file " + in)
            return s.split('=')(1).trim()
          }
        } while (!s.trim.startsWith("!") && s != null)

        return in.getName().dropRight(4)
      }

      s = r.readLine
    }
    in.getName().dropRight(4)
  }

}
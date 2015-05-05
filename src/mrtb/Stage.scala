package mrtb

import java.io.File

/**
 * Represents a single game. The enemy waves and other info are read from external
 * files, and this class keeps track of the playfield's state as well as structures
 * containing the relevant enemies and towers.
 */

class Stage(fileName: String) {

}

object Stage {

  // load the stages when initialized
  def loadStages(directory: String) = {
    if (new File(directory).exists()) {
      for (x <- new File(directory).listFiles().filter(_.getName().takeRight(4) == ".txt"))
        0
    }
  }

}
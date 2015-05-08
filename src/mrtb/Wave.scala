package mrtb

import java.awt.Color
import scala.collection.mutable.Buffer

class Wave(wavetype: String, position: Int) {

  // An ordered list that tracks the time left to spawn a specific enemy in milliseconds.
  var enemyList = Buffer[(Int, Enemy)]()
  
  val description: (String, Color) = wavetype.trim().toLowerCase() match {
    case "normal" => (position + ": Normal", new Color(235, 235, 235))
    case "slow" => (position + ": Slow", new Color(255, 130, 80))
    case "mixed" => (position + ": Mixed", new Color(40, 250, 40))
    case "fast" => (position + ": Fast", new Color(235, 235, 65))
    case "flying" => (position + ": Flying", new Color(80, 160, 255))
    case "spawning" => (position + ": Spawning", new Color(235, 120, 235))
    case "boss" => (position + ": Boss", new Color(235, 45, 0))
    case _ => {
      if (Manager.debug) println("unrecognized wave type defined in wave #" + position + "; replacing with normal")
      (position + ": Normal", new Color(235, 235, 235))
    }
  }
  
}
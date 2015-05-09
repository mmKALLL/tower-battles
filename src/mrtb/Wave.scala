package mrtb

import java.awt.Color
import scala.collection.mutable.Buffer

class Wave(position: Int) {

  // An ordered list that tracks the time left to spawn a specific enemy in milliseconds.
  var enemyList = Buffer[(Int, Enemy)]()
  var wavetype = "normal"
  var text = descriptionS
  var color = descriptionC
  var buildphase = 10
  
  def setBuildPhase(in: Int) = buildphase = in
  def setType(in: String) = {
    wavetype = in
    text = descriptionS
    color = descriptionC
  }
  
  def addEnemy(in: Enemy, time: Int) = enemyList += Tuple2(time, in)

  def descriptionS: String = wavetype match {
    case "normal" => position + ": Normal"
    case "slow" => position + ": Slow"
    case "mixed" => position + ": Mixed"
    case "fast" => position + ": Fast"
    case "flying" => position + ": Flying"
    case "spawning" => position + ": Spawning"
    case "boss" => position + ": Boss"
    case _ => {
      if (Manager.debug) println("unrecognized wave type defined in wave #" + position + "; replacing with normal")
      position + ": Normal"
    }
  }

  def descriptionC: Color = wavetype match {
    case "normal" => new Color(235, 235, 235)
    case "slow" => new Color(255, 130, 80)
    case "mixed" => new Color(40, 250, 40)
    case "fast" => new Color(235, 235, 65)
    case "flying" => new Color(80, 160, 255)
    case "spawning" => new Color(235, 120, 235)
    case "boss" => new Color(235, 45, 0)
    case _ => {
      if (Manager.debug) println("unrecognized wave type defined in wave #" + position + "; replacing with normal")
      new Color(235, 235, 235)
    }
  }

}
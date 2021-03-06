package mrtb

import javax.swing.Timer
import scala.collection.mutable.Buffer
import scala.collection.mutable.Map
import java.io.File

/**
 * Manager is an object that brings together the UI and engine with various abstractions.
 *
 * The aim is to loosely provide a framework of classes that will be mostly adjustable by editing
 * only this file. Along with that, the game is designed to have a high degree of
 * customization options; whether it's by extending the code or writing user-generated
 * content, the aim is to provide a very generic platform for tower-defense games to be
 * created on.
 */

object Manager {

  // Some constants.  \\\\Do not alter.\\\\
  final val TILESIZE = 32
  final val GRIDSIZE = (18, 11)
  final val STAGEDIRECTORY = ".\\stages\\"
  final val FPS = 60
  final val VERSION = "1.0"

  // Variables to hold the game system's internal state.
  // Possible gamestates: init, menu, game_setup, game_wave, over, end
  var gameState = "init"
  // stagelist maps each stage id to its file and an array containing its info (map name, creator name, description)
  var stagelist: Map[String, (File, Array[String])] = null
  // towerlist and enemylist map id's to their respective objects
  var towerlist: Map[String, Tower] = null
  var enemylist: Map[String, Enemy] = null
  var currentStage: Stage = null
  var stageOK: Boolean = true
  var debug = false

  // A timer to keep track with the real-time events ingame.
  val animationTimer = Ticker(1000 / FPS, true) { if (gameState.take(4) == "game") this.update }

  // The GUI is designed to be 800x480; don't change these values!
  var interface: mrtb.gui.GUI = null

  def initialize(args: Array[String]) = {
    gameState = "menu"
    if (!args.isEmpty)
      if (args(0) == "-debug")
        debug = true			// Debug messages are specified to be displayed by GUI as well if possible...

    towerlist = Tower.loadTowers
    enemylist = Enemy.loadEnemies
    stagelist = Stage.listStages(STAGEDIRECTORY)
    interface = new mrtb.gui.GUI(800, 480)
  }

  // Updates are chained "events" that are fired by the animationTimer, and passed
  // along to keep up the game state with time passing.
  def update = {
    if (gameState.take(4) == "game") {
      currentStage.update
    }
    interface.update
  }
  
  // Handler for enemy death
  def destroy(in: Enemy) = {
    if (in.HP <= 0) {
      currentStage.gold += in.goldgain
      currentStage.score += in.scoregain
    } else {
      currentStage.loseLife(in.damage)
    }
    currentStage.waves.head.removeEnemy(in)
  }

  // A method to load one of the stages from the stage list. Called by UI.
  // Individual stages' names need to be unique.
  def loadStage(name: String) = {
    stageOK = true
    if (debug) println("enemies found: " + enemylist + "\nstages found: " + stagelist + "\ntowers found: " + towerlist)
    if (stagelist.contains(name)) {
      currentStage = Stage.createStage(name, stagelist(name)._1)
      gameState = "game_setup"
      Enemy.findShortestPath(currentStage.tiles)
    } else {
      throw new IllegalArgumentException("Tried to load a stage with an id that doesn't exist!!!")
    }
  }

  def exitStage = {
    //todo; a method for exiting the current stage and returning to menu
    // will not be implemented, game will exit on complete for now
  }

  // A string parser for the current wave's phase duration.
  def parsePhaseTime: String = if (currentStage.timeLeft % 60 < 10) (currentStage.timeLeft / 60) + ":0" + (currentStage.timeLeft % 60)
  else (currentStage.timeLeft / 60) + ":" + (currentStage.timeLeft % 60)

}

/**
 * The Ticker enables the creation of Swing Timers that execute anonymous functions.
 * Code by Otfried Cheong, fetched from http://otfried.org/scala/timers.html
 */

object Ticker {
  def apply(interval: Int, repeat: Boolean = true)(op: => Unit) {
    val timeOut = new javax.swing.AbstractAction() {
      def actionPerformed(e: java.awt.event.ActionEvent) = op
    }
    val t = new Timer(interval, timeOut)
    t.setRepeats(repeat)
    t.start()
  }
}

// A handy object for parsing basic written arithmetics; unfortunately, it's unused in v1.0.
object parseCalc {
  def apply(in: String) = {
    var a = 0
    var b = 0
    for (x <- in.split('+')) {
      b = x.split('*').head.trim.toInt
      for (y <- x.split('*').drop(1)) {
        b *= y.trim.toInt
      }
      a += b
    }
    a
  }
}

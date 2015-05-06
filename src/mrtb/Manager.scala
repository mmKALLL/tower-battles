package mrtb

import scala.collection.mutable.Buffer

/**
 * Manager is an object that brings together the UI and engine with various abstractions.
 *
 * The aim is to provide a framework of classes that will be mostly adjustable by editing
 * only this file. Along with that, the game is designed to have a high degree of
 * customization options; whether it's by extending the code or writing user-generated
 * content, the aim is to provide a very generic platform for tower-defense games to be
 * created on.
 */

object Manager {

  final val TILESIZE = 32
  final val GRIDSIZE = (18, 11)

  // Variables to hold the game system's internal state.
  var gameState = "init"
  var stagelist: Buffer[String] = Stage.listStages(".\\stages")
  var currentStage: Stage = null

  // The GUI is designed to be 800x480; don't change these values!
  var interface = new mrtb.gui.GUI(800, 480)

  def initialize = {
    gameState = "menu"
  }

  def loadStage(id: String) = {
    if (stagelist.contains(id)) {
      currentStage = Stage.createStage(id)
      gameState = "game_prewave"
      interface.enterGame
    }
  }

}
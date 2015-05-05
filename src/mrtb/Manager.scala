package mrtb

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
  final val GRIDSIZE = (16, 10)
  var interface = new mrtb.gui.GUI(800, 480)

  def initialize = {
    Stage.loadStages(".\\stages")
    
  }

}
package mrtb

/**
 * Manager is an object that brings together the UI and engine with various abstractions.
 */

object Manager {
  
  final val TILESIZE = 32
  final val GRIDSIZE = (16, 12)
  
  def initialize = {
    Stage.loadStages(".\\stages")
    
    
  }
 
}
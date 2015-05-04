package mrtb

/**
 * A class that represents a single tile on the game field.
 * Provides a basis for pathfinding.
 */

class Tile (x: Int, y: Int, initialTower: Tower = null) {
  
  
  private var placedTower: Tower = initialTower
  
  //todo: not empty if there are enemies in this tile
  def isEmpty: Boolean = placedTower == null && (???)
  
  def getTower: Tower = ???
  def setTower: Tower = ???
  
  
}
package mrtb

import java.awt.Graphics2D

/**
 * A class that represents a single tile on the game field.
 * Provides a basis for pathfinding.
 */

class Tile (x: Int, y: Int, initialTower: Tower = null) {
  
  
  private var placedTower: Tower = initialTower
  
  //todo: not empty if there are enemies in this tile
  def isEmpty: Boolean = placedTower == null && (???)
  
  def getTower: Tower = placedTower
  def setTower(tower: Tower) = placedTower = tower
  def destroyTower = placedTower = null
  
  // A function for drawing the contents of this tile onto the screen.
  def drawTile(g: Graphics2D) = {
    //todo
  }
  
}
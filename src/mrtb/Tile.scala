package mrtb

import java.awt.Graphics2D

/**
 * A class that represents a single tile on the game field.
 * Provides a basis for pathfinding.
 */

class Tile (x: Int, y: Int, initialTower: Tower = null) {
  
  
  private var placedTower: Tower = initialTower
  
  def isEmpty: Boolean = placedTower == null
  def containsEnemies: Boolean = ??? // needed when towers can be places while enemies on screen
  
  def getTower: Tower = placedTower
  def setTower(tower: Tower) = placedTower = tower
  def destroyTower = placedTower = null
  
  def getX = x
  def getY = y
  
  // A function for drawing the contents of this tile onto the screen.
  def drawTile(g: Graphics2D) = {
    //todo
  }
  
}
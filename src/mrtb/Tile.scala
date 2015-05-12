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
  def leftEdge = (Manager.TILESIZE * (x - 1), Manager.TILESIZE * (y - 1) + Manager.TILESIZE / 2)
  def rightEdge = (Manager.TILESIZE * x, Manager.TILESIZE * (y - 1) + Manager.TILESIZE / 2)
  def upperEdge = (Manager.TILESIZE * (x - 1) + Manager.TILESIZE / 2, Manager.TILESIZE * (y - 1))
  def lowerEdge = (Manager.TILESIZE * (x - 1) + Manager.TILESIZE / 2, Manager.TILESIZE * (y))
  def center = (Manager.TILESIZE * (x) - Manager.TILESIZE / 2, Manager.TILESIZE * (y) - Manager.TILESIZE / 2)
  
}
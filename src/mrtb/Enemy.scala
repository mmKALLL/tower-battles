package mrtb

abstract class Enemy(id: String) {
  
}

class StandardEnemy(id: String) extends Enemy(id) {

}



/**
 * The companion object provides some helper functions for using external
 * files to store and load enemies.
 */
object Enemy {
  
  
  
  def loadEnemy(id: String) = ???
  
  def storeEnemy(in: Enemy) = ???
  
  
  
}
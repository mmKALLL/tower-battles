package mrtb

abstract class Enemy(id: String) {
  
}

class StandardEnemy(id: String) extends Enemy(id) {

}



/**
 * The companion object provides some helper functions for using external
 * files to load and create enemies.
 */
object Enemy {
  
  def loadEnemy(id: String): (Boolean, Enemy) = ???
  
  def loadEnemyList = ???
  
  def apply(id: String): Enemy = ???
  
}
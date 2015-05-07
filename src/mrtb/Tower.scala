package mrtb

/**
 * A class that represents a single tower.
 * @cost The tower's placement cost.
 * @speed The shooting speed of a tower, measured in frames.
 * @damage The tower's damage per shot.
 * @upgradesTo The tower that this tower upgrades into, null if final upgrade.
 * @special The special ability of this tower; 0 indicates no special.
 */

class Tower (name: String, cost: Int, speed: Int, damage: Int, range: Int, upgradesTo: Tower, special: String = "") {
  
  
  def getCost: Int = cost
  
  def isUpgradeable: Boolean = upgradesTo != null
  def upgradeCost: Int = if (isUpgradeable) upgradesTo.getCost - cost else -1
  
}
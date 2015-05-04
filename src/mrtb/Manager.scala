package mrtb

/**
 * Manager is an object that brings together the UI and engine with various abstractions.
 */

object Manager {
  
}


/**
 * Timer is an object that keeps track of the frames passed, deals with notifications and
 * updates the reload timings on towers etc.
 */

object Timer extends Thread {
  def run() = {
    val start = System.nanoTime()
    var frames = 0
    
  }
}
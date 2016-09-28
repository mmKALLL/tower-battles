Tower Battles version 1.0-alpha.

An extremely customizable tower defense game. Made in a fairly short timeframe. The code is a mess, but at least the game is somewhat cool.

Run with -debug program argument to enable debug messages in console.

You can edit enemies.txt, towers.txt and the files found under the stages/ directory to modify the game without having to program anything!

Very brief rundown on the classes:

Manager - brings together all the program components
GUI - represents the MainFrame
GameScreen - is the actual drawn surface (using Graphics2D)
The other classes represent a single component of the gameplay, often with companion objects that help streamline their processing.

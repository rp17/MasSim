package raven;

import raven.game.RavenGame;
import raven.ui.GameCanvas;
import raven.ui.RavenUI;
import raven.utils.Log;
import raven.utils.Log.Level;

import javax.swing.SwingUtilities;

import masSim.world.*;
import masSim.world.WorldEventListener;


public class Main {
	private static RavenUI ui;
	private static RavenGame game;
	private static boolean debug = true;
	
	public static void Message(boolean flag, String message)
	{
		if (debug && flag) System.out.println(message);
	}
	
    public static void main(String args[]) {
    	
    	Log.setLevel(Level.DEBUG);
    	game = new RavenGame();
    	ui = new RavenUI(game);
    	SwingUtilities.invokeLater(new Runnable() {
  	      public void run() {
  	    	GameCanvas.getInstance().setNewSize(game.getMap().getSizeX(), game.getMap().getSizeY());
  	      }
  	    });
    	//ui = new RavenUI(game);
    	//GameCanvas.getInstance().setNewSize(game.getMap().getSizeX(), game.getMap().getSizeY());
		SimWorld3 world = new SimWorld3((WorldEventListener) ui);
		Thread MasSimThread = new Thread(world);
		MasSimThread.start();
		game.togglePause();
    	gameLoop();
	}
    
    public static RavenUI getUI(){return ui;};
	//////////////////////////////////////////////////////////////////////////
	// Game simulation

	private static void gameLoop() {
    	
    	Log.info("raven", "Starting game...");
    	
    	long lastTime = System.nanoTime();
    	
    	while (true) {
    		// TODO Resize UI if the map changes!
    		
    		long currentTime = System.nanoTime();

    		game.update((currentTime - lastTime) * 1.0e-9); // converts nano to seconds
    		lastTime = currentTime;
    		// Always dispose the canvas
    		//if(game.getMap() != null){
    		//if(!game.isPaused()) {
    			try {
    				//GameCanvas.startDrawing(game.getMap().getSizeX(), game.getMap().getSizeY());
    				
    				SwingUtilities.invokeLater(new Runnable() {
    			  	      public void run() {
    			  	    	GameCanvas.startDrawing();
    			  	    	game.render();
    			  	      }
    			  	    });

    			} finally {
    				SwingUtilities.invokeLater(new Runnable() {
  			  	      public void run() {
  			  	    	GameCanvas.stopDrawing();
  			  	      }
  			  	    });
    			}
    		//}
    		//}
    		//TestTaemsScheduler();

    		long millisToNextUpdate = (long) Math.max(0, 16.66667 - (System.nanoTime() - currentTime)*1.0e-6);
			
			try {
				Thread.sleep(millisToNextUpdate);
			} catch (InterruptedException e) {
				break;
			}
    	}
    }


}

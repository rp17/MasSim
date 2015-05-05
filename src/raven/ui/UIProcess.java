package raven.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import raven.TaskIssuer;
import raven.game.RavenGame;
import raven.utils.Level;
import raven.utils.Log;
import raven.utils.SchedulingLog;
import masSim.world.*;

public class UIProcess {
	private static RavenUI ui;
	private static RavenGame game;
	private static boolean debug = true;
	
	public static void main(String[] args) {
    	//Launch a new thread containing a console window to issue commands to agents via mqtt
    	new Thread(new TaskIssuer("127.0.0.1", 1883)).start();
    	
    	Log.setLevel(Level.DEBUG);
    	SchedulingLog.setLevel(Level.INFO);
    	game = new RavenGame();
    	ui = new RavenUI(game);
    	SwingUtilities.invokeLater(new Runnable() {
  	      public void run() {
  	    	GameCanvas.getInstance().setNewSize(game.getMap().getSizeX(), game.getMap().getSizeY());
  	      }
  	    });
    	
		game.togglePause();
    	uiLoop();
	}

	private static void uiLoop() {
    	
    	long lastTime = System.nanoTime();
    	
    	while (true) {
    		// TODO Resize UI if the map changes!
    		
    		long currentTime = System.nanoTime();

    		//game.update((currentTime - lastTime) * 1.0e-9); // converts nano to seconds
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

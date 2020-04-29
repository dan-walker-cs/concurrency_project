//package project2Concurrent;

import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class P2 {
	static int numPlayers;
	int numChairs;
	int currentRound = 1;
	
	Vector<Chair> chairList = new Vector<Chair>();
	Vector<Player> playerList = new Vector<Player>();
	
	P2(int players) {
		setNumPlayers(players);
		setNumChairs();
		setupChairs();
	}
	
	
	public static void main(String[] args) {
		P2 game;
		
		if(args.length > 0)
			game = new P2(Integer.parseInt(args[0]));
		else
			game = new P2(10);
		
		// make lots of latches for GREAT concurrency
		CountDownLatch setupLatchEmcee = new CountDownLatch(1);
		CountDownLatch setupLatchPlayer = new CountDownLatch(numPlayers);
		CountDownLatch gameLatchEmcee = new CountDownLatch(1);
		CountDownLatch gameLatchPlayer = new CountDownLatch(numPlayers);
		CountDownLatch postLatchEmcee = new CountDownLatch(1);
		CountDownLatch postLatchPlayer = new CountDownLatch(numPlayers);
		
		// start player threads
		for(int i = 0; i < numPlayers; i++) {
			game.playerList.add(new Player(i, setupLatchEmcee, setupLatchPlayer, gameLatchEmcee, gameLatchPlayer, postLatchEmcee, postLatchPlayer, game));
			game.playerList.get(i).start();
		}
		
		// wait for emcee to finish
		setupLatchEmcee.countDown();
		// wait for players to finish
		try {
			setupLatchPlayer.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//-------------------------------------
		
		// starts the game
		System.out.println("BEGIN");
		int startingPlayers = numPlayers;
		
		while(game.currentRound < startingPlayers) {
			Player.chairsLeft = numPlayers - 1;
			// signals beginning of round
			System.out.println("\nround " + game.currentRound);
						
			// generates random values
			Random mixTape = new Random(50);
			// random value to check
			int stopMusic;
			// holds whether music is playing or not
			boolean isPlaying = true;
						
			// while loop for while music is playing
			while(isPlaying) {
				// generate random value
				stopMusic = mixTape.nextInt(10);
				// checks if the value will stop the music
				isPlaying = game.playMusic(stopMusic);
			}
					
			// Lets players know to begin looking for seats
			System.out.println("music off");
			// waits for emcee to finish execution
			gameLatchEmcee.countDown();
			// waits for players to find seats
			try {
				gameLatchPlayer.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			//-----------------------------------------
		
			//removes the last indexed chair from the game
			game.chairList.remove(game.chairList.size() - 1);
			// decrement number of players
			numPlayers = numPlayers - 1;
			// reset number of chairs
			game.setNumChairs();
			// iterate current round
			game.currentRound = game.currentRound + 1;
			// wait for emcee to finish
			postLatchEmcee.countDown();
			// wait for players to finish
			try {
				postLatchPlayer.await(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// reset latches for new round
			gameLatchEmcee = new CountDownLatch(1);
			gameLatchPlayer = new CountDownLatch(numPlayers);
			postLatchEmcee = new CountDownLatch(1);
			postLatchPlayer = new CountDownLatch(numPlayers);
			
			// pass latches to players
			for(int i = 0; i < numPlayers; i++) {
				game.playerList.get(i).setLatches(gameLatchEmcee, gameLatchPlayer, postLatchEmcee, postLatchPlayer);
			}
		}
		System.out.println("\n" + game.playerList.get(0).playerName + " wins!\nEND");
	}
	
	// this method removes a player from the player list
	public void removePlayer(Player LOSER) {
		playerList.removeElement(LOSER);
	}
	
	// This method controls whether or not the music is playing
	public boolean playMusic(int value) {
		if(value != 7)
			// keep playing music
			return true;
			
		// stop the music
		return false;
	}
	
	public void setNumPlayers(int players) {
		numPlayers = players;
	}
	
	public int getNumPlayers() {
		return numPlayers;
	}
	
	public void setNumChairs() {
		this.numChairs = numPlayers - 1;
	}
	
	public int getNumChairs() {
		return numChairs;
	}
	
	public void setupChairs() {
		for(int i = 0; i < numChairs; i++) {
			chairList.add(new Chair(i));
		}
	}
}

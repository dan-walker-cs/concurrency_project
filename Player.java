//package project2Concurrent;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Player extends Thread implements Runnable {
	int playerId;
	String playerName;
	boolean isSitting;
	Chair currentChair;
	volatile static int chairsLeft;
	Random random = new Random(50);
	private CountDownLatch setupLatchEmcee;
	private CountDownLatch setupLatchPlayer;
	private CountDownLatch gameLatchEmcee;
	private CountDownLatch gameLatchPlayer;
	private CountDownLatch postLatchEmcee;
	private CountDownLatch postLatchPlayer;
	P2 game;
	
	
	Player(int id, CountDownLatch setupLatchEmcee, CountDownLatch setupLatchPlayer, CountDownLatch gameLatchEmcee, 
			CountDownLatch gameLatchPlayer, CountDownLatch postLatchEmcee, CountDownLatch postLatchPlayer, P2 game) {
		this.playerId = id;
		this.playerName = "P" + (playerId + 1);
		this.setupLatchEmcee = setupLatchEmcee;
		this.setupLatchPlayer = setupLatchPlayer;
		this.gameLatchEmcee = gameLatchEmcee;
		this.gameLatchPlayer = gameLatchPlayer;
		this.postLatchEmcee = setupLatchEmcee;
		this.postLatchPlayer = setupLatchPlayer;
		this.game = game;
	}
	
	public void run() {
		try {
			// wait on notification from Emcee
			setupLatchEmcee.await();
			// tracks player arrival
			setupLatchPlayer.countDown();
			// waits for all players to arrive
			setupLatchPlayer.await();
			
			//-------------------------------------------
			
			// repeat until game's over
			while(game.getNumPlayers() > 1) {
				Thread.sleep(3000);
				// waits for Emcee to prompt the game start
				gameLatchEmcee.await();
				// while not in a chair, find a chair
				while(isSitting == false) {
					// player finds a seat and attempts to sit in it
					sit(findSeat());
					if(chairsLeft == 0) {
						break;
					}
				}
				// tracks players still looking to sit
				gameLatchPlayer.countDown();
				// waits for all players to sit
				gameLatchPlayer.await();
				// kill losing player
				
				if(isSitting == false) {
					System.out.println(playerName + " lost");
					game.removePlayer(this);
					return;
				}
				postLatchPlayer.countDown();
				//--------------------------------------------
			
				// waits for emcee to finish postgame stuff
				postLatchEmcee.await();
			
				// everyone stands back up for next round
				getUp();
				Thread.sleep(3000);
				//---------------------------------------------
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	// reset latches for new round
	public synchronized void setLatches(CountDownLatch gameLatchEmcee, CountDownLatch gameLatchPlayer, CountDownLatch postLatchEmcee, CountDownLatch postLatchPlayer) {
		this.gameLatchEmcee = gameLatchEmcee;
		this.gameLatchPlayer = gameLatchPlayer;
		this.postLatchEmcee = postLatchEmcee;
		this.postLatchPlayer = postLatchPlayer;
	}
	
	public Chair findSeat() {
		int randomChair = random.nextInt(game.chairList.size());
		Chair targetChair = game.chairList.get(randomChair);
		
		return targetChair;
	}
	
	// attempts to match player to chair
	public boolean sit(Chair chair) {
		try {
			Thread.sleep(random.nextInt(1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		isSitting = chair.sit(playerId);
		if(isSitting) {
			currentChair = chair;
			System.out.println(playerName + " sat in " + chair.chairName);
			chairsLeft = chairsLeft - 1;
		}
			
		return isSitting;
	}
		
	// leaves chair for next round
	public void getUp() {
		try {
			Thread.sleep(random.nextInt(1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		currentChair.emptyChair();
		currentChair = null;
		isSitting = false;
	}
}

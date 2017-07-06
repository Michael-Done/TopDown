package com.topdown.game;

import java.io.Serializable;

public class PlayerAddedResponse implements Serializable {
	private static final long serialVersionUID = 6369306932330693322L;

	public final int index;
	// public final Player[] currentPlayers;
	public String map;

	public PlayerAddedResponse(int index, String map) { // Array<Player>
														// currentPlayers) {
		this.index = index;
		this.map = map;
		// List<Player> t = new ArrayList<Player>(currentPlayers.size);
		// for (Player p : currentPlayers) {
		// t.add(p);
		// }
		//
		// this.currentPlayers = t.toArray(new Player[0]);
	}

}

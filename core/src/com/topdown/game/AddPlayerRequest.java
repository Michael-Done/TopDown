package com.topdown.game;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

public class AddPlayerRequest implements Serializable  {
	private static final long serialVersionUID = 7643033334188497399L;

	public Vector2 spawnLocation;
	public String image;
	public int index;
	
	public AddPlayerRequest(String image, Vector2 spawnLocation) {
		this.image = image;
		this.spawnLocation = spawnLocation;
		index = -1;
	}
	public String toString(){
		return "Request to add new Player";
	}
}

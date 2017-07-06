package com.topdown.game;

import com.badlogic.gdx.utils.Array;

public abstract class Gun implements Weapon {

	protected int dmg;
	protected int magCap;
	protected int currentAmmo;
	protected Array<Bullet> bullets;

	public abstract void fire();
}

package com.topdown.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class Bullet {
	Vector2 start = new Vector2();
	Vector2 end = new Vector2();
	Vector2 normal = new Vector2();
	boolean dead = false;
	ShapeRenderer sr;
	private int deathCount;
	private final int LIFESPAN = 30;
	private float alpha;
	private final float LENGTH;
	Vector2 dir;
	Sprite bulletTrail;
	private Sprite[] hitAnimation;

	private int frameCount;

	public Bullet(Vector2 start, Vector2 end, Vector2 normal, World world) {
		bulletTrail = new Sprite(new Texture("Bullet_Trail_Inverse.png"));
		hitAnimation = new Sprite[8];
		for (int i = 0; i < 8; i++) {
			hitAnimation[i] = new Sprite(new Texture("Bullet_Hit_" + (i + 1) + ".png"));
			hitAnimation[i].setOrigin(11, 6);
			hitAnimation[i].setRotation(normal.angle() + 180);
			hitAnimation[i].setPosition(end.x - 11, end.y - 6);
		}
		System.out.println(normal.angle());
		sr = new ShapeRenderer();
		this.start.set(start);
		this.end.set(end);
		this.normal.set(normal);
		deathCount = LIFESPAN;
		alpha = 0.5f;
		LENGTH = start.dst(end);
		dir = new Vector2(end);
		dir.sub(start).nor().scl(20);
		bulletTrail.setSize(LENGTH, 0.5f);
		bulletTrail.setOrigin(0, bulletTrail.getHeight() / 2);
		bulletTrail.setPosition(start.x, start.y);
		bulletTrail.rotate(dir.angle());
		frameCount = 0;
	}

	public void update() {
		deathCount--;
		frameCount++;
		alpha *= 0.9f;
		if (deathCount <= 0) {
			dead = true;
		}
	}

	public void draw(Matrix4 matrix, SpriteBatch batch) {
		// Gdx.gl.glEnable(GL20.GL_BLEND);
		// Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		if (!dead) {
			bulletTrail.setAlpha(alpha);
			bulletTrail.draw(batch);
			if (frameCount < 8) {
				hitAnimation[frameCount].draw(batch);
			}
		}

		// Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	public void updateAndDraw(Matrix4 matrix, SpriteBatch batch) {
		update();
		draw(matrix, batch);
	}
}

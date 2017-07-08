package com.topdown.game;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class TopDownServer implements ApplicationListener {
	private Skin skin;
	private Stage stage;
	private Server server;
	private Kryo kryo;
	private Label labelDetails;
	Array<Player> players;
	Table table;
	World world;
	// Map stuff
	private TiledMap background;

	@Override
	public void create() {
		// Initialize world
		world = new World(new Vector2(0, 0), true);
		// Initialize map
		background = new TmxMapLoader().load("Map_1.tmx");

		MapLayer layer = background.getLayers().get("CollisionLayer");
		for (MapObject object : layer.getObjects()) {
			if (object instanceof RectangleMapObject) {
				RectangleMapObject rectangleObject = (RectangleMapObject) object;
				Rectangle rect = rectangleObject.getRectangle();
				System.out.println(rect.x + ", " + rect.y + "| width: " + rect.width + " height: " + rect.height);
				Body body;
				BodyDef bodyDef = new BodyDef();
				bodyDef.type = BodyDef.BodyType.StaticBody;
				bodyDef.position.set((rect.getX() + rect.getWidth() / 2), (rect.getY() + rect.getHeight() / 2));

				body = world.createBody(bodyDef);

				PolygonShape shape = new PolygonShape();
				shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);

				FixtureDef fixtureDef = new FixtureDef();
				fixtureDef.shape = shape;
				fixtureDef.density = 0.1f;
				// fixtureDef.filter.categoryBits = (short) 1;
				fixtureDef.filter.groupIndex = (short) 1;
				// fixtureDef.filter.maskBits = (short) 2;

				body.createFixture(fixtureDef);
				shape.dispose();
			}
		}
		// Create a list of players
		players = new Array<Player>();
		// Load skin and stage
		skin = new Skin(Gdx.files.internal("Holo-dark-hdpi.json"));
		stage = new Stage();
		// set the input processor
		Gdx.input.setInputProcessor(stage);

		// Get a list of the server's IP addresses
		List<String> addresses = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface ni : Collections.list(interfaces)) {
				for (InetAddress address : Collections.list(ni.getInetAddresses())) {
					if (address instanceof Inet4Address) {
						addresses.add(address.getHostAddress());
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// Print the contents of our array to a string. Yeah, should have used
		// StringBuilder
		String ipAddress = new String("");
		for (String str : addresses) {
			ipAddress = ipAddress + str + "\n";
		}
		// Make the label display the IPs
		labelDetails = new Label(ipAddress, skin);
		// Start the server
		server = new Server();
		server.start();
		try {
			server.bind(54555, 54777);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// Register the classes with the server's kryo serializer
		kryo = server.getKryo();
		Registration.registerClasses(kryo);

		// Add a listener to the server
		server.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				if (object instanceof AddPlayerRequest) {
					AddPlayerRequest request = (AddPlayerRequest) object;
					System.out.println("add player request recieved");
					connection.sendTCP(new PlayerAddedResponse(players.size, "Map_1.tmx"));
					addPlayer(request);
				} else if (object instanceof PlayerMoveRequest) {
					PlayerMoveRequest request = (PlayerMoveRequest) object;
					players.get(request.index).setVelocity(request.delta);
				} else if (object instanceof RotatePlayerRequest){
					RotatePlayerRequest request = (RotatePlayerRequest) object;
					players.get(request.index).getSprite().setAngle(request.angle);
					connection.sendTCP(new PlayerLocationResponse(players.get(request.index).getLocation()));
				}
			}
		});
		// Add the table
		table = new Table();
		table.add(labelDetails);
		table.row();
		table.pack();
		// Add table to stage
		stage.addActor(table);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		// Clear the screen
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		// Draw the stage
		stage.draw();

		world.step(1 / 60f, 6, 2);
		SerializableSprite[] sprites = new SerializableSprite[players.size];
		for (int i = 0; i < players.size; i++) {
			players.get(i).update();
			sprites[i] = players.get(i).getSprite();
		}
		server.sendToAllTCP(sprites);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		server.close();
	}

	private void addPlayer(AddPlayerRequest request) {
		players.add(new Player(request.image, request.spawnLocation, world));
	}
	

}

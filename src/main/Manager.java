package main;

//project imports
import main.misc.Vector2;
import main.misc.Music;
import main.character.*;
import main.object.wall.*;
import main.object.floor.*;
import main.object.Object;
import main.object.Projectile;
import main.ui.Button;
import main.ui.Debugger;
import main.ui.UserInterface;

//default java imports
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Manager extends JPanel implements ActionListener, KeyListener, MouseListener, ComponentListener, ContainerListener {
	
	public boolean menu = true;
	public boolean running = false;
	public boolean info = false;
	public boolean fullscreen = false;
	public boolean paused = false;
	public int[] secret = new int[] { 38, 38, 40, 40, 37, 39, 37, 39 };
	public int currentSecret = 0;
	public boolean wub = false;
	
	public Window window;
	
	public boolean[] keyPress = new boolean[255];
	public int lastKeyPress = 0;
	
	//highest -> lowest level containers: (highest is displayed above, lowest is displayed below)
	//	ui -> wall -> player -> projectile -> floor
	public Container uiContainer;
	public Container playerContainer;
	public Container projectileContainer;
	public Container wallContainer;
	public Container floorContainer;
	
	public Pathfinder pathfinder;
	
	public Timer gameTimer;
	public Vector2 mouse = new Vector2();
	public Player player;
	public long lastFrame = System.nanoTime();
	public double fixedFps = 60.0;
	public double fpsCap = 500.0;
	public boolean displayEnemyMovements = false;
	
	public Vector2 defaultScreen = new Vector2(1920, 1080);
	public Vector2 screen = defaultScreen;
	public Vector2 ratio = new Vector2(1, 1);
	
	public BufferedImage canvas;
	public Debugger debugger;
	public String currentTween = "r";
	public int tween = 1;
	public int cr = 0;
	public int cg = 0;
	public int cb = 0;
	public int angle = 0;
	public int counter = 0;
	
	public Music backgroundMusic = new Music("resources\\sound\\Big Mine.wav");
	public Background bg;
	public Wall w1;
	public Font font;
	
	public HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage> ();
	public HashMap<String, Music> music = new HashMap<String, Music> ();
	
	public Manager(Window window) {
		this.window = window;
		
		canvas = new BufferedImage((int) screen.x, (int) screen.y, BufferedImage.TYPE_INT_RGB);
		
		// create default font
		try {
			InputStream myStream = new BufferedInputStream(new FileInputStream("resources\\pixelfont.ttf"));
			font = Font.createFont(Font.TRUETYPE_FONT, myStream).deriveFont(24f);
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}
		
		uiContainer = new Container();
		playerContainer = new Container();
		projectileContainer = new Container();
		wallContainer = new Container();
		floorContainer = new Container();
		
		uiContainer.addContainerListener(this);
		playerContainer.addContainerListener(this);
		projectileContainer.addContainerListener(this);
		wallContainer.addContainerListener(this);
		floorContainer.addContainerListener(this);
		
		pathfinder = new Pathfinder(this);
		
		backgroundMusic.loop = true;
		backgroundMusic.setVolume(1);
		backgroundMusic.play();
		
		//add containers to JPanel
		add(uiContainer);
		add(playerContainer);
		add(wallContainer);
		add(projectileContainer);
		add(floorContainer);
		
		debugger = new Debugger();
		uiContainer.add(debugger);
		
		//info user interface
		debugger.addString(new String[] { "fps" });
		debugger.addString(new String[] { "key press" });
		debugger.addString(new String[] { "window size" });
		debugger.addString(new String[] { "fullscreen" });
		debugger.addString(new String[] { "characters" });
		debugger.addString(new String[] { "projectiles" });
		
		addKeyListener(this);
		addMouseListener(this);
		addComponentListener(this);
		setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        requestFocus();
        
        //start();
        menu();
        
		gameTimer = new Timer(0, this);
		gameTimer.start();
	}
	
	public void paintComponent(Graphics g) throws java.lang.ArithmeticException {
		super.paintComponent(g);
		
		AffineTransform at = new AffineTransform();
		at.scale(screen.x/canvas.getWidth(), screen.y/canvas.getHeight());
		
		//supar rotut
		if (wub) {
			at.translate(canvas.getWidth()/2, canvas.getHeight()/2);
			at.rotate(angle*Math.PI/180);
			at.translate(-canvas.getWidth()/2, -canvas.getHeight()/2);
			angle += 1;
		}
		
		Graphics2D g2 = (Graphics2D) g;
		g2.drawRenderedImage(canvas, at);
		
		//supar seizur
		if (wub) {
			if (currentTween.equals("r")) {
				cr += 5*tween;
				if ((tween == 1 && cr >= 255) || (tween == -1 && cr <= 0)) {
					currentTween = "g";
				}
			} else if (currentTween.equals("g")) {
				cg += 5*tween;
				if ((tween == 1 && cg >= 255) || (tween == -1 && cg <= 0)) {
					currentTween = "b";
				}
			} else if (currentTween.equals("b")) {
				cb += 5*tween;
				if ((tween == 1 && cb >= 255) || (tween == -1 && cb <= 0)) {
					tween = -tween;
					currentTween = "r";
				}
			}
			g2.setColor(new Color(cr, cg, cb, 150));
			g2.fillRect(0, 0, (int) screen.x, (int) screen.y);
		}
		
		//info display
		debugger.updateString(new String[] { "key press", String.valueOf(lastKeyPress) });
		debugger.updateString(new String[] { "window size", screen.toString() });
		debugger.updateString(new String[] { "fullscreen", String.valueOf(fullscreen) });
		debugger.updateString(new String[] { "characters", String.valueOf(playerContainer.getComponentCount()) });
		debugger.updateString(new String[] { "projectiles", String.valueOf(projectileContainer.getComponentCount()) });
	}
	
	public void menu() {
		ArrayList<Object> menuObjects = new ArrayList<Object> ();
		
		Button startButton = new Button("resources\\image\\button.png", new Vector2(0, 600), new Vector2(700, 70));
		menuObjects.add(startButton);
		
		Background menuBack = new Background(defaultScreen, "resources\\image\\menu_back.png");
		menuObjects.add(menuBack);
		
		for (int i = 0; i < menuObjects.size(); i++) {
			uiContainer.add(menuObjects.get(i));
		}
	}
	
	public void start() {
		bg = new Background(defaultScreen);
		floorContainer.add(bg);
		
		//create walls
		w1 = new Wall("top", new Vector2(200, 0));
		wallContainer.add(w1);
		
		w1 = new Wall("top", new Vector2(1060, 0));
		wallContainer.add(w1);
		
		Door d1 = new Door("top", new Vector2(860, 0));
		wallContainer.add(d1);
		
		w1 = new Wall("bottom", new Vector2(200, 880));
		wallContainer.add(w1);
		
		w1 = new Wall("bottom", new Vector2(1060, 880));
		wallContainer.add(w1);
		
		Door d2 = new Door("bottom", new Vector2(860, 880));
		wallContainer.add(d2);
		
		w1 = new Wall("left", new Vector2(0, 200));
		wallContainer.add(w1);
		
		w1 = new Wall("left", new Vector2(0, 640));
		wallContainer.add(w1);
		
		Door d3 = new Door("left", new Vector2(0, 440));
		wallContainer.add(d3);
		
		w1 = new Wall("right", new Vector2(1720, 200));
		wallContainer.add(w1);
		
		w1 = new Wall("right", new Vector2(1720, 640));
		wallContainer.add(w1);
		
		Door d4 = new Door("right", new Vector2(1720, 440));
		wallContainer.add(d4);
		
		Wall corner = new Wall("corner", new Vector2());
		wallContainer.add(corner);
		
		corner = new Wall("corner", new Vector2(1720, 880));
		corner.rotation = 180;
		wallContainer.add(corner);
		
		corner = new Wall("corner2", new Vector2(0, 880));
		corner.rotation = 180;
		wallContainer.add(corner);
		
		corner = new Wall("corner2", new Vector2(1720, 0));
		wallContainer.add(corner);
	}
	
	public void actionPerformed(ActionEvent e) {
		mouse = new Vector2(MouseInfo.getPointerInfo().getLocation()).sub(new Vector2(window.frame.getLocation()).add(new Vector2(window.frame.getInsets())));
		long now = System.nanoTime();
		double delta = ((System.nanoTime()) - lastFrame) / 1000000;
		delta = delta/1000;
		
		boolean skip = false;
		if (1/delta > fpsCap) {
			skip = true;
		}
		
		
		if (!skip) {
			lastFrame = now;
			debugger.updateString(new String[] { "fps", String.format("%.0f", 1/delta) });
			
			ArrayList<Player> characters = new ArrayList<Player>();
			
			for (int i = 0; i < getComponentCount(); i++) {	
				if (getComponent(i) instanceof Container) {
					Container container = (Container) getComponent(i);
					container.setSize(screen.dimension());
					container.setLocation(0, 0);
					
					for (int j = 0; j < container.getComponentCount(); j++) {
						Object object = (Object) container.getComponent(j);
						object.step(delta);
						
						if (object instanceof Projectile) {
							Projectile projectile = (Projectile) object;
							
							if (projectile.expired()) {
								container.remove(object);
							}
						}
						
						if (object instanceof Player) {
							Player plr = (Player) object;
							
							if (plr.health <= 0) {
								playerContainer.remove(plr);
							} else {
								characters.add(plr);
							}
						}
						
						if (object instanceof UserInterface) {
							UserInterface ui = (UserInterface) object;
							if (ui.inside(mouse) && !ui.hovering) {
								ui.hovering = true;
							} else if (!ui.inside(mouse)) {
								ui.hovering = false;
							}
						}
						
						if (displayEnemyMovements) {
							if (object instanceof Enemy) {
								Enemy enemy = (Enemy) object;
								enemy.drawPath(canvas);
							}
						}
					}
				}
			}
			
			// Sort character list according to y position to assign z order
			for (int i = 0; i + 1 < characters.size(); i++) {
				if (characters.get(i).position.y < characters.get(i + 1).position.y) {
					Player temp = characters.get(i);
					characters.set(i, characters.get(i + 1));
					characters.set(i + 1, temp);
					i = 0;
				}
			}
			
			for (int i = 0; i < characters.size(); i++) {
				playerContainer.setComponentZOrder(characters.get(i), i);
			}
			
			int code = (keyPress[37] ? 37 : keyPress[38] ? 38 : keyPress[39] ? 39 : keyPress[40] ? 40 : 0);
			if (code != 0) {
				Vector2 direction = new Vector2();
				switch (code) {
					case 37:
						direction = new Vector2(-1, 0);
						break;
					case 38:
						direction = new Vector2(0, -1);
						break;
					case 39:
						direction = new Vector2(1, 0);
						break;
					case 40:
						direction = new Vector2(0, 1);
						break;
				}
				
				if (!paused && player != null) {
					player.attack(direction);
				}
			}
			
			repaint();
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		lastKeyPress = code;
		
		if (secret[currentSecret] == code) {
			currentSecret++;
			if (currentSecret == secret.length) {
				currentSecret = 0;
				wub = !wub;
				angle = 0;
			}
		} else {
			currentSecret = 0;
		}
		
		if (running && player != null) {
			if (!keyPress[code]) {
				switch (code) {
					//movement
					//	68 -> D
					//	65 -> A
					//  83 -> W
					//	87 -> S
					case 68:
						player.velocity.x += 1;
						break;
					case 65:
						player.velocity.x -= 1;
						break;
					case 83:
						player.velocity.y += 1;
						break;
					case 87:
						player.velocity.y -= 1;
						break;
				}
			}
			
			switch(code) {
				//attacking
				//	37 -> left
				//	38 -> up
				//	39 -> right
				//	40 -> down
					
				//abilities
				//	81 -> Q
				//	69 -> E
				case 81:
					player.ability1();
					break;
				case 69:
					player.ability2();
					break;
				
				//toggle pathfinding drawing
				case 86:
					displayEnemyMovements = !displayEnemyMovements;
					break;
				
				//pause
				case 80:
					if (paused) {
						fixedFps = 60;
					} else {
						fixedFps = 0;
					}
					paused = !paused;
					break;
			}
		}
		
		//key presses (all)
		switch(code) {
			//toggle info
			case 67:
				debugger.display = !debugger.display;
				break;
			
			//toggle fullscreen
			case 122:
				if (window.fullscreen) {
					window.setWindowed();
				} else {
					window.setFullscreen();
				}
				break;
				
			//exits fullscreen
			case 27:
				if (window.fullscreen) {
					window.setWindowed();
				}
		}
		
		keyPress[e.getKeyCode()] = true;
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();
		if (player != null && keyPress[code]) {
			switch (code) {
				case 68:
					player.velocity.x -= 1;
					break;
				case 65:
					player.velocity.x += 1;
					break;
				case 83:
					player.velocity.y -= 1;
					break;
				case 87:
					player.velocity.y += 1;
					break;
			}
		}
		
		keyPress[e.getKeyCode()] = false;
	}

	@Override
	public void keyTyped(KeyEvent arg0) { }

	@Override
	public void mouseClicked(MouseEvent e) {
		for (int i = 0; i < uiContainer.getComponentCount(); i++) {
			if (uiContainer.getComponent(i) instanceof UserInterface) {
				UserInterface ui = (UserInterface) uiContainer.getComponent(i);
				if (ui.inside(mouse)) {
					ui.click();
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) {
		for (int i = 0; i < uiContainer.getComponentCount(); i++) {
			if (uiContainer.getComponent(i) instanceof UserInterface) {
				UserInterface ui = (UserInterface) uiContainer.getComponent(i);
				if (ui.inside(mouse)) {
					ui.clickDown();
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		for (int i = 0; i < uiContainer.getComponentCount(); i++) {
			if (uiContainer.getComponent(i) instanceof UserInterface) {
				UserInterface ui = (UserInterface) uiContainer.getComponent(i);
				if (ui.inside(mouse)) {
					ui.clickUp();
				}
			}
		}
	}

	@Override
	public void componentHidden(ComponentEvent e) { }

	@Override
	public void componentMoved(ComponentEvent e) { }

	@Override
	public void componentResized(ComponentEvent e) {
		if (window != null) {
			screen = screen.div(ratio);
			Vector2 currentScreen = new Vector2(window.frame.getContentPane().getSize());
			ratio = currentScreen.div(defaultScreen);
			screen = screen.mult(ratio);
			canvas = new BufferedImage((int) screen.x, (int) screen.y, BufferedImage.TYPE_INT_RGB);
		}
	}

	@Override
	public void componentShown(ComponentEvent e) { }

	@Override
	public void componentAdded(ContainerEvent e) {
		if (e.getChild() instanceof Object) {
			Object addedObject = (Object) e.getChild();
			addedObject.manager = this;
		}
	}

	@Override
	public void componentRemoved(ContainerEvent e) { }
}

package main;

import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.FileInputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import main.misc.Vector2;

public class Window {
	
	public boolean fullscreen = false;
	public Vector2 minimumSize = new Vector2(400, 300);
	public Manager manager;
	public GraphicsEnvironment ge;
	public GraphicsDevice gd;
	public JFrame frame;
	private JFrame holder;
	private String icon = "resources\\image\\dungen.png";
	
	public Window() {
		super();
		newFrame();
		
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
	}
	
	public void setFullscreen() {
		newFrame();
		
		frame.setUndecorated(true);
		frame.pack();
		frame.add(manager);
		frame.setVisible(true);
		
		fullscreen = true;
		
		if (holder != null) {
			holder.dispose(); // disposes of old window
		}
	}
	
	public void setFullscreen(Vector2 resolution) {
		setFullscreen();
		
		System.out.println(gd.isFullScreenSupported());
		if (gd.isFullScreenSupported()) {
			gd.setFullScreenWindow(frame);
		}
		System.out.println(gd.isDisplayChangeSupported());
		if (gd.isDisplayChangeSupported()) {
			DisplayMode dm = new DisplayMode((int) resolution.x, (int) resolution.y, DisplayMode.BIT_DEPTH_MULTI, DisplayMode.REFRESH_RATE_UNKNOWN);
			gd.setDisplayMode(dm);
		}
		
		manager.resolutionChange();
	}
	
	public void setWindowed() {
		newFrame();
		
		Vector2 screen = new Vector2();
		frame.setLocation(screen.scalar(.1).point());
		frame.setSize(screen.scalar(.85).dimension());
		frame.pack();
		frame.add(manager);
		frame.setVisible(true);
		
		fullscreen = false;
		
		if (holder != null) {
			holder.dispose(); // disposes of old window
		}
		
		manager.resolutionChange();
	}
	
	private void newFrame() {
		holder = frame;
		
		frame = new JFrame();
		frame.setTitle("You're in a Dungeon");
		frame.setMinimumSize(minimumSize.dimension());
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		try {
			frame.setIconImage(ImageIO.read(new FileInputStream(icon)));
        } catch(Exception e) {
        	System.out.println("missing logo");
        }
	}
}
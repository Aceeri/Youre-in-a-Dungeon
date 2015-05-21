package main.character;

import main.misc.Vector2;
import main.Manager;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Enemy extends Player {
	private Node[] path;
	
	public Enemy(Vector2 position) {
		super(position);
		
		type = "enemy";
	}
	
	public void step(double delta) {
		super.step(delta);
		
		Player nearestPlayer = getNearestPlayer();
		if (nearestPlayer != null) {
			if (!intersectsAnyWall(position.add(Size.scalar(1/2)), nearestPlayer.position.add(nearestPlayer.Size.scalar(1/2)))) {
				velocity = nearestPlayer.position.sub(position).normalize();
			} else {
				path = manager.pathfinder.route(position.add(Size.scalar(1/2)), nearestPlayer.position.add(nearestPlayer.Size.scalar(1/2)), 30);
				
				if (path.length > 0) {
					velocity = path[1].position.sub(position).normalize();
					
					System.out.println("enemy velocity: " + velocity);
				}
			}
		}
	}
	
	public Player getNearestPlayer() {
		Player current = null;
		double distance = -1;
		
		for (int i = 0; i < manager.playerContainer.getComponentCount(); i++) {
			Player plr = (Player) manager.playerContainer.getComponent(i);
			double toPlayer = position.distance(plr.position);
			if (plr.type == "player" && (position.distance(plr.position) < distance || distance == -1)) {
				distance = toPlayer;
				current = plr;
			}
		}
		
		return current;
	}
	
	public double getDistanceToNearestPlayer() {
		double distance = -1;
		for (int i = 0; i < manager.playerContainer.getComponentCount(); i++) {
			Player plr = (Player) manager.playerContainer.getComponent(i);
			double toPlayer = position.distance(plr.position);
			if (plr.type == "player" && (toPlayer < distance || distance == -1)) {
				distance = toPlayer;
			}
		}
		
		return distance;
	}
	
	public void drawPath(BufferedImage canvas) {
		Graphics c = canvas.getGraphics();
		
		if (path != null && path.length > 0) {
			for (int i = 0; i + 1 < path.length; i++) {
				path[i].position.drawVector(canvas, path[i + 1].position);
			}
		}
	}
}

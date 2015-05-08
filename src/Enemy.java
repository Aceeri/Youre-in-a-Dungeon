import java.awt.Color;
import java.awt.Graphics;


public class Enemy extends Player {
	
	public Enemy(Manager manager, Vector2 position) {
		super(manager, position);
		
		this.type = "enemy";
		
		System.out.println(this.getLocation() + " " + this.getSize());
		System.out.println(this.position + " " + this.Size);
		System.out.println("Nearest Player: " + this.getNearestPlayer());
	}
	
	public void step() {
		Player nearestPlayer = getNearestPlayer();
		
		if (nearestPlayer != null) {
			double angle = this.position.angle(nearestPlayer.position);
			/*if (Math.abs(Math.cos(angle)) > Math.abs(Math.sin(angle))) {
				if (Math.cos(angle) < 0) {
					this.velocity = new Vector2(1, 0);
				} else {
					this.velocity = new Vector2(-1, 0);
				}
			} else {
				if (Math.sin(angle) < 0) {
					this.velocity = new Vector2(0, 1);
				} else {
					this.velocity = new Vector2(0, -1);
				}
			}*/
			//this.velocity = Math.abs(Math.cos(angle)) > Math.abs(Math.sin(angle)) ? new Vector2(1, 0) : new Vector2(0, 1);
			this.velocity = new Vector2(-Math.cos(angle), -Math.sin(angle)).mult(this.speed);
			//System.out.println(velocity + " " + this.collision);
			this.position = this.position.sub(this.velocity.sub(this.collision.mult(this.speed)));
		}
	}
	
	public void paintComponent(Graphics g) {
		this.paintLocation();
		
		g.setColor(Color.GRAY);
		g.fillRect((int) (this.position.x), (int) (this.position.y), 19, 19);
		
		g.setColor(Color.CYAN);
		g.fillRect((int) (this.position.x + 2), (int) (this.position.y + 2), 15, 15);
	}
	
	public Player getNearestPlayer() {
		Player current = null;
		double distance = -1;
		for (int i = 0; i < manager.playerContainer.getComponentCount(); i++) {
			Player plr = (Player) manager.playerContainer.getComponent(i);
			double toPlayer = this.position.distance(plr.position);
			if (plr.type == "player" && (this.position.distance(plr.position) < distance || distance == -1)) {
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
			double toPlayer = this.position.distance(plr.position);
			if (plr.type == "player" && (toPlayer < distance || distance == -1)) {
				distance = toPlayer;
			}
		}
		
		return distance;
	}
}

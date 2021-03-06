package main.dungeon;

import main.Manager;
import main.object.Background;
import main.object.Object;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import main.misc.Vector2;

public class Dungeon {
	public String path;
	public char[][] charset;
	public Room[][] rooms;
	public Vector2 start;
	private Manager manager;
	
	public Dungeon(Manager manager, String p) {
		System.out.println("New Dungeon: " + p);
		this.manager = manager;
		path = p;
		assignCharset();
	}
	
	// convert file to char[][] to evaluate
	public void assignCharset() {
		try {
			File file = new File(path);
			Scanner scanner = new Scanner(file);
			
			int x = 0;
			int y = 0;
			int row = 0;
			
			while(scanner.hasNextLine()) {
				String str = scanner.nextLine();
				x = str.length();
				y++;
			}
			
			scanner.close();
			
			System.out.println("dimensions: " + x + ", " + y);
			charset = new char[y][x];
			rooms = new Room[(int) Math.floor(y/2)][(int) Math.floor(x/2)];
			scanner = new Scanner(file);
			
			while (scanner.hasNextLine()) {
				String nextLine = scanner.nextLine();
				if (!nextLine.equals("")) {
					for (int i = 0; i < nextLine.length(); i++) {
						charset[row][i] = nextLine.charAt(i);
					}
					row++;
				}
			}
			
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void generate() {
		for (int row = 1; row < charset.length; row += 2) {
			for (int col = 1; col < charset[0].length; col += 2) {
				char c = charset[row][col];
				if (c != '0' || c != '-') {
					int x = (row-1)/2;
					int y = (col-1)/2;
					Room current = new Room(x, y);
					
					// evaluate character for room type
					switch (c) {
						case 's':
							current = new Room(x, y);
							current.type = "start";
							start = new Vector2(current.x, current.y);
							break;
						case 'e':
							current = new Room(x, y);
							current.type = "end";
							break;
						case 'S':
							current = new BossRoom("Spider", x, y);
							break;
						case 'W':
							current = new BossRoom("Wraith", x, y);
							break;
						case '1':
							current = new Room(x, y);
							break;
						case '2':
							current = new SpiderRoom(x, y);
							break;
						case '3':
							current = new WraithRoom(x, y);
							break;
						case '4':
							current = new CombinedRoom(x, y);
							break;
					}
					rooms[(row-1)/2][(col-1)/2] = current;
					
					// check characters surrounding room type char for doors
					char top = charset[row - 1][col];
					char bottom = charset[row + 1][col];
					char left = charset[row][col - 1];
					char right = charset[row][col + 1];
					current.evaluateDoors(top == '|', left == '|', bottom == '|', right == '|');
					current.generate();
				}
			}
		}
	}
	
	public void printCharset() {
		for (int i = 0; i < charset.length; i++) {
			for (int j = 0; j < charset[0].length; j++) {
				System.out.print(charset[i][j]);
			}
			System.out.println();
		}
	}
}
package main;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Snake extends Canvas implements Runnable, KeyListener
{
	boolean running = true;
	ArrayList<ArrayList<Box>> squares = new ArrayList<ArrayList<Box>>();
	ArrayList<Segment> snake = new ArrayList<Segment>(100);
	BufferedImage snakeImage, appleImage;
	Segment apple;
	int direction = 1;
	boolean lost = false, growSnake = false;
	int prevDirection = 0;
	int reset = 25;
	JFrame frame;
	Dimension stagesize = new Dimension(50, 50);
	int oldscore, highscore = 0;
	public Snake()
	{
		init();
		Thread t = new Thread(this);
		t.start();
		addKeyListener(this);
		try
		{
			File hs = new File("highscore.txt");
			if (hs.createNewFile())
			{
				BufferedWriter bw = new BufferedWriter(new FileWriter(hs));
				bw.write(highscore);
				bw.close();
			}
			Scanner scan = new Scanner(hs);
			oldscore = scan.nextInt();
			System.out.println(oldscore);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void init()
	{
		frame = new JFrame("Snake Game!");
		frame.setSize(550, 522);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(3);
		frame.setVisible(true);
		try //loadImages
		{
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream appleinput = classLoader.getResourceAsStream("Apple.png");
			InputStream snakeinput = classLoader.getResourceAsStream("SnakeSegment.png");
			//appleImage = ImageIO.read(appleinput);	
			snakeImage = ImageIO.read(snakeinput);
		}
		catch(IllegalArgumentException e)
		{
			System.out.println("could not find images 'Apple.png' and 'SnakeSegment.png' in the project.");
		}
		catch(Exception e)
		{
			e.printStackTrace(); //Prints exception
		}
		setSize(new Dimension(frame.getContentPane().getWidth(), frame.getContentPane().getHeight()));
		frame.add(this);
		for (int x = 0; x < stagesize.width; x++)
		{
			ArrayList<Box> temp = new ArrayList<Box>();
			for (int y = 0; y < stagesize.height; y++)
			{
				temp.add(new Box(x, y));
			}
			squares.add(temp);
		}
		Segment main = new Segment((int)(Math.random()*stagesize.width/2+stagesize.width/4), (int)(Math.random()*stagesize.height/2+stagesize.height/4));
		moveApple();
		snake.add(main);
	}
	public void moveApple()
	{
		apple = new Segment((int)(Math.random()*stagesize.width), (int)(Math.random()*stagesize.height));
	}
	public void reset()
	{
		if (oldscore < highscore)
		{
			File hs = new File("highscore.txt");
			try
			{
				BufferedWriter bw = new BufferedWriter(new FileWriter(hs));
				bw.write(highscore);
				bw.close();
				System.out.println("NEW HIGHSCORE!");
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		squares.clear();
		for (int x = 0; x < stagesize.width; x++)
		{
			ArrayList<Box> temp = new ArrayList<Box>();
			for (int y = 0; y < stagesize.height; y++)
			{
				temp.add(new Box(x, y));
			}
			squares.add(temp);
		}
		snake.clear();
		Segment main = new Segment((int)(Math.random()*stagesize.width/2+stagesize.width/4), (int)(Math.random()*stagesize.height/2+stagesize.height/4));
		moveApple();
		snake.add(main);
		direction = 1;
		reset = 25;
		lost = false;
	}
	public void run()
	{
	    this.requestFocus();
	    long lastTime = System.nanoTime();
	    double amountOfTicks = 20.0;
	    double ns = 1000000000 / amountOfTicks;
	    double delta = 0;
	    long timer = System.currentTimeMillis();
	    int updates = 0;
	    int frames = 0;
	    while(running){
	          long now = System.nanoTime();
	          delta += (now - lastTime) / ns;
	          lastTime = now;
	          while(delta >= 1){
	              tick(); // << Make sure to add this method!
	              updates++;
	              delta--;
	          }
	          render();   // << Make sure to add this method!
	          frames++;
	          if(System.currentTimeMillis() - timer > 1000){
	              timer += 1000;
	              System.out.println("FPS: "+frames+" TICKS: "+updates);
	              frames = 0;
	              updates = 0;
	          }
	    }
	}
	public void tick()
	{
		if (growSnake)
		{
			for (int i = 0; i < 5; i++)
			{
				snake.add(snake.get(snake.size()-1).clone());
			}
			growSnake = false;
		}
		ArrayList<Segment> temp = new ArrayList<Segment>();
		temp.add(snake.get(0).clone());
		for (int i = 1; i < snake.size(); i++)
		{
			if (snake.get(i) != null)
			{
				temp.add(snake.get(i).clone());
				snake.get(i).setPosition(temp.get(i-1).getPosition());
			}
		}
		if (direction == 1)
			snake.get(0).incX(1);
		if (direction == 2)
			snake.get(0).incX(-1);
		if (direction == 3)
			snake.get(0).incY(-1);
		if (direction == 4)
			snake.get(0).incY(1);
		prevDirection = direction;
		for (int i = 1; i < snake.size();i++)
		{
			if (snake.get(i) != null)
			{
				if (snake.get(i).getPosition().equals(snake.get(0).getPosition()))
				{
					lost = true;
				}
			}
		}
		if ((snake.get(0).x >= squares.size()) || (snake.get(0).x < 0))
		{
			lost = true;
		}
		if ((snake.get(0).y >= squares.get(0).size()) || (snake.get(0).y < 0))
		{
			lost = true;
		}
		if (lost)
		{
			reset--;
			if (reset <= 0)
			{
				reset();
			}
		}
		if ((snake.get(0).x == apple.x) && (snake.get(0).y == apple.y))
		{
			highscore++;
			growSnake = true;
			moveApple();
		}
//		System.out.println(frame.getSize().width + " " + frame.getSize().height);
	}
	public void render() //MAKE SURE TO SET CANVAS SIZE! (Doesn't automatically resize on mac atleast)
	{
		this.createBufferStrategy(2);
		
		BufferStrategy g = this.getBufferStrategy();
		Graphics2D g2d = (Graphics2D) g.getDrawGraphics();
		g2d.setColor(Color.black);	
		g2d.fillRect(0,0,getWidth(),getHeight());
		g2d.setColor(Color.lightGray);
		int width = getWidth()/squares.size(); //Width of a box/pixel
		int height = getHeight()/squares.get(0).size(); //Height of a box/pixel
		for (int x = 0; x < squares.size(); x++)
		{
			for (int y = 0; y < squares.get(0).size(); y++)
			{
				g2d.fillRect(width*x, height*y, width, height);
			}
		}
		if (apple != null)
		{
			int x = apple.x;
			int y = apple.y;
			g2d.setColor(new Color(139,69,19)); //Brown
			g2d.fillRect(width*x+(width/2)-1, height*y-1, 3, height/2);
			g2d.setColor(Color.red);
			g2d.fillOval(width*x+1, height*y+1, width-2, height-2);
			g2d.setColor(new Color(230, 230, 230));
			g2d.fillOval(width*x+4, height*y+2, 4, 4);
			g2d.setColor(new Color(255, 255, 255));
			g2d.fillOval(width*x+5, height*y+2, 2, 2);
		}
		g2d.setColor(new Color(0, 0, 200));
		for (int i = 0; i < snake.size(); i++)
		{
			int x = snake.get(i).x;
			int y = snake.get(i).y;
			g2d.drawImage(snakeImage, width*x, height*y, width, height, null);
		}
		if (lost)
		{
			float alpha = 1-reset/25f;
			int type = AlphaComposite.SRC_OVER; 
			AlphaComposite composite = 
			  AlphaComposite.getInstance(type, alpha);
			g2d.setComposite(composite);
			g2d.setColor(Color.white);
			g2d.fillRect(0, 0, getWidth(), getHeight());
			g2d.setColor(new Color(0, 0, 200));
			g2d.setFont(new Font("Arial", 0, 50));
			String stringTime = "YOU LOST!!!";
		    FontMetrics fm = g2d.getFontMetrics();
		    Rectangle2D r = fm.getStringBounds(stringTime, g2d);
		    int x = (this.getWidth() - (int) r.getWidth()) / 2;
		    int y = (this.getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
		    g2d.drawString(stringTime, x, y);
		}
		g2d.setFont(new Font("Arial", 0, 10));
		g2d.setColor(Color.black);
	    FontMetrics fm = g2d.getFontMetrics();
	    String stringTime = Integer.toString(highscore);
	    Rectangle2D r = fm.getStringBounds(stringTime, g2d);
	    int x = (this.getWidth() - (int) r.getWidth()) / 2;
	    int y = (10 + fm.getAscent());
	    g2d.drawString(stringTime, x, y);
	    fm = g2d.getFontMetrics();
	    String stringDirections = "Use wasd to move";
	    r = fm.getStringBounds(stringTime, g2d);
	    x = (this.getWidth() - (int) r.getWidth()) / 2-40;
	    y = (fm.getAscent());
	    g2d.drawString(stringDirections, x, y);
		g2d.dispose();
		g.show();
	}
	
	public static void main(String[] args)
	{
		new Snake();
	}

	public void keyTyped(KeyEvent e) {
		
	}

	public void keyPressed(KeyEvent e) {
		
		switch(e.getKeyChar())
		{
		case 'd':
			if (direction != 2 && prevDirection != 2)
				direction = 1;		
			break;
		case 'a':
			if (direction != 1 && prevDirection != 1)
				direction = 2;
			break;
		case 'w':
			if (direction != 4 && prevDirection != 4)
				direction = 3;
			break;
		case 's':
			if (direction != 3 && prevDirection != 3)
				direction = 4;
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
		
	}

}
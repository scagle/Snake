package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public class Segment {
	int x, y;
	public Segment(int x, int y)
	{
		setPosition(new Point(x, y));
	}
	public Segment clone()
	{
		return new Segment(this.x, this.y);
	}
	public void setPosition(Point p)
	{
		this.x = p.x;
		this.y = p.y;
	}
	public Point getPosition()
	{
		return new Point(x, y);
	}
	public void incX(int n)
	{
		x+=n;
	}
	public void incY(int n)
	{
		y+=n;
	}
}

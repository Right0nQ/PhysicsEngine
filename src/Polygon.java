import java.awt.Graphics;

public class Polygon extends Object {
	PVector[] points;

	public Polygon(double[] xPoints, double[] yPoints, boolean fixed, Material mat) {
		super(center(xPoints, yPoints), fixed, mat);
		
		points = new PVector[xPoints.length];
		
		for (int i = 0; i < points.length; i++)
			points[i] = new PVector(xPoints[i], yPoints[i]);
		
		translate(position.scale(-1));
		
		if (mass != 0)
			calcMass();
	}
	
	public Polygon(PVector center, double[] xPoints, double[] yPoints, boolean fixed, Material mat) {
		super(center, fixed, mat);
		
		points = new PVector[xPoints.length];
		
		for (int i = 0; i < points.length; i++)
			points[i] = new PVector(xPoints[i], yPoints[i]);
		
	}
	
	public static PVector center(double[] xPoints, double[] yPoints) {
		double x = 0;
		double y = 0;
		for (int i = 0; i < xPoints.length; i++) {
			x += xPoints[i];
			y += yPoints[i];
		}
		
		x /= xPoints.length;
		y /= xPoints.length;
		
		return new PVector(x, y);
	}
	
	public PVector faceNormal(int pointIndex) {
		if (pointIndex < 0 || pointIndex >= points.length)
			return null;
		
		PVector p1 = points[pointIndex];
		PVector p2;
		if (pointIndex != points.length - 1)
			p2 = points[pointIndex + 1];
		else
			p2 = points[0];
		
		return p2.sub(p1).cross(1, true).unit();
	}
	
	public PVector getSupport(PVector v) {
		double best = -Double.MAX_VALUE;
		double current;
		int bestIndex = 0;
		
		for (int i = 0; i < points.length; i++) {
			current = points[i].dot(v);
			
			if (current > best) {
				best = current;
				bestIndex = i;
			}
		}
		
		return points[bestIndex];
	}

	@Override
	public void calcMass() {
		PVector centroid = new PVector(0, 0);
		
		PVector p1, p2;
		
		double triArea, x2, y2;
		double area = 0;
		double inert = 0;
		
		for (int i = 0; i < points.length; i++) {
			p1 = points[i];
			p2 = points[(i + 1) % points.length];
			
			triArea = Math.abs(p1.cross(p2) * 0.5);
			area += triArea;
			
			centroid.addThis(p1.scale(triArea / 3));
			centroid.addThis(p2.scale(triArea / 3));
			
			x2 = p1.getX() * p1.getX() + p1.getX() * p2.getX() + p2.getX() * p2.getX();
			y2 = p1.getY() * p1.getY() + p1.getY() * p2.getY() + p2.getY() * p2.getY();
			
			inert += (triArea / 6) * (x2 + y2);
		}
		
		centroid.scaleThis(1 / area);
		translate(centroid.scale(-1));
		
		mass = mat.density * area;
		invMass = 1 / mass;
		inertia = inert * mat.density;
		invInertia = 1 / inertia;
	}

	@Override
	public void draw(Graphics g, double alpha) {
		rotate(angle);
		translate(position.round());
		
		int[] xs = new int[points.length];
		int[] ys = new int[points.length];
		
		for (int i = 0; i < points.length; i++) {
			xs[i] = (int) points[i].getX();
			ys[i] = (int) points[i].getY();
		}
		
		g.drawPolygon(xs, ys, points.length);
		
		translate(position.round().scale(-1));
		rotate(-angle);
	}

	@Override
	public void changePos(PVector amt) {
		position.addThis(amt);
	}
	
	@Override
	public void rotate(double theta) {
		for (int i = 0; i < points.length; i++) {
			points[i].rotateThis(theta);
		}
	}
	
	@Override
	public void translate(PVector amt) {
		for (int i = 0; i < points.length; i++) {
			points[i].addThis(amt);
		}
	}

	@Override
	public Rect boundRect() {
		double greatestX = -Double.MAX_VALUE;
		double greatestY = -Double.MAX_VALUE;
		double leastX = Double.MAX_VALUE;
		double leastY = Double.MAX_VALUE;
		
		rotate(angle);
		
		for (int i = 0; i < points.length; i++) {
			if (points[i].getX() > greatestX)
				greatestX = points[i].getX();
			if (points[i].getY() > greatestY)
				greatestY = points[i].getY();
			if (points[i].getX() < leastX)
				leastX = points[i].getX();
			if (points[i].getY() < leastY)
				leastY = points[i].getY();
		}
		
		rotate(-angle);
		
		return new Rect(new PVector(leastX + position.getX(), leastY + position.getY()), new PVector(greatestX + position.getX(), greatestY + position.getY()));
	}

}

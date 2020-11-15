
public class PVector {
	private double x;
	private double y;
	
	public PVector(double setX, double setY) {
		x = setX;
		y = setY;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public void setX(double setX) {
		x = setX;
	}
	
	public void setY(double setY) {
		y = setY;
	}
	
	public double length() {
		return Math.sqrt(x*x + y*y);
	}
	
	public double lengthSquared() {
		return x*x + y*y;
	}
	
	public PVector unit() {
		double length = length();
		if (length == 0)
			return new PVector(0, 0);
		return new PVector(x / length, y/ length);
	}
	
	public void unitThis() {
		double length = length();
		if (length == 0) {
			x = 0;
			y = 0;
			return;
		}
		x /= length;
		y /= length;
	}
	
	public PVector round() {
		return new PVector(Math.round(x), Math.round(y));
	}
	
	public void roundThis() {
		x = Math.round(x);
		y = Math.round(y);
	}
	
	public PVector add(PVector v) {
		return new PVector(x + v.getX(), y + v.getY());
	}
	
	public void addThis(PVector v) {
		x += v.getX();
		y += v.getY();
	}
	
	public PVector add(double vx, double vy) {
		return new PVector(x + vx, y + vy);
	}
	
	public PVector sub(PVector v) {
		return new PVector(x - v.getX(), y - v.getY());
	}
	
	public void subThis(PVector v) {
		x -= v.getX();
		y -= v.getY();
	}
	
	public PVector sub(double vx, double vy) {
		return new PVector(x - vx, y - vy);
	}
	
	public void scaleThis(double scalar) {
		x *= scalar;
		y *= scalar;
	}
	
	public PVector scale(double scalar) {
		return new PVector(x * scalar, y * scalar);
	}
	
	public PVector scale(double xScalar, double yScalar) {
		return new PVector(x * xScalar, y * yScalar);
	}
	
	public double dot(PVector v) {
		return x * v.x + y * v.y;
	}
	
	public double cross(PVector v) {
		return x * v.getY() - y * v.getX();
	}
	
	public PVector cross(double scalar, boolean vecFirst) {
		if (vecFirst)
			return new PVector(scalar * y, -scalar * x);
		else
			return new PVector(-scalar * y, scalar * x);
	}
	
	public PVector rotate(double theta) {
		double s = Math.sin(theta);
		double c = Math.cos(theta);
		return new PVector(x * c - y * s, x * s + y * c);
	}
	
	public void rotateThis(double theta) {
		double s = Math.sin(theta);
		double c = Math.cos(theta);
		double holdX = x;
		x = x * c - y * s;
		y = holdX * s + y * c;
	}
	
	public double angle() {
		return Math.atan2(y, x);
	}
	
	public PVector copy() {
		return new PVector(x, y);
	}
	
	public void clear() {
		x = 0;
		y = 0;
	}
	
	public String toString() {
		return "x: " + x + " y: " + y;
	}
}

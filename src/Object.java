import java.awt.Graphics;

public abstract class Object {
	PVector position;
	PVector velocity;
	PVector force;
	
	double angle;
	double angleVel;
	double torque;
	
	double mass = 1;
	double invMass = 1;
	double inertia = 1;
	double invInertia = 1;
	
	final static double areaScale = 0.01;
	
	Material mat;
	
	public Object(PVector position, boolean fixed, Material mat) {
		this.position = position;
		velocity = new PVector(0, 0);
		force = new PVector(0, 0);
		
		this.mat = mat;
		
		if (fixed) {
			mass = 0;
			invMass = 0;
			inertia = 0;
			invInertia = 0;
		}
	}
	
	public void updateVel(double dt) {
		force = force.scale(invMass).scale(dt);
		velocity = velocity.add(force);
		force.clear();	
	}
	
	public void updateAngleVel(double dt) {
		torque = torque * invMass * dt;
		angleVel += torque;
		torque = 0;
	}
	
	public void updatePos(double dt, PVector grav) {
		force = force.add(grav.scale(mass));
		updateVel(dt);
		updateAngleVel(dt);
		changePos(velocity.scale(dt));
		angle += angleVel * dt;
	}
	
	public void applyImpulse(PVector impulse, PVector contact) {
		velocity.addThis(impulse.scale(invMass));
		angleVel += invInertia * contact.cross(impulse);
	}
	
	/*public void handleMass() {
		if (mass != 0) {
			mass = calcMass();
			invMass = 1 / mass;
		}
	}*/
	
	public abstract void calcMass();
	
	public abstract void draw(Graphics g, double alpha);
	
	public abstract void changePos(PVector amt);
	
	public abstract void rotate(double theta);
	
	public abstract void translate(PVector amt);
	
	public abstract Rect boundRect();
}

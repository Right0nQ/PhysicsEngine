
public class CollisionManifold {
	Object a;
	Object b;
	double penetration;
	PVector normal;
	PVector[] contactPoints;
	int contactNum;
	
	public CollisionManifold(Object a, Object b) {
		this.a = a;
		this.b = b;
		contactPoints = new PVector[2];
	}
	
	public String toString() {
		return "pen: " + penetration + "\nnorm: " + normal + "\nnum: " + contactNum;
	}
}

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Physics {

	ArrayList<Object> objects;
	ArrayList<CollisionManifold> pairs;
	
	final static PVector grav = new PVector(0, 3);
	
	final static Material stone = new Material(0.012, 0.1, 0.1, 0.08);
	final static Material bounce = new Material(0.002, 0.9, 0.1, 0.08);
	
	public Physics() {
		objects = new ArrayList<Object>();
		pairs = new ArrayList<CollisionManifold>();
	
		objects.add(new Polygon(new double[] {50, 350, 350, 50}, new double[] {320, 320, 370, 370}, true, stone));
	}
	
	public void updatePhysics(double dt) {
		
		broadPhase();
		narrowPhase();
		
		for (Object o: objects) {
			o.updatePos(dt, grav);
		}
	}
	
	public void draw(Graphics g, double alpha) {
		g.setColor(Color.BLACK);
		
		for (Object o: objects)
			o.draw(g, alpha);
	}
	
	public void broadPhase() {
		pairs.clear();
		
		Object A, B;
		Rect a, b;
		
		for (int i = 0; i < objects.size() - 1; i++) {
			for (int j = i+1; j < objects.size(); j++) {
				A = objects.get(i);
				B = objects.get(j);
				a = A.boundRect();
				b = B.boundRect();
				
				if (rectsIntersect(a, b))
					pairs.add(new CollisionManifold(A, B));
			}
		}
	}
	
	public void narrowPhase() {
		Object a, b;
		for (CollisionManifold pair: pairs) {
			a = pair.a;
			b = pair.b;
			
			boolean collide = false;
			
			if (a.getClass().toString().equals("class Polygon") && b.getClass().toString().equals("class Polygon"))
				collide = polyvPoly(pair);
			
			if (collide) {
				resolveCollision(pair);
			}
		}
	}
	
	public void resolveCollision(CollisionManifold m) {

		correctPosition(m);
		
		for (int i = 0; i < m.contactNum; i++) {
			PVector ra = m.contactPoints[i].sub(m.a.position);
			PVector rb = m.contactPoints[i].sub(m.b.position);
			
			
			PVector relativeVel = m.b.velocity.add(rb.cross(m.b.angleVel, false));
			relativeVel.subThis(m.a.velocity);
			relativeVel.subThis(ra.cross(m.a.angleVel, false));
			
			double velAlongNormal = relativeVel.dot(m.normal);
			
			if (velAlongNormal > 0)
				return;
			
			double raCrossN = ra.cross(m.normal);
			double rbCrossN = rb.cross(m.normal);
			
			double e = Math.max(m.a.mat.restitution, m.b.mat.restitution);
			
			double totalInvMass = m.a.invMass + m.b.invMass + raCrossN * raCrossN * m.a.invInertia + rbCrossN * rbCrossN * m.b.invInertia;
			
			double j = -(1 + e) * velAlongNormal;
			j /= totalInvMass;
			j /= m.contactNum;
			
			PVector impulse = m.normal.scale(j);
			
			if (j < 0.1)
				return;
			
			m.a.applyImpulse(impulse.scale(-1), ra);
			m.b.applyImpulse(impulse, rb);
			
			relativeVel = m.b.velocity.add(rb.cross(m.b.angleVel, false));
			relativeVel.subThis(m.a.velocity);
			relativeVel.subThis(ra.cross(m.a.angleVel, false));
			
			PVector tangent = relativeVel.sub(m.normal.scale(velAlongNormal));
			tangent.unitThis();
			
			double jt = -relativeVel.dot(tangent);
			jt /= totalInvMass;
			jt /= m.contactNum;
			
			double fric = Math.sqrt(m.a.mat.staticFric * m.a.mat.staticFric + m.b.mat.staticFric * m.b.mat.staticFric);
			
			if (Math.abs(jt) < j * fric)
				impulse = tangent.scale(-jt);
			else {
				fric = Math.sqrt(m.a.mat.kineticFric * m.a.mat.kineticFric + m.b.mat.kineticFric * m.b.mat.kineticFric);
				impulse = tangent.scale(-j * fric);
			}
			
			m.a.applyImpulse(impulse.scale(-1), ra);
			m.b.applyImpulse(impulse, rb);
		}
	}
	
	final static double correctPercent = 0.2;
	final static double correctAllowance = 0.01;
	public void correctPosition(CollisionManifold m) {
		PVector correction = m.normal.scale(correctPercent * Math.max(m.penetration - correctAllowance, 0) * 2);
		
		if (m.a.invMass != 0)
			m.a.changePos(correction.scale(-1));
		
		
		if (m.b.invMass != 0)
			m.b.changePos(correction);
		
	}
	
	public boolean rectsIntersect(Rect a, Rect b) {
		if (a.min.getX() > b.max.getX() || a.max.getX() < b.min.getX())
			return false;
		if (a.min.getY() > b.max.getY() || a.max.getY() < b.min.getY())
			return false;
		return true;
	}
	
	private int faceIndex;
	public boolean polyvPoly(CollisionManifold m) {
		Polygon a = (Polygon) m.a;
		Polygon b = (Polygon) m.b;
		
		m.contactNum = 0;
		
		double aPen = findAxisSeparation(a, b);
		int aFace = faceIndex;
		
		if (aPen > 0)
			return false;
		
		double bPen = findAxisSeparation(b, a);
		int bFace = faceIndex;
		
		if (bPen > 0)
			return false;
		
		boolean flipped;
		Polygon ref;
		Polygon inc;
		
		if (aPen > bPen * 0.95 + aPen * 0.01) {
			ref = a;
			inc = b;
			faceIndex = aFace;
			flipped = false;
		} else {
			ref = b;
			inc = a;
			faceIndex = bFace;
			flipped = true;
		}
		
		PVector[] incFace = new PVector[2];
		PVector[] refFace = new PVector[2];
		
		ref.rotate(ref.angle);
		inc.rotate(inc.angle);
		
		findIncidentFace(incFace, ref, inc, faceIndex);
		
		
		refFace[0] = ref.points[faceIndex].add(ref.position);
		faceIndex = faceIndex + 1 >= ref.points.length? 0: faceIndex + 1;
		refFace[1] = ref.points[faceIndex].add(ref.position);
		
		PVector sidePlaneNormal = refFace[1].sub(refFace[0]).unit();
		PVector refFaceNormal = new PVector(sidePlaneNormal.getY(), -sidePlaneNormal.getX());
		
		double refC = refFaceNormal.dot(refFace[0]);
		double negSide = -sidePlaneNormal.dot(refFace[0]);
		double posSide = sidePlaneNormal.dot(refFace[1]);
		
		if (clip(sidePlaneNormal.scale(-1), negSide, incFace) < 2) {
			ref.rotate(-ref.angle);
			inc.rotate(-inc.angle);
			return false;
		}
		
		if (clip(sidePlaneNormal, posSide, incFace) < 2) {
			ref.rotate(-ref.angle);
			inc.rotate(-inc.angle);
			return false;
		}
		
		m.normal = refFaceNormal.copy();
		
		if (flipped) {
			m.normal.scaleThis(-1);
		}
		
		int contactNum = 0;
		double separation = refFaceNormal.dot(incFace[0]) - refC;
		
		if (separation <= 0) {
			m.contactPoints[contactNum++] = incFace[0].copy();
			m.penetration = -separation;
		} else {
			m.penetration = 0;
		}
		
		separation = refFaceNormal.dot(incFace[1]) - refC;
		
		if (separation <= 0) {
			m.contactPoints[contactNum++] = incFace[1].copy();
			
			if (-separation > m.penetration)
				m.penetration = -separation;
			
			m.penetration /= contactNum;
		}
		
		m.contactNum = contactNum;
		
		ref.rotate(-ref.angle);
		inc.rotate(-inc.angle);
		
		return true;
	}
	
	public void findIncidentFace(PVector[] face, Polygon ref, Polygon inc, int index) {
		PVector faceNormal = ref.faceNormal(index);
		
		int incFace = 0;
		double minDot = Double.MAX_VALUE;
		
		for (int i = 0; i < inc.points.length; i++) {
			double dot = faceNormal.dot(inc.faceNormal(i));
			
			if (dot < minDot) {
				minDot = dot;
				incFace = i;
				
			}
		}
		
		face[0] = inc.points[incFace].add(inc.position);
		incFace = incFace + 1 >= inc.points.length? 0: incFace + 1;
		face[1] = inc.points[incFace].add(inc.position);
		
	}
	
	public double findAxisSeparation(Polygon a, Polygon b) {
		
		double penetration = -Double.MAX_VALUE;
		int bestIndex = 0;
		

		a.rotate(a.angle);
		a.translate(a.position);
		b.rotate(b.angle);
		b.translate(b.position);
		
		
		for (int i = 0; i < a.points.length; i++) {
			PVector normal = a.faceNormal(i);
			
			PVector support = b.getSupport(normal.scale(-1));
			
			PVector faceVertex = a.points[i];
			
			double dist = normal.dot(support.sub(faceVertex));
			
			if (dist > penetration) {
				penetration = dist;
				bestIndex = i;
			}
		}
		faceIndex = bestIndex;
		
		a.translate(a.position.scale(-1));
		a.rotate(-a.angle);
		b.translate(b.position.scale(-1));
		b.rotate(-b.angle);
		
		return penetration;
	}
	
	public int clip(PVector normal, double c, PVector[] face) {
		int sp = 0;
		
		PVector[] faceCopy = {face[0].copy(), face[1].copy()};
		
		double dist1 = normal.dot(face[0]) - c;
		double dist2 = normal.dot(face[1]) - c;
		
		if (dist1 <= 0)
			faceCopy[sp++] = face[0].copy();
		if (dist2 <= 0)
			faceCopy[sp++] = face[1].copy();
		
		if (dist1 * dist2 < 0) {
			double alpha = dist1 / (dist1 - dist2);
			
			faceCopy[sp++] = face[1].sub(face[0]).scale(alpha).add(face[0]);
		}
		
		face[0] = faceCopy[0];
		face[1] = faceCopy[1];
		
		return sp;
	}
	
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int w = 20;
		int h = 40;
		
		if (e.getButton() == 1)
			objects.add(new Polygon(new double[] {e.getX(), e.getX() + 50, e.getX() + 30, e.getX() - 20}, new double[] {e.getY(), e.getY() - 20, e.getY() + 40, e.getY() + 50}, false, bounce));
		else
			//objects.add(new Polygon(new double[] {e.getX(), e.getX() + 50, e.getX() + 25}, new double[] {e.getY(), e.getY(), e.getY() + 50}, false, stone));
			objects.add(new Polygon(new double[] {x - w / 2, x - w / 2, x, x + w / 2, x + w / 2}, new double[] {y + h / 2, y, y - h / 2, y, y + h / 2}, false, stone));
			
			//objects.add(new Polygon(new double[] {x - w / 2, x - w, x - w, x - w / 2, x + w / 2, x + w, x + w, x + w / 2}, new double[] {y - w, y - w / 2, y + }))
	}
}

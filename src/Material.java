
public class Material {
	double density;
	double restitution;
	double staticFric;
	double kineticFric;
	
	public Material(double density, double restitution, double staticFric, double kineticFric) {
		this.density = density;
		this.restitution = restitution;
		this.staticFric = staticFric;
		this.kineticFric = kineticFric;
	}
}

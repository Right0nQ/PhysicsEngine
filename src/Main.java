
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main {
	final static double fps = 50;
	final static double dt = 1 / fps;
	double alpha;
	
	JFrame jf;
	DrawPanel dp;
	
	public int w, h;
	
	Physics phys;

	/**
	 * Made with help from:
	 * https://gamedevelopment.tutsplus.com/tutorials/how-to-create-a-custom-2d-physics-engine-the-basics-and-impulse-resolution--gamedev-6331
	 */
	
	public static void main(String[] args) {
		new Main().run();
	}
	
	private void run() {
		jf = new JFrame("Physics Engine");
		jf.setDefaultCloseOperation(jf.EXIT_ON_CLOSE);
		
		dp = new DrawPanel();
		jf.getContentPane().add(BorderLayout.CENTER, dp);
		
		jf.setSize(400, 420);
		jf.setLocation(375, 50);
		jf.setVisible(true);
		jf.setResizable(false);
		jf.addMouseListener(new MouseListen());
		
		w = jf.getWidth();
		h = jf.getHeight() - 20;
		
		double accumulator = 0;
		double start = System.currentTimeMillis();
		
		phys = new Physics();
		
		while (true) {
			final double current = System.currentTimeMillis();
			
			accumulator += current - start;
			start = current;
			
			if (accumulator > 0.2f) {
				accumulator = 0.2f;
			}
			
			while (accumulator > dt) {
				phys.updatePhysics(dt);
				accumulator -= dt;
			}
			
			alpha = accumulator / dt;
			jf.repaint();
			
			try {
				Thread.sleep((int) (dt * 1000));
			} catch (Exception exc) {}
		}
	}
	
	
	
	public class DrawPanel extends JPanel {
		public void paintComponent(Graphics g) {
			try {
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, w, h);
				phys.draw(g, alpha);
			} catch (Exception exc) {}
		}
	}
	
	public class MouseListen extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			phys.mouseClicked(e);
		}
	}
	
}

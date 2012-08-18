import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class Splash extends JDialog {
	
	private static final long serialVersionUID = 1L;
	private JLabel splashLabel;
	private ImageIcon imgSplash;
	private Font fontVersion;
	private String strVersion;
	private boolean timeout;
	
	public Splash(boolean timeout) {
		this.timeout = timeout;
		createDialog();
	}

	public Splash(JFrame f) {
		super(f, true);
		createDialog();
	}

	private void createDialog() {
		strVersion = "";
		fontVersion = new Font("Arial", Font.BOLD, 12);

		imgSplash = new ImageIcon("img/splash.jpg");

        splashLabel = new JLabel(imgSplash) {

        	private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
				super.paint(g);
				g.setFont(fontVersion);
				g.drawString(strVersion, 350, 30);
			}
        };
        splashLabel.setBorder(BorderFactory.createLineBorder(Color.black));

		setUndecorated(true);
        getContentPane().add(splashLabel);
        pack();

        Rectangle screenRect = this.getGraphicsConfiguration().getBounds();
        setLocation(screenRect.x + screenRect.width / 2 - getSize().width / 2,
            		screenRect.y + screenRect.height / 2 - getSize().height / 2);

        setVisible(true);
        try {
        	if (this.timeout) {
        		Thread.sleep(1000);
        		dispose();
        	} else {
        		this.addMouseListener(new MouseAdapter() {
        			public void mouseClicked(MouseEvent e) {
        				dispose();
        			}
        		});
        	}
        } catch (Exception e) {}

	}
	
}

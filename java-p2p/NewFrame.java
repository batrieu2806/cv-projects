import javax.swing.JFrame;

public class NewFrame extends JFrame{
    NewFrame (int width, int height, boolean resizable, String text) {
        this.setTitle(text);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(resizable);
        this.setSize(width, height);
        this.setVisible(true);
        this.setLayout(null);
    }
}

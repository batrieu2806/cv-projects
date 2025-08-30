import javax.swing.JButton;

public class NewButton extends JButton{
    NewButton (int x, int y, int width, int height, String text, boolean setenable) {
        //this.setBounds(x, y, width, height);
        this.setText(text);
        this.setFocusable(true);
        this.setEnabled(setenable);
    }
}

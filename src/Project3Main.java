//  Brian Vegh
//  UMCG CMSC-335 Project 3
//  December 15, 2020
//  Project3Main.java - The main driver of this application.
import javax.swing.*;
import java.awt.*;

/**
 * The main driver of this application. Draws initial frame and controls Debug.
 *
 */
public class Project3Main extends JFrame {
    public static final int DISTANCE_BETWEEN_LIGHTS = 1000;
    private static MainPanel mainPanel;

    /**
     * main constructor
     */
    public Project3Main(){
        //set frame values
        super("Stoplight Unit Test");
        this.setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        //create mainPanel
        mainPanel = new MainPanel();
        //add mainPanel to 'this' JFrame
        this.add(mainPanel, BorderLayout.CENTER);
        mainPanel.setVisible(true);
        mainPanel.updateUI();
        pack();
    }

    /**
     * main
     * @param args
     */
    public static void main(String[] args) {
        Project3Main project3Main = new Project3Main();
        project3Main.setVisible(true);
        Debug.TURN_OFF();
    }
}

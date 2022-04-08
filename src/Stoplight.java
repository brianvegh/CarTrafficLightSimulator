//  Brian Vegh
//  UMCG CMSC-335 Project 3
//  December 15, 2020
//  Stoplight.java - Class that creates each intersection JPanel on the GUI and runs it's multi-threaded code

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Class that creates each intersection JPanel on the GUI and runs it's multi-threaded code
 * @author Brian Vegh
 */
public class Stoplight extends JPanel implements Runnable {
    private final int DISTANCE_BETWEEN_LIGHTS = Project3Main.DISTANCE_BETWEEN_LIGHTS;

    final static JPanel[] images = new JPanel[4];
    private static int staticLightNumberCounter;
    final int lightNumber;
    private TrafficLightColor color;//holds current color
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    public final AtomicBoolean suspended = new AtomicBoolean(false);
    Thread thread;
    String threadName;
    public final int xPosition;
    private static int xTotalDistance;


    public Stoplight() {
        getPictures();
        initComponents();
        incrementStaticLightCounter();
        this.lightNumber = staticLightNumberCounter;
        xPosition = lightNumber * DISTANCE_BETWEEN_LIGHTS;
        xTotalDistance = xPosition;
        this.threadName = "Light " + lightNumber;
        thread = new Thread(this, threadName);
        lightNumberTF.setText(String.valueOf(lightNumber));
        color = TrafficLightColor.MAINTENANCE;
        updatePanel();
    }

    private synchronized void incrementStaticLightCounter() {
        staticLightNumberCounter += 1;
    }
    /**
     * Updates the picture to the elected image
     */
    protected void getPictures() {
        String[] filenames = {"red.jpg", "yellow.jpg", "green.jpg", "all.jpg"};
        for (int i = 0; i < 4; i++) {
            String path = "/resources/" + filenames[i];
            JPanel jPanel = new JPanel();
            JLabel picture = new JLabel();
            ImageIcon icon;
            icon = new ImageIcon(getClass().getResource(path));
            picture.setIcon(icon);
            jPanel.add(picture);
            images[i] = jPanel;
        }
    }

    /**
     * run method that controls thread initialization, and invokes SwingUtilities invoke later
     */
    @Override
    public void run() {
        Debug.print("Running " + threadName);
        isRunning.set(true);
        while (isRunning.get()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updatePanel();
                }
            });
            try {
                synchronized (this) {
                    while (suspended.get()) {
                        Debug.print(threadName + " waiting");
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                lightStatusTF.setText("MAINTENANCE");
                            }
                        });
                        wait();
                    }
                }
                switch (getTrafficLightColor()) {
                    case GREEN:
                        setTrafficLightColor(TrafficLightColor.YELLOW);
                        Debug.print(threadName + " changed to " + color.name());
                        Thread.sleep(1750);
                        break;
                    case YELLOW:
                        setTrafficLightColor(TrafficLightColor.RED);
                        Debug.print(threadName + " changed to " + color.name());
                        Thread.sleep(5000);
                        break;
                    case RED:
                        setTrafficLightColor(TrafficLightColor.GREEN);
                        Debug.print(threadName + " changed to " + color.name());
                        Thread.sleep(6000);
                        break;
                    case MAINTENANCE:
                        setTrafficLightColor(TrafficLightColor.RED);
                        Debug.print(threadName + " changed to " + color.name());
                        Thread.sleep(lightNumber * 500L);
                    default:
                        break;
                }

            } catch (InterruptedException exc) {
                suspended.set(true);
            }
        }

    }

    /**
     * allows Cars access to  the number of stoplights
     * @return
     */
    public static int getStaticLightNumberCounter() {
        return staticLightNumberCounter;
    }
    /**
     * updates the UI component for Stoplight. Only called by SwingWorker
     * to prevent delays in GUI update
     */
    private synchronized void updatePanel() {
        lightImagePanel.removeAll();
        lightImagePanel.add(images[getTrafficLightColor().ordinal()]);
        lightImagePanel.updateUI();
        lightStatusTF.setText(getTrafficLightColor().toString());
        this.updateUI();
    }

    /**
     * resume Stoplight thread
     */
    public synchronized void resume() {
        suspended.set(false);
        notify();
        Debug.print("Resuming " + threadName);
    }
    /**
     * stop Stoplight thread
     */
    public synchronized void stop() {
        thread.interrupt();
        isRunning.set(false);
        Debug.print("Stopping " + threadName);
    }
    /**
     * interrupt Stoplight thread
     */
    public void interrupt() {
        //If light is sleeping, we can call interrupt to wake it when hitting "Pause" button
        thread.interrupt();
    }
    /**
     * suspend Stoplight thread
     */
    public void suspend() {
        interrupt();
        suspended.set(true);
        Debug.print("Suspending " + threadName);
        lightStatusTF.setText("MAINTENANCE");
    }

    /**
     * synchronized set traffic light color
     * @param c
     */
    synchronized void setTrafficLightColor(TrafficLightColor c) {
        this.color = c;
    }
    /**
     * synchronized get traffic light color
     */
    public TrafficLightColor getTrafficLightColor() {
        return this.color;
    }
    /**
     * synchronized get traffic light color
     */
    public static synchronized int getxTotalDistance() {
        return xTotalDistance;
    }

    /**
     * initialize Swing components
     */
    private void initComponents() {
        infoPanel = new JPanel();
        l1 = new JLabel();
        lightNumberTF = new JTextField();
        l2 = new JLabel();
        lightStatusTF = new JTextField();
        lightImagePanel = new JPanel();

        //======== this ========
        setBorder(LineBorder.createBlackLineBorder());
        setMinimumSize(new Dimension(200, 460));
        setMaximumSize(new Dimension(200, 460));
        setPreferredSize(new Dimension(200, 460));
        setLayout(new MigLayout(
                "hidemode 3,alignx center",
                // columns
                "[0,fill]0" +
                        "[206,fill]",
                // rows
                "[42]" +
                        "[]0"));

        //======== infoPanel ========
        {
            infoPanel.setBorder(LineBorder.createBlackLineBorder());
            infoPanel.setLayout(new GridLayout(2, 0));

            //---- l1 ----
            l1.setText("Light Number:");
            l1.setPreferredSize(new Dimension(30, 13));
            l1.setHorizontalAlignment(SwingConstants.CENTER);
            l1.setMaximumSize(null);
            l1.setMinimumSize(null);
            infoPanel.add(l1);

            //---- lightNumberTF ----
            lightNumberTF.setColumns(2);
            lightNumberTF.setHorizontalAlignment(SwingConstants.CENTER);
            infoPanel.add(lightNumberTF);

            //---- l2 ----
            l2.setText("Status:");
            l2.setPreferredSize(new Dimension(30, 13));
            l2.setHorizontalAlignment(SwingConstants.CENTER);
            infoPanel.add(l2);

            //---- lightStatusTF ----
            lightStatusTF.setColumns(8);
            lightStatusTF.setHorizontalAlignment(SwingConstants.CENTER);
            infoPanel.add(lightStatusTF);
        }
        add(infoPanel, "cell 1 0");

        //======== lightImagePanel ========
        {
            lightImagePanel.setMinimumSize(new Dimension(129, 377));
            lightImagePanel.setMaximumSize(new Dimension(129, 377));
            lightImagePanel.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
            lightImagePanel.setLayout(new BorderLayout());
        }
        add(lightImagePanel, "cell 1 1,align center center,grow 0 0");
    }

    private JPanel infoPanel;
    private JLabel l1;
    private JTextField lightNumberTF;
    private JLabel l2;
    private JTextField lightStatusTF;
    private JPanel lightImagePanel;
}


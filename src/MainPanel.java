//  Brian Vegh
//  UMCG CMSC-335 Project 3
//  December 15, 2020
//  MainPanel.java - The main panel inside of the GUI. Has containers for cars, stoplights,
//  holds their ActionEvent triggers and displays the current time
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;

import net.miginfocom.swing.*;

/**
 * The main panel inside of the GUI. Has containers for cars, stoplights,
 * holds their ActionEvent triggers and displays the current time
 * @author Brian Vegh
 */
public class MainPanel extends JPanel {
    //sets number of lights and cars generated upon startup.
    private final int INITIAL_LIGHTS_AND_CARS=4;

    private ArrayList <Stoplight> intersections;
    private String timePattern = "hh:mm:ss a";
    private ArrayList <Thread> intersectionThreads;
    private ArrayList <Car> vehicles;
    private ArrayList <Thread> vehicleThreads;
    private static final AtomicBoolean lightsRunningBoolean = new AtomicBoolean(false);
    private static final AtomicBoolean vehiclesRunningBoolean = new AtomicBoolean(false);
    int lightsLoopCounter, vehiclesLoopCounter;

    /**
     * default constructor.
     */
    public MainPanel() {
        //form initialization
        initComponents();
        //call method to add initial intersections and vehicles to mainPanel
        fillMainPanel();

        startClock();
    }

    /**
     * displays auto-updating current time in MainPanel's toolbar
     */
    private void startClock() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String string = new SimpleDateFormat("HH:mm:ss").format(new Date());
                timeLabel.setText(string);
            }
        }, 0, 1000);
    }

    /**
     * extension of default constructor
     */
    private void fillMainPanel() {
        //initialize arraylists
        intersections = new ArrayList <>();
        intersectionThreads = new ArrayList <>();
        vehicles = new ArrayList <>();
        vehicleThreads = new ArrayList <>();

        //loop to create initial sets of components and corresponding threads
        for (int i = 0; i < INITIAL_LIGHTS_AND_CARS; i++) {
            //create runnable components, add to arraylists
            intersections.add(new Stoplight());
            vehicles.add(new Car(this));
            //tie runnable components to threads in arraylists
            intersectionThreads.add(new Thread(intersections.get(i)));
            vehicleThreads.add(new Thread(vehicles.get(i)));
            //add runnable components to mainPanel
            this.stoplightContainer.add(intersections.get(i));
            this.carsContainer.add(vehicles.get(i));
        }
        //update ui
        this.stoplightContainer.updateUI();
        this.carsContainer.updateUI();
    }

    /**
     * starts each Stoplight thread in arraylist
     * @param e
     */
    public void startLights(ActionEvent e) {
        // TODO add your code here
        lightsLoopCounter = Stoplight.getStaticLightNumberCounter();
        try {
            for (int i = 0; i < lightsLoopCounter; i++) {
                intersectionThreads.get(i).start();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            lightsRunningBoolean.set(true);
        } catch (IllegalThreadStateException ignore) {}
    }

    /**
     * pauses or resumes each individual Stoplight thread in arraylist
     * @param e
     */
    public void pauseLights(ActionEvent e) {
        if (lightsRunningBoolean.get()) {
            for (Stoplight s : intersections) {
                s.interrupt();
                s.suspend();
            }
            lightsRunningBoolean.set(false);
            pauseLights.setText("Resume Lights");
        } else {
            for (Stoplight s : intersections) {
                s.resume();
                try {
                    Thread.sleep(750);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            lightsRunningBoolean.set(true);
            pauseLights.setText("Pause Lights");
        }
    }

    /**
     * creates a new stoplight, adds to GUI and starts it's thread
     * @param e
     */
    private void addIntersection(ActionEvent e) {
        intersections.add(new Stoplight());
        intersectionThreads.add(new Thread(intersections.get(intersections.size() - 1)));
        stoplightContainer.add(intersections.get(intersections.size() - 1));
        stoplightContainer.updateUI();
        intersectionThreads.get(intersections.size() - 1).start();

    }
    /**
     * creates a new car, adds to GUI and starts it's thread
     * @param e
     */
    private void addCar(ActionEvent e) {
        vehicles.add(new Car(this));
        vehicleThreads.add(new Thread(vehicles.get(vehicles.size() - 1)));
        carsContainer.add(vehicles.get(vehicles.size() - 1));
        carsContainer.updateUI();
        vehicleThreads.get(vehicles.size() - 1).start();
    }
    /**
     * starts each Car thread in arraylist
     * @param e
     */
    private void startCars(ActionEvent e) {
        vehiclesLoopCounter = vehicles.get(0).getStaticCarNumberCounter();
        if (!vehiclesRunningBoolean.get()) {
            for (int i = 0; i < vehiclesLoopCounter; i++) {
                try {
                    vehicleThreads.get(i).start();
                } catch (IllegalThreadStateException ignore){}
            }
            vehiclesRunningBoolean.set(true);
        } else {
            Debug.print("System is already running");
        }
    }
    /**
     * pauses or resumes each Car thread in arraylist
     * @param e
     */
    private void pauseCars(ActionEvent e) {
        if (vehiclesRunningBoolean.get()) {
            for (Car c : vehicles) {
                c.interrupt();
                c.suspend(CarStatus.PAUSED);
            }
            vehiclesRunningBoolean.set(false);
            pauseCars.setText("Resume Cars");
        } else {
            pauseCars.setText("Pause Cars");
            for (Car c : vehicles) {
                c.resume();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            vehiclesRunningBoolean.set(true);
        }
    }

    /**
     * exits program after stoping all running threads
     * @param e
     */
    private void exitProgram(ActionEvent e) {
        int i = JOptionPane.showConfirmDialog(this,
                "Thank you for using this program. Goodbye!", "Exit?", JOptionPane.OK_CANCEL_OPTION);
        if (i == 0) {
        boolean grace = false;
        if (vehiclesRunningBoolean.get()) {
            for (Car c : vehicles) {
                c.interrupt();
                c.stop();
            }
            vehiclesRunningBoolean.set(false);
            grace = true;
        }
        if (lightsRunningBoolean.get()) {
            for (Stoplight s : intersections) {
                s.interrupt();
                s.stop();
            }
            lightsRunningBoolean.set(false);
            grace = true;
        }
        if (grace) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
            System.exit(0);
        }
    }

    /**
     * method allowing Cars to retrieve access to Stoplights
     * @return
     */
    public synchronized ArrayList <Stoplight> getIntersections() {
        return intersections;
    }

    /**
     * initialize components
     */
    private void initComponents() {
        toolBar1 = new JToolBar();
        label1 = new JLabel();
        timeLabel = new JLabel();
        splitPane2 = new JSplitPane();
        panel7 = new JPanel();
        scrollPane1 = new JScrollPane();
        splitPane3 = new JSplitPane();
        carsContainer = new CarsContainer();
        panel3 = new JPanel();
        stoplightContainer = new StoplightContainer();
        panel8 = new JPanel();
        panel6 = new JPanel();
        startCars = new JButton();
        pauseCars = new JButton();
        addCarButton = new JButton();
        vSpacer1 = new JPanel(null);
        exitButton = new JButton();
        vSpacer4 = new JPanel(null);
        startButton = new JButton();
        pauseLights = new JButton();
        addInterButton = new JButton();

        //======== this ========
        setPreferredSize(new Dimension(1350, 1120));
        setLayout(new BorderLayout());

        //======== toolBar1 ========
        {
            //---- label1 ----
            label1.setText("Current Time: ");
            label1.setFont(label1.getFont().deriveFont(label1.getFont().getSize() + 4f));
            toolBar1.add(label1);

            //---- timeLabel ----
            timeLabel.setText("Current Time");
            timeLabel.setFont(timeLabel.getFont().deriveFont(timeLabel.getFont().getStyle() | Font.BOLD, timeLabel.getFont().getSize() + 4f));
            toolBar1.add(timeLabel);
        }
        add(toolBar1, BorderLayout.NORTH);

        //======== splitPane2 ========
        {
            splitPane2.setResizeWeight(1.0);

            //======== panel7 ========
            {
                panel7.setLayout(new GridLayout());

                //======== scrollPane1 ========
                {
                    scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

                    //======== splitPane3 ========
                    {
                        splitPane3.setOrientation(JSplitPane.VERTICAL_SPLIT);
                        splitPane3.setResizeWeight(1.0);
                        splitPane3.setTopComponent(carsContainer);

                        //======== panel3 ========
                        {
                            panel3.setLayout(new MigLayout(
                                    "insets 10,hidemode 3",
                                    // columns
                                    "[grow,fill]",
                                    // rows
                                    "[grow,fill]"));
                            panel3.add(stoplightContainer, "cell 0 0");
                        }
                        splitPane3.setBottomComponent(panel3);
                    }
                    scrollPane1.setViewportView(splitPane3);
                }
                panel7.add(scrollPane1);
            }
            splitPane2.setLeftComponent(panel7);

            //======== panel8 ========
            {
                panel8.setMinimumSize(new Dimension(120, 181));
                panel8.setPreferredSize(new Dimension(80, 181));
                panel8.setMaximumSize(new Dimension(120, 2147483647));
                panel8.setLayout(new MigLayout(
                        "insets 05 5 5 5,hidemode 3",
                        // columns
                        "[150,grow,fill]",
                        // rows
                        "[grow,fill]"));

                //======== panel6 ========
                {
                    panel6.setMinimumSize(new Dimension(100, 196));
                    panel6.setLayout(new GridLayout(9, 0, 4, 4));

                    //---- startCars ----
                    startCars.setText("Start Cars");
                    startCars.addActionListener(e -> startCars(e));
                    panel6.add(startCars);

                    //---- pauseCars ----
                    pauseCars.setText("Pause Cars");
                    pauseCars.addActionListener(e -> pauseCars(e));
                    panel6.add(pauseCars);

                    //---- addCarButton ----
                    addCarButton.setText("Add Car");
                    addCarButton.addActionListener(e -> addCar(e));
                    panel6.add(addCarButton);
                    panel6.add(vSpacer1);

                    //---- exitButton ----
                    exitButton.setText("EXIT PROGRAM");
                    exitButton.addActionListener(e -> exitProgram(e));
                    panel6.add(exitButton);
                    panel6.add(vSpacer4);

                    //---- startButton ----
                    startButton.setText("Start Lights");
                    startButton.addActionListener(e -> startLights(e));
                    panel6.add(startButton);

                    //---- pauseButton ----
                    pauseLights.setText("Pause Lights");
                    pauseLights.addActionListener(e -> pauseLights(e));
                    panel6.add(pauseLights);

                    //---- addInterButton ----
                    addInterButton.setText("Add Intersection");
                    addInterButton.addActionListener(e -> addIntersection(e));
                    panel6.add(addInterButton);
                }
                panel8.add(panel6, "cell 0 0");
            }
            splitPane2.setRightComponent(panel8);
        }
        add(splitPane2, BorderLayout.CENTER);
    }

    protected JToolBar toolBar1;
    private JLabel label1;
    private JLabel timeLabel;
    protected JSplitPane splitPane2;
    protected JPanel panel7;
    protected JScrollPane scrollPane1;
    protected JSplitPane splitPane3;
    public CarsContainer carsContainer;
    protected JPanel panel3;
    public StoplightContainer stoplightContainer;
    protected JPanel panel8;
    protected JPanel panel6;
    private JButton startCars;
    private JButton pauseCars;
    protected JButton addCarButton;
    protected JPanel vSpacer1;
    private JButton exitButton;
    protected JPanel vSpacer4;
    protected JButton startButton;
    protected JButton pauseLights;
    protected JButton addInterButton;

    /**
     * class to hold Stoplight Components
     */
    public static class StoplightContainer extends JPanel {
        private StoplightContainer() {
            initComponents();
        }

        private void initComponents() {
            //======== this ========
            setMinimumSize(new Dimension(610, 460));
            setMaximumSize(new Dimension(50000, 460));
            setPreferredSize(new Dimension(610, 460));
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        }

    }
    /**
     * class to hold Vehicle Components
     */
    public static class CarsContainer extends JPanel {
        private CarsContainer() {
            initComponents();
        }

        private void initComponents() {
            //======== this ========
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

    }
}

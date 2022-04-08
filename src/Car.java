//  Brian Vegh
//  UMCG CMSC-335 Project 3
//  December 15, 2020
//  Car.java - Class that creates each car JPanel on the GUI and runs it's multi-threaded code
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
//import mig layout
import net.miginfocom.swing.*;
/**
 * Car.java - Class that creates each car JPanel on the GUI and runs it's multi-threaded code
 * @author Brian Vegh
 */
public class Car extends JPanel implements Runnable {
    private final int DISTANCE_BETWEEN_LIGHTS = Project3Main.DISTANCE_BETWEEN_LIGHTS;

    private static int staticCarNumberCounter;
    private static Integer initialSpeed;
    final int carNumber;
    private int speed;
    private int xPosition;
    private TrafficLightColor nextLightColor;
    private Thread thread;
    private int totalDistance;
    String threadName;
    final DefaultBoundedRangeModel model = new DefaultBoundedRangeModel();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    public final AtomicBoolean suspended = new AtomicBoolean(false);
    private CarStatus carStatus;
    private boolean done;
    private MainPanel mainPanel;
    private int nextLightNumber;
    final TrafficLightColor YELLOW = TrafficLightColor.YELLOW;
    final TrafficLightColor RED = TrafficLightColor.RED;

    /**
     * default constructor.
     * @param mainPanel
     */
    public Car(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        //fill panel
        initComponents();
        staticCarNumberCounter += 1;
        this.carNumber = staticCarNumberCounter;
        setInitialSpeed();
        speed = initialSpeed;
        xPosition=0;
        totalDistance = Stoplight.getxTotalDistance();
        //create and name class's thread
        this.threadName = "Car " + carNumber;
        thread = new Thread(this, threadName);
        //set gui component values
        speedSpinner.setValue(initialSpeed);
        carStatus = CarStatus.STOPPED;
        carStatusTF.setText(carStatus.getStatus());
        carNumberTF.setText(String.valueOf(carNumber));
        progressBar = new JProgressBar(0, DISTANCE_BETWEEN_LIGHTS * 3);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
    }
    /**
     * sets initial speed of each car. the first three are created at predefined speeds; after that, each
     * car's speed is a random value between 35 and 100
     */
    private void setInitialSpeed() {
        if (initialSpeed == null) {
            initialSpeed = 20;
        } else if (initialSpeed == 20) {
            initialSpeed = 50;
        } else if (initialSpeed == 50) {
            initialSpeed = 125;
        } else {
            Random r = new Random();
            initialSpeed = r.nextInt(100 - 35) + 35;
        }
    }
    /**
     * returns cars current speed in feet per second
     * @return
     */
    private double getFPS() {
        return speed * 1.46667;
    }
    /**
     * run method that controls thread initialization, and invokes SwingUtilities invoke later
     */
    @Override
    public void run() {
        Debug.print("Running " + threadName);
        model.setMinimum(0);    //set model of progress bar, for concurrent update
        model.setMaximum(totalDistance);
        isRunning.set(true);
        carStatus = CarStatus.DRIVING;
        while (isRunning.get()) {
            //call update to GUI using a background SwingWorker
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateCarPanel();
                }
            });
            try {
                //while suspended check next light color at .5 second intervals
                while (suspended.get()) {
                    if (carStatus == CarStatus.ATLIGHT) {
                        if (checkNextLight() == TrafficLightColor.GREEN
                                || checkNextLight() == TrafficLightColor.MAINTENANCE) {
                            resume();
                        }
                    }
                    Debug.print(threadName + " waiting");
                    Thread.sleep(500);
                }
                //reset xPosition once car reaches end of road
                if (xPosition > totalDistance) {
                    xPosition = 0;
                }
                totalDistance = Stoplight.getxTotalDistance();
                model.setMaximum(totalDistance);
                model.setValue(getxPosition());
                xPosition += getFPS() / 4;
                Thread.sleep(250);
                nextLightNumber = xPosition / DISTANCE_BETWEEN_LIGHTS;
                //actions if car is at a yellow or red light
                if (nextLightNumber >= Stoplight.getStaticLightNumberCounter()) {
                    nextLightNumber = 0;
                }
                Debug.print(threadName + " Next Light Number: " + nextLightNumber);
                nextLightColor = checkNextLight();
                if (checkIfStop() && (nextLightColor == YELLOW || nextLightColor == RED)){
                    carStatus = CarStatus.ATLIGHT;
                    interrupt();
                    suspend(carStatus);
                }
                progressBar.setValue(getxPosition());
                this.updateUI();
                progressBar.updateUI();

            } catch (InterruptedException exc) {
                model.setValue(getxPosition());
                suspended.set(true);
            }
        }
    }
    /**
     * determine if car will need to stop before light
     * stops if distance to light is less than the distance in which car can stop
     * @return
     */
    private boolean checkIfStop() {
        boolean necessary = false;
        double fps = getFPS();
        int distanceToLight = DISTANCE_BETWEEN_LIGHTS - getxPosition() % DISTANCE_BETWEEN_LIGHTS;
        //if distance to light is less than the distance in which car can stop
        if (distanceToLight < fps/2) {
            necessary = true;
        }
        return necessary;
    }

    /**
     * gets status of the next light for the car
     * the next light affects
     * @return
     */
    private TrafficLightColor checkNextLight() {
        try {
            return mainPanel.getIntersections().get(nextLightNumber).getTrafficLightColor();
        } catch (IndexOutOfBoundsException e){
            System.out.println("Driver in "+threadName+" ran a red light!");
            return TrafficLightColor.RED;
        }
    }
    /**
     * returns string value based off enum CarStatus
     * @return
     */
    private String getStatus() {
        switch (carStatus) {
            case STOPPED:
            case PULLED:
            case ATLIGHT:
                return carStatus.getStatus();
            case DRIVING:
                return carStatus.getStatus() + speed + " mph";
            default:
                return null;
        }
    }
    /**
     * updates the UI component for Car. Only called by SwingWorker
     * to prevent delays in GUI update
     */
    private void updateCarPanel() {
        carStatusTF.setText(getStatus());
        xPositionTF.setText(getxPosition() + " / " + totalDistance);
        progressBar.setValue(getxPosition());
        this.updateUI();
        progressBar.updateUI();
    }
    /**
     * retreive total number of intersections from Stoplight class
     * @return
     */
    public synchronized int getStaticCarNumberCounter() {
        return staticCarNumberCounter;
    }
    /**
     * resume car thread
     */
    public synchronized void resume() {
        suspended.set(false);
        this.carStatus = CarStatus.DRIVING;
        carStatusTF.setText(getStatus());
        notify();
        Debug.print("Resuming " + threadName);
    }
    /**
     * stop car thread
     */
    public synchronized void stop() {
        thread.interrupt();
        this.carStatus = CarStatus.STOPPED;
        isRunning.set(false);
        Debug.print("Stopping " + threadName);
        carStatusTF.setText(getStatus());
    }
    /**
     * interrupt car thread
     */
    public void interrupt() {
        //If light is sleeping, we can call interrupt to wake it when hitting "Pause" button
        thread.interrupt();
    }
    /**
     * suspend car thread
     */
    public void suspend(CarStatus carStatus) {
        this.carStatus = carStatus;
        carNumberTF.setText(getStatus());
        SwingUtilities.invokeLater(this::updateCarPanel);
        Debug.print("Suspending " + threadName);
        suspended.set(true);
    }
    /**
     * update car's speed based on spinner state change
     * @param e
     */
    private void speedSpinnerStateChanged(ChangeEvent e) {
        // TODO add your code here
        speed = (Integer) speedSpinner.getValue();
    }
    /**
     * pause or resume car thread and update status when pull over button pressed
     * @param actionEvent
     */
    private void pullOverButtonPressed(ActionEvent actionEvent) {
        Debug.print("pulledOver pressed");
        if (isRunning.get()) {
            if (!suspended.get()) {
                Debug.print(threadName + " pulled over");
                interrupt();
                suspend(CarStatus.PULLED);
            } else {
                Debug.print(threadName + " back on the road");
                resume();
            }
        }
    }
    /**
     * synchronized method to get xPosition
     * @return
     */
    private synchronized int getxPosition() {
        return xPosition;
    }

    /**
     * initialize GUI components
     */
    private void initComponents() {
        panel9 = new JPanel();
        label7 = new JLabel();
        carNumberTF = new JTextField();
        label10 = new JLabel();
        xPositionTF = new JTextField();
        label8 = new JLabel();
        carStatusTF = new JTextField();
        label9 = new JLabel();
        speedSpinner = new JSpinner();
        pullOverButton = new JButton();
        progressBar = new JProgressBar(model);

        //======== this ========
        setBorder(LineBorder.createBlackLineBorder());
        setPreferredSize(new Dimension(100, 82));
        setMinimumSize(new Dimension(300, 82));
        setMaximumSize(new Dimension(2147483647, 82));
        setLayout(new BorderLayout());

        //======== panel9 ========
        {
            panel9.setPreferredSize(new Dimension(100, 80));
            panel9.setMinimumSize(new Dimension(300, 82));
            panel9.setLayout(new MigLayout(
                    "insets 10 10 10 18,hidemode 3",
                    // columns
                    "[74,fill]0" +
                            "[42,fill]0" +
                            "[fill]0" +
                            "[fill]0" +
                            "[fill]0" +
                            "[43,fill]0" +
                            "[98,fill]0" +
                            "[fill]0" +
                            "[fill]0" +
                            "[fill]0" +
                            "[78,fill]0" +
                            "[fill]0" +
                            "[59,fill]0" +
                            "[7,fill]0" +
                            "[93,fill]" +
                            "[fill]" +
                            "[198,fill]",
                    // rows
                    "[]" +
                            "[]" +
                            "[]"));

            //---- label7 ----
            label7.setText("Car # :");
            label7.setHorizontalAlignment(SwingConstants.CENTER);
            panel9.add(label7, "cell 0 0");

            //---- carNumberTF ----
            carNumberTF.setColumns(10);
            carNumberTF.setMinimumSize(new Dimension(20, 28));
            carNumberTF.setMaximumSize(new Dimension(20, 2147483647));
            panel9.add(carNumberTF, "cell 0 0");

            //---- label10 ----
            label10.setText("X Position:");
            label10.setHorizontalAlignment(SwingConstants.CENTER);
            panel9.add(label10, "cell 2 0");

            //---- xPositionTF ----
            xPositionTF.setColumns(20);
            xPositionTF.setText("0");
            xPositionTF.setFocusable(false);
            xPositionTF.setHorizontalAlignment(SwingConstants.CENTER);
            panel9.add(xPositionTF, "cell 4 0");

            //---- label8 ----
            label8.setText("Status: ");
            label8.setHorizontalAlignment(SwingConstants.RIGHT);
            panel9.add(label8, "cell 6 0");

            //---- statusTF ----
            carStatusTF.setColumns(25);
            carStatusTF.setText("Driving @ X mph");
            carStatusTF.setFocusable(false);
            panel9.add(carStatusTF, "cell 8 0");

            //---- label9 ----
            label9.setText("Speed: ");
            label9.setHorizontalAlignment(SwingConstants.RIGHT);
            panel9.add(label9, "cell 10 0");

            //---- speedSpinner ----
            speedSpinner.setModel(new SpinnerNumberModel(15, 0, 225, 5));
            speedSpinner.addChangeListener(this::speedSpinnerStateChanged);
            panel9.add(speedSpinner, "cell 12 0");

            //---- pullOverButton ----
            pullOverButton.setText("Pull Over / Drive");
            pullOverButton.addActionListener(this::pullOverButtonPressed);
            panel9.add(pullOverButton, "cell 14 0");

            //---- progressBar ----
            progressBar.setMinimumSize(new Dimension(10, 25));
            panel9.add(progressBar, "cell 0 1 17 1");
        }
        add(panel9, BorderLayout.CENTER);
    }

    public JPanel panel9;
    private JLabel label7;
    public JTextField carNumberTF;
    private JLabel label10;
    public JTextField xPositionTF;
    private JLabel label8;
    public JTextField carStatusTF;
    private JLabel label9;
    public JSpinner speedSpinner;
    public JButton pullOverButton;
    public JProgressBar progressBar;

}

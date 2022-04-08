//  Brian Vegh
//  UMCG CMSC-335 Project 3
//  December 15, 2020
//  CarStatus.java - public enum to pass and print each car's status

/**
 * public enum to pass and print each car's status
 */
public enum CarStatus {
    STOPPED("Stopped"),
    PULLED("Pulled Over"),
    DRIVING("Driving at "),
    ATLIGHT("At red light"),
    PAUSED("All Cars Paused");

    private String status;

    CarStatus(String status) {
        this.status=status;
    }

    public synchronized String getStatus() {
        return status;
    }
}

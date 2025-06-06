package road_registry;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;

public class person {private String personID;
    private String firstName;
    private String lastName;
    private String address;
    private String birthdate;
    private HashMap<String, Integer> demeritPoints;
    private boolean isSuspended;

    // Constructor to initialize person attributes
    public Person(String personID, String firstName, String lastName, String address, String birthdate) {
        this.personID = personID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.birthdate = birthdate;
        this.demeritPoints = new HashMap<>();
        this.isSuspended = false;
    }

    // Set new birthdate with format validation
    public void setBirthdate(String newBirthdate) {
        if (isValidBirthdate(newBirthdate)) {
            this.birthdate = newBirthdate;
        }
    }

    // Replace current demerit points with a new set
    public void setDemeritPoints(HashMap<String, Integer> points) {
        if (points != null) {
            this.demeritPoints = new HashMap<>(points);
        }
    }

    // Adds the person to people.txt after validation
    public boolean addPerson() {
        if (!isValidPersonID(this.personID) || !isValidAddress(this.address) || !isValidBirthdate(this.birthdate)) {
            return false;
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter("people.txt", true));
            writer.println(formatPersonData());
            writer.close();
        } catch (Exception e) {
            System.out.println("Error saving person: " + e.getMessage());
            return false;
        }
        return true;
    }

    // Validates the format and structure of person ID
    private boolean isValidPersonID(String id) {
        if (id.length() != 10 || !id.substring(0, 2).matches("[2-9]{2}") || !id.substring(8).matches("[A-Z]{2}")) return false;
        String middle = id.substring(2, 8);
        int specialCount = 0;
        for (char ch : middle.toCharArray()) {
            if (!Character.isLetterOrDigit(ch)) specialCount++;
        }
        return specialCount >= 2;
    }

    // Validates address format and ensures state is Victoria
    private boolean isValidAddress(String addr) {
        String[] parts = addr.split("\\|");
        return parts.length == 5 && parts[3].equals("Victoria");
    }

    // Checks if birthdate is in DD-MM-YYYY format
    private boolean isValidBirthdate(String date) {
        return date.matches("\\d{2}-\\d{2}-\\d{4}");
    }

    // Formats the person object into a string for saving
    private String formatPersonData() {
        String[] parts = address.split("\\|");
        return this.personID + "|" + this.firstName + "|" + this.lastName + "|" +
               parts[0] + "|" + parts[1] + "|" + parts[2] + "|" + parts[3] + "|" + parts[4] +
               "|" + this.birthdate;
    }
}

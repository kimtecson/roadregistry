package road_registry;

import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

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


public boolean updatePersonalDetails(String newID, String newFirstName, String newLastName, String newAddress, String newBirthdate) {
        if (!isValidPersonID(newID) || !isValidAddress(newAddress) || !isValidBirthdate(newBirthdate)) return false;

        // Check age of current person
        String[] bParts = this.birthdate.split("-");
        Date birth = new Date(Integer.parseInt(bParts[2]) - 1900, Integer.parseInt(bParts[1]) - 1, Integer.parseInt(bParts[0]));
        Date now = new Date();
        int age = (int) ((now.getTime() - birth.getTime()) / (1000L * 60 * 60 * 24 * 365));

        // Under 18 can't change address
        if (age < 18 && !newAddress.equals(this.address)) return false;

        // If birthdate changes, no other field should change
        boolean birthdayChanged = !this.birthdate.equals(newBirthdate);
        boolean otherChanged = !this.personID.equals(newID) || !this.firstName.equals(newFirstName) || !this.lastName.equals(newLastName) || !this.address.equals(newAddress);
        if (birthdayChanged && otherChanged) return false;

        // Disallow ID change if current ID starts with even digit
        char firstChar = this.personID.charAt(0);
        if (Character.isDigit(firstChar) && ((firstChar - '0') % 2 == 0) && !newID.equals(this.personID)) return false;

        List<String> lines = new ArrayList<>();
        boolean found = false;

        // Read and update record from file
        try {
            BufferedReader reader = new BufferedReader(new FileReader("people.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(this.personID + "|")) {
                    found = true;
                    String[] addrParts = newAddress.split("\\|");
                    line = newID + "|" + newFirstName + "|" + newLastName + "|" +
                           addrParts[0] + "|" + addrParts[1] + "|" + addrParts[2] + "|" +
                           addrParts[3] + "|" + addrParts[4] + "|" + newBirthdate;
                }
                lines.add(line);
            }
            reader.close();
        } catch (Exception e) {
            return false;
        }

        if (!found) return false;

        // Rewrite updated file
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("people.txt"));
            for (String l : lines) writer.println(l);
            writer.close();
        } catch (Exception e) {
            return false;
        }

        // Rename demerits file if ID changed
        if (!this.personID.equals(newID)) {
            File oldFile = new File("demerits_" + this.personID + ".txt");
            File newFile = new File("demerits_" + newID + ".txt");
            if (oldFile.exists()) oldFile.renameTo(newFile);
        }

        // Apply changes
        this.personID = newID;
        this.firstName = newFirstName;
        this.lastName = newLastName;
        this.address = newAddress;
        this.birthdate = newBirthdate;

        return true;
    }



}

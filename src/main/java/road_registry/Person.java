package road_registry;

import java.io.*;
import java.util.*;

public class Person {
    private String personID;
    private String firstName;
    private String lastName;
    private String address;
    private String birthdate;
    private HashMap<String, Integer> demeritPoints = new HashMap<>();
    private boolean isSuspended = false;

    // Constructor
    public Person(String personID, String firstName, String lastName, String address, String birthdate) {
        this.personID = personID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.birthdate = birthdate;
    }

    // Setters
    public void setBirthdate(String newBirthdate) {
        if (isValidBirthdate(newBirthdate)) this.birthdate = newBirthdate;
    }

    public void setDemeritPoints(HashMap<String, Integer> points) {
        if (points != null) this.demeritPoints = new HashMap<>(points);
    }

    // Validation helpers
    private boolean isValidPersonID(String id) {
        if (id.length() != 10 || !id.substring(0, 2).matches("[2-9]{2}") || !id.substring(8).matches("[A-Z]{2}")) return false;
        String middle = id.substring(2, 8);
        return middle.chars().filter(c -> !Character.isLetterOrDigit(c)).count() >= 2;
    }

    private boolean isValidAddress(String addr) {
        String[] parts = addr.split("\\|");
        return parts.length == 5 && parts[3].equals("Victoria");
    }

    private boolean isValidBirthdate(String date) {
        return date.matches("\\d{2}-\\d{2}-\\d{4}");
    }

    // File I/O
    private void writePersonToFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("people.txt", true))) {
            writer.println(formatPersonData());
        }
    }

    private String formatPersonData() {
        String[] parts = address.split("\\|");
        return String.join("|", personID, firstName, lastName, parts[0], parts[1], parts[2], parts[3], parts[4], birthdate);
    }

    public boolean addPerson() {
        if (!isValidPersonID(personID) || !isValidAddress(address) || !isValidBirthdate(birthdate)) return false;
        try {
            writePersonToFile();
            return true;
        } catch (IOException e) {
            System.out.println("Error saving person: " + e.getMessage());
            return false;
        }
    }

    private boolean personExistsInFile() throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader("people.txt"))) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(this.personID + "|")) return true;
        }
    } catch (FileNotFoundException e) {
        // If file does not exist yet, assume person doesn't exist
        return false;
    }
    return false;
    }

    private int calculateAge(Date birthDate) {
        long diffMillis = new Date().getTime() - birthDate.getTime();
        return (int) (diffMillis / (1000L * 60 * 60 * 24 * 365));
    }

    private Date parseDate(String dateStr) {
        String[] parts = dateStr.split("-");
        return new Date(Integer.parseInt(parts[2]) - 1900, Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[0]));
    }

    private void updateSuspensionStatus(Date offenseDate) {
        int totalPoints = 0;
        for (Map.Entry<String, Integer> entry : demeritPoints.entrySet()) {
            Date d = parseDate(entry.getKey());
            long diff = offenseDate.getTime() - d.getTime();
            if (diff >= 0 && diff <= 2L * 365 * 24 * 60 * 60 * 1000) {
                totalPoints += entry.getValue();
            }
        }

        Date birth = parseDate(this.birthdate);
        int age = calculateAge(birth);

        if ((age < 21 && totalPoints > 6) || (age >= 21 && totalPoints > 12)) {
            isSuspended = true;
        } else {
            isSuspended = false;
        }
    }

    private boolean saveDemeritPointsToFile(boolean append) {
        File file = new File("demerits_" + personID + ".txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, append))) {
            for (Map.Entry<String, Integer> entry : demeritPoints.entrySet()) {
                writer.println(entry.getKey() + "|" + entry.getValue());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Failed to save demerit points: " + e.getMessage());
            return false;
        }
    }

    public String addDemeritPoints(String offenseDateStr, int points) {
        if (!offenseDateStr.matches("\\d{2}-\\d{2}-\\d{4}") || points < 1 || points > 6) return "Failed";

        Date offenseDate = parseDate(offenseDateStr);
        demeritPoints.put(offenseDateStr, points);
        updateSuspensionStatus(offenseDate);

        try {
            boolean exists = personExistsInFile();
            if (!saveDemeritPointsToFile(exists)) return "Failed";
        } catch (IOException e) {
            System.out.println("Failed checking or writing person: " + e.getMessage());
            return "Failed";
        }

        return "Success";
    }

    public boolean updatePersonalDetails(String newID, String newFirstName, String newLastName, String newAddress, String newBirthdate) {
        if (!isValidPersonID(newID) || !isValidAddress(newAddress) || !isValidBirthdate(newBirthdate)) return false;

        int age = calculateAge(parseDate(this.birthdate));
        boolean birthdayChanged = !this.birthdate.equals(newBirthdate);
        boolean otherChanged = !this.personID.equals(newID) || !this.firstName.equals(newFirstName)
                             || !this.lastName.equals(newLastName) || !this.address.equals(newAddress);

        if (age < 18 && !newAddress.equals(this.address)) return false;
        if (birthdayChanged && otherChanged) return false;
        if (Character.isDigit(personID.charAt(0)) && (personID.charAt(0) - '0') % 2 == 0 && !newID.equals(personID)) return false;

        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader("people.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(this.personID + "|")) {
                    found = true;
                    String[] addrParts = newAddress.split("\\|");
                    line = String.join("|", newID, newFirstName, newLastName, addrParts[0], addrParts[1], addrParts[2], addrParts[3], addrParts[4], newBirthdate);
                }
                lines.add(line);
            }
        } catch (IOException e) {
            return false;
        }

        if (!found) return false;

        try (PrintWriter writer = new PrintWriter(new FileWriter("people.txt"))) {
            for (String l : lines) writer.println(l);
        } catch (IOException e) {
            return false;
        }

        if (!this.personID.equals(newID)) {
            File oldFile = new File("demerits_" + this.personID + ".txt");
            File newFile = new File("demerits_" + newID + ".txt");
            if (oldFile.exists()) oldFile.renameTo(newFile);
        }

        // Update current instance
        this.personID = newID;
        this.firstName = newFirstName;
        this.lastName = newLastName;
        this.address = newAddress;
        this.birthdate = newBirthdate;

        return true;
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public static Person[] loadAllFromFile(String filename) {
        ArrayList<Person> persons = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 9) continue;

                String personID = parts[0];
                String firstName = parts[1];
                String lastName = parts[2];
                String address = String.join("|", parts[3], parts[4], parts[5], parts[6], parts[7]);
                String birthdate = parts[8];

                Person p = new Person(personID, firstName, lastName, address, birthdate);

                File demeritFile = new File("demerits_" + personID + ".txt");
                if (demeritFile.exists()) {
                    try (BufferedReader dr = new BufferedReader(new FileReader(demeritFile))) {
                        String dLine;
                        while ((dLine = dr.readLine()) != null) {
                            String[] dp = dLine.split("\\|");
                            if (dp.length == 2) p.demeritPoints.put(dp[0], Integer.parseInt(dp[1]));
                        }
                        p.addDemeritPoints("01-01-2099", 1); // trigger suspension logic
                        p.demeritPoints.remove("01-01-2099");
                    } catch (IOException ignored) {}
                }
                persons.add(p);
            }
        } catch (IOException e) {
            System.out.println("Error loading people: " + e.getMessage());
        }
        return persons.toArray(new Person[0]);
    }

    @Override
    public String toString() {
        return "ID: " + personID + "\nName: " + firstName + " " + lastName +
               "\nAddress: " + address + "\nBirthdate: " + birthdate +
               "\nSuspended: " + isSuspended + "\nDemerits: " + demeritPoints;
    }

    public static void main(String[] args) {
    // Step 1: Create a person with valid data
    Person p = new Person("23!!88!!AB", "Kim", "Tecson", "123|Main|St|Victoria|Australia", "01-01-2005");

    // Step 2: Add to people.txt
    boolean added = p.addPerson();
    System.out.println("Person added: " + added);

    // Step 3: Add valid demerit points
    String result = p.addDemeritPoints("01-01-2024", 5);
    System.out.println("Demerit add result: " + result);

    // Step 4: Check if file was created and print its contents
    File file = new File("demerits_23!!88!!AB.txt");
    if (file.exists()) {
        System.out.println("✅ Demerits file created.");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            System.out.println("📄 Demerits file content:");
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
        } catch (IOException e) {
            System.out.println("Error reading demerits file: " + e.getMessage());
        }
    } else {
        System.out.println("Demerits file not found.");
    }

    // Step 5: Print suspension status
    System.out.println("Suspended? " + p.isSuspended());
  }
}
// This is a dummy comment to retrigger GitHub Actions for demo

package road_registry;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;


public class PersonTest {

    // Delete people.txt before each test to avoid test pollution
    @BeforeEach
    public void clearFiles() {
        new File("people.txt").delete();
    }

    // --- addPerson() tests ---

    @Test
    public void testValidPerson() {
        // Test a person with all valid details
        Person p = new Person("24!!88!!AB", "Kimberly", "Tecson", "987|RMIT|St|Victoria|Australia", "01-01-1990");
        assertTrue(p.addPerson());
    }

    @Test
    public void testInvalidIDTooShort() {
        // Test with an ID that is too short
        Person p = new Person("23!AB", "Kimberly", "Tecson", "987|RMIT|St|Victoria|Australia", "01-01-1998");
        assertFalse(p.addPerson());
    }

    @Test
    public void testInvalidIDMissingSpecials() {
        // Test with an ID missing required special characters
        Person p = new Person("23ABCDE987", "Kimberly", "Tecson", "987|RMIT|St|Victoria|Australia", "01-01-1998");
        assertFalse(p.addPerson());
    }

    @Test
    public void testInvalidAddress() {
        // Test with an address where state is not Victoria
        Person p = new Person("23!!88!!AB", "Kimberly", "Tecson", "987|RMIT|St|NSW|Australia", "01-01-1998");
        assertFalse(p.addPerson());
    }

    @Test
    public void testInvalidDOBFormat() {
        // Test with an incorrectly formatted birthdate
        Person p = new Person("23!!88!!AB", "Kimberly", "Tecson", "987|RMIT|St|Victoria|Australia", "1998-01-01");
        assertFalse(p.addPerson());
    }

    // --- addDemeritPoints() tests ---

    @Test
    public void testValidDemeritPoints() {
        // Add valid demerit points
        Person p = new Person("23!!88!!AB", "K", "T", "987|RMIT|St|Victoria|Australia", "01-01-1998");
        p.addPerson();
        String result = p.addDemeritPoints("01-01-2023", 4);
        assertEquals("Success", result);
    }

    @Test
    public void testInvalidDateFormat() {
        // Test with a date in wrong format
        Person p = new Person("23!!88!!AB", "K", "T", "987|RMIT|St|Victoria|Australia", "01-01-1998");
        String result = p.addDemeritPoints("2023-01-01", 3);
        assertEquals("Failed", result);
    }

    @Test
    public void testPointsBelowRange() {
        // Test with points below the minimum allowed
        Person p = new Person("23!!88!!AB", "K", "T", "987|RMIT|St|Victoria|Australia", "01-01-1998");
        String result = p.addDemeritPoints("01-01-2023", 0);
        assertEquals("Failed", result);
    }

    @Test
    public void testPointsAboveRange() {
        // Test with points above the allowed maximum
        Person p = new Person("23!!88!!AB", "K", "T", "987|RMIT|St|Victoria|Australia", "01-01-1998");
        String result = p.addDemeritPoints("01-01-2023", 7);
        assertEquals("Failed", result);
    }

    @Test
    public void testEdgeValidPoints() {
        // Test with maximum valid points (6)
        Person p = new Person("23!!88!!AB", "K", "T", "987|RMIT|St|Victoria|Australia", "01-01-1998");
        String result = p.addDemeritPoints("01-01-2024", 6);
        assertEquals("Success", result);
    }

    // --- updatePersonalDetails() tests ---
    // First add a valid person to enable update
    @Test
    public void testValidUpdateSameID() {
        // Valid update with same ID and a changed last name
        Person p = new Person("23!!88!!AB", "Kim", "Tecson", "987|RMIT|St|Victoria|Australia", "01-01-1998");
        p.addPerson();
        boolean result = p.updatePersonalDetails("23!!88!!AB", "Kim", "Smith", "987|RMIT|St|Victoria|Australia", "01-01-1998");
        assertTrue(result);
    }

    @Test
    public void testChangeIDWithEvenStartDigit() {
        // Attempt to change ID when current ID starts with an even digit
        Person p = new Person("23!!88!!AB", "Kim", "Doe", "987|RMIT|St|Victoria|Australia", "01-01-1998");
        p.addPerson();
        boolean result = p.updatePersonalDetails("24!!88!!AB", "Jane", "Doe", "987|RMIT|St|Victoria|Australia", "01-01-1990");
        assertFalse(result);
    }

    @Test
    public void testChangeDOBWithOtherFields() {
        // Change DOB and another field — should fail
        Person p = new Person("23!!88!!AB", "Kim", "Doe", "987|RMIT|St|Victoria|Australia", "01-01-1998");
        p.addPerson();
        boolean result = p.updatePersonalDetails("23!!88!!AB", "Janet", "Doe", "987|RMIT|St|Victoria|Australia", "02-02-1991");
        assertFalse(result);
    }

    @Test
    public void testChangeAddressUnder18() {
        // Address change for underage person — should fail
        Person p = new Person("23!!88!!AB", "Jane", "Doe", "987|RMIT|St|Victoria|Australia", "01-01-2010");
        p.addPerson();
        boolean result = p.updatePersonalDetails("23!!88!!AB", "Jane", "Doe", "124|RMIT|St|Victoria|Australia", "01-01-2010");
        assertFalse(result);
    }

    @Test
    public void testUpdatePersonWithInvalidAddressFormat() {

        Person p = new Person("23!!88!!AB", "Jane", "Doe", "987|RMIT|St|Victoria|Australia", "01-01-1998");
        p.addPerson();
        // Try to update with an invalid address format (missing '|' or wrong number of parts)
        boolean result = p.updatePersonalDetails("23!!88!!AB", "Jane", "Doe", "RMIT Street, Melbourne", "01-01-1998");
        assertFalse(result);
    }
}


package nl.odin.assignment.utils.randomuser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nl.odin.assignment.User;
import nl.odin.assignment.utils.randomuser.Document.Result;

public final class DocumentTest {

    @Test
    public void testUserConversion() {
        // Given some test date
        final String testFirstName = "John";
        final String testSurname = "Doe";
        final ZonedDateTime testDate = ZonedDateTime.now();
        final String testPictureFilename = "john_doe.jpg";
        final Result.Gender testGender = Result.Gender.MALE;

        // When we create a randomUser result using the test data
        final Document.Result randomUserResult = new Document.Result(//
                testGender, //
                new Document.Result.Name(testFirstName, testSurname), //
                new Document.Result.DateOfBirth(testDate), //
                new Document.Result.Picture(testPictureFilename));
        // and we convert this randomUser result into a user
        final User user = randomUserResult.toUser();

        // We expect the properties of the user to match our test data.
        assertEquals(user.getGender(), testGender.toGender());
        assertEquals(user.getFirstName().getValue(), testFirstName);
        assertEquals(user.getSurname().getValue(), testSurname);
        assertEquals(user.getDateOfBirth(), testDate);
        assertEquals(user.getPictureFileName(), testPictureFilename);
    }

    /**
     * Test the ability of RandomUser to deserialize JSON from randomuser.me.
     * 
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Test
    public void testDeserialize()
            throws JsonParseException, JsonMappingException, IOException {
        // Given a json file, with the result of randomuser.me/api requests
        final String path = "/deserilization_testdata.json";
        final InputStream input = getClass().getResourceAsStream(path);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        //
        final Document randomUser = mapper.readValue(input, Document.class);
        assertNotNull(randomUser);
        final List<Document.Result> results = randomUser.getResults();
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("klaus-dieter", results.get(0).getName().getFirst());
        assertEquals(Result.Gender.MALE, results.get(0).getGender());
    }

    @Test
    public void testToGender() {
        // For completeness:
        assertEquals(User.Gender.FEMALE, Result.Gender.FEMALE.toGender());
        assertEquals(User.Gender.MALE, Result.Gender.MALE.toGender());
    }
}

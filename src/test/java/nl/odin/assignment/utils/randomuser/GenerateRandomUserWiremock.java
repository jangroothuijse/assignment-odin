package nl.odin.assignment.utils.randomuser;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import lombok.extern.slf4j.Slf4j;
import nl.odin.assignment.User;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

/**
 * Generates files to mock a back-end server using WireMock.
 * 
 * The files generated by this process are checked into version control, so that
 * we do not depend on an external service to be active for this service to
 * build, test or run.
 */
@Slf4j
public class GenerateRandomUserWiremock {
    /**
     * Seed for randomuser.me, so that the results of this generation process
     * are deterministic.
     */
    private static final String SEED = "123";
    /**
     * Number of users to generate.
     */
    public static final int NUMBER_OF_USERS = 100;

    /**
     * Location of wire mock docker files.
     */
    public static final String WIREMOCK = "src/main/docker/wiremock";
    /**
     * Location of static files served by wiremock.
     */
    public static final String FILES = WIREMOCK + "/__files";
    /**
     * Location of JSON documents with mappings from requests to responses.
     */
    public static final String MAPPINGS = WIREMOCK + "/mappings";
    private static final String MEN = FILES + "/portraits/med/men";
    private static final String WOMEN = FILES + "/portraits/med/women";
    private static final String UTF_8 = "UTF-8";
    /**
     * URL to get the data from. Limits the number of results to NUMBER_OF_USERS
     * Uses a seed SEED to make this operation repeatable.
     */
    private final static String URL =
            String.format("https://randomuser.me/api?results=%d&seed=%s",
                    NUMBER_OF_USERS, SEED);

    /**
     * <strong>Deletes</strong> files in
     * <ul>
     * <li>src/main/docker/wiremock/__files</li>
     * <li>src/main/docker/wiremock/mappings</li>
     * </ul>
     * 
     * @param args are ignored
     * @throws IOException when files cannot be found or created
     */
    public static void main(final String[] args) throws IOException {
        // Remove mappings and __files directory, if it exists
        log.info("Removing {} and {}", FILES, MAPPINGS);
        FileUtils.deleteDirectory(new File(FILES));
        FileUtils.deleteDirectory(new File(MAPPINGS));
        // Add mappings and __files directory
        log.info("Creating {}, {} and {}", MEN, WOMEN, MAPPINGS);
        Files.createDirectories(Paths.get(MEN));
        Files.createDirectories(Paths.get(WOMEN));
        Files.createDirectories(Paths.get(MAPPINGS));

        // Start wiremock server
        final WireMockServer wireMockServer =
                new WireMockServer(options().withRootDirectory(WIREMOCK));
        wireMockServer.start();

        // Query random.me for data
        RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<Document> entity =
                restTemplate.getForEntity(URL, Document.class);

        final Document document = entity.getBody();

        // Get the rest template ready to download image files
        restTemplate.getMessageConverters()
                .add(new ByteArrayHttpMessageConverter());
        HttpHeaders imageHeaders = new HttpHeaders();
        imageHeaders
                .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        final int prefixLength = "https://randomuser.me/api/".length();
        final List<User> users = document.getResults() //
                .stream() //
                // randomuser.me serves up documents which reference files on
                // randomuser.me. Here, we save those files and alter those
                // references.
                .map((final Document.Result user) -> {
                    // correct file name
                    final String filename = user.getPicture().getMedium()
                            .substring(prefixLength);

                    // Download the file
                    final ResponseEntity<byte[]> response =
                            restTemplate.exchange(user.getPicture().getMedium(),
                                    HttpMethod.GET, entity, byte[].class);

                    try {
                        Files.write(Paths.get(FILES + '/' + filename),
                                response.getBody());
                    } catch (IOException e) {
                        log.error("Unable to write downloaded picture {}",
                                filename, e);
                    }
                    return new Document.Result(user.getGender(), user.getName(),
                            user.getDob(),
                            new Document.Result.Picture(filename));
                }).map(Document.Result::toUser) //
                .collect(Collectors.toList());

        log.info("Received {} users from {}", users.size(), URL);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final String allUsers = mapper.writeValueAsString(users);

        // Stub for all users
        stubFor(get(urlEqualTo("/users")).willReturn(aResponse() //
                .withHeader("content-type", "application/json; charset=utf-8") //
                .withBody(allUsers)));

        users.forEach((final User user) -> {

            try {
                final String userUrl = String.format("/users/%s/%s", //
                        // URLEncoder encodes to x-www-form, which uses + sign
                        // for space Since this is part of the URL before the ?,
                        // we need %20
                        URLEncoder.encode(user.getFirstName().getValue(), UTF_8)
                                .replace("+", "%20"), //
                        URLEncoder.encode(user.getSurname().getValue(), UTF_8)
                                .replace("+", "%20"));
                try {
                    stubFor(get(urlEqualTo(userUrl)).willReturn(aResponse() //
                            .withHeader("content-type",
                                    "application/json; charset=utf-8") //
                            .withBody(mapper.writeValueAsString(user))));
                } catch (JsonProcessingException e) {
                    log.error(
                            "Failed to serialize user, {} will not be stubbed",
                            userUrl, e);
                }
            } catch (UnsupportedEncodingException e1) {
                log.error("{} not available on system", UTF_8, e1);
            }
        });

        // persist all stubs to mapping files
        WireMock.saveAllMappings();

        // Stop server.
        wireMockServer.stop();
    }
}
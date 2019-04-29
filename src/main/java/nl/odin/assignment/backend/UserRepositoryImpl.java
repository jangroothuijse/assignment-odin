package nl.odin.assignment.backend;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import nl.odin.assignment.User;
import nl.odin.assignment.UserRepository;
import nl.odin.assignment.User.FirstName;
import nl.odin.assignment.User.Surname;

@Component
@Slf4j
public final class UserRepositoryImpl implements UserRepository {
    private static final String EXTERNAL_URL_PROPERTY =
            "${nl.odin.assignment.repository.upstreamExternalUrl}";
    private static final String URL_PROPERTY =
            "${nl.odin.assignment.repository.upstreamBaseUrl}";
    final RestTemplate restTemplate = new RestTemplate();
    final String upstreamBaseUrl;
    final String upstreamExternalUrl;

    /**
     * @param upstreamBaseUrl URL including protocol and port number, but
     *            without a trailing slash. So for example http://localhost:8080
     */
    UserRepositoryImpl(//
            final @Value(URL_PROPERTY) String upstreamBaseUrl, //
            final @Value(EXTERNAL_URL_PROPERTY) String upstreamExternalUrl) {
        this.upstreamBaseUrl = upstreamBaseUrl;
        this.upstreamExternalUrl = upstreamExternalUrl;
    }

    @Override
    public List<User> findAll() {
        final ParameterizedTypeReference<List<User>> type;
        type = new ParameterizedTypeReference<List<User>>() {
        };

        final String url = String.format("%s/users", upstreamBaseUrl);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, null, type)//
                    .getBody() //
                    .stream() //
                    .map(this::makeUrlAbsolute) //
                    .collect(Collectors.toList());
        } catch (RestClientException e) {
            log.error("Unable to contact backend at {}", url, e);
            throw new BackendException("Unable to contact backend", e);
        }
    }

    private User makeUrlAbsolute(User user) {
        return user.withPictureFileName(
                upstreamExternalUrl + '/' + user.getPictureFileName());
    }

    @Override
    public Optional<User> findByName(//
            final FirstName firstName, //
            final Surname surname) {

        final String url = String.format(//
                "%s/users/%s/%s", //
                upstreamBaseUrl, //
                firstName.getValue(), //
                surname.getValue());
        log.info("URL: {}", url);

        try {
            return Optional.of(//
                    restTemplate.getForEntity(url, User.class).getBody()) //
                    .map(this::makeUrlAbsolute);
        } catch (HttpClientErrorException ex) {
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Unable to contact backend at {}", url, e);
            throw new BackendException("Unable to contact backend", e);
        }
    }

}

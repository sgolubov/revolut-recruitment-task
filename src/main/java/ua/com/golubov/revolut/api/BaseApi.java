package ua.com.golubov.revolut.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.com.golubov.revolut.exception.BadRequestException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.StringJoiner;

public class BaseApi {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseApi.class);

    protected final ObjectMapper objectMapper;
    protected final Validator validator;

    protected BaseApi(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    protected Long getId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException e) {
            LOG.warn("Id in the path is not numeric - {}.", id);
            throw new BadRequestException(e);
        }
    }

    protected <T> T convertAndValidate(String requestBody, Class<T> requestType) {
        try {
            T object = objectMapper.readValue(requestBody, requestType);
            Set<ConstraintViolation<T>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                StringJoiner errorJoiner = new StringJoiner("/:/", "Errors: ", "\n");
                for (ConstraintViolation<T> violation : violations) {
                    errorJoiner.add(violation.getMessage());
                }
                String errors = errorJoiner.toString();
                LOG.warn("Following errors were found in the request {}", errors);
                throw new BadRequestException(errors);
            }

            return object;
        } catch (JsonProcessingException e) {
            LOG.warn("Problem with parsing request body occurred.", e);
            throw new BadRequestException(e);
        }
    }

}

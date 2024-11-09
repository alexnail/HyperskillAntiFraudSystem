package antifraud.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity handleRuntimeException(RuntimeException e) {
        if (e.getMessage().equals("Username already exists")
                || e.getMessage().equals("Role already assigned")
                || e.getMessage().equals("IP already exists")
                || e.getMessage().equals("Card already exists")
                || e.getMessage().equals("Feedback already exists")) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }

        log.debug(e.getMessage(), e);
        return new ResponseEntity(HttpStatus.BAD_REQUEST); //default
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity handleBindException(BindException e) {
        log.debug(e.getMessage(), e);
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({IpNotFoundException.class, UsernameNotFoundException.class,
            StolenCardNotFound.class, TransactionNotFoundException.class})
    public ResponseEntity handleIpNotFoundException(RuntimeException e) {
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FeedbackEqualToResultException.class)
    public ResponseEntity handleFeedbackEqualToResultException(FeedbackEqualToResultException e) {
        return new ResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}

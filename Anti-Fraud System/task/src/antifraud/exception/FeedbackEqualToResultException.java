package antifraud.exception;

public class FeedbackEqualToResultException extends RuntimeException {

    public FeedbackEqualToResultException() {
        super("The feedback should be different to the transaction result");
    }
}

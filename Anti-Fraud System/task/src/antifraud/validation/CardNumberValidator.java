package antifraud.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CardNumberValidator implements ConstraintValidator<ValidCardNumber, String> {

    @Override
    public boolean isValid(String number, ConstraintValidatorContext context) {
        return checkWithLuhnAlgorithm(number);
    }

    private boolean checkWithLuhnAlgorithm(String number) {
        //String IIN = number.substring(0, 6); //Issuer Identification Number
        //String MII = number.substring(0, 1); //Major Industry Identifier
        //String customerAccountNumber = number.substring(6, number.length() - 1);
        //String checkDigit = number.substring(number.length() - 1);

        // Convert the combined string (IIN, customerAccountNumber, and checkDigit) to a char array
        //String combinedNumber = IIN + customerAccountNumber + checkDigit;
        char[] digits = number.toCharArray();

        int sum = 0;
        boolean isAlternate = false;

        // Start from the rightmost digit
        for (int i = digits.length - 1; i >= 0; i--) {
            int n = Integer.parseInt(String.valueOf(digits[i]));

            if (isAlternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }

            sum += n;
            isAlternate = !isAlternate;
        }

        return (sum % 10 == 0);
    }
}

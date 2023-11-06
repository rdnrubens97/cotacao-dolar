package shx.cotacaodolar.infra;

public class InvalidDateException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "As datas inseridas devem ser válidas, estando no formato 'MM-dd-yyyy'. Verifique, também, se a data de início é anterior à data final.";

    public InvalidDateException() {
        super(DEFAULT_MESSAGE);
    }
}

package shx.cotacaodolar.infra;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shx.cotacaodolar.dto.ExceptionDto;

import javax.persistence.EntityNotFoundException;

@RestControllerAdvice
public class ExceptionHandler {

    /**
     * Trata exceções do tipo MoedaException e retorna uma resposta HTTP com status 500 (Erro Interno do Servidor).
     *
     * @param exception Exceção do tipo MoedaException a ser tratada.
     * @return Uma resposta HTTP com status 500 contendo um objeto ExceptionDto com a mensagem de erro.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(MoedaException.class)
    public ResponseEntity<ExceptionDto> handleMoedaException(MoedaException exception) {
        ExceptionDto exceptionDto = new ExceptionDto(exception.getMessage(), "500");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionDto);
    }

    /**
     * Trata exceções do tipo EntityNotFoundException e retorna uma resposta HTTP com status 404 (Recurso Não Encontrado).
     *
     * @param exception Exceção do tipo EntityNotFoundException a ser tratada.
     * @return Uma resposta HTTP com status 404 contendo um objeto ExceptionDto com a mensagem de erro.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionDto> handleEntityNotFound(EntityNotFoundException exception) {
        ExceptionDto exceptionDto = new ExceptionDto(exception.getMessage(), "404");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionDto);
    }

    /**
     * Trata exceções do tipo InvalidDateException e retorna uma resposta HTTP com status 400 (Solicitação Inválida).
     *
     * @param exception Exceção do tipo InvalidDateException a ser tratada.
     * @return Uma resposta HTTP com status 400 contendo um objeto ExceptionDto com a mensagem de erro.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ExceptionDto> handleInvalidDate(InvalidDateException exception) {
        ExceptionDto exceptionDto = new ExceptionDto(exception.getMessage(), "400");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionDto);
    }

}

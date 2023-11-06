package shx.cotacaodolar.dto;

/**
 * Classe que representa uma exceção a ser retornada para o usuário de forma mais "amigável"
 *
 * @param message
 * @param statusCode
 */
public record ExceptionDto(String message, String statusCode) {
}

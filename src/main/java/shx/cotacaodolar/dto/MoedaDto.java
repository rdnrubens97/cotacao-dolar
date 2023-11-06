package shx.cotacaodolar.dto;

import java.math.BigDecimal;

public record MoedaDto(BigDecimal preco, String data, String hora) {
}

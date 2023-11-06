package shx.cotacaodolar.util;

import org.springframework.stereotype.Service;
import shx.cotacaodolar.dto.MoedaDto;
import shx.cotacaodolar.model.Moeda;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class MoedaMapper {

    /**
     * Método responsável por converter uma MoedaDto para Moeda
     *
     * @param moedaDto
     * @return Moeda
     */
    public Moeda mapearDeMoedaDtoParaMoeda(MoedaDto moedaDto) throws ParseException {
        Moeda moeda = new Moeda();
        moeda.setPreco(moedaDto.preco());

        // Converter a data de string para um objeto Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date dataConvertida = dateFormat.parse(moedaDto.data());
        moeda.setData(dataConvertida);

        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime horaConvertida = LocalTime.parse(moedaDto.hora(), horaFormatter);
        moeda.setHora(horaConvertida);

        return moeda;
    }

    /**
     * Método responsável por converter uma Moeda para MoedaDto
     *
     * @param moeda
     * @return MoedaDto
     */
    public MoedaDto mapearDeMoedaParaMoedaDto(Moeda moeda) {
        BigDecimal preco = moeda.getPreco();

        // Converter a data de Date para string
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dataConvertida = dateFormat.format(moeda.getData());

        // Converter a hora de LocalTime para string
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String horaConvertida = moeda.getHora().format(horaFormatter);

        MoedaDto moedaDto = new MoedaDto(preco, dataConvertida, horaConvertida);

        return moedaDto;
    }

}

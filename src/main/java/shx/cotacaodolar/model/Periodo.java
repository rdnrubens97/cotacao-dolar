package shx.cotacaodolar.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Periodo {
    @NotNull
    private Date dataInicial;
    @NotNull
    private Date dataFinal;
    private Long diasEntreAsDatas;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

    public Periodo(String dataInicial, String dataFinal) throws ParseException {
        this.dataInicial = new SimpleDateFormat("MM-dd-yyyy").parse(dataInicial);
        this.dataFinal = new SimpleDateFormat("MM-dd-yyyy").parse(dataFinal);
        diasEntreAsDatas = this.dataFinal.getTime() - this.dataInicial.getTime();
    }

    public String getDiasEntreAsDatasMaisUm() {
        Long resp = diasEntreAsDatas;
        resp = TimeUnit.DAYS.convert(resp, TimeUnit.MILLISECONDS) + 1;
        return resp.toString();
    }

    public String getDataInicial() {
        if (this.dataInicial != null) {
            return this.dateFormat.format(this.dataInicial);
        }
        return null;
    }

    public String getDataFinal() {
        if (this.dataFinal != null) {
            return this.dateFormat.format(this.dataFinal);
        }
        return null;
    }

}

package shx.cotacaodolar.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "tb_cotacao_dolar", uniqueConstraints = @UniqueConstraint(columnNames = {"data"}))
public class Moeda implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;
    @NotNull(message = "O preço não pode ser nulo")
    @Min(value = 0, message = "O preço não pode ser negativo")
    @Column(name = "cotacao")
    public BigDecimal preco;

    @NotNull(message = "A data não pode ser nula")
    @Temporal(TemporalType.DATE) // Configura o formato da data no banco de dados
    @DateTimeFormat(pattern = "dd/MM/yyyy") // Formato da data ao receber entrada do usuário
    @Column(name = "data")
    public Date data;

    @NotNull(message = "A hora não pode ser nula")
    @Column(name = "hora")
    public LocalTime hora;

    public String toString() {
        return "\n- - - - - - -" +
                "\nPreço: " + this.preco +
                "\nData: " + this.data +
                "\nHora: " + this.hora;
    }

}
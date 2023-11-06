package shx.cotacaodolar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shx.cotacaodolar.model.Moeda;

import java.util.Date;
import java.util.Optional;

@Repository
public interface MoedaRepository extends JpaRepository<Moeda, Long> {
    Optional<Moeda> findByData(Date data);
}

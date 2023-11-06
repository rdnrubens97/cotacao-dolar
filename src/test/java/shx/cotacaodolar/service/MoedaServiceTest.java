package shx.cotacaodolar.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import shx.cotacaodolar.dto.MoedaDto;
import shx.cotacaodolar.infra.InvalidDateException;
import shx.cotacaodolar.model.Moeda;
import shx.cotacaodolar.repository.MoedaRepository;
import shx.cotacaodolar.util.MoedaMapper;

import javax.persistence.EntityNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class MoedaServiceTest {
    private MoedaService moedaService;
    private MoedaRepository moedaRepository;
    private MoedaMapper mapper;

    @BeforeEach
    private void setUp() {
        moedaRepository = Mockito.mock(MoedaRepository.class);
        mapper = Mockito.mock(MoedaMapper.class);
        this.moedaService = new MoedaService(moedaRepository, mapper);
    }

    @Test
    public void deveRetornarCotacaoAtual() {
        MoedaDto moedaDto = moedaService.getCotacaoAtual();
        Assertions.assertNotNull(moedaDto);
    }

    @ParameterizedTest
    @MethodSource(value = "dataProviderValidsDates")
    public void getCotacoesPeriodoDeveRetornarUmaListaMoedaDtoParaDatasValidas(String startDate, String endDate) {
        List<MoedaDto> moedaDtoList = moedaService.getCotacoesPeriodo(startDate, endDate);
        if (!moedaDtoList.isEmpty()) {
            Assertions.assertAll("Verificar cada MoedaDto da lista",
                    () -> Assertions.assertTrue(moedaDtoList.stream().allMatch(Objects::nonNull), "Nenhuma MoedaDto pode ser nula")
            );
        }
    }

    @ParameterizedTest
    @MethodSource(value = "dataProviderInvalidsDates")
    public void getCotacoesPeriodoDeveLancarExcecaoParaDatasInvalidas(String startDate, String endDate) {
        Assertions.assertThrows(InvalidDateException.class, () -> moedaService.getCotacoesPeriodo(startDate, endDate));
    }

    @ParameterizedTest
    @MethodSource(value = "dataProviderValidsDates")
    public void getCotacoesMenoresAtualDeveRetornarUmaListaMoedaDtoParaDatasValidas(String startDate, String endDate) {
        List<MoedaDto> moedaDtoList = moedaService.getCotacoesMenoresAtual(startDate, endDate);
        Assertions.assertNotNull(moedaDtoList);
        if (!moedaDtoList.isEmpty()) {
            Assertions.assertAll("Verificar cada MoedaDto da lista",
                    () -> Assertions.assertTrue(moedaDtoList.stream().allMatch(Objects::nonNull), "Nenhuma MoedaDto pode ser nula")
            );
        }
    }

    @ParameterizedTest
    @MethodSource(value = "dataProviderInvalidsDates")
    public void getCotacoesMenoresAtualDeveLancarExcecaoParaDatasInvalidas(String startDate, String endDate) {
        Assertions.assertThrows(InvalidDateException.class, () -> moedaService.getCotacoesMenoresAtual(startDate, endDate));
    }

    @ParameterizedTest
    @MethodSource(value = "dataProviderValidsDates")
    public void getCotacoesMaioresAtualDeveRetornarUmaListaMoedaDtoParaDatasValidas(String startDate, String endDate) {
        List<MoedaDto> moedaDtoList = moedaService.getCotacoesMaioresAtual(startDate, endDate);
        Assertions.assertNotNull(moedaDtoList);
        if (!moedaDtoList.isEmpty()) {
            Assertions.assertAll("Verificar cada MoedaDto da lista",
                    () -> Assertions.assertTrue(moedaDtoList.stream().allMatch(Objects::nonNull), "Nenhuma MoedaDto pode ser nula")
            );
        }
    }

    @ParameterizedTest
    @MethodSource(value = "dataProviderInvalidsDates")
    public void getCotacoesMaioresAtualDeveLancarExcecaoParaDatasInvalidas(String startDate, String endDate) {
        Assertions.assertThrows(InvalidDateException.class, () -> moedaService.getCotacoesMaioresAtual(startDate, endDate));
    }

    @ParameterizedTest
    @MethodSource(value = "dataProviderValidsDates")
    public void salvarNoBdCotacoesDeDolarPorPeriodoDeveSalvarComSucessoParaDatasValidas(String startDate, String endDate) {
        String message = moedaService.salvarNoBdCotacoesDeDolarPorPeriodo(startDate, endDate);
        Assertions.assertEquals("Cotações salvas com sucesso", message);
        Mockito.verify(moedaRepository, Mockito.atLeastOnce()).save(Mockito.any());
    }

    @ParameterizedTest
    @MethodSource(value = "dataProviderInvalidsDates")
    public void salvarNoBdCotacoesDeDolarPorPeriodoDeveLancarExcecaoParaDatasInvalidas(String startDate, String endDate) {
        Assertions.assertThrows(InvalidDateException.class, () -> moedaService.salvarNoBdCotacoesDeDolarPorPeriodo(startDate, endDate));
    }

    @ParameterizedTest
    @MethodSource(value = "dataProviderValiDate")
    public void retornaCotacaoJaSalvaPorDataDeveRetornarMoedaDtoParaDataValida(String date) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date dataFormatada = inputFormat.parse(date);
        Mockito.when(moedaRepository.findByData(dataFormatada)).thenReturn(Optional.of(new Moeda()));
        moedaService.retornaCotacaoJaSalvaPorData(date);
        Mockito.verify(moedaRepository).findByData(dataFormatada);
        Optional<Moeda> optionalMoeda = moedaRepository.findByData(dataFormatada);
        Assertions.assertTrue(optionalMoeda.isPresent());
    }

    @ParameterizedTest
    @MethodSource(value = "dataProviderValiDate")
    public void retornaCotacaoJaSalvaPorDataDeveRetornarEntityNotFoundExceptionParaCotacaoNaoEncontrada(String date) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date dataFormatada = inputFormat.parse(date);
        Mockito.when(moedaRepository.findByData(dataFormatada)).thenReturn(Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            moedaService.retornaCotacaoJaSalvaPorData(date);
        });
        Mockito.verify(moedaRepository).findByData(dataFormatada);
    }

    @ParameterizedTest
    @MethodSource(value = "dataProviderInvalidDate")
    public void retornaCotacaoJaSalvaPorDataDeveLancarExcecaoParaDataInvalida(String date) {
        Assertions.assertThrows(InvalidDateException.class, () -> moedaService.retornaCotacaoJaSalvaPorData(date));
    }

    // métodos auxiliares

    private static Stream<Arguments> dataProviderValidsDates() {
        return Stream.of(
                Arguments.of("12-01-2020", "11-01-2023"),
                Arguments.of("02-08-2023", "10-20-2023"),
                Arguments.of("12-01-2010", "12-01-2020"),
                Arguments.of("01-01-2023", "05-01-2023"),
                Arguments.of("07-29-2023", "09-15-2023")
        );
    }

    private static Stream<Arguments> dataProviderInvalidsDates() {
        return Stream.of(
                Arguments.of("13-01-2023", "12-01-2023"), // Mês inválido no primeiro argumento
                Arguments.of("02-08-2023", "02-32-2023"), // Dia inválido no segundo argumento
                Arguments.of("12-01-23", "12-01-2023"),   // Ano com formato inválido no primeiro argumento
                Arguments.of("12/01/2023", "12-01-2023"), // Formato inválido no primeiro argumento
                Arguments.of("05-29-2023", "05-29-2022")  // Data inicial é posterior à data final
        );
    }

    private static Stream<Arguments> dataProviderValiDate() {
        return Stream.of(
                Arguments.of("12-01-2020"),
                Arguments.of("02-08-2023"),
                Arguments.of("12-01-2010"),
                Arguments.of("01-01-2023"),
                Arguments.of("07-29-2023")
        );
    }

    private static Stream<Arguments> dataProviderInvalidDate() {
        return Stream.of(
                Arguments.of("13-01-2023"), // Mês inválido
                Arguments.of("02-32-2023"), // Dia inválido
                Arguments.of("12-01-23"),   // Ano com formato inválido
                Arguments.of("12/01/2023"), // Formato inválido
                Arguments.of("5-29-2023")  // Mês inválido
        );
    }


}

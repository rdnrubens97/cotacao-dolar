package shx.cotacaodolar.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import shx.cotacaodolar.dto.MoedaDto;
import shx.cotacaodolar.infra.InvalidDateException;
import shx.cotacaodolar.infra.MoedaException;
import shx.cotacaodolar.model.Moeda;
import shx.cotacaodolar.model.Periodo;
import shx.cotacaodolar.repository.MoedaRepository;
import shx.cotacaodolar.util.MoedaMapper;

import javax.persistence.EntityNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class MoedaService {
    private MoedaRepository moedaRepository;
    private MoedaMapper mapper;

    public MoedaService(MoedaRepository moedaRepository, MoedaMapper mapper) {
        this.moedaRepository = moedaRepository;
        this.mapper = mapper;
    }


    /**
     * Obtém a cotação atual do dólar em relação ao Real (BRL) a partir da API do Banco Central do Brasil.
     * A cotação geralmente é disponibilizada pela API em torno de 13h, caso tentemos realizar esse método em algum horário em que a
     * cotação do dia não esteja disponível, será retornado a última cotação disponibilizada.
     * Considera-se cotação atual como a última cotação disponível na API do BCB.
     *
     * @return MoedaDto com a última cotação do dólar disponibilizada pela API do BCB.
     */
    public MoedaDto getCotacaoAtual() {
        try {
            // Obtém a data atual.
            LocalDate hoje = LocalDate.now();

            // Formata a data no formato desejado.
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
            String hojeFormatado = hoje.format(formatter);

            // booleano que inicializa como false e assume o valor true assim que a primeira cotação válida for encontrada
            boolean achouCotacaoValida = false;
            // variável de controle para o bloco 'while', onde decrementamos um dia à partir da data atual até encontrarmos uma cotação disponível
            int diasARetirar = 1;
            JsonArray cotacoesArray = new JsonArray();

            // Loop para tentar obter a cotação válida.
            while (!achouCotacaoValida) {

                // Constrói a URL da API com a data atual formatada.
                String urlString = "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia(dataCotacao=@dataCotacao)?%40dataCotacao='"
                        + hojeFormatado + "'&%24format=json";

                // Cria uma conexão HTTP para a URL.
                URL url = new URL(urlString);
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.connect();

                // Parseia a resposta da API.
                JsonElement response = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
                JsonObject rootObj = response.getAsJsonObject();
                cotacoesArray = rootObj.getAsJsonArray("value");

                // Verifica se a resposta da API está vazia.
                if (cotacoesArray.isEmpty()) {
                    // Se estiver vazia, tenta obter a cotação do dia anterior ajustando a data.
                    LocalDate dataAjustada = hoje.minus(diasARetirar, ChronoUnit.DAYS);
                    hojeFormatado = dataAjustada.format(formatter);
                    diasARetirar++;
                } else {
                    // Caso encontre uma cotação, saímos do 'while' setando a variável booleana como true
                    achouCotacaoValida = true;
                }
            }

            List<MoedaDto> moedasLista = new ArrayList<MoedaDto>();

            // Itera sobre os objetos JSON e os converte para objetos MoedaDto.
            for (JsonElement obj : cotacoesArray) {
                Date data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getAsJsonObject().get("dataHoraCotacao").getAsString());
                BigDecimal preco = obj.getAsJsonObject().get("cotacaoCompra").getAsBigDecimal();

                String dataRecebida = new SimpleDateFormat("dd/MM/yyyy").format(data);
                String hora = new SimpleDateFormat("HH:mm:ss").format(data);

                MoedaDto moedaRef = new MoedaDto(preco, dataRecebida, hora);
                moedasLista.add(moedaRef);
            }

            // Retorna a cotação encontrada. Pegamos o índíce zero pois, neste caso, o array recebido deve ter apenas um registro.
            return moedasLista.get(0);
        } catch (Exception e) {
            throw new MoedaException("Algum erro inesperado aconteceu: " + e.getMessage());
        }
    }

    /**
     * Obtém as cotações do dólar em relação ao Real (BRL) para um período especificado, buscando as informações à partir da API do Banco Central do Brasil.
     *
     * @param startDate A data de início do período no formato "MM-dd-yyyy".
     * @param endDate   A data de término do período no formato "MM-dd-yyyy".
     * @return Uma lista de objetos MoedaDto contendo as cotações para o período especificado.
     * @throws MoedaException Se ocorrer um erro ao obter as cotações do período.
     */
    public List<MoedaDto> getCotacoesPeriodo(String startDate, String endDate) {
        try {
            boolean verificaDatas = verificaDatas(startDate, endDate);
            if (!verificaDatas) throw new InvalidDateException();

            // Cria uma instância do objeto Período
            Periodo periodo = new Periodo(startDate, endDate);

            // Constrói a URL para a consulta das cotações no período especificado
            String urlString = "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarPeriodo(dataInicial=@dataInicial,dataFinalCotacao=@dataFinalCotacao)?%40dataInicial='"
                    + periodo.getDataInicial() + "'&%40dataFinalCotacao='" + periodo.getDataFinal() + "'&%24format=json&%24skip=0&%24top=" + periodo.getDiasEntreAsDatasMaisUm();

            // Cria uma URL a partir da string
            URL url = new URL(urlString);

            // Estabelece uma conexão HTTP
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            // Faz a análise do JSON de resposta
            JsonElement response = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
            JsonObject rootObj = response.getAsJsonObject();
            JsonArray cotacoesArray = rootObj.getAsJsonArray("value");

            List<MoedaDto> moedasLista = new ArrayList<MoedaDto>();

            for (JsonElement obj : cotacoesArray) {
                // Converte a data e hora da cotação
                Date data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getAsJsonObject().get("dataHoraCotacao").getAsString());
                BigDecimal preco = obj.getAsJsonObject().get("cotacaoCompra").getAsBigDecimal();

                // Formata a data para o formato dd/MM/yyyy
                String dataRecebida = new SimpleDateFormat("dd/MM/yyyy").format(data);
                // Formata a hora para o formato HH:mm:ss
                String hora = new SimpleDateFormat("HH:mm:ss").format(data);

                // Cria um objeto MoedaDto e adiciona à lista
                MoedaDto moedaRef = new MoedaDto(preco, dataRecebida, hora);
                moedasLista.add(moedaRef);
            }

            // Retorna a lista de cotações para o período especificado
            return moedasLista;
        } catch (InvalidDateException e) {
            throw new InvalidDateException();
        } catch (Exception e) {
            throw new MoedaException("Algum erro inesperado aconteceu: " + e.getMessage());
        }
    }

    /**
     * Obtém as cotações do dólar em relação ao Real (BRL) para um período especificado, considerando apenas as cotações menores do que a cotação atual.
     *
     * @param startDate A data de início do período no formato "MM-dd-yyyy".
     * @param endDate   A data de término do período no formato "MM-dd-yyyy".
     * @return Uma lista de objetos MoedaDto contendo as cotações menores que a cotação atual no período especificado.
     * @throws MoedaException Se ocorrer um erro ao obter as cotações do período.
     */
    public List<MoedaDto> getCotacoesMenoresAtual(String startDate, String endDate) {
        try {
            // Obtém a cotação atual do dólar
            MoedaDto moedaHoje = getCotacaoAtual();

            boolean verificaDatas = verificaDatas(startDate, endDate);
            if (!verificaDatas) throw new InvalidDateException();

            BigDecimal cotacaoHoje = moedaHoje.preco();

            // Cria uma instância do objeto Período
            Periodo periodo = new Periodo(startDate, endDate);

            // Constrói a URL para a consulta das cotações no período especificado
            String urlString = "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarPeriodo(dataInicial=@dataInicial,dataFinalCotacao=@dataFinalCotacao)?%40dataInicial='"
                    + periodo.getDataInicial() + "'&%40dataFinalCotacao='" + periodo.getDataFinal() + "'&%24format=json&%24skip=0&%24top=" + periodo.getDiasEntreAsDatasMaisUm();

            // Cria uma URL a partir da string
            URL url = new URL(urlString);

            // Estabelece uma conexão HTTP
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            // Faz a análise do JSON de resposta
            JsonElement response = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
            JsonObject rootObj = response.getAsJsonObject();
            JsonArray cotacoesArray = rootObj.getAsJsonArray("value");

            List<MoedaDto> moedasLista = new ArrayList<MoedaDto>();

            for (JsonElement obj : cotacoesArray) {
                // Converte a data e hora da cotação
                Date data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getAsJsonObject().get("dataHoraCotacao").getAsString());
                BigDecimal preco = obj.getAsJsonObject().get("cotacaoCompra").getAsBigDecimal();

                // Formata a data para o formato dd/MM/yyyy
                String dataRecebida = new SimpleDateFormat("dd/MM/yyyy").format(data);
                // Formata a hora para o formato HH:mm:ss
                String hora = new SimpleDateFormat("HH:mm:ss").format(data);

                // Cria um objeto MoedaDto e verifica se a cotação é menor que a cotação atual
                MoedaDto moedaRef = new MoedaDto(preco, dataRecebida, hora);
                BigDecimal cotacaoRef = moedaRef.preco();

                if (cotacaoHoje.compareTo(cotacaoRef) == 1) {
                    moedasLista.add(moedaRef);
                }
            }

            // Retorna a lista de cotações menores que a cotação atual para o período especificado
            return moedasLista;
        } catch (InvalidDateException e) {
            throw new InvalidDateException();
        } catch (Exception e) {
            throw new MoedaException("Algum erro inesperado aconteceu: " + e.getMessage());
        }
    }

    /**
     * Obtém as cotações do dólar em relação ao Real (BRL) para um período especificado, considerando apenas as cotações maiores do que a cotação atual.
     *
     * @param startDate A data de início do período no formato "MM-dd-yyyy".
     * @param endDate   A data de término do período no formato "MM-dd-yyyy".
     * @return Uma lista de objetos MoedaDto contendo as cotações maiores que a cotação atual no período especificado.
     * @throws MoedaException Se ocorrer um erro ao obter as cotações do período.
     */
    public List<MoedaDto> getCotacoesMaioresAtual(String startDate, String endDate) {
        try {
            boolean verificaDatas = verificaDatas(startDate, endDate);
            if (!verificaDatas) throw new InvalidDateException();

            // Obtém a cotação atual do dólar
            MoedaDto moedaHoje = getCotacaoAtual();
            BigDecimal cotacaoHoje = moedaHoje.preco();

            // Cria uma instância do objeto Período
            Periodo periodo = new Periodo(startDate, endDate);

            // Constrói a URL para a consulta das cotações no período especificado
            String urlString = "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarPeriodo(dataInicial=@dataInicial,dataFinalCotacao=@dataFinalCotacao)?%40dataInicial='"
                    + periodo.getDataInicial() + "'&%40dataFinalCotacao='" + periodo.getDataFinal() + "'&%24format=json&%24skip=0&%24top=" + periodo.getDiasEntreAsDatasMaisUm();

            // Cria uma URL a partir da string
            URL url = new URL(urlString);

            // Estabelece uma conexão HTTP
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            // Faz a análise do JSON de resposta
            JsonElement response = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
            JsonObject rootObj = response.getAsJsonObject();
            JsonArray cotacoesArray = rootObj.getAsJsonArray("value");

            List<MoedaDto> moedasLista = new ArrayList<MoedaDto>();

            for (JsonElement obj : cotacoesArray) {
                // Converte a data e hora da cotação
                Date data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getAsJsonObject().get("dataHoraCotacao").getAsString());
                BigDecimal preco = obj.getAsJsonObject().get("cotacaoCompra").getAsBigDecimal();

                // Formata a data para o formato dd/MM/yyyy
                String dataRecebida = new SimpleDateFormat("dd/MM/yyyy").format(data);
                // Formata a hora para o formato HH:mm:ss
                String hora = new SimpleDateFormat("HH:mm:ss").format(data);

                // Cria um objeto MoedaDto e verifica se a cotação é maior que a cotação atual
                MoedaDto moedaRef = new MoedaDto(preco, dataRecebida, hora);
                BigDecimal cotacaoRef = moedaRef.preco();

                if (cotacaoRef.compareTo(cotacaoHoje) == 1) {
                    moedasLista.add(moedaRef);
                }
            }

            // Retorna a lista de cotações maiores que a cotação atual para o período especificado
            return moedasLista;
        } catch (InvalidDateException e) {
            throw new InvalidDateException();
        } catch (Exception e) {
            throw new MoedaException("Algum erro inesperado aconteceu: " + e.getMessage());
        }
    }

    /**
     * Realiza a operação de buscar cotações de dólar em um período específico e salva no banco de dados.
     *
     * @param startDate Data de início do período.
     * @param endDate   Data de término do período.
     * @return Uma mensagem indicando o resultado da operação.
     */
    public String salvarNoBdCotacoesDeDolarPorPeriodo(String startDate, String endDate) {
        try {
            boolean verificaDatas = verificaDatas(startDate, endDate);
            if (!verificaDatas) throw new InvalidDateException();

            // Obtém a lista de MoedaDto a partir do método getCotacoesPeriodo.
            List<MoedaDto> listaMoedaDto = getCotacoesPeriodo(startDate, endDate);

            // Verifica se a lista não está vazia.
            if (!listaMoedaDto.isEmpty()) {
                // Itera sobre cada MoedaDto da lista.
                for (MoedaDto dto : listaMoedaDto) {
                    // Converte MoedaDto para a entidade Moeda.
                    Moeda moeda = mapper.mapearDeMoedaDtoParaMoeda(dto);

                    try {
                        // Tenta salvar a entidade no banco de dados.
                        moedaRepository.save(moeda);
                    } catch (DataIntegrityViolationException ex) {
                        // Em caso de violação de restrição única, ignora o registro duplicado.
                        continue;
                    }
                }
                // Retorna uma mensagem de sucesso.
                return "Cotações salvas com sucesso";
            }
            return "Não foram encontradas cotações para o período especificado";
        } catch (InvalidDateException e) {
            throw new InvalidDateException();
        } catch (Exception e) {
            throw new MoedaException("Algum erro inesperado aconteceu: " + e.getMessage());
        }
    }

    /**
     * Obtém a cotação de dólar já salva no banco de dados para a data especificada.
     *
     * @param data A data da cotação no formato "MM-dd-yyyy".
     * @return Um objeto MoedaDto contendo a cotação encontrada, data e hora da cotação.
     * @throws MoedaException Se a data não for encontrada no banco de dados ou se ocorrer um erro ao analisar a data.
     */
    public MoedaDto retornaCotacaoJaSalvaPorData(String data) {
        try {
            boolean dataValida = verificaData(data);
            if (!dataValida) throw new InvalidDateException();

            // Analisa a data fornecida no formato "MM-dd-yyyy".
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy");
            Date dataFormatada = inputFormat.parse(data);

            // Busca a cotação no banco de dados com base na data.
            Moeda moeda = moedaRepository.findByData(dataFormatada).orElseThrow(
                    EntityNotFoundException::new);

            // Use o Mapper para converter a entidade Moeda em MoedaDto.
            MoedaDto moedaDto = mapper.mapearDeMoedaParaMoedaDto(moeda);

            return moedaDto;
        } catch (InvalidDateException e) {
            throw new InvalidDateException();
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Não há registro de cotação no banco de dados para o dia '" + data + "'.");
        } catch (Exception e) {
            throw new MoedaException("Algum erro inesperado aconteceu: " + e.getMessage());
        }
    }


    // Métodos auxiliares

    /**
     * Verifica se a data fornecida em formato de string é válida no formato "MM-dd-yyyy".
     *
     * @param data A string que representa a data a ser verificada.
     * @return true se a data é válida, caso contrário, false.
     */
    private boolean verificaData(String data) {
        // Verifica se a string da data corresponde ao padrão "MM-dd-yyyy".
        if (data.matches("\\d{2}-\\d{2}-\\d{4}")) {
            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
            format.setLenient(false);
            try {
                // Tenta analisar a data. Se bem-sucedido, a data é válida.
                format.parse(data);
                return true;
            } catch (ParseException e) {
                // Em caso de erro na análise, a data é inválida.
                return false;
            }
        } else {
            // Se a string não corresponder ao padrão, a data é inválida.
            return false;
        }
    }

    /**
     * Verifica se as datas fornecidas em formato de string são válidas no formato "MM-dd-yyyy" e se a data de início é anterior a data fim.
     *
     * @param dataInicio
     * @param dataFim
     * @return
     */
    private boolean verificaDatas(String dataInicio, String dataFim) {
        if (dataInicio.matches("\\d{2}-\\d{2}-\\d{4}") && dataFim.matches("\\d{2}-\\d{2}-\\d{4}")) {
            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
            format.setLenient(false);
            try {
                Date inicio = format.parse(dataInicio);
                Date fim = format.parse(dataFim);

                // Verifique se a data de início é anterior à data de término
                return inicio.before(fim);
            } catch (ParseException e) {
                // Em caso de erro na análise, as datas são inválidas.
                return false;
            }
        } else {
            // Se as strings não corresponderem ao padrão, as datas são inválidas.
            return false;
        }
    }

}

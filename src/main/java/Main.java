import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

@SpringBootConfiguration
@EnableAutoConfiguration
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CSVManager csvManager() {
        return new CSVManager(Path.of("dados"));
    }

    @Bean
    Estacionamento estacionamento(CSVManager csvManager) throws IOException {
        Estacionamento estacionamento = csvManager.carregarTudo(new TabelaTarifas());
        csvManager.instalarSalvamentoAutomatico(estacionamento);
        return estacionamento;
    }

    @Bean
    VaadinServiceInitListener registrarRotaPrincipal() {
        return evento -> evento.getSource().getRouter().getRegistry()
                .setRoute("", InterfaceUsuario.class, Collections.emptyList());
    }
}

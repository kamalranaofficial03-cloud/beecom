package bee.basket.identityserver.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@ConditionalOnProperty(prefix = "app.datasource", name="provider", havingValue = "rd", matchIfMissing =true)
public class RDDataConfig {


}

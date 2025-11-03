package poly.store.config;

import org.apache.catalina.session.StandardManager;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatSessionConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addContextCustomizers(context -> {
            // Ensure Tomcat does not try to load persisted sessions (SESSIONS.ser)
            try {
                if (context.getManager() instanceof StandardManager) {
                    StandardManager manager = (StandardManager) context.getManager();
                    manager.setPathname(null); // disable persistence
                } else {
                    StandardManager manager = new StandardManager();
                    manager.setPathname(null);
                    context.setManager(manager);
                }
            } catch (Throwable ignored) {
                // Best effort: if anything goes wrong, don't block startup
            }
        });
    }
}


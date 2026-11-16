/*
 * WebConfig.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Spring MVC configuration.
 */
package ge.melodia.config;

import ge.melodia.service.FileStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation
        .ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.data.web.config.EnableSpringDataWebSupport
        .PageSerializationMode.VIA_DTO;

/**
 * Serves uploaded images under /media/images/** and makes REST
 * endpoints return pages in a stable JSON format.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorageService;

    public WebConfig(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = fileStorageService.getImageDirectory().toUri()
                .toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/media/images/**")
                .addResourceLocations(location);
    }
}

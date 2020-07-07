package net.rawburn.websocket;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

/**
 * @author renchao
 * @since v1.0
 * @see SpringApplicationBuilder
 */
@SpringBootApplication
public class App/* extends SpringBootServletInitializer*/ {

	// public static void main(String[] args) {
	// 	SpringApplication.run(App.class, args);
	// }

	public static void main(String[] args) {
		new SpringApplicationBuilder(App.class).bannerMode(Banner.Mode.OFF).run(args);
	}

	// @Override
	// protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
	// 	return application.sources(App.class);
	// }

}

package com.zuul.server.oauth;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
	
/**
 * Clase de configuracion para la gestion de acceso a recursos
 * @author Angel Ayuso
 */
@RefreshScope
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
	
	@Autowired
	private Environment env;
	
	
	/**
	 * Configuracion Recursos con tokenStorage
	 */
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.tokenStore(tokenStore());
	}

	/**
	 * Se protegen los endpoints
	 * 1. Se configura la ruta de Acceso publica
	 * 2. Todas las rutas GET publicas
	 * 3. Se configura la ruta para crear un Usuario publica
	 * 4. Las rutas no configuradas para los usuarios autentificados
	 */
	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/api/security/oauth/token").permitAll()	//Ruta para generar el Token para autentificarse, ruta configurada en Zuul
																						//Ruta publica para que todos puedan iniciar sesion
			.antMatchers(HttpMethod.GET, "/api/usuario/**").permitAll()					
			.antMatchers(HttpMethod.POST , "/api/usuario/crearUsuario").permitAll()
			.anyRequest().authenticated() 	//El resto que no se ha configurado para los que se han autentificado
			.and().cors().configurationSource(corsConfigurationSource());	//Configuracion CORS
		
	}
	
	/**
	 * Gestion CORS - Intercambio de recursos de origen cruzado
	 * Configuracion las aplicaciones clientes, su aplicacion, dominio y puerto
	 * @return
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.addAllowedOrigin("*");	//Permitido todos los origenes
		corsConfig.addAllowedMethod("*");	//Permitir todos los metodos
		corsConfig.setAllowCredentials(true); 
		corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
		
		//Configurar los endpoints - Se aplica a todas las rutas
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);
		
		return source;
	}
	
	/**
	 * Filtro para que se aplique de forma global el CORS
	 * @return
	 */
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter(){
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<CorsFilter>(new CorsFilter(corsConfigurationSource()));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE); //Prioridad Alta
		return bean;
	}
	
	/**
	 * Componente que se encarga de generar y guardar los datos del Token con el JwtTokenStore
	 * @return
	 */
	@Bean
	public JwtTokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}
	
	/**
	 * Codigo secreto para la firma del token
	 * @return
	 */
	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter tokenCorverter = new JwtAccessTokenConverter();
		tokenCorverter.setSigningKey(env.getProperty("config.security.oauth.jwt.key"));
		return tokenCorverter;
	}
}
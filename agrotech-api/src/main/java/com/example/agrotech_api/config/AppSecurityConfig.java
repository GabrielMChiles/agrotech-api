package com.example.agrotech_api.config;

import com.example.agrotech_api.exception.UnauthorizedException;
import com.example.agrotech_api.model.RoleUsuario;
import com.example.agrotech_api.model.Usuario;
import com.example.agrotech_api.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppSecurityConfig implements WebMvcConfigurer {

    public static final String ATRIBUTO_USUARIO_LOGADO = "usuarioLogado";

    private final UsuarioService usuarioService;

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String[] allowedOrigins;

    public AppSecurityConfig(@Lazy UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                            return true;
                        }

                        String authHeader = request.getHeader("Authorization");
                        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                            escreverErro(response, HttpStatus.UNAUTHORIZED, "Não Autorizado", "Sessão ausente. Faça login novamente.");
                            return false;
                        }

                        try {
                            String token = authHeader.substring(7);
                            Usuario usuarioLogado = usuarioService.validarToken(token);

                            // Bloqueio OWASP A01: Apenas ADMIN acessa /api/usuarios
                            String uri = request.getRequestURI();
                            String metodo = request.getMethod();
                            boolean escrita = !"GET".equalsIgnoreCase(metodo);
                            boolean areaAdmin = uri.startsWith("/api/usuarios")
                                    || (uri.startsWith("/api/funcionarios") && escrita)
                                    || "DELETE".equalsIgnoreCase(metodo)
                                    || (uri.startsWith("/api/vendas") && uri.endsWith("/cancelar"));
                            if (areaAdmin && usuarioLogado.getRole() != RoleUsuario.ADMIN) {
                                escreverErro(response, HttpStatus.FORBIDDEN, "Acesso Negado", "Esta operação requer perfil ADMIN.");
                                return false;
                            }

                            request.setAttribute(ATRIBUTO_USUARIO_LOGADO, usuarioLogado);
                            return true;
                        } catch (UnauthorizedException ex) {
                            escreverErro(response, HttpStatus.UNAUTHORIZED, "Não Autorizado", ex.getMessage());
                            return false;
                        }
                    }
                }).addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }

    private static void escreverErro(HttpServletResponse response, HttpStatus status, String titulo, String detalhe)
            throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        String json = "{\"status\":" + status.value()
                + ",\"title\":\"" + titulo + "\""
                + ",\"detail\":\"" + detalhe.replace("\"", "'") + "\"}";
        response.getWriter().write(json);
    }
}

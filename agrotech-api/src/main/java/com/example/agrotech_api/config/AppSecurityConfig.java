package com.example.agrotech_api.config;

import com.example.agrotech_api.exception.UnauthorizedException;
import com.example.agrotech_api.model.RoleUsuario;
import com.example.agrotech_api.model.Usuario;
import com.example.agrotech_api.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    private final UsuarioService usuarioService;

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
                .allowedOrigins("http://localhost:4200")
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
                            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token ausente ou malformado");
                            return false;
                        }

                        try {
                            String token = authHeader.substring(7);
                            Usuario usuarioLogado = usuarioService.validarToken(token);

                            // Bloqueio OWASP A01: Apenas ADMIN acessa /api/usuarios
                            if (request.getRequestURI().startsWith("/api/usuarios") && usuarioLogado.getRole() != RoleUsuario.ADMIN) {
                                response.sendError(HttpStatus.FORBIDDEN.value(), "Acesso negado: requer perfil ADMIN");
                                return false;
                            }

                            return true;
                        } catch (UnauthorizedException ex) {
                            response.sendError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
                            return false;
                        }
                    }
                }).addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }
}
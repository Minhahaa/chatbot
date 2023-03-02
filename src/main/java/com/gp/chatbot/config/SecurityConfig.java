package com.gp.chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	 public void configure(WebSecurity webSecurity) throws Exception {
		  webSecurity.ignoring().antMatchers("/resources/**", "/css/**", "/fonts/**", "/js/**", "/less/**", "/scss/**", "/images/**", "/webjars/**"); 
	}
	 
    @Override
    protected void configure(HttpSecurity http) throws Exception{
    	
	        CharacterEncodingFilter filter = new CharacterEncodingFilter();
	        filter.setEncoding("UTF-8");
	        filter.setForceEncoding(true);
	        http.addFilterBefore(filter,CsrfFilter.class);
	        // 동일 도메인에서 iframe 접근이 가능하도록 X-Frame-Options을 sameOrigin으로 설정
    		http.headers().frameOptions().sameOrigin();
    		http
    			.cors()
    				.disable()
    			.authorizeRequests()
    				// API 호출은 모두 허용 / 문제가 되는 내용은 해당 로직에서 검사
    				.antMatchers("/v1/**").permitAll()
    				.antMatchers("/api/**").permitAll()
    				.antMatchers("/admin/**").hasAuthority("M")
    					.anyRequest()
    					.permitAll()
    			.and()
    			.anonymous()
    			.principal("annonymouseUser")
    				.and()
    			.exceptionHandling()
    			.accessDeniedPage("/WEB-INF/views/denied.jsp")
    				.and()
    			.sessionManagement()
				.maximumSessions(-1) // 최대 허용 가능 세션 수, -1인 경우 무제한 세션 
    			.sessionRegistry(sessionRegistry());
    		
	http.csrf().disable();//csrf 미적용
	
    }
 
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
    
}
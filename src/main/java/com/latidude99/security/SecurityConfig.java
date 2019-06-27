/**Copyright (C) 2018-2019  Piotr Czapik.
 * @author Piotr Czapik
 *
 *  This file is part of EnquirySystem.
 *  EnquirySystem is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  EnquirySystem is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with EnquirySystem.  If not, see <http://www.gnu.org/licenses/>
 *  or write to: latidude99@gmail.com
 */

package com.latidude99.security;


import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

//@Profile("development")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	public DataSource dataSource;

	@Bean
	public UserDetailsService customUserDetailsService() {
		return new CustomUserDetailsService();
	}

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http
			.httpBasic()
			.and()
			.csrf()
//                .disable() // for RestAssured when login not required
//                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRepository(csrfTokenRepository())
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
			.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/", "/static/**", "/js/**", "/css/**", "/images/**", "/public/**").permitAll()
                .antMatchers(HttpMethod.POST, "/", "/static/**", "/js/**", "/css/**", "/images/**", "/public/**").permitAll()
                .antMatchers("/api/index*").permitAll()
                .antMatchers("/api/terms*").permitAll()
                .antMatchers("/api/user/forgot**").permitAll()
				.antMatchers("/api/user/activate**").permitAll()
				.antMatchers("/api/user/reset**").permitAll()
				.antMatchers("/api/enquiry/form**").permitAll()
                .antMatchers("/api/user/activate*").permitAll()
				.antMatchers("/api/user/passwordreset").hasAnyRole("ADMIN","USER","APPADMIN")
				.antMatchers("/api/enquiry/list").hasAnyRole("ADMIN","USER","APPADMIN")
				.antMatchers("/api/enquiry/page").hasAnyRole("ADMIN","USER","APPADMIN")
				.antMatchers("/api/enquiry/user").hasAnyRole("ADMIN","USER","APPADMIN")
				.antMatchers("/api/enquiry/admin").hasAnyRole("ADMIN","USER","APPADMIN")
				.anyRequest().authenticated()
			.and()
			.logout()
			;
	}

	 @Bean
		public PasswordEncoder passwordEncoder(){
			PasswordEncoder encoder = new BCryptPasswordEncoder();
			return encoder;
		}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(customUserDetailsService()).passwordEncoder(passwordEncoder());
	}

}
























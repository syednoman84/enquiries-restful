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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	public DataSource dataSource;

	@Bean
	public UserDetailsService customUserDetailsService() {
		return new CustomUserDetailsService();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http
			.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/", "/static/**", "/js/**", "/css/**", "/images/**", "/public/**").permitAll()
				.antMatchers("/user/forgot**").permitAll()
				.antMatchers("/user/activate**").permitAll()
				.antMatchers("/user/reset**").permitAll()
				.antMatchers("/user/resetForm**").permitAll()
				.antMatchers("/enquiry/form**").permitAll()
				.antMatchers("/enquiry/form/uploadfail**").permitAll()
				.antMatchers("/*").permitAll()
				.antMatchers("/confirm*").permitAll()
				.antMatchers("/forgot*").permitAll()
				.antMatchers("/reset*").permitAll()
				.antMatchers("/index*").permitAll()
				.antMatchers("/terms*").permitAll()
				.antMatchers("/user/passwordreset").hasAnyRole("ADMIN","USER","APPADMIN")
				.antMatchers("/enquiry/list").hasAnyRole("ADMIN","USER","APPADMIN")
				.antMatchers("/enquiry/page").hasAnyRole("ADMIN","USER","APPADMIN")
				.antMatchers("/enquiry/user").hasAnyRole("ADMIN","USER","APPADMIN")
				.antMatchers("/enquiry/admin").hasAnyRole("ADMIN","USER","APPADMIN")
				.anyRequest().authenticated()
			.and()
			.formLogin()
				.loginPage("/enquiryForm").loginProcessingUrl("/enquiry/login")	.permitAll()
				.defaultSuccessUrl("/enquiry/list", true)
				.failureUrl("/?error=true")
			.and()
				.rememberMe()
				.userDetailsService(customUserDetailsService())
				.tokenRepository(persistentTokenRepository())
				.rememberMeParameter("remember-me")
				.rememberMeCookieName("latidude99-remember-me")
				.tokenValiditySeconds(7*24*60*60)
			.and()
			.logout()
			.logoutUrl("/logout").logoutSuccessUrl("/").permitAll()
			;
	}

	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
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
























/**
 * Copyright (C) 2018-2019  Piotr Czapik.
 *
 * @author Piotr Czapik
 * <p>
 * This file is part of EnquirySystem.
 * EnquirySystem is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * EnquirySystem is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with EnquirySystem.  If not, see <http://www.gnu.org/licenses/>
 * or write to: latidude99@gmail.com
 */

package com.latidude99;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.latidude99.model.Role;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.UserService;
import com.latidude99.util.EnquiryListWrapper;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Autowired
    UserService userService;

    @Autowired
    EnquiryService enquiryService;


    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        userService.addDbUser(Role.APPADMIN); // adding users with roles defined in data.sql
        userService.addDbUser(Role.ADMIN);
        userService.addDbUser(Role.DEFAULT);

        enquiryService.addProgressUser(13L, 1L); //testing adding progress users
        enquiryService.addProgressUser(14L, 1L);

        userService.getAll().forEach(u -> userService.trimUserEmail(u));
    }

    @Bean
//	@Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
    public EnquiryListWrapper createEnquiryListWrapper() {
        return new EnquiryListWrapper();
    }
		
	
		
		
		
		
/*	
		System.err.println(enquiryService.getById(7L).getProgressUser().toString());
		System.err.println(enquiryService.getById(8L).getProgressUser().toString());
		Date date = new Date();
		System.out.println("date: " + date);
		Timestamp sqlTimestamp = new java.sql.Timestamp(new java.util.Date().getTime());
		System.out.println("sqlTimestamp: " + sqlTimestamp);
*/


}


/*
	@Bean
	   public LocaleResolver localeResolver() {
	       SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
	       sessionLocaleResolver.setDefaultLocale(Locale.US);
	       return sessionLocaleResolver;
	   }
	 
   @Bean
   public LocaleChangeInterceptor localeChangeInterceptor() {
       LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
       lci.setParamName("lang");
       return lci;
   }
	 
   @Override
   public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(localeChangeInterceptor());
   }
   
   @Bean //optional?
   public Java8TimeDialect java8TimeDialect() {
       return new Java8TimeDialect();
   }
   
   @Bean
   public FromView fromView() {
	   return new FromView();
   }
	

*/












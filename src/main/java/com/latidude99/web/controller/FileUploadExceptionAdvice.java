package com.latidude99.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class FileUploadExceptionAdvice {
	
	@ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(
    		MaxUploadSizeExceededException exc, HttpServletRequest request, HttpServletResponse response) {
          return "redirect:/enquiry/form/uploadfail";
    }

}

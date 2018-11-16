package com.latidude99.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.latidude99.model.Attachment;
import com.latidude99.repository.AttachmentRepository;

@Service
public class AttachmentService {
	private static final Logger logger = LoggerFactory.getLogger(AttachmentService.class);
	
	@Autowired
	AttachmentRepository attachmentRepository;
	
	public Attachment getById(long id) {
		return attachmentRepository.findById(id);
	};
	
}
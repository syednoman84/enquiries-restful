package com.latidude99.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.latidude99.model.Attachment;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long>{
	
	Attachment findById(long id);
	
		
	
	
}

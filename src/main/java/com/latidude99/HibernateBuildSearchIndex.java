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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.latidude99.service.EnquiryService;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Transactional
@Component
public class HibernateBuildSearchIndex implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(HibernateBuildSearchIndex.class);

    @Autowired
    private EntityManager entityManager;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        try {
            FullTextEntityManager fullTextEntityManager
                    = Search.getFullTextEntityManager(entityManager);
            fullTextEntityManager
                    .createIndexer()
                    .startAndWait();
        } catch (InterruptedException e) {
            logger.error("An error occurred when trying to build the search index: "
                    + e.toString());
        }
        return;
    }

}



















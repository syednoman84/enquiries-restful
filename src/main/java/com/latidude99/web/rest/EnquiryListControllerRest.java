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

package com.latidude99.web.rest;

import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.HibernateSearchService;
import com.latidude99.service.UserService;
import com.latidude99.util.*;
import net.bytebuddy.implementation.bind.annotation.Default;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api")
public class EnquiryListControllerRest {
    private static final Logger logger = LoggerFactory.getLogger(EnquiryListControllerRest.class);

    @Autowired
    UserService userService;

    @Autowired
    EnquiryService enquiryService;

    @Autowired
    EnquiryStatsRest enquiryStatsRest;

    @Autowired
    EnquiryListWrapperRest enquiryListWrapperRest;

    @Autowired
    private HibernateSearchService searchService;


    /*
     * Returns a list of 100 most recent enquiries, sorted, the latest first plus enquiries stats
     */
    @GetMapping(path = "/enquiry/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public EnquiryListWrapperRest enquiryList(Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        enquiryListWrapperRest.setEnquiryList(enquiryService.getRecent100Sorted());
        enquiryListWrapperRest.setWaiting(enquiryService.getNumByStatus("waiting"));
        enquiryListWrapperRest.setOpened(enquiryService.getNumByStatus("in progress"));
        enquiryListWrapperRest.setClosed(enquiryService.getNumByStatus("closed"));
        enquiryListWrapperRest.setAssignedToUser(
                enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress"));
        enquiryListWrapperRest.setClosedByUser(
                enquiryService.getNumByClosingUserAndStatus(currentUser, "closed"));
        return enquiryListWrapperRest;
    }

    /*
     *  Returns only statisctics about enquiries, w/o enquiry list
     */
    @GetMapping(path = "/enquiry/list/stats",  produces = MediaType.APPLICATION_JSON_VALUE)
    public EnquiryStatsRest enquiryListStats(Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        enquiryStatsRest.setWaiting(enquiryService.getNumByStatus("waiting"));
        enquiryStatsRest.setOpened(enquiryService.getNumByStatus("in progress"));
        enquiryStatsRest.setClosed(enquiryService.getNumByStatus("closed"));
        enquiryStatsRest.setAssignedToUser(
                enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress"));
        enquiryStatsRest.setClosedByUser(
                enquiryService.getNumByClosingUserAndStatus(currentUser, "closed"));
        return enquiryStatsRest;
    }


    /*
     * Returns user defined custom number of enquiries
     */
    @GetMapping(path = "/enquiry/list/last/{number}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Enquiry> enquiryListLast(@PathVariable int number) {
        List<Enquiry> enquiries = enquiryService.getLastUserDefined(0, number);
        return enquiries;
    }


    @PostMapping(path = "/enquiry/search/ids",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Enquiry> enquiryLoadById(@RequestParam String ids) {
        List<Enquiry> enquiries =
                enquiryService.getUserDefinedIdsProgressUsersSorted(ids);
        return enquiries;
    }

    /*
     * Returns enquires based on their status
     */
    @GetMapping(path = "/enquiry/list/{fetchBy}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Enquiry> fetchBy(@PathVariable String fetchBy) {
        List<Enquiry> enquiriesFetchedBy = enquiryService.getByStatus(fetchBy);
        return enquiriesFetchedBy;
    }

    /*
     * Returns enquires based on user relation to it and their status
     */
    @GetMapping(path = "/enquiry/list/user/{fetchBy}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Enquiry>  fetchByOpenedAndUserAssigned(@PathVariable String fetchBy, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        List<Enquiry> enquiries;
        switch(fetchBy){
            case "assigned": // assigned to the user and opened
                enquiries = enquiryService.getByStatusAndUser("in progress", currentUser);
                break;
            case "closedbyother": // assigned to the user and closed by another user
                enquiries = enquiryService.getByClosedAndUserAssigned(currentUser);
                break;
            case "closedbyuser": // closed by the user
                enquiries = enquiryService.getByStatusAndUser("closed", currentUser);
                break;
            default:
                enquiries = new ArrayList<>();
        }
        return enquiries;
    }


    /*
     * Sorts enquiries displayed on the main page by given criteria
     */
    @PostMapping(path = "/enquiry/sort/{sortBy}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Enquiry> sortById(@RequestBody List<Enquiry> enquiries, @PathVariable String sortBy) {
        List<Enquiry> enquiryListWithProgressUsers =
                enquiryService.sortProgressUsers(enquiries);
        List<Enquiry> enquiriesSorted =
                enquiryService.sortBy(enquiryListWithProgressUsers, sortBy);
        enquiryListWrapperRest.setEnquiryList(enquiriesSorted);
        return enquiries;
    }


    /*
     * Searches by enquiry/customer properties, single or multiple combination thereof
     * (wildcard search with '*' at the end of the search term)
     */
    @PostMapping(path = "/enquiry/search/regular",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enquirySearchRegular(@RequestParam (defaultValue = "") String searchFor,
                                              @RequestParam (defaultValue = "all")String searchIn,
                                              @RequestParam (defaultValue = "0") String limit,
                                              @RequestParam (defaultValue = "") String dateRange,
                                              @RequestParam (defaultValue = "any user") String assignedUser,
                                              @RequestParam (defaultValue = "any user") String closingUser,
                                              @RequestParam (defaultValue = "all") String status,
                                              @RequestParam (defaultValue = "all") String sortBy,
                                              @RequestParam (defaultValue = "ascending") String direction
                                              ) {
        SearchWrapper searchWrapper = new SearchWrapper();
        searchWrapper.setSearchFor(searchFor);
        searchWrapper.setSearchIn(searchIn);
        searchWrapper.setDateRange(dateRange);
        searchWrapper.setAssignedUser(assignedUser);
        searchWrapper.setClosingUser(closingUser);
        searchWrapper.setStatus(status);
        searchWrapper.setSortBy(sortBy);
        searchWrapper.setDirection(direction);
        try{
            searchWrapper.setLimit(Integer.parseInt(limit));
        }catch(NumberFormatException e){
            ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                    "request parameter 'limit is not a valid integer",
                    e.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
        }
        enquiryService.logSearchParameters(logger, searchWrapper);

        List<Enquiry> enquiryListSearchResult =
                enquiryService.searchRegularResultListWithProgressUserList(searchWrapper);

        return ResponseEntity.ok(enquiryListSearchResult);
    }

    /*
     * Full-Text Apache Lucene / Hibernate search
     */
    @PostMapping(path = "/enquiry/search/fulltext",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enquirySearchFull(@RequestParam (defaultValue = "") String searchFor,
                                               @RequestParam (defaultValue = "0") String limit,
                                               @RequestParam (defaultValue = "") String dateRange,
                                               @RequestParam (defaultValue = "keywordWildcard") String selector
                                        ) {
        int limitInt;
        try{
            limitInt = Integer.parseInt(limit);
        }catch(NumberFormatException e){
            ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                    "request parameter 'limit is not a valid integer",
                    e.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
        }
        logger.info("SearchFor-> " + searchFor);
        logger.info("Limit-> " + limitInt);
        logger.info("DateRange-> " + dateRange);

        List<Enquiry> enquiryListSearchResult =
                searchService.hibernateSearch(selector,
                        searchFor, limitInt, dateRange);

        return  ResponseEntity.ok(enquiryListSearchResult);
    }


}





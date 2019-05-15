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

package com.latidude99.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.google.common.collect.Ordering;
import com.latidude99.model.Comment;
import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.repository.EnquiryRepository;
import com.latidude99.repository.UserRepository;
import com.latidude99.util.FormBean;
import com.latidude99.util.SearchWrapper;

@Service
public class EnquiryService {
    private static final Logger logger = LoggerFactory.getLogger(EnquiryService.class);

    @Autowired
    EnquiryRepository enquiryRepository;

    @Autowired
    UserRepository userRepository;

    public void save(Enquiry enquiry) {
        enquiry.setEmail(enquiry.getEmail().trim());
        enquiryRepository.save(enquiry);
    }

    public Enquiry getById(long id) {
        return enquiryRepository.findById(id);
    }

    public long getTotalNum() {
        return enquiryRepository.count();
    }

    public Long getNumByStatus(String status) {
        return enquiryRepository.countByStatus(status);
    }

    public Long getNumByClosingUserAndStatus(User user, String status) {
        return enquiryRepository.countByClosingUserAndStatus(user, status);
    }

    public Long getNumByProgressUserAndStatus(User user, String status) {
        return enquiryRepository.countByProgressUserAndStatus(user, status);
    }

    public int getNumByClosedAndUserAssigned(User user) {
        return getByClosedAndUserAssigned(user).size();
    }

    public byte[] imageUrlToByteArray(String imageUrl) throws IOException {
        InputStream inputStream = new URL(imageUrl).openStream();
        byte[] imageByteArray = IOUtils.toByteArray(inputStream);

        return imageByteArray;
    }

    public Enquiry saveComment(FormBean formBean) {
        User user = userRepository.findById(Long.parseLong(formBean.getUserId()));
        Enquiry enquiry = enquiryRepository.findById(Long.parseLong(formBean.getEnquiryId()));
        Comment comment = new Comment();
        comment.setUserId(user.getId());
        comment.setUserName(user.getName());
        comment.setContent(formBean.getCommentContent());
        comment.setDate(ZonedDateTime.now());
        enquiry.addComment(comment);
        return enquiry;
    }

    public List<Enquiry> getRecent100Sorted() {
        List<Enquiry> recent100 = sortProgressUsers(enquiryRepository.findFirst100ByOrderByCreatedDateDesc());
        //Collections.reverse(recent100);
        return recent100;
    }

    public List<Enquiry> getLastUserDefined(int pageNumber, int rowsNumber) {
        if (rowsNumber < 1) rowsNumber = 50;
        Page<Enquiry> enquiriesPage = enquiryRepository.findAll(PageRequest.of(pageNumber, rowsNumber, Sort.by(Direction.DESC, "createdDate")));
        List<Enquiry> enquiriesList = enquiriesPage.getContent();
        List<Enquiry> enquiriesListWithProgressUsers = sortProgressUsers(enquiriesList);
        return enquiriesListWithProgressUsers;
    }

    public List<Enquiry> getByStatus(String status) {
        List<Enquiry> listByStatus = sortProgressUsers(enquiryRepository.findByStatus(status));
        return listByStatus;
    }

    public List<Enquiry> getByStatusAndUser(String status, User user) {
        List<Enquiry> listByStatus = sortProgressUsers(enquiryRepository.findByStatus(status));
        List<Enquiry> listByStatusAndUser = new ArrayList<Enquiry>();
        if ("in progress".equals(status)) {
            listByStatusAndUser = listByStatus.stream().filter(e -> e.getSortedProgressUsers().contains(user)).collect(Collectors.toList());
            System.out.println("assigned to user, open");
        }
        if ("closed".equals(status)) {
            listByStatusAndUser = listByStatus.stream().filter(e -> e.getClosingUser().equals(user)).collect(Collectors.toList());
            System.out.println("closed by user");
        }
        return listByStatusAndUser;
    }


    public List<Enquiry> getByClosedAndUserAssigned(User user) {
        List<Enquiry> listByStatus = sortProgressUsers(enquiryRepository.findByStatus("closed"));
        List<Enquiry> listByStatusAndUser = listByStatus.stream().filter(e -> e.getSortedProgressUsers().contains(user)).collect(Collectors.toList());
        if (listByStatusAndUser == null) listByStatusAndUser = new ArrayList<Enquiry>();
        return listByStatusAndUser;
    }


    public void addProgressUser(long enquiryId, long userId) {
        Enquiry enquiry = enquiryRepository.findById(enquiryId);
        User user = userRepository.findById(userId);
        enquiry.addProgressUser(user);
        enquiryRepository.save(enquiry);
    }

    // not the most efficient but is fine for small sets of numbers, as intended in this case
    public List<Enquiry> getUserDefinedIdsProgressUsersSorted(String numbersAsString) {
//		String numbersAsString = "2, 4, 5,,67,7 6,32,3, 5,7-9, 8-17,15, 17, e9 0, 9-q9, 1 2-14";
//		System.out.println("getUserDefinedId() ->");
        List<String> numbersList = new ArrayList<>();
        Set<Long> numbersSetFinal = new TreeSet<>();
        int notValid = 0;
//      System.out.println(numbersAsString);
        String[] numbers = numbersAsString.split(",");
        for (String s : numbers) {
            s = s.trim().replaceAll(" ", "").replaceAll("\u00A0", "");
//	        System.out.println("after trim() and replace() s =" + s);
            notValid = 0;
            for (int i = 0; i < s.length(); i++) {
                if ((s.charAt(i) != '0') && (s.charAt(i) != '1') && (s.charAt(i) != '2') && (s.charAt(i) != '3') && (s.charAt(i) != '4') && (s.charAt(i) != '5') && (s.charAt(i) != '6') && (s.charAt(i) != '7') && (s.charAt(i) != '8') && (s.charAt(i) != '9') && (s.charAt(i) != '-'))
                    notValid++;
//	            System.out.print(s.charAt(i) + ", ");
//	            System.out.println("notValid for the char: " + notValid);
            }
//	        System.out.println("notValid for the string: " + notValid);
            if (!s.isEmpty() && notValid < 1) numbersList.add(s);
        }
//      System.out.println(numbersList);
        for (String n : numbersList) {
            if (n.contains("-")) {
                String[] range = n.split("-");
                long range0 = Long.parseLong(range[0]);
                long range1 = Long.parseLong(range[1]);
                if (range0 < range1) {
                    for (long i = range0; i < range1 + 1; i++) {
                        numbersSetFinal.add(i);
                    }
                } else {
                    for (long i = range1; i < range0 + 1; i++) {
                        numbersSetFinal.add(i);
                    }
                }
            } else {
                numbersSetFinal.add(Long.parseLong(n));
            }
        }
//      System.out.println(numbersSetFinal);
        List<Enquiry> listByIds = enquiryRepository.findByIdIn(numbersSetFinal.stream().collect(Collectors.toList()));
        return sortProgressUsers(listByIds);
    }


    public List<Enquiry> sortProgressUsers(List<Enquiry> enquiries) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        for (Enquiry enquiry : enquiries) {
            Map<java.util.Date, User> map = enquiry.getProgressUser();
            Map<java.util.Date, User> mapSorted = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            enquiry.setProgressUser(mapSorted);

            Collection<User> values = mapSorted.values();
            List<User> list = new ArrayList<User>(values);
            enquiry.setSortedProgressUsers(list);

            Collection<Entry<Date, User>> entrySet = mapSorted.entrySet();
            List<Entry<Date, User>> entryList = new LinkedList<Entry<Date, User>>(entrySet);
            LinkedList<String> dateUserName = new LinkedList<>();
            for (Entry<Date, User> entry : entryList) {
                dateUserName.addFirst(entry.getValue().getName() + " (" + dateFormat.format(entry.getKey()) + ")");
            }
            enquiry.setSortedProgressUsersWithDate(dateUserName);
        }
        return enquiries;
    }

    public Enquiry sortProgressUsers(Enquiry enquiry) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        if (enquiry != null) {
            Map<java.util.Date, User> map = enquiry.getProgressUser();
            Map<java.util.Date, User> mapSorted = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            enquiry.setProgressUser(mapSorted);

            Collection<User> values = mapSorted.values();
            List<User> list = new ArrayList<User>(values);
            enquiry.setSortedProgressUsers(list);

            Collection<Entry<Date, User>> entrySet = mapSorted.entrySet();
            List<Entry<Date, User>> entryList = new LinkedList<Entry<Date, User>>(entrySet);
            LinkedList<String> dateUserName = new LinkedList<>();
            for (Entry<Date, User> entry : entryList) {
                dateUserName.add(entry.getValue().getName() + " (" + dateFormat.format(entry.getKey()) + ")");
            }
            enquiry.setSortedProgressUsersWithDate(dateUserName);
        } else {
            return enquiry;
        }


        return enquiry;
    }

    private String searchSortByConverter(SearchWrapper searchWrapper) { //converting searchWrapper sorting field names to the existing method used for sorting
        String sortBy = "creation date";
        if ("closing date".equals(searchWrapper.getSortBy())) sortBy = "closed";
        if ("status".equals(searchWrapper.getSortBy())) sortBy = "status";
        if ("customer's name".equals(searchWrapper.getSortBy())) sortBy = "name";
        if ("customer's email".equals(searchWrapper.getSortBy())) sortBy = "email";

        return sortBy;
    }

    public List<Enquiry> sortBy(List<Enquiry> enquiries, String sortBy) {
        Comparator<Enquiry> comparatorAsc;
        Comparator<Enquiry> comparatorDesc;
        switch (sortBy) {
            case "id":
                comparatorAsc = Comparator.comparing(enq -> enq.getId());
                comparatorDesc = Comparator.comparing(Enquiry::getId).reversed();
                break;
            case "name":
                comparatorAsc = Comparator.comparing(enq -> enq.getName());
                comparatorDesc = Comparator.comparing(Enquiry::getName).reversed();
                break;
            case "email":
                comparatorAsc = Comparator.comparing(enq -> enq.getEmail());
                comparatorDesc = Comparator.comparing(Enquiry::getEmail).reversed();
                break;
            case "type":
                comparatorAsc = Comparator.comparing(enq -> enq.getType());
                comparatorDesc = Comparator.comparing(Enquiry::getType).reversed();
                break;
            case "created":
                comparatorAsc = Comparator.comparing(enq -> enq.getCreatedDate());
                comparatorDesc = Comparator.comparing(Enquiry::getCreatedDate).reversed();
                break;
            case "status":
                comparatorAsc = Comparator.comparing(enq -> enq.getStatus());
                comparatorDesc = Comparator.comparing(Enquiry::getStatus).reversed();
                break;
            case "closed":
                comparatorAsc = new Comparator<Enquiry>() {
                    public int compare(Enquiry o1, Enquiry o2) {
                        if (o1.getClosedDate() == null) {
                            return (o2.getClosedDate() == null) ? 0 : -1;
                        }
                        if (o2.getClosedDate() == null) {
                            return 1;
                        }
                        return o2.getClosedDate().compareTo(o1.getClosedDate());
                    }
                };

                comparatorDesc = new Comparator<Enquiry>() {
                    public int compare(Enquiry o1, Enquiry o2) {
                        if (o1.getClosedDate() == null) {
                            return (o2.getClosedDate() == null) ? 0 : 1;
                        }
                        if (o2.getClosedDate() == null) {
                            return -1;
                        }
                        return o1.getClosedDate().compareTo(o2.getClosedDate());
                    }
                };
                break;
            default:
                comparatorAsc = Comparator.comparing(enq -> enq.getId());
                comparatorDesc = Comparator.comparing(Enquiry::getId).reversed();
                System.out.println("default, sortBy:" + sortBy);
        }

        if (Ordering.from(comparatorAsc).isOrdered(enquiries)) {
            Collections.sort(enquiries, comparatorDesc);
        } else if (Ordering.from(comparatorAsc).reverse().isOrdered(enquiries)) {
            Collections.sort(enquiries, comparatorAsc);
        } else {
            Collections.sort(enquiries, comparatorAsc);
        }
        return enquiries;
    }


    public List<Enquiry> updateEnquiryListToView(List<Enquiry> enquiryList, long enquiryId, String status) {
        Enquiry enquiryToUpdate = getById(enquiryId);
        List<Enquiry> enquiryListToUpdate;
        if (enquiryToUpdate != null) {
            enquiryListToUpdate = enquiryList.stream().filter(e -> e.getId() != enquiryId).collect(Collectors.toList());
            enquiryToUpdate.setStatus(status);
            enquiryListToUpdate.add(enquiryToUpdate);
            return sortProgressUsers(enquiryListToUpdate);
        } else {
            return enquiryList;
        }

    }


    public List<Enquiry> searchRegularResultListWithProgressUserList(SearchWrapper searchWrapper) {

        int limit = searchWrapper.getLimit(); // ------------------------------------------setting limit
        if (limit < 1) limit = Integer.MAX_VALUE;

        String option = "exact"; //-------------------------------------------------------setting searchFor
        String searchFor = "";
        if (!(searchWrapper.getSearchFor() == null)) {
            searchFor = searchWrapper.getSearchFor().trim();
            if (searchFor.endsWith("*")) {
                searchFor = searchFor.substring(0, searchFor.length() - 1);
                option = "fuzzy";
            }
            System.out.println("searchFor*: " + searchFor);
        }

        System.out.println("--------------------------service: searchFor: " + searchFor);

        String dateRangeString; // -------------------------------------------------------setting date range
        String[] dateRange;
        if ("".equals(searchWrapper.getDateRange()) || searchWrapper.getDateRange() == null) {
            dateRangeString = "01/01/1500 - 01/01/3000";
            dateRange = dateRangeString.trim().split("-");
        } else dateRange = searchWrapper.getDateRange().trim().split("-");
        ZonedDateTime startDate = stringToZonedDateTimeConverter(dateRange[0].trim());
        ZonedDateTime endDate = stringToZonedDateTimeConverter(dateRange[1].trim());
        System.out.println("startDate: " + startDate + ", endDate: " + endDate);

        //--------------------------------------------------------------------------------setting other criteria
        String searchIn = searchWrapper.getSearchIn();
        String status = searchWrapper.getStatus();
        String sortBy = searchWrapper.getSortBy();
        String direction = searchWrapper.getDirection();

        //-----------------------------search logic------------------------------------------------------------
        List<Enquiry> resultListTemp = new ArrayList<>();
        List<Enquiry> resultListFinal = new ArrayList<>();

        if (("".equals(searchWrapper.getSearchFor())) || (searchWrapper.getSearchFor() == null)) {
            resultListTemp = enquiryRepository.findAllByCreatedDateBetween(startDate, endDate);
        } else if (option.equals("exact")) {
            String searchForWithSpaces = " " + searchFor + " ";
            switch (searchIn) {
                case "in customer names":
                    resultListTemp = enquiryRepository.findAllByNameIgnoreCaseAndCreatedDateBetween(searchFor, startDate, endDate);
                    break;
                case "in customer emails":
                    resultListTemp = enquiryRepository.findAllByEmailIgnoreCaseAndCreatedDateBetween(searchFor, startDate, endDate);
                    break;
                case "in customer phone numbers":
                    resultListTemp = enquiryRepository.findAllByCreatedDateBetweenAndPhoneIgnoreCaseContaining(startDate, endDate, searchFor);
                    break;
                case "in customer messages":
                    resultListTemp = enquiryRepository.findAllByCreatedDateBetweenAndMessageIgnoreCaseContaining(startDate, endDate, searchForWithSpaces);
                    break;
                case "in product isbn":
                    resultListTemp = enquiryRepository.findAllByIsbnAndCreatedDateBetween(searchFor, startDate, endDate);
                    break;
                default:
                    resultListTemp = Stream.of(enquiryRepository.findAllByNameIgnoreCaseAndCreatedDateBetween(searchFor, startDate, endDate), enquiryRepository.findAllByEmailIgnoreCaseAndCreatedDateBetween(searchFor, startDate, endDate), enquiryRepository.findAllByCreatedDateBetweenAndPhoneIgnoreCaseContaining(startDate, endDate, searchFor), enquiryRepository.findAllByCreatedDateBetweenAndMessageIgnoreCaseContaining(startDate, endDate, searchForWithSpaces), enquiryRepository.findAllByIsbnAndCreatedDateBetween(searchFor, startDate, endDate)).flatMap(Collection::stream).collect(Collectors.toList());
            }
            System.out.println("--------------------------service: exact, resultListTemp: " + resultListTemp.size());
        } else if (option.equals("fuzzy")) {
            switch (searchIn) {
                case "in customer names":
                    resultListTemp = enquiryRepository.findAllByCreatedDateBetweenAndNameIgnoreCaseContaining(startDate, endDate, searchFor);
                    break;
                case "in customer emails":
                    resultListTemp = enquiryRepository.findAllByCreatedDateBetweenAndEmailIgnoreCaseContaining(startDate, endDate, searchFor);
                    break;
                case "in customer phone numbers":
                    resultListTemp = enquiryRepository.findAllByCreatedDateBetweenAndPhoneIgnoreCaseContaining(startDate, endDate, searchFor);
                    break;
                case "in customer messages":
                    resultListTemp = enquiryRepository.findAllByCreatedDateBetweenAndMessageIgnoreCaseContaining(startDate, endDate, searchFor);
                    break;
                case "in product isbn":
                    resultListTemp = enquiryRepository.findAllByCreatedDateBetweenAndIsbnContaining(startDate, endDate, searchFor);
                    break;
                default:
                    resultListTemp = Stream.of(enquiryRepository.findAllByCreatedDateBetweenAndNameIgnoreCaseContaining(startDate, endDate, searchFor), enquiryRepository.findAllByCreatedDateBetweenAndEmailIgnoreCaseContaining(startDate, endDate, searchFor), enquiryRepository.findAllByCreatedDateBetweenAndPhoneIgnoreCaseContaining(startDate, endDate, searchFor), enquiryRepository.findAllByCreatedDateBetweenAndMessageIgnoreCaseContaining(startDate, endDate, searchFor), enquiryRepository.findAllByCreatedDateBetweenAndIsbnContaining(startDate, endDate, searchFor)).flatMap(Collection::stream).collect(Collectors.toList());
            }
            System.out.println("--------------------------service: fuzzy, resultListTemp: " + resultListTemp.size());
        }

        resultListTemp = resultListTemp.stream() //------------ eliminating duplicates
                .distinct().collect(Collectors.toList());

        sortProgressUsers(resultListTemp); //--------------adding progresUser list (transient in entity Enquiry)

        if (!"any user".equals(searchWrapper.getAssignedUser())) { //--------------filtering by progressUser
            resultListTemp = resultListTemp.stream().filter(e -> e.getSortedProgressUsers() != null).filter(e -> e.getSortedProgressUsers().contains(userRepository.findByName(searchWrapper.getAssignedUser()))).collect(Collectors.toList());
            System.out.println("--------------------------service: assigned user: " + resultListTemp.size());
        }
        if (!"any user".equals(searchWrapper.getClosingUser())) { //--------------filtering by closingUser
            resultListTemp = resultListTemp.stream().filter(e -> e.getClosingUser() != null).filter(e -> e.getClosingUser().getName().equals(searchWrapper.getClosingUser())).collect(Collectors.toList());
            System.out.println("--------------------------service: closing user: " + resultListTemp.size());
        }
        if (!"all".equals(searchWrapper.getStatus())) { //---------------filtering by status
            resultListTemp = resultListTemp.stream().filter(e -> e.getStatus().equals(searchWrapper.getStatus())).collect(Collectors.toList());
            System.out.println("--------------------------service: status: " + resultListTemp.size());
        }

        resultListTemp = resultListTemp.stream() //--------------------------limiting the results number
                .limit(limit).collect(Collectors.toList());

        resultListFinal = sortBy(resultListTemp, searchSortByConverter(searchWrapper)); //----------sorting the final list

        System.out.println("--------------------------service: after sorting: " + resultListFinal.size());

        return resultListFinal;
    }


    public ZonedDateTime stringToZonedDateTimeConverter(String dateString) {
        ZonedDateTime dateConvertedWithZone = ZonedDateTime.now();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyy");
            LocalDate dateConverted = LocalDate.parse(dateString, formatter);
            dateConvertedWithZone = dateConverted.atStartOfDay(ZoneId.of("Europe/London"));
//		    System.out.println("converted date: " + dateConverted);
            return dateConvertedWithZone;
        } catch (DateTimeException e) {
            System.out.println("Error parsing date: " + e.getMessage());
        }
        return dateConvertedWithZone;
    }


    public String convertRoundBracketToSquareCoordsArrayString(String roundBrackets) {
        String squareBrackets = "[ " + roundBrackets.replace("(", "[").replace(")", "]") + " ]";
        return squareBrackets;
    }


}

















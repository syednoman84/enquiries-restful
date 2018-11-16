package com.latidude99.web.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.HibernateSearchService;
import com.latidude99.service.UserService;
import com.latidude99.util.EnquiryListWrapper;
import com.latidude99.util.FormBean;
import com.latidude99.util.SearchWrapper;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;


@Controller
public class EnquiryListController {
	private static final Logger logger = LoggerFactory.getLogger(EnquiryListController.class);
	
	@Autowired
	UserService userService;
	
	@Autowired
	EnquiryService enquiryService;
	
	@Autowired
	EnquiryListWrapper enquiryListWrapper;
	
	@Autowired
    private HibernateSearchService searchService;
	
	@ModelAttribute("formBean")
	public FormBean createFormBean() {
	  FormBean formBean = new FormBean();
	  formBean.setSelector("0");
	  formBean.setNumber(100);
	  return formBean;
	}
	
	@ModelAttribute("searchWrapper")
	public SearchWrapper createSearchWrapper() {
		SearchWrapper searchWrapper = new SearchWrapper();
		searchWrapper.setSearchFor("");
		searchWrapper.setSelector("keywordWildcard");  //setting pre-selected Hibernate query type in enquryList.html
		searchWrapper.setLimit(0);
		searchWrapper.setSearchIn("all");
		searchWrapper.setStatus("all");
//		searchWrapper.setDateRange("all");
		searchWrapper.setSortBy("all");
		searchWrapper.setDirection("ascending");
//		System.out.println(userList);
		searchWrapper.setUserList(userService.getUserListAsStringList());
		searchWrapper.setAssignedUser("any user");
		searchWrapper.setClosingUser("any user");
		searchWrapper.setCustomer("all");
	  	return searchWrapper;
	}
	
	
	
	
	
	@GetMapping("/enquiry/list")
	public String enquiryList(Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
//		EnquiryListWrapper enquiryListWrapper = new EnquiryListWrapper();
		enquiryListWrapper.setEnquiryList(enquiryService.getRecent100Sorted());
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		logger.info("logged in as:  " + currentUser.getEmail());
		System.out.println("GET  /enquiry/list -> " + enquiryListWrapper.getEnquiryList().size());
		return "enquiryList";
	}
	
	@PostMapping("/enquiry/list")
	public String enquiryListSelection(@ModelAttribute Enquiry enquiry, Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
		
		List<Enquiry> enquiryListToUpdate = enquiryListWrapper.getEnquiryList();
		List<Enquiry> enquiryListUpdated = enquiryService.updateEnquiryListToView(enquiryListToUpdate, enquiry.getId(), enquiry.getStatus());
		enquiryListWrapper.setEnquiryList(enquiryListUpdated);
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		logger.info("logged in as:  " + currentUser.getEmail());
		System.out.println("POST  /enquiry/list -> " + enquiryListWrapper.getEnquiryList().size());
		return "enquiryList";
	}
	
	//------------------------------------- implemented, needs testing for edge cases
	@PostMapping("/enquiry/search/regular")
	public String enquirySearchRegular(@ModelAttribute SearchWrapper searchWrapper, Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		logger.info("logged in as:  " + currentUser.getEmail());
		//----------------------------------------------------------------------------------
		
		System.out.println("getSearchFor-> " + searchWrapper.getSearchFor());
		System.out.println("getSearchIn-> " + searchWrapper.getSearchIn());
		System.out.println("getLimit-> " + searchWrapper.getLimit());
		System.out.println("getDateRange-> " + searchWrapper.getDateRange());
		System.out.println("getAssignedUser-> " + searchWrapper.getAssignedUser());
		System.out.println("getClosingUser-> " + searchWrapper.getClosingUser());
		System.out.println("getStatus-> " + searchWrapper.getStatus());
		System.out.println("getSortBy-> " + searchWrapper.getSortBy());
		System.out.println("getDirection-> " + searchWrapper.getDirection());
		
		List<Enquiry> enquiryListSearchResult;
		enquiryListSearchResult = enquiryService.searchRegularResultListWithProgressUserList(searchWrapper);
		enquiryListWrapper.setEnquiryList(enquiryListSearchResult);
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
		
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
//		System.out.println("POST  /enquiry/list -> " + enquiryListWrapper.getEnquiryList().size());
		
		return "enquiryList";
	}
	
	//------------------------------------- implemented, needs testing for edge cases
	@PostMapping("/enquiry/search/fuzzy")
	public String enquirySearchFull(@ModelAttribute SearchWrapper searchWrapper, Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		logger.info("logged in as:  " + currentUser.getEmail());
		//----------------------------------------------------------------------------------
		
		System.out.println("getSearchFor-> " + searchWrapper.getSearchFor());
		System.out.println("getLimit-> " + searchWrapper.getLimit());
		System.out.println("getDateRange-> " + searchWrapper.getDateRange());
		
		List<Enquiry> enquiryListSearchResult;
		enquiryListSearchResult = searchService.hibernateSearch(
				searchWrapper.getSelector(), 
				searchWrapper.getSearchFor(), 
				searchWrapper.getLimit(), 
				searchWrapper.getDateRange());
		enquiryListWrapper.setEnquiryList(enquiryListSearchResult);
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
		
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
//		System.out.println("POST  /enquiry/list -> " + enquiryListWrapper.getEnquiryList().size());
		
		return "enquiryList";
	}
	
	@PostMapping("/enquiry/sort")
	public String sortById(@ModelAttribute EnquiryListWrapper enquiryListWrapper, Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		
		List<Enquiry> enquiryListWithProgressUsers = enquiryService.sortProgressUsers(enquiryListWrapper.getEnquiryList());
		List<Enquiry> enquiriesSorted = enquiryService.sortBy(enquiryListWithProgressUsers, enquiryListWrapper.getSortBy());
		enquiryListWrapper.setEnquiryList(enquiriesSorted);
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		System.out.println("enquiryListWrapper.getSortBy(): " + enquiryListWrapper.getSortBy());
		
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
		return "enquiryList";
	}
	
	@GetMapping("/enquiry/list/clear")
	public String enquiryListClear(Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
//		EnquiryListWrapper enquiryListWrapper = new EnquiryListWrapper();
		enquiryListWrapper.setEnquiryList(new ArrayList<Enquiry>());
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		logger.info("logged in as:  " + currentUser.getEmail());
		return "enquiryList";
	}
	
	@GetMapping("/enquiry/list/last100")
	public String enquiryListLast100(Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
//		EnquiryListWrapper enquiryListWrapper = new EnquiryListWrapper();
		enquiryListWrapper.setEnquiryList(enquiryService.getRecent100Sorted());
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		logger.info("logged in as:  " + currentUser.getEmail());
		return "enquiryList";
	}
	
	@PostMapping("/enquiry/list/last/custom")
	public String enquiryListLast(@ModelAttribute FormBean formBean, Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		
//		EnquiryListWrapper enquiryListWrapper = new EnquiryListWrapper();
		enquiryListWrapper.setEnquiryList(enquiryService.getLastUserDefined(0, formBean.getNumber()));
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		
		logger.info("logged in as:  " + currentUser.getEmail());
		return "enquiryList";
	}
	
	//------------------------------------------------------not implemented yet
	@PostMapping("/enquiry/search/id")
	public String enquiryLoadById(@ModelAttribute FormBean formBean, Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		
//		EnquiryListWrapper enquiryListWrapper = new EnquiryListWrapper();
		enquiryListWrapper.setEnquiryList(enquiryService.getUserDefinedIdsProgressUsersSorted(formBean.getNumbersAsString()));
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		
		logger.info("logged in as:  " + currentUser.getEmail());
		return "enquiryList";
	}
		
	@GetMapping("/enquiry/list/waiting")
	public String enquiryListWaiting(Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
//		EnquiryListWrapper enquiryListWrapper = new EnquiryListWrapper();
		enquiryListWrapper.setEnquiryList(enquiryService.getByStatus("waiting"));
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		logger.info("logged in as:  " + currentUser.getEmail());
		return "enquiryList";
	}
	
	@GetMapping("/enquiry/list/progress")
	public String enquiryListProgress(Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
//		EnquiryListWrapper enquiryListWrapper = new EnquiryListWrapper();
		enquiryListWrapper.setEnquiryList(enquiryService.getByStatus("in progress"));
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		logger.info("logged in as:  " + currentUser.getEmail());
		return "enquiryList";
	}
	
	@GetMapping("/enquiry/list/closed")
	public String enquiryListClosed(Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
//		EnquiryListWrapper enquiryListWrapper = new EnquiryListWrapper();
		enquiryListWrapper.setEnquiryList(enquiryService.getByStatus("closed"));
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		logger.info("logged in as:  " + currentUser.getEmail());
		return "enquiryList";
	}
	
	@GetMapping("/enquiry/list/progress/user/assigned")
	public String enquiryListOpenedAndUserAssigned(Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
//		EnquiryListWrapper enquiryListWrapper = new EnquiryListWrapper();
		enquiryListWrapper.setEnquiryList(enquiryService.getByStatusAndUser("in progress", currentUser));
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		logger.info("logged in as:  " + currentUser.getEmail());
		return "enquiryList";
	}
	
	@GetMapping("/enquiry/list/closed/user/assigned")
	public String enquiryListClosedAndUserAssigned(Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
//		EnquiryListWrapper enquiryListWrapper = new EnquiryListWrapper();
		enquiryListWrapper.setEnquiryList(enquiryService.getByClosedAndUserAssigned(currentUser));
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		logger.info("logged in as:  " + currentUser.getEmail());
		return "enquiryList";
	}
	
	
	@GetMapping("/enquiry/list/closed/user/closed")
	public String enquiryListClosedByUser(Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
//		EnquiryListWrapper enquiryListWrapper = new EnquiryListWrapper();
		enquiryListWrapper.setEnquiryList(enquiryService.getByStatusAndUser("closed", currentUser));
		model.addAttribute("enquiryListWrapper", enquiryListWrapper);
//		enquiryListWrapper.getEnquiryList().forEach(e -> System.out.println(e.getSortedProgressUsersWithDate()));
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
		model.addAttribute("openedByUser", openedByUser);
		int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
		model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
		model.addAttribute("closedByUser", closedByUser);
		logger.info("logged in as:  " + currentUser.getEmail());
		return "enquiryList";
	}
	
	
	
}

	






















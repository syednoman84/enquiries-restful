# Enquiry Management System

[![](https://img.shields.io/badge/release-1.2-blue.svg)](https://github.com/latidude99/enquiries/tree/master/release)
[![License: GPL v3](https://img.shields.io/badge/license-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![GitHub issues](https://img.shields.io/badge/issues-open%201-greenred.svg)](https://GitHub.com/latidude99/enquiries/issues/)
[![Maintenance](https://img.shields.io/badge/maintained-bug%20fixing%20only-green.svg)](https://GitHub.com/latidude99/enquiries/graphs/commit-activity)
[![](https://img.shields.io/badge/%20$%20-buy%20me%20a%20coffe-yellow.svg)](https://www.buymeacoffee.com/zWn1I6bVf)

Customer service enquiry system for a travel bookshop.


## Table of contents
* [General Info](#general-info)
* [Technologies](#technologies)
* [Setup](#setup)
* [Launch](#launch)
* [Features](#features)
* [Status](#status)
* [Screenshots](#screenshots)
* [License](#license)
* [Contact](#contact)


# General Info

A small customer service enquiry system, geared towards map and travel bookshops. Includes options to attach files and draw a polygon on Google Maps making it much simpler to describe the exact area of interest.  

<p align="center">
  <img src="images/enquiry_00b.JPG" width=100%>
</p>


# Technologies
- Java JDK 1.8
- Maven 3.60
- Spring Boot 2.0 with Spring Security 5, Spring Validation and Spring Data
- Thymeleaf 3 with Thymeleaf Extras for Spring Security and Java 8 Time API support
- Bootstrap 4
- Google Maps iFrame
- Hibernate
- MySQL
- Apache Tomcat 9.0
- VPS with Debian 8
- JUnit5 5.4.1
- AssertJ 3.12.2
- Eclipse / STS 3.9 , unit tests in IntelliJ IDEA. 

**Plugins and libraries**
- Spring Boot Maven Plugin
- Maven Surefire plugin  version 2.22.0


# Setup

Clone the repository to a folder on your computer and import it in your favourite IDE as a Maven project. It uses Tomcat 9.0 as a web server so you need to set it up in your IDE as well.

### Build

Run maven command: 'mvn clean package'. This should create an executable 'jar' with all libraries needed included.

# Launch
You should have MySQL empty database named 'enquiries' up and running on your machine. The database should be availabe on the 3306 port - you can change the default settings in the 'application.properties' file. 
Then, after successful build process navigate to the 'target/' folder of the project and run 'java -jar enquiry.jar' command. 
The application should be availavle at localhost://8080 in your Internet browser. 
In windows instead of command line you should also be able to double click the 'contacts.jar' file although you will not get any information about the start-up process and its possible failure. You can check in Task Manager if process is running (JVM process).


# Features

The application consist of two parts:
 - customer side enquiry form
 - enquiry management side for customer service staff
 
 ## Customer Enquiry Form
 - standard fields for customer first and last names, email address and a phone number
 - two-step validation for mandatory fields 
    - Javascript check for empty fields
    - Spring Validation check for the email address  corectness
 - drop-down list with options of the enquiry subject (Maps, Travel Literature, Guides, Customised Maps etc.)
 - message box for the actual enquiry text
 - Google Maps window with an option to draw/delete a polygon delineating the area of interest
 - option to attach up to three files (the limit is set as 1MB for each file at the moment)
 - information of the enuiry sending success or failure
 - login/password fields (with a 'Remember Me option) for enquiry management side
 
 ## Enquiry Management Side
 
 ### Main screen
 - information about number of enquiries waiting, being processed and total number of enquiries
 - a list of enquiries in a table
Each enquiry (line) in the table include:
- customer's name
- customer's email
- enquiry type (guides, cistomised mapping etc.)
- when it was created
- enquiry status (waiting / in progress / closed)
- last employeee dealing with the enquiry
- closing date (if closed)
- info icon telling if there is a polygon attached
- info icon with a number of comments relevent to the enquiry
- a button 'more...' taking you to a detailed enquiry page when clicked (shows a pop-up with enquiry message on hoover)

### Detailed enquiry page
Left Panel:
- 

# Status
- Development: Closed
- Bug fixing: Open

# Screenshots
<p align="center">
  <img src="images/" width=100%>
</p>

# License
Enquiry System is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
Enquiry System is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with Enquiry System. 
If not, see http://www.gnu.org/licenses/ or write to: latidude99@gmail.com

# Contact
You can email me at latidude99@gmail.com


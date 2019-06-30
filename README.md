# Enquiry Management System

[![](https://img.shields.io/badge/release-1.2-blue.svg)](https://github.com/latidude99/enquiries/tree/master/release)

RESTful API for the **enquiries** project. The original project makes use of
 Thymeleaf v.3 template engine and Bootstrap v.4 for the frontend.

Please refer for a detailed description to the original project
 ( repository  [enquiries](https://github.com/latidude99/enquiries.git))
 
 This repository contains RESTful API only.

 As opposed to the original Enquiry System this version is not deployed and available online.

## Table of contents
* [REST API](#rest-api)
  - [Enquiry Form API](###enquiry-form-api)
  - [Attachment API](###attachment-api)
  - [Enquiry List API](###enquiry-list-api)
  - [Enquiry Page API](###enquiry-page-api)
  - [IO API](###io-api)
  - [USER API](###user-api)
  - [ADMIN API](###admin-api)
* [Technologies](#technologies)
* [Tests](#tests)
* [Status](#status)
* [License](#license)
* [Contact](#contact)

# REST API

Spring Security configured as Basic Auth with Custom UserDetails Service (users kept in DB).
*SessionCreationPolicy* flag is set to **STATELESS** making restricted API endoints users
to log in with every request. 
The only endpoint permitted to all is the Enquiry Form processing endpoint (`/api/enquiry/form`)

### Enquiry Form API

##### endpoint: `/api/enquiry/form`  
restricted to: `no restrictions`  
method: `POST`  
consumes:  `multipart/form-data`  
request parameters:
 - `name` - customer's name (required, minimum 3 characters);
 - `email` - customer's email  (required, syntactically correct email addres, validity not checked);
 - `phone` - customer's phone (optional)
 - `isbn` - searched item ISBN (optional)  
 - `type` - enquiry category (required):  
    -   `Maps` |`Guides` | `Customised Maps` | `Travel Literature` | `Returns` | `Other`
 - `message` - the actual enquiry text (required, min 10, max 2048 characters)
 - `polygon` - polygon drawn using Google Maps API v3 without processing (optional)
 - `polygonencoded` - encoded version of the polygon, Google Maps API v3 encoding (optional)
 - `files` - Multipart[] files, limit for a single file currently set to 2048KB 
 can be changed in`application.properties` (optional)
 
On submit all the submitted data are saved in DB, including files. Additionally a static image 
generated from a polygon drawn on Google Maps (if present).

### Attachment API

##### endpoint: `/api/enquiry/{id}/attachments`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
path variable `id`: enquiry number  
produces: `application/json`  

Returns a list of info objects on all the files attached to the given enquiry's `id`  
Info object include: 
- `id` - file id
- `name` - file name
- `mimetype` - file format
- `size` - file size
- `enquiryId` - enquiry number that file is submitted with  
  
##### endpoint: `/api/enquiry/attachment/{id}`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
path variable `id`: file number  
produces: `image/jpeg`  
response body: `byte[]`  

Fetches a file with a given `id` flagged as a `jpeg` image (easy to display in html)  

##### endpoint: `/api/enquiry/attachment/{id}/download`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
path variable `id`: file number  
produces: `application/octet-stream`  
response body: `byte[]`  

Fetches a file with a given `id` flagged as a `octet-stream` data (triggers `Save as...` in browsers)

##### endpoint: `/api/enquiry/{id}/image`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
path variable `id`: file number  
response body: `byte[]`  

Fetches the enquiry from drawn polygon as static image (if present)


### Enquiry List API

##### endpoint: `/api/enquiry/list`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
produces: `application/json`

Returns the most resent 100 enquiries along with info about number of enquiries marked as 
`waiting` | `in progress` | `closed` | `closedByUser` | `assignedToUser` 


##### endpoint: `/api/enquiry/list/stats`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
produces: `application/json`

Returns only info about number of enquiries marked as 
`waiting` | `in progress` | `closed` | `closedByUser` | `assignedToUser` 

##### endpoint: `/api/enquiry/list/last/{number}`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
path variable `number`: number of enquiries    
produces: `application/json`

Fetches a user defined number of the most recent enquiries

##### endpoint: `/api/enquiry/search/ids`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `POST`  
request parameter: `ids`  
produces: `application/json`

`@RequestParam ids` accepts a single number or a series of numbers separated by comma 
as well as a range of numbers with a hyphen (eg. `12, 34, 23-26, 67-64`) 


##### endpoint: `/api/enquiry/list/{fetchBy}`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
path variable `fetchBy`: enquiry status, valid input: `waiting` | `opened` | `closed`  
produces: `application/json`

Returns a list of enquiries filtered by their status, passing any input other 
then valid options returns an empty list. 


##### endpoint: `/api/enquiry/list/user/{fetchBy}`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
path variable `fetchBy`: enquiry status, valid input: `assigned` | `closedbyother` | `closedbyuser`  
produces: `application/json`

Returns a list of enquiries filtered by their relation to the logged in user and
their status, passing any input other then valid options returns an empty list:  
 
`assigned` -  *assigned to the user and opened*  
`closedbyother`  - *assigned to the user and closed by another user*   
`closedbyuser` - *closed by the user*   

##### endpoint: `/api/enquiry/sort/{sortBy}`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `POST`
request body: `List<Enquiry>`  
path variable `sortBy`: sorting, valid input: `id` | `name` | `email` | `type` | `created` |
 `status` |  `closed`
produces: `application/json`

Sorts the enquiry list passed in the request body according to the criteria 
passed as the path variable. If the criterium passed is not valid the list is sorted by
enquiry `id`.
First hit sorts the list ascending (if not sorted already) or the opposite 
(if already sorted).


##### endpoint: `/api/enquiry/search/regular`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `POST`  
consumes:  `multipart/form-data` 
produces: `application/json`  
request parameters:
 - `searchFor`  - optional, default `[empty]`, if no term specified fetches all
 - `limit` - limits the result to the given number (last step)
 - `searchIn` - optional, default `all` , valid options: 
   - `in customer names` 
   - `in customer emails`
   - `in customer phone numbers`
   - `in customer messages` 
   - `in product isbn`
   - `all` or `any other input` - searches in all categories
 - `dateRange`  - optional, default `[empty]` 
 - `assignedUser` - optional, default `any user`, fetches only enquiries with the logged user marked as `assignedUser`
 - `closingUser` - optional, default `any user`, fetches only enquiries with the logged user marked as `closingUser`
 - `status` - optional, default `all`, valid input: `waiting` | `opened` | `closed`
 - `sortBy` - optional, default `all`, valid input: `id` | `name` | `email` | `type` | `created` |
                                        `status` |  `closed`
 - `direction` - optional, default `ascending`                                  

Returns `List<Enquiry>`.  
The search is conducted on the basis of the exact match of the entered search term `searchFor`.
If the `searchFor` ends with `*` then the search looks for entries containing the `searchFor`
substring (after removing `*`). 


##### endpoint: `/api/enquiry/search/fulltext`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `POST`  
consumes:  `multipart/form-data`  
produces: `application/json`  
request parameters:
 - `searchFor`  - optional, default `[empty]`, if no term specified fetches all
 - `limit` - optional, default `no limit`, limits the result to the given number (last step)
 - `dateRange`  - optional, default `[empty]` 
 - `selector`- optional, default `keywordWildcard`, valid options:
   - `keywordExact` 
   - `keywordFuzzy1`
   - `keywordFuzzy2`
   - `keywordWildcard`
   - `phraseExact`
   - `phraseSlop1`
   - `phraseSlop2`
   - `phraseSlop3`
   - `simpleQueryString`
   
Returns `List<Enquiry>`.  
Please see `HibernateSearchService.java` for the options' configuration.  
Refer to [Hibernate Search Reference Guide 5.9.3](https://docs.jboss.org/hibernate/search/5.9/reference/en-US/html_single/#preface)
for detailed explanation.
   

### Enquiry Page API


##### endpoint: `/api/enquiry/{id}`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
produces: `application/json`

Fetches an enquiry with the `id`.

##### endpoint: `/api/enquiry/{id}/comment`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `POST`  
consumes: `application/x-www-form-urlencoded`  
produces: `application/json`  
path variable: `id` - enquiry `id` that the `comment` belongs to  
request parameters: 
 - `comment` - text of the user's comment  
 
Returns the enquiry that the comment was successfully added to.

##### endpoint: `/api/enquiry/{id}/email`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `POST`  
consumes: `application/x-www-form-urlencoded`  
produces: `application/json`  
path variable: `id` - enquiry `id` that is being emailed 
request parameters: 
 - `email` - optional, default value: logged in user `email`  
 
Returns the enquiry that was emailed successfully.

##### endpoint: `/api/enquiry/{id}/assign`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
produces: `application/json`
path variable: `id` - enquiry `id` that is being assigned 

Assigns the enquiry to the logged in user. Changes the enquiry status to `opened`/`in progress` 
(if it was not). Returns updated enquiries stats  - info about number of enquiries marked as:  
     `waiting` | `opened`/`in progress` | `closed` | `closedByUser` | `assignedToUser` 

##### endpoint: `/api/enquiry/{id}/deassign`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
produces: `application/json`
path variable: `id` - enquiry `id` that is being de-assigned 

De-assigns the enquiry from the logged in user. Changes the enquiry status to `closed`
(if there are no assigned users any more). 
Returns updated enquiries stats  - info about number of enquiries marked as:  
     `waiting` | `opened`/`in progress` | `closed` | `closedByUser` | `assignedToUser` 

##### endpoint: `/api/enquiry/{id}/close`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
produces: `application/json`
path variable: `id` - enquiry `id` that is being closed 

Changes the enquiry status to `closed` with the logged in user as a closingUser.
Returns updated enquiries stats  - info about number of enquiries marked as:  
     `waiting` | `opened`/`in progress` | `closed` | `closedByUser` | `assignedToUser` 

##### endpoint: `/api/enquiry/{id}/open`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
produces: `application/json`
path variable: `id` - enquiry `id` that is being opened 

Changes the enquiry status to `opened`/`in progress` without assigning the logged in user.
Returns updated enquiries stats  - info about number of enquiries marked as:  
     `waiting` | `opened`/`in progress` | `closed` | `closedByUser` | `assignedToUser` 



### IO API

##### endpoint: `/api/enquiry/list/pdf`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
produces: `application/pdf`

Returns a list of a 100 most recent enquiries as a PDF file.


##### endpoint: `/api/enquiry/{id}/pdf`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `GET`  
path variable `id`: enquiry number   
produces: `application/pdf`

Returns the enquiry with the given `id` as a PDF file.


### USER API

##### endpoint: `/api/user/activate`  
restricted to: `no restrictions`  
method: `GET`  
request parameters:
 - `activationToken` (URL encoded)

Activates users after following a link with a token sent to them.
Returns Http status code `202 /ACCEPTED` if succesful or `417 /EXPECTATION_FAILED` if failed.

##### endpoint: `/api/user/update`  
restricted to: `USER` `ADMIN` `APPADMIN`  
method: `POST`  
request parameters:
  - `passwordNew` - minimum 6 characters
  - `passwordOld` 

Updates logged in user's password
Returns Http status code `202 /ACCEPTED` if succesful or `417 /EXPECTATION_FAILED` if failed.

##### endpoint: `/api/user/reset`  
restricted to: `no restrictions`  
method: `GET`  
request parameters: `resetToken` (URL encoded)

Check if the token is valid (finds the user) and fetches back the same token for further 
processing (eg. redirecting an already identified user to the password resetting form)
Returns Http status code `200 /OK` if succesful or `417 /EXPECTATION_FAILED` if failed.

#### endpoint: `/api/user/reset`  
restricted to: `no restrictions`     
method: `POST`  
produces: `application/json`  
request parameters: 
  - `token` 
  - `password` - minimum 6 characters

Fetches the user that has just had their password changed.
Returns Http status code `202 /ACCEPTED` if succesful or `417 /EXPECTATION_FAILED` if failed.

#### endpoint: `/api/user/forgot`  
restricted to: `no restrictions`     
method: `POST`  
request parameters: 
  - `email` - syntactically valid email address 

Sends an email with a reset token (forgot password form) and fetches the user.
Returns Http status code `200 /OK` if succesful or `404 /NOT_FOUND` if there is no user
with submitted email address or `417 /EXPECTATION_FAILED` if an error ocurred during 
sending the email.


### Admin API

##### endpoint: `/api/admin/{userId}/priviledges/add`  
restricted to: `ADMIN` `APPADMIN`  
method: `POST`
path variable `userId` 
request parameters: `priviledges` - as `List<String>`

Add rights to users with given `userId`.  
Rules: 
 - `USER` is not allowed to use this endpoint at all
 - logged in users cannot modify themselves
 - `ADMIN` can add `ADMIN` rights to a `USER` 
 - `ADMIN` cannot add `APPADMIN` rights to neither `USER` nor `ADMIN`
 - `APPADMIN` can add `ADMIN` and`APPADMIN` to `USER` and`ADMIN`
 
Returns Http status code `202 /ACCEPTED` if succesful or `404 /NOT_FOUND` if there is no user
with submitted `userId` or `403 /FORBIDDEN` if the logged in user doesn't have sufficient rights 
for the operation.

##### endpoint: `/api/admin/{userId}/priviledges/remove`  
restricted to: `ADMIN` `APPADMIN`  
method: `POST`
path variable `userId` 
request parameters: `priviledges` - as `List<String>`

Removes rights from users with given `userId`.  
Rules: 
 - `USER` is not allowed to use this endpoint at all
 - logged in users cannot modify themselves
 - `ADMIN` can remove `ADMIN` rights from another `ADMIN` 
 - `ADMIN` cannot remove `APPADMIN` rights from ` APPADMIN`
 - `APPADMIN` can remove `ADMIN` and`APPADMIN` from `ADMIN` and` APPADMIN`
 
Returns Http status code `202 /ACCEPTED` if succesful or `404 /NOT_FOUND` if there is no user
with submitted `userId` or `403 /FORBIDDEN` if the logged in user doesn't have sufficient rights 
for the operation.

##### endpoint: `/api/admin/{userId}/block`  
restricted to: `ADMIN` `APPADMIN`  
method: `POST`
path variable `userId` 

Block (if unblocked) or unblock (if blocked) users with given `userId`.  
Rules: 
 - `USER` is not allowed to use this endpoint at all
 - logged in users cannot block/unblock themselves
 - `ADMIN` can block/unblock `USER` 
 - `ADMIN` cannot block/unblock `APPADMIN` or ` ADMIN`
 - `APPADMIN` can block/unblock `USER` or `ADMIN`
 - `APPADMIN` cannot block/unblock another `APPADMIN`
 
Returns Http status code `202 /ACCEPTED` if succesful or `404 /NOT_FOUND` if there is no user
with submitted `userId` or `403 /FORBIDDEN` if the logged in user doesn't have sufficient rights 
for the operation.

##### endpoint: `/api/admin/{userId}/disable`  
restricted to: `ADMIN` `APPADMIN`  
method: `POST`
path variable `userId` 

Enables/Activates (if disabled) or disables/de-activates (if enabled) users with given `userId`.  
Rules: 
 - `USER` is not allowed to use this endpoint at all
 - logged in users cannot enable/disable themselves
 - `ADMIN` can enable/disable `USER` 
 - `ADMIN` cannot enable/disable `APPADMIN` or ` ADMIN`
 - `APPADMIN` can enable/disable `USER` or `ADMIN`
 - `APPADMIN` cannot enable/disable another `APPADMIN`
 
Returns Http status code `202 /ACCEPTED` if succesful or `404 /NOT_FOUND` if there is no user
with submitted `userId` or `403 /FORBIDDEN` if the logged in user doesn't have sufficient rights 
for the operation.

#### endpoint: `/api/admin/{userId}/reset`  
restricted to: `ADMIN` `APPADMIN`  
method: `POST`
path variable `userId` 

Sends an email with a password reset link and token to users with given `userId`.  
Rules: 
 - `USER` is not allowed to use this endpoint at all
 - logged in users cannot send the reset email to themselves
 - `ADMIN` can reset `USER` 
 - `ADMIN` cannot reset `APPADMIN` or ` ADMIN`
 - `APPADMIN` can reset `USER` or `ADMIN`
 - `APPADMIN` cannot reset another `APPADMIN`
 
Returns Http status code `202 /ACCEPTED` if succesful or `404 /NOT_FOUND` if there is no user
with submitted `userId` or `403 /FORBIDDEN` if the logged in user doesn't have sufficient rights 
for the operation.

#### endpoint: `/api/admin/{userId}/activate`  
restricted to: `ADMIN` `APPADMIN`  
method: `POST`
path variable `userId` 

Sends an email with an activation link and token to users with given `userId`.  
Rules: 
 - `USER` is not allowed to use this endpoint at all
 - logged in users cannot send the activation email to themselves
 - `ADMIN` can activate `USER` 
 - `ADMIN` cannot activate `APPADMIN` or ` ADMIN`
 - `APPADMIN` can activate `USER` or `ADMIN`
 - `APPADMIN` cannot activate another `APPADMIN`
 
Returns Http status code `202 /ACCEPTED` if succesful or `404 /NOT_FOUND` if there is no user
with submitted `userId` or `403 /FORBIDDEN` if the logged in user doesn't have sufficient rights 
for the operation.

#### endpoint: `/api/admin/adduser`  
restricted to: `ADMIN` `APPADMIN`  
method: `POST`
consumes: `multipart/form-data`
request parameters: 
 - `name` - required
 - `email` - required, syntactically valid email address
 - `role` - optional, default value `USER`, valid options: `USER` | `ADMIN` | `APPADMIN`
 
Rules:
- `USER` is not allowed to use this endpoint at all
 - `ADMIN` can create a new `USER` and `ADMIN`
 - `ADMIN` cannot create a new `APPADMIN`
 - `APPADMIN` can create a new`USER`, `ADMIN` and `APPADMIN`
 
Returns Http status code `201 /CREATED` if succesful or `409 /CONFLICT` if there is already
a user with submitted `name` or `email`, or `403 /FORBIDDEN` if the logged in user doesn't 
have sufficient rights for the operation.


# Technologies
- Java JDK 1.8
- Maven 3.60
- Spring Boot 2.0 with Spring Security 5, Spring Validation and Spring Data
- JPA (Hibernate implementation)
- MySQL
- Flying Saucer 9.1.6
- JUnit5 5.4.1
- AssertJ 3.12.2
- Hamcrest 1.3
- Mockito 2.24
- Rest Assured 4.0.0
- Json Path 4.0
- IntelliJ IDEA. 

# Tests

Test written using JUnit5 and Harmcrest. Integration and slice test make use of 
Spring MockMvc, Test Rest Template and Rest Assured. 

Integration tests rely on entries loaded from *data.sql* script at the start of the application.
Additionally, some tests execute their own local .sql scripts.

# Status
Finished.

Bug fixing only.

# License
Enquiry System is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
Enquiry System is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with Enquiry System. 
If not, see http://www.gnu.org/licenses/ or write to: latidude99@gmail.com

# Contact
You can email me at latidude99@gmail.com


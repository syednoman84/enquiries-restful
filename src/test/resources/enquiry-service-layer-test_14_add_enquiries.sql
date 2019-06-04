/*delete from enquiry where name="Customer A";
delete from enquiry where name="Customer B";
delete from enquiry where name="Customer C";
*/
INSERT INTO enquiry(name, email, type, created_date, status, closed_date, message) VALUES ("Customer A", "a.cust@gmail.com", "General Information", "2018-08-05 12:45:24", "waiting", null, "1st enquiry");
INSERT INTO enquiry(name, email, type, created_date, status, closed_date, message) VALUES ("Customer B", "b.cust@gmail.com", "Maps", "2017-05-23 15:11:54", "in progress", null, "2nd enquiry");
INSERT INTO enquiry(name, email, type, created_date, status, closed_date, message) VALUES ("Customer C", "c.cust@gmail.com", "Customised Mapping", "2017-11-16 10:25:27", "closed", "2017-12-06 09:21:17", "3rd enquiry");
INSERT INTO enquiry(name, email, type, created_date, status, closed_date, message) VALUES ("Customer D", "d.cust@gmail.com", "Customised Mapping", "2017-01-18 11:45:47", "closed", "2017-02-06 07:21:11", "4th enquiry");
INSERT INTO enquiry(name, email, type, created_date, status, closed_date, message) VALUES ("Customer E", "e.cust@gmail.com", "Maps", "2017-10-11 09:16:22", "in progress", "2017-11-06 11:26:13", "5th enquiry");
INSERT INTO enquiry(name, email, type, created_date, status, closed_date, message) VALUES ("Customer F", "f.cust@gmail.com", "Maps", "2019-10-12 08:15:29", "waiting", null, "6th enquiry");
INSERT INTO enquiry(name, email, type, created_date, status, closed_date, message) VALUES ("Customer G", "g.cust@gmail.com", "Maps", "2017-03-12 11:41:24", "in progress", "2018-04-22 17:11:14", "7th enquiry");
INSERT INTO enquiry(name, email, type, created_date, status, closed_date, message) VALUES ("Customer H", "h.cust@gmail.com", "Customised Mapping", "2018-05-12 06:14:59", "closed", "2018-06-28 09:21:17", "8th enquiry");


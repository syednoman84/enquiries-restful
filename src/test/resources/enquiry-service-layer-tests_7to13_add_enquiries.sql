/*delete from enquiry where name="Customer A";
delete from enquiry where name="Customer B";
delete from enquiry where name="Customer C";
*/
INSERT INTO enquiry(name, email, type, created_date, status, closed_date) VALUES ("Customer A", "a.cust@gmail.com", "General Information", "2018-08-05 12:45:24", "waiting", null);
INSERT INTO enquiry(name, email, type, created_date, status, closed_date) VALUES ("Customer B", "b.cust@gmail.com", "Maps", "2017-05-23 15:11:54", "in progress", null);
INSERT INTO enquiry(name, email, type, created_date, status, closed_date) VALUES ("Customer C", "c.cust@gmail.com", "Customised Mapping", "2017-11-16 10:25:27", "closed", "2017-12-06 09:21:17");


INSERT INTO user(name, email, password) VALUES ("Test User USER", "test_user_USER@test.com", "plain_text_password_USER");
INSERT INTO user(name, email, password) VALUES ("Test User ADMIN", "test_user_ADMIN@test.com", "plain_text_password_ADMIN");
INSERT INTO user(name, email, password) VALUES ("Test User APPADMIN", "test_user_APPADMIN@test.com", "plain_text_password_APPADMIN");

INSERT INTO user_roles(user_id, roles_id) VALUES ("5", "1");
INSERT INTO user_roles(user_id, roles_id) VALUES ("6", "2");
INSERT INTO user_roles(user_id, roles_id) VALUES ("7", "3");
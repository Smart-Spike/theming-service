CREATE DATABASE themingdb;

CREATE USER 'themingdbuser' IDENTIFIED BY 'themingdbuser';
GRANT ALL PRIVILEGES ON themingdb.* TO 'themingdbuser';
FLUSH PRIVILEGES;

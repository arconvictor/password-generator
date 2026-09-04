create user testUser identified by "password";
grant connect to testUser;

grant DBA to testUser;
grant SELECT any table to testUser;

CREATE USER dummy1 IDENTIFIED BY "password";
grant connect to dummy1;

CREATE USER dummy2 IDENTIFIED BY "password";
grant connect to dummy2;

CREATE USER dummy3 IDENTIFIED BY "password";
grant connect to dummy3;
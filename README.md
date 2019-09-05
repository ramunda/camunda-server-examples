# camunda-server-examples
Generic camunda server with examples

### Walk-through

1. Clone git repository https://github.com/ramunda/camunda-server-examples.git.
2. Import Maven project to your IDE.
3. By default this server uses POSTGRES, so you need to download it and install it.
4. Create a Database with the name camunda in POSTGRES. If you decide that you want another name you must change the file application.properties to the match the new name.
5. Fill the fields with username and password refering to the created database in application.properties.
6. Create the necessary tables in POSTGRES. You can achive this by running the sript in the file “TableCreation.sql” from the package "repo".
7. Fill the created tables from the step before by executing the scripts in the file “TableInserts.sql” from the package "repo".
8. [IMPORTANT!] Some service tasks require some fields to be previously fullfilled. An example is the process "generatePaymentRefAndNotification", this process requires that before running the server some values must be attributed to the fields of the service task "SendEmail" and changing the values of SMTP host and SMTP port.
9. Run the Application by executing the class "CamundaApplication".

### Tests
1. In case you desire to run the tests you only need to have in mind that these run in another POSTGRES database, so you must create this database. The name of the database is "camunda-tests", if you want another name you must change the the "application.properties" file found in the following path, "src\test\resources\application.properties".
2. You must also fill the fields username and password in "application.properties" with the values of this new database.
3. Run tests.

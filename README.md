# Database Project 2
 
# Thomas's Thoughts and getting started.
**He said we would need**
- MySQL Community Server
- Connector/J
- Workbench

Turns out you don't need workbench. That's for the non cmdline folks

Install the github from [Here](github.com/datacharmer/test_db)
 

 My boi. That tutorial Sucked. Get the MySQL stuff downloaded on your computer. The tutorial commands may not work. Use ChatGPT to debug them. 

 (Here is a text version of the file structure in case you need to explain what the project looks like to a friend who only speaks binary :)



    
# How to run this
Connector J is already installed in the lib folder so you don't need to worry about that at all. You may however need to delete the entire mysql-connector-j-9.0.0 DIRECTORY (not the jar file please) in order to get the program to run

then you are going to need MySQL community server downloaded on your computer
- follow the tutorial and create a database on your computer using the text_db-master folder
    - If that doesn't make sense, follow the tutorial and it should make sense as you go. Basically populate your database using the github repo on your computer

After that replace my password in DatabaseApp.java with your database password
Run the java program

We need to make that process simpler. 
If it's true that you have to delete that directory, then we need to add it to a gitignore
Also we should make the password ignorable or we should make the program prompt for your password

### Degrees of Seperation

**1 Degree Apart**
- 10001 and 10006
- 10001 and 100


**2 degrees Apart**
- 10001 and 10327
Employee ID: 10008
Employee ID: 10012
Employee ID: 10014
Employee ID: 10018
Employee ID: 10021
Employee ID: 10022
Employee ID: 10023
Employee ID: 10025
Employee ID: 10027
Employee ID: 10028
Employee ID: 10031
Employee ID: 10037
Employee ID: 10040
Employee ID: 10043
Employee ID: 10048
Employee ID: 10056
Employee ID: 10057
Employee ID: 10062
Employee ID: 10065
Employee ID: 10066
Employee ID: 10070
Employee ID: 10072
Employee ID: 10074
Employee ID: 10075
Employee ID: 10076
Employee ID: 10078
Employee ID: 10079
Employee ID: 10090
Employee ID: 10091
Employee ID: 10092
Employee ID: 10103
Employee ID: 10116
Employee ID: 10118
Employee ID: 10121
Employee ID: 10122
Employee ID: 10127
Employee ID: 10129
Employee ID: 10134
Employee ID: 10139
Employee ID: 10142
Employee ID: 10143
Employee ID: 10145
Employee ID: 10150
Employee ID: 10153
Employee ID: 10157
Employee ID: 10166
Employee ID: 10169
Employee ID: 10172
Employee ID: 10179
Employee ID: 10181
Employee ID: 10182
Employee ID: 10189
Employee ID: 10191
Employee ID: 10193
Employee ID: 10197
Employee ID: 10198
Employee ID: 10201
Employee ID: 10202
Employee ID: 10204
Employee ID: 10206
Employee ID: 10207
Employee ID: 10211
Employee ID: 10212
Employee ID: 10219
Employee ID: 10223
Employee ID: 10227
Employee ID: 10238
Employee ID: 10244
Employee ID: 10249
Employee ID: 10258
Employee ID: 10261
Employee ID: 10264
Employee ID: 10268
Employee ID: 10271
Employee ID: 10274
Employee ID: 10275
Employee ID: 10276
Employee ID: 10278
Employee ID: 10283
Employee ID: 10287
Employee ID: 10289
Employee ID: 10290
Employee ID: 10294
Employee ID: 10305
Employee ID: 10309
Employee ID: 10311
Employee ID: 10315
Employee ID: 10318
Employee ID: 10322
Employee ID: 10324
Employee ID: 10327
Employee ID: 10328
Employee ID: 10333
Employee ID: 10342
Employee ID: 10345
Employee ID: 10352
Employee ID: 10358
Employee ID: 10359
Employee ID: 10366

# Improvements

For the demo video, we need to demonstrate the use of 1 and 2 degrees of seperation but we don't have any example employees to use. 

Maybe we create a function such that you can input an employee ID number and a degree like 1 degree of seperation, 2 degrees etc. and it will output all employees with EXACTLY that many degrees of seperation
Also if the output exceeds 100 it only displays the first 100 tuples. 



# Information for Binary Friend

user: root 
Password: BigBoyServerProject2!

This is the output when I type the tree command in the database-project-2 folder

 DATABASE-PROJECT-2/
├── lib/
│   └── mysql-connector-java-x.x.xx.jar  // MySQL Connector/J JAR file
├── src/
│   └── DatabaseApp.java                   
└── text_db-master/
    ├── employees.sql
    ├───images
    └───sakila


typing ls Inside test_db-master:
Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d-----         9/25/2024  12:38 PM                images
d-----         9/25/2024  12:38 PM                sakila
-a----         9/25/2024  12:38 PM            989 Changelog
-a----         9/25/2024  12:38 PM           4320 employees.sql
-a----         9/25/2024  12:38 PM           6453 employees_partitioned.sql
-a----         9/25/2024  12:38 PM           8147 employees_partitioned_5.1.sql
-a----         9/25/2024  12:38 PM            260 load_departments.dump
-a----         9/25/2024  12:38 PM       14491483 load_dept_emp.dump
-a----         9/25/2024  12:38 PM           1138 load_dept_manager.dump
-a----         9/25/2024  12:38 PM       18022856 load_employees.dump
-a----         9/25/2024  12:38 PM       40773364 load_salaries1.dump
-a----         9/25/2024  12:38 PM       40752967 load_salaries2.dump
-a----         9/25/2024  12:38 PM       40010648 load_salaries3.dump
-a----         9/25/2024  12:38 PM       22152044 load_titles.dump
-a----         9/25/2024  12:38 PM           4793 objects.sql
-a----         9/25/2024  12:38 PM           4440 README.md
-a----         9/25/2024  12:38 PM            277 show_elapsed.sql
-a----         9/25/2024  12:38 PM           1872 sql_test.sh
-a----         9/25/2024  12:38 PM           4833 test_employees_md5.sql
-a----         9/25/2024  12:38 PM           4835 test_employees_sha.sql
-a----         9/25/2024  12:38 PM           2106 test_versions.sh


inside sakila:
Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
-a----         9/25/2024  12:38 PM            342 README.md
-a----         9/25/2024  12:38 PM        3398269 sakila-mv-data.sql
-a----         9/25/2024  12:38 PM          24121 sakila-mv-schema.sql

The list of predefined SQL queries to be implemented in the application are as follows (consider both past and current data when answering all queries):  
- List department(s) with maximum ratio of average female salaries to average men salaries 
- List manager(s) who holds office for the longest duration. A person can be a manager for multiple departments at different time frames.
- For each department, list number of employees born in each decade and their average salaries
- List employees, who are female, born before Jan 1, 1990, makes more than 80K annually and hold a manager position
- Find 1 degree of separation between 2 given employees E1 and E2:o1 degree: E1 --> D1 <-- E2 (E1 and E2 work at department D1 at the same time)
- Find 2 degrees of separation between 2 given employees E1 and E2:o2 degrees: E1 --> D1 <-- E3 --> D2 <-- E2 (E1 and E3 work at D1 at the same time; E3 and E2 work at D2 at the same time



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

Run the java program
Input your database password
Select your query

We need to make that process simpler. 
If it's true that you have to delete that directory, then we need to add it to a gitignore

### Degrees of Seperation

**1 Degree Apart**
Enter the name of E1: Georgi Facello
Enter the name of E2: Anneke Preusig

- 10001 and 100


**2 degrees Apart**
Georgi Facello
1
Domenick Tempesti

Guther Holburn
Anwar Krybus


# Issues

The second file seems to work except for 

Q6

It contains duplicates. (employees can be 1 degree away from themselves)
And it only outputs one example of 2 degrees of separation when there are potentially very many


# NewMovieServer
1. What is it?
This is a pet project of localhost HTTP server written on Scala. It connects to Postgres SQL database, creates and operates relative tables of movies, movie directors and actors; and allows you to send different queries by hitting endpoints in your browser (or HTTP test instruments like Postman). The internal interaction logic is built on Akka Actors, and the endpoints are written with Akka HTTP. Interacting to PSQL database is provided by JDBC Driver.
2. Why did I made it?
For personal training. That was fun.
3. What is consists of?
The main Scala code is written in two packages with two objects in each:
- SqlMethods contains all the methods that we need to interact with SQL database;
- SqlLoader contains all the data to fill the database (to test it and perform some actions);
- RequestHandler describes endpoints and activates HTTP connection;
- ServerCore describes Akka Actor logic that implemented to simplify endpoints writing (and also for training, yes).
Also, there is a directory with “full movie” JSON file to test POST method.   
4. How does it work?
First, the PostgreSQL database system is must be installed and running (check the port – is it standard 5432 or not) on your computer.
Second, you need to open in and create empty database “movies_base_demo” (you can call it other way, but so make sure you changed DB name in ServerCore class in Scala project). 
Third, open project and go to ServerCore class. Replace my values of user and password (“postgres” and “qwe”) with yours (and check dbName, as I mentioned).   
Make sure you loaded sbt chandes. After that all is ready to work. Go SqlLoader object and run it. If you made all actions above, after the compiling it will create some tables your new database (movies_base_demo) and fill them with the data.
Once the process is done, go to RequestHandler object and run it. It runs at localhost:8088 by default. You will see message “server is running…” in the console of your IDE. Now you able to hit endpoints in browser (localhost:8088/api…).
Endpoints:
- GET api/movies/  - returns all movies from db as list of JSONs
- GET api/movies/your_title - returns JSON of movie with requested title "your_title"
- GET api/movies?=your_title - does the same
- GET api/movies?search=your_search_request - returns list of movie JSONs where first characters in title matches to your_search_request
(for example "?search=the" returns “the terminator”, “the godfather”, “the english patient”)
describes- POST api/movies/addFullMovie adds to database a "full movie" JSON file (with movie, director and actor).
Example - "green elephant" in main/json directory. Separate endpoints for movie, director and actor are not implemented to avoid conflicts in related tables of database for this project.
Method called by this endpoint puts movie, director and actor in different tables, "directors" related with "movies" with external table (movie_id - director_id),
"actors" also related with "movies" with external table (movie_id - actor_id).
- DELETE api/movies/your_title deletes movie with given title from database (also deletes director and actor in this project).
I hope it works on your machine. It was interesting job to write it!


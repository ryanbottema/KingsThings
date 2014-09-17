TEAM 17
COMP 3004 Winter 2014
Tyler Babin
Ryan Bottema
Brandon Schurman
---------------------

Type of Game: Server-client Desktop Application

Language and Resources:
    Java (requires at least 1.7)
    JavaFX graphics library.
        There is a .jar file called jfxrt.jar located in your Java install location/jre/lib. This MUST be included in your classpath if running the game via command line, or if using eclipse add a new user library for this jfxrt.jar file.

Running the Game:
    Using the command line
        1) Make sure jfxrt.jar is added to your CLASSPATH system variable.
        2) Compile the code using javac
            javac Application/*.java -d .
        3) Run the Game file
            java KAT.Game
    Using Eclipse
        1) Open Eclipse and select File > Import
        2) Select General > Existing Project into Workspace.
        3) Choose "Select archive file" and browse to the KingsAndThings.zip file
        4) Finish, then run the project

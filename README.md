# Code Challenge - Contestant
## Setup
### 1 Ensure Java is installed and set-up
You need a JDK installed of version 9 or later. If you don't have this, you can download one from the same machine this
site is running on. In your web browser, navigate to `http://<host>:8081/repository/java/jdk-windows.zip` or
`http://<host>:8081/repository/java/jdk-linux.tgz`, where `<host>` is the same host name as this Git server is on.
Extract the downloaded archive onto your hard drive.

Make sure you also have your `JAVA_HOME` environment variable set to the path of the correct JDK folder.
In Windows, search for 'env' in your Start Menu, then follow the prompts. In Linux, edit your `~/.profile`
or `~/.bashrc` file appropriately.

### 1a (Optional) Install Git
It may help to have Git installed. On Windows, download Git for Windows [here](https://gitforwindows.org/).
This also gives you access to a Linux-style Bash shell terminal called Git Bash.

### 2 Download this project
If you have Git installed, you can clone the repository to your local machine using the clone URL above.
Alternatively, there's also a link above to download the repository as a zip or tar archive.

A third option, which may make working with your team mates easier, is to register a username on this git server,
then fork the project and clone it from there. That will allow you to share code with your team mates by pushing
commits back up to your forked project.

### 3 Setup build proxy paths

Configure internet settings used to build this project.  This allows us to set a proxy that will be used to download
Gradle and Maven dependencies used when building the project.

Windows command prompt:
```batch
install -r http://<host>:8081/repository
```

Unix shell:
```sh
./install.sh -r http://<host>:8081/repository
```

In both of the above, `<host>` is the same hostname as this site is on.
For example if the URL of this web page is `http://WS01161:3000/ScottLogic/hackathon-contestant`, then (on Windows):
```batch
install -r http://WS01161:8081/repository
```

### 4 (Optional) Install and import into a Java IDE
It will probably be easier to do development in a Java IDE - ideally one that supports importing Gradle projects, like
Eclipse or IntelliJ IDEA. We recommend IntelliJ. Make sure you've set your JDK to one with at least version 9 in
your IDE settings.

In IntelliJ if you open the repostory's root folder as a new project, it should detect Gradle and start the import
wizard automatically. In Eclipse, choose `File -> Import... -> Gradle`.

## Building & Running

The repository includes a pre-compiled game simulator application.
It can be run from either the command line, or your IDE.

After each phase of the game, the application prints an ASCII-art representation of the game's current state to the
console. You can review this before hitting `Enter` to play the next phase.
Since the printed state takes up quite a few lines in the console,
you may find that your IDE's output console can't be made big enough to properly review it,
in which case you may prefer to build and run the game from a command-line terminal.   

### From your IDE

There's a `void main(...)` method in the `ExampleBot` class. You can use that to run or debug a game simulation from
within your IDE.

### From your command line terminal

Open a command line terminal in the root folder of this project and run one of the following commands.

Windows command prompt:
```batch
gradlew run -P mainClass=<your_bot_class_fully_qualified_name>
```

Unix shell:
```sh
./gradlew run -P mainClass=<your_bot_class_fully_qualified_name>
```

### Game Output
The turn-by-turn game output is rendered as an ascii-art representation of the board with the following key:
 - `A`-`D`: spawn point
 - `a`-`d`: player
 - `X`: out of bounds
 - `+`: collectable

With the initial bot you should see a very short game consisting of 7 phases ending due to the `LONE_SURVIVOR` end
condition.  The turn-by-turn replay should show 8 players appearing from one spawn point (don't get your hopes up, these
belong to the default bot), and none appearing from the other spawn point.  This is because the initial bot does not
issue any orders, resulting in each player being eliminated by the next player to emerge from the spawn point.

If you have successfully reached this point then you're all set to start improving on the initial bot to ensure your
players have a longer, and perhaps more prosperous, life.

## Next Steps
First things first, rename the bot class so that there are no namespace clashes when uploading to the server,
additionally you might want to change the display name which is shown when testing locally.  There are only a few
restrictions on the compiled code:
- the jar file must be < 20MB
- the com.contestantbots.team package should only contain classes that extend `com.scottlogic.hackathon.game.Bot`
- any helper or utility classes should either be
  - inner classes of your Bot, or
  - not have a public constructor
- your bot should take no more than 5 seconds to calculate the moves otherwise it will be timed out
- you can include more than one bot class in the uploaded jar file to allow you to test different strategies, however
only one bot can be active at any given time

When you're ready to move on this [tutorial](docs/tutorial/index.md) provides a step-by-step guide to adding
some basic intelligence to your bot.

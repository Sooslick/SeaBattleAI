# Battleships game

Simple implementation of the Battleships game as RESTful web service and AI to play with.

### Build and execute

Build and launch JAR by executing maven goals

> `mvn clean compile assembly:single exec:java@server`

As soon as app is started, service will be available at port 65535
(unless another port is specified in `app.properties`)

### API Documentation

[Documentation](https://htmlpreview.github.io/?https://github.com/Sooslick/SeaBattleAI/blob/master/apiDoc.html)

[Local file doc](apiDoc.html)

### AI flags

Launch AI from the same jar by executing maven goal

> `mvn exec:java@ai -Dexec.args="-param1 -param2=value ..."`

Common parameters list:

`host`: battleships server address. Default host is http://localhost:65535;

`sessionId`: ID of session to join. If parameter was not specified, AI will create new session;

`sessionPw`: password of private session to join;

`useHeatMap`: Smart mode flag.

AI has Simple and Smart modes. Simple mode is enabled by default and this mode requires no data at all.
Smart mode collects and stores some game statistics, and you can use following parameters to change this behaviour:

`heatDir`: collected data directory;

`ignorePost`: AI will not update collected game data if presents;

`analyze`: AI will analyze collected datafiles that are stored in heatDir/analyze directory. If presents, AI will only update data
and will stop after analysis.
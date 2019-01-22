# Excel Web Service

[![CircleCI](https://circleci.com/gh/NatelEnergy/excel-server/tree/master.svg?style=svg)](https://circleci.com/gh/NatelEnergy/excel-server/tree/master)[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Simple web service to execute exel spreadsheets.


# Build and Run with maven

```
mvn exec:java  
```

This will build and run the server on:
http://localhost:8080/


# Build / Deploy Docker

```
mvn clean install
docker build -t upstream-excel .
docker run -p 8080:8080 -p 8081:8081 upstream-excel
```


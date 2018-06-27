# Excel Web Service

[![CircleCI](https://circleci.com/gh/NatelEnergy/excel-server/tree/master.svg?style=svg)](https://circleci.com/gh/NatelEnergy/excel-server/tree/master)

Simple web service to execute exel spreadsheets.



# Build / Deploy Docker

```
mvn clean install
docker build -t upstream-excel .
docker run -p 8080:8080 -p 8081:8081 upstream-excel
```


# Excel Web Service

This lets you execute exel spreadsheets in a web service


# Build / Deploy Docker


```
mvn clean install
docker build -t upstream-excel .
docker run -p 8080:8080 -p 8081:8081 upstream-excel
```


To build/deploy:
```
make
```


To log the activity:
```
kubectl get pods -l type=excel-server
kubectl logs -f excel-servers-XXX
```


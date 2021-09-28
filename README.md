# akka-http-prometheus-metrics
This is a simple akka http application which adds a new custom label to the existing metrics.

### Add the dependency to get the metrics
```
    <dependency>
            <groupId>fr.davit</groupId>
            <artifactId>akka-http-metrics-prometheus_2.12</artifactId>
            <version>1.1.1</version>
    </dependency>
 ```
### Generate the metrics
Run the application and hit the below url:
 ```http://localhost:8080/user?userId=4```

### Check the metrics
 Hit the below url:
 ```http://localhost:8080/admin/prometheus/metrics```

### Add a custom label to existing metrics
Added custom label ```userId``` in existing metrics
 
```akka_http_responses_duration_seconds_count{method="GET",path="unlabelled",status="2xx",userId="4",} 1.0```
# akka-http-prometheus-metrics
This is a simple akka http application which exposes a new custom metric in Prometheus format.

### Add the dependency to get the metrics
```
    <dependency>
            <groupId>fr.davit</groupId>
            <artifactId>akka-http-metrics-prometheus_2.12</artifactId>
            <version>1.1.1</version>
    </dependency>
 ```
    
 ### Check the metrics
 Run the application and hit the below url:
 ```  
 http://localhost:8080/admin/prometheus/metrics
 
  ```
### New custom metrics
 Added a new custom metric ```akka_http_request_by_user_id``` which has one label
 ```userId```.
 We can add as many as label we want.
 
```akka_http_request_by_user_id{userId="3",} 1.0```
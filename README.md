# akka-http-prometheus-metrics
This is a simple akka http application which exposes the metrics in Prometheus format.

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

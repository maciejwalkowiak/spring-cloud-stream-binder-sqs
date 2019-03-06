# Spring Cloud Stream Binder SQS Sample - Source and Sink

Producing message using `Source` and consuming with `Sink`. 

Application has two Spring profiles

- default - runs in non-partitioned mode
- partitioned - creates two partitions

In order to run application in partitioned mode you need to specify `--spring.profiles-active=partitioned  --spring.cloud.stream.instanceIndex=X` where `instanceIndex` can be either `0` or `1`.

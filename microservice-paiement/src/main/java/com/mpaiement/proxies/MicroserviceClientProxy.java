package com.mpaiement.proxies;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "microservice-client", url = "localhost:8080")
public interface MicroserviceClientProxy {
}

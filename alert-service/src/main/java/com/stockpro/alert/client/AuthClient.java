package com.stockpro.alert.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "auth-service", path = "/auth")
public interface AuthClient {
    @GetMapping("/ids-by-role/{role}")
    List<Long> getIdsByRole(@PathVariable("role") String role);
}

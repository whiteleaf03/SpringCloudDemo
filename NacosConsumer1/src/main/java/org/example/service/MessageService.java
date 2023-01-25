package org.example.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author WhiteLeaf03
 */
@FeignClient(value = "NacosProvider", contextId = "MessageService")
public interface MessageService {
    @GetMapping("/message")
    String getMessage();
}

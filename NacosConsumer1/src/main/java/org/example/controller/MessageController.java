package org.example.controller;

import org.example.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author WhiteLeaf03
 */
@RestController
public class MessageController {
    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;
    private final MessageService messageService;

    @Autowired
    public MessageController(DiscoveryClient discoveryClient, RestTemplate restTemplate, MessageService messageService) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
        this.messageService = messageService;
    }

    //Feign远程调用
    @GetMapping("/message")
    public String getMessage() {
        return messageService.getMessage();
    }

    //Ribbon负载均衡
//    @GetMapping("/message")
//    public String getMessage() {
//        return restTemplate.getForObject("http://NacosProvider/message", String.class);
//    }

    //手动选择微服务
//    @GetMapping("/message")
//    public String getMessage() {
//        List<ServiceInstance> instances = discoveryClient.getInstances("NacosProvider"); //根据服务名获取微服务实例列表
//        ServiceInstance instance = instances.get(0); //取出微服务实例
//        restTemplate.getForObject("http://" + instance.getHost() + ":" + instance.getPort() + "/message", String.class); //调用微服务
//    }
}

package com.microwise.tracker.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2018/5/2.
 */
@RestController
public class IndexController {
    @RequestMapping("/")
    public String index() {
        return "welcome to tracker";
    }
}

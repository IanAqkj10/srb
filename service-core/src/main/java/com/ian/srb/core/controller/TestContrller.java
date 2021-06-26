package com.ian.srb.core.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author:IanJ
 * @date:2021/6/14 1:51
 */
@RestController
public class TestContrller {

    @RequestMapping("/a")
    public  String  test(){

        return "qaq";
    }
}

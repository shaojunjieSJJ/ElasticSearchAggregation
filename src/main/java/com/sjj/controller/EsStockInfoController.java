package com.sjj.controller;

import com.sjj.service.EsStockInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 邵少 on 2019/10/12.
 */
@RequestMapping("/esStockInfoController")
@RestController
public class EsStockInfoController {

    @Autowired
    private EsStockInfoService esStockInfoService;

    @PostMapping("/listSaves")
    public String listSaves() {
        esStockInfoService.listSaves();
        return "ok";
    }

    /**
     * 通过商品类型id分组求和
     */
    @GetMapping("/aggregationByTypeId")
    public void aggregationByTypeId(){
        esStockInfoService.aggregationByTypeId();
    }

    /**
     * 通过商品类型id和仓库id分组求和
     */
    @GetMapping("/aggregationByTypeIdAndHouseId")
    public void aggregationByTypeIdAndHouseId(){
        esStockInfoService.aggregationByTypeIdAndHouseId();
    }


}

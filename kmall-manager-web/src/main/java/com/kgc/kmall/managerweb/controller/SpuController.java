package com.kgc.kmall.managerweb.controller;

import com.kgc.kmall.bean.*;
import com.kgc.kmall.service.SpuService;
import org.apache.commons.io.FilenameUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author shkstart
 * @create 2020-08-30 14:01
 */
@CrossOrigin
@RestController
public class SpuController {

    @Reference
    SpuService spuService;

//    @Value("${fileServer.url}")
//    String fileUrl;

    @RequestMapping("/spuList")
    public List<PmsProductInfo> spuList(Integer catalog3Id){
        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);
        return pmsProductInfos;
    }

    @RequestMapping("/fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {

        //文件上传
        //返回文件上传后的路径
//        String imgUrl=fileUrl;
//        if(file!=null){
//            System.out.println("multipartFile = " + file.getName()+"|"+file.getSize());

            String configFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);
            TrackerClient trackerClient=new TrackerClient();
            TrackerServer trackerServer=trackerClient.getTrackerServer();
            StorageClient storageClient=new StorageClient(trackerServer,null);
            String filename=    file.getOriginalFilename();
            String extName = FilenameUtils.getExtension(filename);

            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
//            imgUrl=fileUrl ;
                  String imgUrl="http://192.168.183.135";
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                imgUrl+="/"+path;
            }
//        }
        System.out.println(imgUrl);
        return imgUrl;
    }

    @RequestMapping("/baseSaleAttrList")
    public  List<PmsBaseSaleAttr> baseSaleAttrList(){
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = spuService.baseSaleAttrList();
        return pmsBaseSaleAttrs;
    }


    @RequestMapping("/saveSpuInfo")
    public Integer saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        Integer integer = spuService.saveSpuInfo(pmsProductInfo);
        return integer;
    }

    @RequestMapping("/spuSaleAttrList")
    public  List<PmsProductSaleAttr> spuSaleAttrList(Integer spuId){
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrList(spuId);
        return pmsProductSaleAttrs;
    }

    @RequestMapping("/spuImageList")
    public List<PmsProductImage> spuImageList(Long spuId){
        List<PmsProductImage> pmsBaseSaleAttrs= spuService.spuImageList(spuId);
        return  pmsBaseSaleAttrs;
    }
}

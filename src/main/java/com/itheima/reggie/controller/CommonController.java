package com.itheima.reggie.controller;


import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    public String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        log.info(file.toString());

        //获取文件原始名称 但是为了防止重名覆盖 我们一般会随机生成一个
        String originalFilename = file.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID重新生成文件名 防止重名文件覆盖
        String fileName = UUID.randomUUID().toString()+substring;

        //创建目录对象
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        //注意这里的file是临时文件 我们需要转存
        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            FileInputStream fis = new FileInputStream(new File(basePath + name));


            ServletOutputStream os = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];

            while((len=fis.read(bytes))>0){
                os.write(bytes,0,len);
                os.flush();
            }

            fis.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}

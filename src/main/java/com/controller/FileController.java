package com.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.ConfigEntity;
import com.entity.EIException;
import com.service.ConfigService;
import com.utils.R;

/**
 * 上传文件映射表
 */
@RestController
@RequestMapping("file")
@SuppressWarnings({"unchecked","rawtypes"})
public class FileController{
	@Autowired
    private ConfigService configService;
	
	// 从配置文件中获取上传路径
	@Value("${web.upload-path}")
	private String uploadPath;
	
	/**
	 * 上传文件
	 */
	@RequestMapping("/upload")
	public R upload(@RequestParam("file") MultipartFile file,String type) throws Exception {
		if (file.isEmpty()) {
			throw new EIException("上传文件不能为空");
		}
		String fileExt = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
		
		// 确保上传目录存在
		File uploadDir = new File(uploadPath);
		if(!uploadDir.exists()) {
		    uploadDir.mkdirs();
		}
		
		// 使用时间戳作为文件名
		String fileName = new Date().getTime()+"."+fileExt;
		File dest = new File(uploadDir, fileName);
		file.transferTo(dest);
		
		// 同时保存到classpath目录，兼容旧的访问方式
		try {
			// 保存到src/main/resources/static/upload/
			File staticPath = new File(System.getProperty("user.dir") + "/src/main/resources/static/upload/");
			if(!staticPath.exists()) {
				staticPath.mkdirs();
			}
			FileUtils.copyFile(dest, new File(staticPath, fileName));
			
			// 保存到target/classes/static/upload/
			File targetPath = new File(System.getProperty("user.dir") + "/target/classes/static/upload/");
			if(!targetPath.exists()) {
				targetPath.mkdirs();
			}
			FileUtils.copyFile(dest, new File(targetPath, fileName));
		} catch (IOException e) {
			// 即使复制失败，也不影响主要功能
			System.out.println("复制文件到resources或target目录失败: " + e.getMessage());
		}
		
		if(StringUtils.isNotBlank(type) && type.equals("1")) {
			ConfigEntity configEntity = configService.selectOne(new EntityWrapper<ConfigEntity>().eq("name", "faceFile"));
			if(configEntity==null) {
				configEntity = new ConfigEntity();
				configEntity.setName("faceFile");
				configEntity.setValue(fileName);
			} else {
				configEntity.setValue(fileName);
			}
			configService.insertOrUpdate(configEntity);
		}
		return R.ok().put("file", fileName);
	}
	
	/**
	 * 下载文件
	 */
	@IgnoreAuth
	@RequestMapping("/download")
	public ResponseEntity<byte[]> download(@RequestParam String fileName) {
		try {
			// 优先从固定上传目录获取文件
			File file = new File(uploadPath, fileName);
			
			// 如果固定目录不存在，尝试从classpath获取
			if(!file.exists()) {
				try {
					File path = new File(ResourceUtils.getURL("classpath:static").getPath());
					if(path.exists()) {
						File upload = new File(path.getAbsolutePath(),"/upload/");
						if(upload.exists()) {
							file = new File(upload.getAbsolutePath()+"/"+fileName);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(file.exists()){
				HttpHeaders headers = new HttpHeaders();
			    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);    
			    headers.setContentDispositionFormData("attachment", fileName);    
			    return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),headers, HttpStatus.CREATED);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}

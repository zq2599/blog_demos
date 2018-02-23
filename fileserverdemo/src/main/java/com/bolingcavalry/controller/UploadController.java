package com.bolingcavalry.controller;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.UUID;


@RestController
public class UploadController {

	protected static final Logger logger = LoggerFactory.getLogger(UploadController.class);

	//生成上传文件的文件名，文件名以：uuid+"_"+文件的原始名称
	public String mkFileName(String fileName){
		return UUID.randomUUID().toString()+"_"+fileName;
	}

	public String mkFilePath(String savePath,String fileName){
		//得到文件名的hashCode的值，得到的就是filename这个字符串对象在内存中的地址
		int hashcode = fileName.hashCode();
		int dir1 = hashcode&0xf;
		int dir2 = (hashcode&0xf0)>>4;
		//构造新的保存目录
		String dir = savePath + "\\" + dir1 + "\\" + dir2;
		//File既可以代表文件也可以代表目录
		File file = new File(dir);
		if(!file.exists()){
			file.mkdirs();
		}
		return dir;
	}

	public void responseAndClose(HttpServletResponse response, String message){
		response.reset();
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
		}catch (IOException exception){
			logger.error("get writer error, ", exception);
		}

		if(null!=out){
			if (!StringUtils.isEmpty(message)) {
				out.print(message);
			}

			out.close();
		}
	}

	@RequestMapping("/upload")
	public void upload(HttpServletRequest request, HttpServletResponse response) throws Exception{
		logger.info("start upload");
		//得到上传文件的保存目录，将上传的文件存放于WEB-INF目录下，不允许外界直接访问，保证上传文件的安全
		String savePath = request.getServletContext().getRealPath("/WEB-INF/upload");
		//上传时生成的临时文件保存目录
		String tempPath = request.getServletContext().getRealPath("/WEB-INF/temp");

		logger.info("savePath [{}], tempPath [{}]", savePath, tempPath);

		File file = new File(tempPath);
		if(!file.exists()&&!file.isDirectory()){
			logger.info("临时文件目录不存在logger.info，现在创建。");
			file.mkdir();
		}

		//消息提示
		String message = "";
		try {
			//使用Apache文件上传组件处理文件上传步骤：
			//1、创建一个DiskFileItemFactory工厂
			DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
			//设置工厂的缓冲区的大小，当上传的文件大小超过缓冲区的大小时，就会生成一个临时文件存放到指定的临时目录当中。
			diskFileItemFactory.setSizeThreshold(1024*100);
			//设置上传时生成的临时文件的保存目录
			diskFileItemFactory.setRepository(file);
			//2、创建一个文件上传解析器
			ServletFileUpload fileUpload = new ServletFileUpload(diskFileItemFactory);
			//解决上传文件名的中文乱码
			fileUpload.setHeaderEncoding("UTF-8");
			//监听文件上传进度
			fileUpload.setProgressListener(new ProgressListener(){
				public void update(long pBytesRead, long pContentLength, int arg2) {
					logger.debug("total [{}], now [{}]", pContentLength, pBytesRead);
				}
			});

			//3、判断提交上来的数据是否是上传表单的数据
			if(!fileUpload.isMultipartContent(request)){
				logger.error("this is not a file post");
				responseAndClose(response, "无效的请求参数，请提交文件");
				//按照传统方式获取数据
				return;
			}

			//设置上传单个文件的大小的最大值，目前是设置为1024*1024*1024字节，也就是1G
			fileUpload.setFileSizeMax(1024*1024*1024);
			//设置上传文件总量的最大值，最大值=同时上传的多个文件的大小的最大值的和，目前设置为10GB
			fileUpload.setSizeMax(10*1024*1024*1024);
			//4、使用ServletFileUpload解析器解析上传数据，解析结果返回的是一个List<FileItem>集合，每一个FileItem对应一个Form表单的输入项
			List<FileItem> list = fileUpload.parseRequest(request);

			logger.info("after parse request, file item size [{}]", list.size());

			for (FileItem item : list) {
				//如果fileitem中封装的是普通输入项的数据
				if(item.isFormField()){
					String name = item.getFieldName();
					//解决普通输入项的数据的中文乱码问题
					String value = item.getString("UTF-8");
					String value1 = new String(name.getBytes("iso8859-1"),"UTF-8");
					logger.info("form field, name [{}], value [{}], name after convert [{}]", name, value, value1);
				}else{
					//如果fileitem中封装的是上传文件，得到上传的文件名称，
					String fileName = item.getName();
					logger.info("not a form field, file name [{}]", fileName);
					if(fileName==null||fileName.trim().equals("")){
						logger.error("invalid file name");
						continue;
					}
					//注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的，如：  c:\a\b\1.txt，而有些只是单纯的文件名，如：1.txt
					//处理获取到的上传文件的文件名的路径部分，只保留文件名部分
					fileName = fileName.substring(fileName.lastIndexOf(File.separator)+1);

					//得到上传文件的扩展名
					String fileExtName = fileName.substring(fileName.lastIndexOf(".")+1);

					logger.info("ext name [{}], file name after cut [{}]", fileExtName, fileName);

					if("zip".equals(fileExtName)||"rar".equals(fileExtName)||"tar".equals(fileExtName)||"jar".equals(fileExtName)){
						logger.error("this type can not upload [{}]", fileExtName);
						responseAndClose(response, "上传文件的类型不符合");
						return;
					}

					//获取item中的上传文件的输入流
					InputStream is = item.getInputStream();
					//得到文件保存的名称
					fileName = mkFileName(fileName);
					//得到文件保存的路径
					String savePathStr = mkFilePath(savePath, fileName);
					System.out.println("保存路径为:"+savePathStr);
					//创建一个文件输出流
					FileOutputStream fos = new FileOutputStream(savePathStr+File.separator+fileName);
					//创建一个缓冲区
					byte buffer[] = new byte[1024];
					//判断输入流中的数据是否已经读完的标识
					int length = 0;
					//循环将输入流读入到缓冲区当中，(len=in.read(buffer))>0就表示in里面还有数据
					while((length = is.read(buffer))>0){
						//使用FileOutputStream输出流将缓冲区的数据写入到指定的目录(savePath + "\\" + filename)当中
						fos.write(buffer, 0, length);
					}
					//关闭输入流
					is.close();
					//关闭输出流
					fos.close();
					//删除处理文件上传时生成的临时文件
					item.delete();
					message = "文件上传成功";
				}
			}
		} catch (FileUploadBase.FileSizeLimitExceededException e) {
			logger.error("1. upload fail, ", e);
			responseAndClose(response, "单个文件超出最大值！！！");
			return;
		}catch (FileUploadBase.SizeLimitExceededException e) {
			logger.error("2. upload fail, ", e);
			responseAndClose(response, "上传文件的总的大小超出限制的最大值！！！");
			return;
		}catch (FileUploadException e) {
			// TODO Auto-generated catch block
			logger.error("3. upload fail, ", e);
			message = "文件上传失败：" + e.toString();
		}

		responseAndClose(response, message);
		logger.info("finish upload");
	}
}
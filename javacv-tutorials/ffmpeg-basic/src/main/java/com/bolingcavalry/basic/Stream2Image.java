package com.bolingcavalry.basic;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swscale.*;
import static org.bytedeco.javacpp.swscale.sws_freeContext;

/**
 * @author willzhao
 * @version 1.0
 * @description 体验FFmpeg的常用API和数据结构
 * @date 2021/7/24 15:40
 */
@Slf4j
public class Stream2Image {

//    private static final int DEST_FORMAT = AV_PIX_FMT_YUVJ420P;
    private static final int DEST_FORMAT = AV_PIX_FMT_YUVJ420P;

    /**
     * 打开流媒体，取一帧，转为YUVJ420P，再保存为jpg文件
     * @param url
     * @param out_file
     * @throws IOException
     */
    public void openMediaAndSaveImage(String url,String out_file) throws IOException {
        log.info("正在打开流媒体 [{}]", url);

        // 打开指定流媒体，进行解封装，得到解封装上下文
        AVFormatContext pFormatCtx = getFormatContext(url);

        if (null==pFormatCtx) {
            log.error("获取解封装上下文失败");
            return;
        }

        // 控制台打印流媒体信息
        av_dump_format(pFormatCtx, 0, url, 0);

        // 流媒体解封装后有一个保存了所有流的数组，videoStreamIndex表示视频流在数组中的位置
        int videoStreamIndex = getVideoStreamIndex(pFormatCtx);

        // 找不到视频流就直接返回
        if (videoStreamIndex<0) {
            log.error("没有找到视频流");
            return;
        }

        log.info("视频流在流数组中的第[{}]个流是视频流(从0开始)", videoStreamIndex);

        // 得到解码上下文，已经完成了初始化
        AVCodecContext pCodecCtx = getCodecContext(pFormatCtx, videoStreamIndex);

        if (null==pCodecCtx) {
            log.error("生成解码上下文失败");
            return;
        }

        // 从视频流中解码一帧
        AVFrame pFrame = getSingleFrame(pCodecCtx,pFormatCtx, videoStreamIndex);

        if (null==pFrame) {
            log.error("从视频流中取帧失败");
            return;
        }

        // 将YUV420P图像转成YUVJ420P
        // 转换后的图片的AVFrame，及其对应的数据指针，都放在frameData对象中
        FrameData frameData = YUV420PToYUVJ420P(pCodecCtx, pFrame);

        if (null==frameData) {
            log.info("YUV420P格式转成YUVJ420P格式失败");
            return;
        }

        // 持久化存储
        saveImg(frameData.avFrame,out_file);

        // 按顺序释放
        release(true, null, null, pCodecCtx, pFormatCtx, frameData.buffer, frameData.avFrame, pFrame);

        log.info("操作成功");
    }

    /**
     * 生成解封装上下文
     * @param url
     * @return
     */
    private AVFormatContext getFormatContext(String url) {
        // 解封装上下文
        AVFormatContext pFormatCtx = new avformat.AVFormatContext(null);

        // 打开流媒体
        if (avformat_open_input(pFormatCtx, url, null, null) != 0) {
            log.error("打开媒体失败");
            return null;
        }

        // 读取流媒体数据，以获得流的信息
        if (avformat_find_stream_info(pFormatCtx, (PointerPointer<Pointer>) null) < 0) {
            log.error("获得媒体流信息失败");
            return null;
        }

        return pFormatCtx;
    }

    /**
     * 生成解码上下文
     * @param pFormatCtx
     * @param videoStreamIndex
     * @return
     */
    private AVCodecContext getCodecContext(AVFormatContext pFormatCtx, int videoStreamIndex) {
        //解码器
        AVCodec pCodec;

        // 得到解码上下文
        AVCodecContext pCodecCtx = pFormatCtx.streams(videoStreamIndex).codec();

        // 根据解码上下文得到解码器
        pCodec = avcodec_find_decoder(pCodecCtx.codec_id());

        if (pCodec == null) {
            return null;
        }

        // 用解码器来初始化解码上下文
        if (avcodec_open2(pCodecCtx, pCodec, (AVDictionary)null) < 0) {
            return null;
        }

        return pCodecCtx;
    }

    /**
     * 取一帧然后解码
     * @param pCodecCtx
     * @param pFormatCtx
     * @param videoStreamIndex
     * @return
     */
    private AVFrame getSingleFrame(AVCodecContext pCodecCtx, AVFormatContext pFormatCtx, int videoStreamIndex) {
        // 分配帧对象
        AVFrame pFrame = av_frame_alloc();

        // frameFinished用于检查是否有图像
        int[] frameFinished = new int[1];

        // 是否找到的标志
        boolean exists = false;

        AVPacket packet = new AVPacket();

        try {
            // 每一次while循环都会读取一个packet
            while (av_read_frame(pFormatCtx, packet) >= 0) {
                // 检查packet所属的流是不是视频流
                if (packet.stream_index() == videoStreamIndex) {
                    // 将AVPacket解码成AVFrame
                    avcodec_decode_video2(pCodecCtx, pFrame, frameFinished, packet);// Decode video frame

                    // 如果有图像就返回
                    if (frameFinished != null && frameFinished[0] != 0 && !pFrame.isNull()) {
                        exists = true;
                        break;
                    }
                }
            }
        } finally {
            // 一定要执行释放操作
            av_free_packet(packet);
        }

        // 找不到就返回空
        return exists ?  pFrame : null;
    }

    /**
     * 流媒体解封装后得到多个流组成的数组，该方法找到视频流咋数组中的位置
     * @param pFormatCtx
     * @return
     */
    private static int getVideoStreamIndex(AVFormatContext pFormatCtx) {
        int videoStream = -1;

        // 解封装后有多个流，找出视频流是第几个
        for (int i = 0; i < pFormatCtx.nb_streams(); i++) {
            if (pFormatCtx.streams(i).codec().codec_type() == AVMEDIA_TYPE_VIDEO) {
                videoStream = i;
                break;
            }
        }

        return videoStream;
    }

    /**
     * 将YUV420P格式的图像转为YUVJ420P格式
     * @param pCodecCtx 解码上下文
     * @param sourceFrame 源数据
     * @return 转换后的帧极其对应的数据指针
     */
    private static FrameData YUV420PToYUVJ420P(AVCodecContext pCodecCtx, AVFrame sourceFrame) {
        // 分配一个帧对象，保存从YUV420P转为YUVJ420P的结果
        AVFrame pFrameRGB = av_frame_alloc();

        if (pFrameRGB == null) {
            return null;
        }

        int width = pCodecCtx.width(), height = pCodecCtx.height();

        // 一些参数设定
        pFrameRGB.width(width);
        pFrameRGB.height(height);
        pFrameRGB.format(DEST_FORMAT);

        // 计算转为YUVJ420P之后的图片字节数
        int numBytes = avpicture_get_size(DEST_FORMAT, width, height);

        // 分配内存
        BytePointer buffer = new BytePointer(av_malloc(numBytes));

        // 图片处理工具的初始化操作
        SwsContext sws_ctx = sws_getContext(width, height, pCodecCtx.pix_fmt(), width, height, DEST_FORMAT, SWS_BICUBIC, null, null, (DoublePointer) null);

        // 将pFrameRGB的data指针指向刚才分配好的内存(即buffer)
        avpicture_fill(new avcodec.AVPicture(pFrameRGB), buffer, DEST_FORMAT, width, height);

        // 转换图像格式，将解压出来的YUV420P的图像转换为YUVJ420P的图像
        sws_scale(sws_ctx, sourceFrame.data(), sourceFrame.linesize(), 0, height, pFrameRGB.data(), pFrameRGB.linesize());

        // 及时释放
        sws_freeContext(sws_ctx);

        // 将AVFrame和BytePointer打包到FrameData中返回，这两个对象都要做显示的释放操作
        return new FrameData(pFrameRGB, buffer);
    }

    /**
     * 将传入的帧以图片的形式保存在指定位置
     * @param pFrame
     * @param out_file
     * @return 小于0表示失败
     */
    private int saveImg(avutil.AVFrame pFrame, String out_file) {
        av_log_set_level(AV_LOG_ERROR);//设置FFmpeg日志级别（默认是debug，设置成error可以屏蔽大多数不必要的控制台消息）

        AVPacket pkt = null;
        AVStream pAVStream = null;

        int width = pFrame.width(), height = pFrame.height();

        // 分配AVFormatContext对象
        avformat.AVFormatContext pFormatCtx = avformat_alloc_context();

        // 设置输出格式(涉及到封装和容器)
        pFormatCtx.oformat(av_guess_format("mjpeg", null, null));

        if (pFormatCtx.oformat() == null) {
            log.error("输出媒体流的封装格式设置失败");
            return -1;
        }

        try {
            // 创建并初始化一个和该url相关的AVIOContext
            avformat.AVIOContext pb = new avformat.AVIOContext();

            // 打开输出文件
            if (avio_open(pb, out_file, AVIO_FLAG_READ_WRITE) < 0) {
                log.info("输出文件打开失败");
                return -1;
            }

            // 封装之上是协议，这里将封装上下文和协议上下文关联
            pFormatCtx.pb(pb);

            // 构建一个新stream
            pAVStream = avformat_new_stream(pFormatCtx, null);

            if (pAVStream == null) {
                log.error("将新的流放入媒体文件失败");
                return -1;
            }

            int codec_id = pFormatCtx.oformat().video_codec();

            // 设置该stream的信息
            avcodec.AVCodecContext pCodecCtx = pAVStream.codec();
            pCodecCtx.codec_id(codec_id);
            pCodecCtx.codec_type(AVMEDIA_TYPE_VIDEO);
            pCodecCtx.pix_fmt(DEST_FORMAT);
            pCodecCtx.width(width);
            pCodecCtx.height(height);
            pCodecCtx.time_base().num(1);
            pCodecCtx.time_base().den(25);

            // 打印媒体信息
            av_dump_format(pFormatCtx, 0, out_file, 1);

            // 查找解码器
            avcodec.AVCodec pCodec = avcodec_find_encoder(codec_id);
            if (pCodec == null) {
                log.info("获取解码器失败");
                return -1;
            }

            // 用解码器来初始化解码上下文
            if (avcodec_open2(pCodecCtx, pCodec, (PointerPointer<Pointer>) null) < 0) {
                log.error("解码上下文初始化失败");
                return -1;
            }

            // 输出的Packet
            pkt = new avcodec.AVPacket();

            // 分配
            if (av_new_packet(pkt, width * height * 3) < 0) {
                return -1;
            }

            int[] got_picture = { 0 };

            // 把流的头信息写到要输出的媒体文件中
            avformat_write_header(pFormatCtx, (PointerPointer<Pointer>) null);

            // 把帧的内容进行编码
            if (avcodec_encode_video2(pCodecCtx, pkt, pFrame, got_picture)<0) {
                log.error("把帧编码为packet失败");
                return -1;
            }

            // 输出一帧
            if ((av_write_frame(pFormatCtx, pkt)) < 0) {
                log.error("输出一帧失败");
                return -1;
            }

            // 写文件尾
            if (av_write_trailer(pFormatCtx) < 0) {
                log.error("写文件尾失败");
                return -1;
            }

            return 0;
        } finally {
            // 资源清理
            release(false, pkt, pFormatCtx.pb(), pAVStream.codec(), pFormatCtx);
        }
    }

    /**
     * 释放资源，顺序是先释放数据，再释放上下文
     * @param pCodecCtx
     * @param pFormatCtx
     * @param ptrs
     */
    private void release(boolean isInput, AVPacket pkt, AVIOContext pb, AVCodecContext pCodecCtx, AVFormatContext pFormatCtx, Pointer...ptrs) {


        if (null!=pkt) {
            av_free_packet(pkt);
        }

        // 解码后，这是个数组，要遍历处理
        if (null!=ptrs) {
            Arrays.stream(ptrs).forEach(avutil::av_free);
        }

        // 解码
        if (null!=pCodecCtx) {
            avcodec_close(pCodecCtx);
        }

        // 解协议
        if (null!=pb) {
            avio_close(pb);
        }

        // 解封装
        if (null!=pFormatCtx) {
            if (isInput) {
                avformat_close_input(pFormatCtx);
            } else {
                avformat_free_context(pFormatCtx);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // CCTV13，1920*1080分辨率，不稳定，打开失败时请多试几次
        String url = "http://ivi.bupt.edu.cn/hls/cctv13hd.m3u8";

        // 安徽卫视，1024*576分辨率，较为稳定
//        String url = "rtmp://58.200.131.2:1935/livetv/ahtv";
        // 本地视频文件，请改为您自己的本地文件地址
//        String url = "E:\\temp\\202107\\24\\test.mp4";

        // 完整图片存放路径，注意文件名是当前的年月日时分秒
        String localPath = "E:\\temp\\202107\\24\\save\\" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";

        // 开始操作
        new Stream2Image().openMediaAndSaveImage(url, localPath);
    }
}

/**
 * AVFrame及其关联的数据，在释放资源的时候都要做释放操作，
 * 这里将它们放在一个类中，在方法调用的时候方便传递
 */
class FrameData {

    AVFrame avFrame;
    BytePointer buffer;

    public FrameData(AVFrame avFrame, BytePointer buffer) {
        this.avFrame = avFrame;
        this.buffer = buffer;
    }
}
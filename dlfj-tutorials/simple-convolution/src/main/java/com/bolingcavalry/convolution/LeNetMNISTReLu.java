/*******************************************************************************
 * Copyright (c) 2020 Konduit K.K.
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package com.bolingcavalry.convolution;

import lombok.extern.slf4j.Slf4j;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.schedule.MapSchedule;
import org.nd4j.linalg.schedule.ScheduleType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Implementation of LeNet-5 for handwritten digits image classification on MNIST dataset (99% accuracy)
 * <a href="http://yann.lecun.com/exdb/publis/pdf/lecun-01a.pdf">[LeCun et al., 1998. Gradient based learning applied to document recognition]</a>
 * Some minor changes are made to the architecture like using ReLU and identity activation instead of
 * sigmoid/tanh, max pooling instead of avg pooling and softmax output layer.
 * <p>
 * This example will download 15 Mb of data on the first run.
 *
 * @author hanlon
 * @author agibsonccc
 * @author fvaleri
 * @author dariuszzbyrad
 */
@Slf4j
public class LeNetMNISTReLu {

    // 存放文件的地址，请酌情修改
//    private static final String BASE_PATH = System.getProperty("java.io.tmpdir") + "/mnist";
    private static final String BASE_PATH = "/home/will/temp/202106/26";

    public static void main(String[] args) throws Exception {
        // 图片像素高
        int height = 28;
        // 图片像素宽
        int width = 28;

        // 因为是黑白图像，所以颜色通道只有一个
        int channels = 1;

        // 分类结果，0-9，共十种数字
        int outputNum = 10;

        // 批大小
        int batchSize = 54;

        // 循环次数
        int nEpochs = 1;

        // 初始化伪随机数的种子
        int seed = 1234;

        // 随机数工具
        Random randNumGen = new Random(seed);

        log.info("检查数据集文件夹是否存在：{}", BASE_PATH + "/mnist_png");

        if (!new File(BASE_PATH + "/mnist_png").exists()) {
            log.info("数据集文件不存在，请下载压缩包并解压到：{}", BASE_PATH);
            return;
        }

        // 标签生成器，将指定文件的父目录作为标签
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        // 归一化配置(像素值从0-255变为0-1)
        DataNormalization imageScaler = new ImagePreProcessingScaler();

        // 不论训练集还是测试集，初始化操作都是相同套路：
        // 1. 读取图片，数据格式为NCHW
        // 2. 根据批大小创建的迭代器
        // 3. 将归一化器作为预处理器

        log.info("训练集的矢量化操作...");
        // 初始化训练集
        File trainData = new File(BASE_PATH + "/mnist_png/training");
        FileSplit trainSplit = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
        ImageRecordReader trainRR = new ImageRecordReader(height, width, channels, labelMaker);
        trainRR.initialize(trainSplit);
        DataSetIterator trainIter = new RecordReaderDataSetIterator(trainRR, batchSize, 1, outputNum);
        // 拟合数据(实现类中实际上什么也没做)
        imageScaler.fit(trainIter);
        trainIter.setPreProcessor(imageScaler);

        log.info("测试集的矢量化操作...");
        // 初始化测试集，与前面的训练集操作类似
        File testData = new File(BASE_PATH + "/mnist_png/testing");
        FileSplit testSplit = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
        ImageRecordReader testRR = new ImageRecordReader(height, width, channels, labelMaker);
        testRR.initialize(testSplit);
        DataSetIterator testIter = new RecordReaderDataSetIterator(testRR, batchSize, 1, outputNum);
        testIter.setPreProcessor(imageScaler); // same normalization for better results

        log.info("配置神经网络");

        // 在训练中，将学习率配置为随着迭代阶梯性下降
        Map<Integer, Double> learningRateSchedule = new HashMap<>();
        learningRateSchedule.put(0, 0.06);
        learningRateSchedule.put(200, 0.05);
        learningRateSchedule.put(600, 0.028);
        learningRateSchedule.put(800, 0.0060);
        learningRateSchedule.put(1000, 0.001);

        // 超参数
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            // L2正则化系数
            .l2(0.0005)
            // 梯度下降的学习率设置
            .updater(new Nesterovs(new MapSchedule(ScheduleType.ITERATION, learningRateSchedule)))
            // 权重初始化
            .weightInit(WeightInit.XAVIER)
            // 准备分层
            .list()
            // 卷积层
            .layer(new ConvolutionLayer.Builder(5, 5)
                .nIn(channels)
                .stride(1, 1)
                .nOut(20)
                .activation(Activation.IDENTITY)
                .build())
            // 下采样，即池化
            .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
            // 卷积层
            .layer(new ConvolutionLayer.Builder(5, 5)
                .stride(1, 1) // nIn need not specified in later layers
                .nOut(50)
                .activation(Activation.IDENTITY)
                .build())
            // 下采样，即池化
            .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
            // 稠密层，即全连接
            .layer(new DenseLayer.Builder().activation(Activation.RELU)
                .nOut(500)
                .build())
            // 输出
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(outputNum)
                .activation(Activation.SOFTMAX)
                .build())
            .setInputType(InputType.convolutionalFlat(height, width, channels)) // InputType.convolutional for normal image
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        // 每十个迭代打印一次损失函数值
        net.setListeners(new ScoreIterationListener(10));

        log.info("神经网络共[{}]个参数", net.numParams());

        long startTime = System.currentTimeMillis();
        // 循环操作
        for (int i = 0; i < nEpochs; i++) {
            log.info("第[{}]个循环", i);
            net.fit(trainIter);
            Evaluation eval = net.evaluate(testIter);
            log.info(eval.stats());
            trainIter.reset();
            testIter.reset();
        }
        log.info("完成训练和测试，耗时[{}]毫秒", System.currentTimeMillis()-startTime);

        // 保存模型
        File ministModelPath = new File(BASE_PATH + "/minist-model.zip");
        ModelSerializer.writeModel(net, ministModelPath, true);
        log.info("最新的MINIST模型保存在[{}]", ministModelPath.getPath());
    }
}

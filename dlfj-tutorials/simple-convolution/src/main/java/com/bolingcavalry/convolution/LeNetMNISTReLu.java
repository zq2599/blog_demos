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

import com.bolingcavalry.commons.utils.DataUtilities;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String BASE_PATH = System.getProperty("java.io.tmpdir") + "/mnist";

    public static void main(String[] args) throws Exception {
        // 图片像素高
        int height = 28;
        // 图片像素宽
        int width = 28;

        int channels = 1;   // single channel for grayscale images

        // 分类结果，0-9，共十种数字
        int outputNum = 10; // 10 digits classification

        // 批大小
        int batchSize = 54; // number of samples that will be propagated through the network in each iteration

        // 循环次数
        int nEpochs = 1;    // number of training epochs

        // 初始化伪随机数的种子
        int seed = 1234;    // number used to initialize a pseudorandom number generator.

        // 随机数工具
        Random randNumGen = new Random(seed);

        log.info("检查数据集文件夹是否存在：{}", BASE_PATH + "/mnist_png");

        if (!new File(BASE_PATH + "/mnist_png").exists()) {
            log.info("数据集文件不存在，请下载压缩包并解压到：{}", BASE_PATH);
            return;
        }

        log.info("训练集的矢量化操作...");
        File trainData = new File(BASE_PATH + "/mnist_png/training");

        // 初始化训练集
        FileSplit trainSplit = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
        // 标签生成器，将指定文件的父目录作为标签
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        // 读取图片，数据格式为NCHW
        ImageRecordReader trainRR = new ImageRecordReader(height, width, channels, labelMaker);
        trainRR.initialize(trainSplit);

        // 根据批大小创建的迭代器
        DataSetIterator trainIter = new RecordReaderDataSetIterator(trainRR, batchSize, 1, outputNum);

        // 归一化配置(像素值从0-255变为0-1)
        DataNormalization imageScaler = new ImagePreProcessingScaler();

        imageScaler.fit(trainIter);
        // 将归一化器作为预处理器
        trainIter.setPreProcessor(imageScaler);


        // 初始化测试集，与前面的训练集操作类似
        log.info("测试集的矢量化操作...");
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
            .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
            .layer(new ConvolutionLayer.Builder(5, 5)
                .stride(1, 1) // nIn need not specified in later layers
                .nOut(50)
                .activation(Activation.IDENTITY)
                .build())
            .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
            .layer(new DenseLayer.Builder().activation(Activation.RELU)
                .nOut(500)
                .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(outputNum)
                .activation(Activation.SOFTMAX)
                .build())
            .setInputType(InputType.convolutionalFlat(height, width, channels)) // InputType.convolutional for normal image
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(10));
        log.info("Total num of params: {}", net.numParams());

        // evaluation while training (the score should go down)
        for (int i = 0; i < nEpochs; i++) {
            net.fit(trainIter);
            log.info("Completed epoch {}", i);
            Evaluation eval = net.evaluate(testIter);
            log.info(eval.stats());

            trainIter.reset();
            testIter.reset();
        }

        File ministModelPath = new File(BASE_PATH + "/minist-model.zip");
        ModelSerializer.writeModel(net, ministModelPath, true);
        log.info("The MINIST model has been saved in {}", ministModelPath.getPath());
    }
}

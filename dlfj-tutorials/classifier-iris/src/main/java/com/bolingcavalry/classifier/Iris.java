package com.bolingcavalry.classifier;

import com.bolingcavalry.commons.utils.DownloaderUtility;
import lombok.extern.slf4j.Slf4j;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 鸢尾花训练
 * @date 2021/6/13 17:30
 */
@SuppressWarnings("DuplicatedCode")
@Slf4j
public class Iris {

    public static void main(String[] args) throws  Exception {

        //第一阶段：准备

        // 跳过的行数，因为可能是表头
        int numLinesToSkip = 0;
        // 分隔符
        char delimiter = ',';

        // CSV读取工具
        RecordReader recordReader = new CSVRecordReader(numLinesToSkip,delimiter);

        // 下载并解压后，得到文件的位置
        String dataPathLocal = DownloaderUtility.IRISDATA.Download();

        log.info("鸢尾花数据已下载并解压至 : {}", dataPathLocal);

        // 读取下载后的文件
        recordReader.initialize(new FileSplit(new File(dataPathLocal,"iris.txt")));

        // 每一行的内容大概是这样的：5.1,3.5,1.4,0.2,0
        // 一共五个字段，从零开始算的话，标签在第四个字段
        int labelIndex = 4;

        // 鸢尾花一共分为三类
        int numClasses = 3;

        // 一共150个样本
        int batchSize = 150;    //Iris data set: 150 examples total. We are loading all of them into one DataSet (not recommended for large data sets)

        // 加载到数据集迭代器中
        DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader,batchSize,labelIndex,numClasses);

        DataSet allData = iterator.next();

        // 洗牌（打乱顺序）
        allData.shuffle();

        // 设定比例，150个样本中，百分之六十五用于训练
        SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65);  //Use 65% of data for training

        // 训练用的数据集
        DataSet trainingData = testAndTrain.getTrain();

        // 验证用的数据集
        DataSet testData = testAndTrain.getTest();

        // 指定归一化器：独立地将每个特征值（和可选的标签值）归一化为0平均值和1的标准差。
        DataNormalization normalizer = new NormalizerStandardize();

        // 先拟合
        normalizer.fit(trainingData);

        // 对训练集做归一化
        normalizer.transform(trainingData);

        // 对测试集做归一化
        normalizer.transform(testData);

        // 每个鸢尾花有四个特征
        final int numInputs = 4;

        // 共有三种鸢尾花
        int outputNum = 3;

        // 随机数种子
        long seed = 6;

        //第二阶段：训练
        log.info("开始配置...");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .activation(Activation.TANH)       // 激活函数选用标准的tanh(双曲正切)
            .weightInit(WeightInit.XAVIER)     // 权重初始化选用XAVIER：均值 0, 方差为 2.0/(fanIn + fanOut)的高斯分布
            .updater(new Sgd(0.1))  // 更新器，设置SGD学习速率调度器
            .l2(1e-4)                          // L2正则化配置
            .list()                            // 配置多层网络
            .layer(new DenseLayer.Builder().nIn(numInputs).nOut(3)  // 隐藏层
                .build())
            .layer(new DenseLayer.Builder().nIn(3).nOut(3)          // 隐藏层
                .build())
            .layer( new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)   // 损失函数：负对数似然
                .activation(Activation.SOFTMAX)                     // 输出层指定激活函数为：SOFTMAX
                .nIn(3).nOut(outputNum).build())
            .build();

        // 模型配置
        MultiLayerNetwork model = new MultiLayerNetwork(conf);

        // 初始化
        model.init();

        // 每一百次迭代打印一次分数（损失函数的值）
        model.setListeners(new ScoreIterationListener(100));

        long startTime = System.currentTimeMillis();

        log.info("开始训练");
        // 训练
        for(int i=0; i<1000; i++ ) {
            model.fit(trainingData);
        }
        log.info("训练完成，耗时[{}]ms", System.currentTimeMillis()-startTime);

        // 第三阶段：评估

        // 在测试集上评估模型
        Evaluation eval = new Evaluation(numClasses);
        INDArray output = model.output(testData.getFeatures());
        eval.eval(testData.getLabels(), output);

        log.info("评估结果如下\n" + eval.stats());
    }
}


/* *****************************************************************************
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

package com.bolingcavalry.commons.utils;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.*;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple plotting methods for the MLPClassifier quickstartexamples
 *
 * @author Alex Black
 */
public class PlotUtil {

    /**
     * Plot the training data. Assume 2d input, classification output
     *
     * @param model         Model to use to get predictions
     * @param trainIter     DataSet Iterator
     * @param backgroundIn  sets of x,y points in input space, plotted in the background
     * @param nDivisions    Number of points (per axis, for the backgroundIn/backgroundOut arrays)
     */
    public static void plotTrainingData(MultiLayerNetwork model, DataSetIterator trainIter, INDArray backgroundIn, int nDivisions) {
        double[] mins = backgroundIn.min(0).data().asDouble();
        double[] maxs = backgroundIn.max(0).data().asDouble();

        DataSet ds = allBatches(trainIter);
        INDArray backgroundOut = model.output(backgroundIn);

        XYZDataset backgroundData = createBackgroundData(backgroundIn, backgroundOut);
        JPanel panel = new ChartPanel(createChart(backgroundData, mins, maxs, nDivisions, createDataSetTrain(ds.getFeatures(), ds.getLabels())));

        JFrame f = new JFrame();
        f.add(panel);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.pack();
        f.setTitle("Training Data");

        f.setVisible(true);
        f.setLocation(0, 0);
    }

    /**
     * Plot the training data. Assume 2d input, classification output
     *
     * @param model         Model to use to get predictions
     * @param testIter      Test Iterator
     * @param backgroundIn  sets of x,y points in input space, plotted in the background
     * @param nDivisions    Number of points (per axis, for the backgroundIn/backgroundOut arrays)
     */
    public static void plotTestData(MultiLayerNetwork model, DataSetIterator testIter, INDArray backgroundIn, int nDivisions) {

        double[] mins = backgroundIn.min(0).data().asDouble();
        double[] maxs = backgroundIn.max(0).data().asDouble();

        INDArray backgroundOut = model.output(backgroundIn);
        XYZDataset backgroundData = createBackgroundData(backgroundIn, backgroundOut);
        DataSet ds = allBatches(testIter);
        INDArray predicted = model.output(ds.getFeatures());
        JPanel panel = new ChartPanel(createChart(backgroundData, mins, maxs, nDivisions, createDataSetTest(ds.getFeatures(), ds.getLabels(), predicted)));

        JFrame f = new JFrame();
        f.add(panel);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.pack();
        f.setTitle("Test Data");

        f.setVisible(true);
        f.setLocationRelativeTo(null);
        //f.setLocation(100,100);

    }


    /**
     * Create data for the background data set
     */
    private static XYZDataset createBackgroundData(INDArray backgroundIn, INDArray backgroundOut) {
        int nRows = backgroundIn.rows();
        double[] xValues = new double[nRows];
        double[] yValues = new double[nRows];
        double[] zValues = new double[nRows];
        for (int i = 0; i < nRows; i++) {
            xValues[i] = backgroundIn.getDouble(i, 0);
            yValues[i] = backgroundIn.getDouble(i, 1);
            zValues[i] = backgroundOut.getDouble(i, 0);

        }

        DefaultXYZDataset dataset = new DefaultXYZDataset();
        dataset.addSeries("Series 1",
                new double[][]{xValues, yValues, zValues});
        return dataset;
    }

    //Training data
    private static XYDataset createDataSetTrain(INDArray features, INDArray labels) {
        int nRows = features.rows();

        int nClasses = 2; // Binary classification using one output call end sigmoid.

        XYSeries[] series = new XYSeries[nClasses];
        for (int i = 0; i < series.length; i++) series[i] = new XYSeries("Class " + i);
        INDArray argMax = Nd4j.getExecutioner().exec(new IMax(labels, 1));
        for (int i = 0; i < nRows; i++) {
            int classIdx = (int) argMax.getDouble(i);
            series[classIdx].add(features.getDouble(i, 0), features.getDouble(i, 1));
        }

        XYSeriesCollection c = new XYSeriesCollection();
        for (XYSeries s : series) c.addSeries(s);
        return c;
    }

    //Test data
    private static XYDataset createDataSetTest(INDArray features, INDArray labels, INDArray predicted) {
        int nRows = features.rows();

        int nClasses = 2; // Binary classification using one output call end sigmoid.

        XYSeries[] series = new XYSeries[nClasses * nClasses];
        int[] series_index = new int[]{0, 3, 2, 1}; //little hack to make the charts look consistent.
        for (int i = 0; i < nClasses * nClasses; i++) {
            int trueClass = i / nClasses;
            int predClass = i % nClasses;
            String label = "actual=" + trueClass + ", pred=" + predClass;
            series[series_index[i]] = new XYSeries(label);
        }
        INDArray actualIdx = labels.argMax(1);
        INDArray predictedIdx = predicted.argMax(1);
        for (int i = 0; i < nRows; i++) {
            int classIdx = actualIdx.getInt(i);
            int predIdx = predictedIdx.getInt(i);
            int idx = series_index[classIdx * nClasses + predIdx];
            series[idx].add(features.getDouble(i, 0), features.getDouble(i, 1));
        }

        XYSeriesCollection c = new XYSeriesCollection();
        for (XYSeries s : series) c.addSeries(s);
        return c;
    }

    private static JFreeChart createChart(XYZDataset dataset, double[] mins, double[] maxs, int nPoints, XYDataset xyData) {
        NumberAxis xAxis = new NumberAxis("X");
        xAxis.setRange(mins[0], maxs[0]);


        NumberAxis yAxis = new NumberAxis("Y");
        yAxis.setRange(mins[1], maxs[1]);

        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setBlockWidth((maxs[0] - mins[0]) / (nPoints - 1));
        renderer.setBlockHeight((maxs[1] - mins[1]) / (nPoints - 1));
        PaintScale scale = new GrayPaintScale(0, 1.0);
        renderer.setPaintScale(scale);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
        JFreeChart chart = new JFreeChart("", plot);
        chart.getXYPlot().getRenderer().setSeriesVisibleInLegend(0, false);


        NumberAxis scaleAxis = new NumberAxis("Probability (class 1)");
        scaleAxis.setAxisLinePaint(Color.white);
        scaleAxis.setTickMarkPaint(Color.white);
        scaleAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 7));
        PaintScaleLegend legend = new PaintScaleLegend(new GrayPaintScale(),
                scaleAxis);
        legend.setStripOutlineVisible(false);
        legend.setSubdivisionCount(20);
        legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        legend.setAxisOffset(5.0);
        legend.setMargin(new RectangleInsets(5, 5, 5, 5));
        legend.setFrame(new BlockBorder(Color.red));
        legend.setPadding(new RectangleInsets(10, 10, 10, 10));
        legend.setStripWidth(10);
        legend.setPosition(RectangleEdge.LEFT);
        chart.addSubtitle(legend);

        ChartUtilities.applyCurrentTheme(chart);

        plot.setDataset(1, xyData);
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
        renderer2.setBaseLinesVisible(false);
        plot.setRenderer(1, renderer2);

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        return chart;
    }

    public static INDArray generatePointsOnGraph(double xMin, double xMax, double yMin, double yMax, int nPointsPerAxis) {
        //generate all the x,y points
        double[][] evalPoints = new double[nPointsPerAxis * nPointsPerAxis][2];
        int count = 0;
        for (int i = 0; i < nPointsPerAxis; i++) {
            for (int j = 0; j < nPointsPerAxis; j++) {
                double x = i * (xMax - xMin) / (nPointsPerAxis - 1) + xMin;
                double y = j * (yMax - yMin) / (nPointsPerAxis - 1) + yMin;

                evalPoints[count][0] = x;
                evalPoints[count][1] = y;

                count++;
            }
        }

        return Nd4j.create(evalPoints);
    }

    /**
     * This is to collect all the data and return it as one minibatch. Obviously only for use here with small datasets
     * @param iter
     * @return
     */
    private static DataSet allBatches(DataSetIterator iter) {

        List<DataSet> fullSet = new ArrayList<>();
        iter.reset();
        while (iter.hasNext()) {
            List<DataSet> miniBatchList = iter.next().asList();
            fullSet.addAll(miniBatchList);
        }
        iter.reset();
        return new ListDataSetIterator<>(fullSet,fullSet.size()).next();
    }

}

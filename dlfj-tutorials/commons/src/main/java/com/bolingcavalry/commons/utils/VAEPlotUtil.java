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

package com.bolingcavalry.commons.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Plotting methods for the VariationalAutoEncoder example
 * @author Alex Black
 */
public class VAEPlotUtil {

    //Scatterplot util used for CenterLossMnistExample
    public static void scatterPlot(List<Pair<INDArray,INDArray>> data, List<Integer> epochCounts, String title ){
        double xMin = Double.MAX_VALUE;
        double xMax = -Double.MAX_VALUE;
        double yMin = Double.MAX_VALUE;
        double yMax = -Double.MAX_VALUE;

        for(Pair<INDArray,INDArray> p : data){
            INDArray maxes = p.getFirst().max(0);
            INDArray mins = p.getFirst().min(0);
            xMin = Math.min(xMin, mins.getDouble(0));
            xMax = Math.max(xMax, maxes.getDouble(0));
            yMin = Math.min(yMin, mins.getDouble(1));
            yMax = Math.max(yMax, maxes.getDouble(1));
        }

        double plotMin = Math.min(xMin, yMin);
        double plotMax = Math.max(xMax, yMax);

        JPanel panel = new ChartPanel(createChart(data.get(0).getFirst(), data.get(0).getSecond(), plotMin, plotMax, title + " (epoch " + epochCounts.get(0) + ")"));
        JSlider slider = new JSlider(0,epochCounts.size()-1,0);
        slider.setSnapToTicks(true);

        final JFrame f = new JFrame();
        slider.addChangeListener(new ChangeListener() {

            private JPanel lastPanel = panel;
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider)e.getSource();
                int  value = slider.getValue();
                JPanel panel = new ChartPanel(createChart(data.get(value).getFirst(), data.get(value).getSecond(), plotMin, plotMax, title + " (epoch " + epochCounts.get(value) + ")"));
                if(lastPanel != null){
                    f.remove(lastPanel);
                }
                lastPanel = panel;
                f.add(panel, BorderLayout.CENTER);
                f.setTitle(title);
                f.revalidate();
            }
        });

        f.setLayout(new BorderLayout());
        f.add(slider, BorderLayout.NORTH);
        f.add(panel, BorderLayout.CENTER);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.pack();
        f.setTitle(title);

        f.setVisible(true);
    }

    public static void plotData(List<INDArray> xyVsIter, INDArray labels, double axisMin, double axisMax, int plotFrequency){

        JPanel panel = new ChartPanel(createChart(xyVsIter.get(0), labels, axisMin, axisMax));
        JSlider slider = new JSlider(0,xyVsIter.size()-1,0);
        slider.setSnapToTicks(true);

        final JFrame f = new JFrame();
        slider.addChangeListener(new ChangeListener() {

            private JPanel lastPanel = panel;
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider)e.getSource();
                int  value = slider.getValue();
                JPanel panel = new ChartPanel(createChart(xyVsIter.get(value), labels, axisMin, axisMax));
                if(lastPanel != null){
                    f.remove(lastPanel);
                }
                lastPanel = panel;
                f.add(panel, BorderLayout.CENTER);
                f.setTitle(getTitle(value, plotFrequency));
                f.revalidate();
            }
        });

        f.setLayout(new BorderLayout());
        f.add(slider, BorderLayout.NORTH);
        f.add(panel, BorderLayout.CENTER);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.pack();
        f.setTitle(getTitle(0, plotFrequency));

        f.setVisible(true);
    }

    private static String getTitle(int recordNumber, int plotFrequency){
        return "MNIST Test Set - Latent Space Encoding at Training Iteration " + recordNumber * plotFrequency;
    }

    //Test data
    private static XYDataset createDataSet(INDArray features, INDArray labelsOneHot){
        int nRows = features.rows();

        int nClasses = labelsOneHot.columns();

        XYSeries[] series = new XYSeries[nClasses];
        for( int i=0; i<nClasses; i++){
            series[i] = new XYSeries(String.valueOf(i));
        }
        INDArray classIdx = Nd4j.argMax(labelsOneHot, 1);
        for( int i=0; i<nRows; i++ ){
            int idx = classIdx.getInt(i);
            series[idx].add(features.getDouble(i, 0), features.getDouble(i, 1));
        }

        XYSeriesCollection c = new XYSeriesCollection();
        for( XYSeries s : series) c.addSeries(s);
        return c;
    }

    private static JFreeChart createChart(INDArray features, INDArray labels, double axisMin, double axisMax) {
        return createChart(features, labels, axisMin, axisMax, "Variational Autoencoder Latent Space - MNIST Test Set");
    }

    private static JFreeChart createChart(INDArray features, INDArray labels, double axisMin, double axisMax, String title ) {

        XYDataset dataset = createDataSet(features, labels);

        JFreeChart chart = ChartFactory.createScatterPlot(title,
            "X", "Y", dataset, PlotOrientation.VERTICAL, true, true, false);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.getRenderer().setBaseOutlineStroke(new BasicStroke(0));
        plot.setNoDataMessage("NO DATA");

        plot.setDomainPannable(false);
        plot.setRangePannable(false);
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);

        plot.setDomainGridlineStroke(new BasicStroke(0.0f));
        plot.setDomainMinorGridlineStroke(new BasicStroke(0.0f));
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlineStroke(new BasicStroke(0.0f));
        plot.setRangeMinorGridlineStroke(new BasicStroke(0.0f));
        plot.setRangeGridlinePaint(Color.blue);

        plot.setDomainMinorGridlinesVisible(true);
        plot.setRangeMinorGridlinesVisible(true);

        XYLineAndShapeRenderer renderer
            = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesOutlinePaint(0, Color.black);
        renderer.setUseOutlinePaint(true);
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        domainAxis.setRange(axisMin, axisMax);

        domainAxis.setTickMarkInsideLength(2.0f);
        domainAxis.setTickMarkOutsideLength(2.0f);

        domainAxis.setMinorTickCount(2);
        domainAxis.setMinorTickMarksVisible(true);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickMarkInsideLength(2.0f);
        rangeAxis.setTickMarkOutsideLength(2.0f);
        rangeAxis.setMinorTickCount(2);
        rangeAxis.setMinorTickMarksVisible(true);
        rangeAxis.setRange(axisMin, axisMax);
        return chart;
    }


    public static class MNISTLatentSpaceVisualizer {
        private double imageScale;
        private List<INDArray> digits;  //Digits (as row vectors), one per INDArray
        private int plotFrequency;
        private int gridWidth;

        public MNISTLatentSpaceVisualizer(double imageScale, List<INDArray> digits, int plotFrequency) {
            this.imageScale = imageScale;
            this.digits = digits;
            this.plotFrequency = plotFrequency;
            this.gridWidth = (int)Math.sqrt(digits.get(0).size(0)); //Assume square, nxn rows
        }

        private String getTitle(int recordNumber){
            return "Reconstructions Over Latent Space at Training Iteration " + recordNumber * plotFrequency;
        }

        public void visualize(){
            JFrame frame = new JFrame();
            frame.setTitle(getTitle(0));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(0,gridWidth));

            JSlider slider = new JSlider(0,digits.size()-1, 0);
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    JSlider slider = (JSlider)e.getSource();
                    int  value = slider.getValue();
                    panel.removeAll();
                    List<JLabel> list = getComponents(value);
                    for(JLabel image : list){
                        panel.add(image);
                    }
                    frame.setTitle(getTitle(value));
                    frame.revalidate();
                }
            });
            frame.add(slider, BorderLayout.NORTH);


            List<JLabel> list = getComponents(0);
            for(JLabel image : list){
                panel.add(image);
            }

            frame.add(panel, BorderLayout.CENTER);
            frame.setVisible(true);
            frame.pack();
        }

        private List<JLabel> getComponents(int idx){
            List<JLabel> images = new ArrayList<>();
            List<INDArray> temp =  new ArrayList<>();
            for( int i=0; i<digits.get(idx).size(0); i++ ){
                temp.add(digits.get(idx).getRow(i));
            }
            for( INDArray arr : temp ){
                BufferedImage bi = new BufferedImage(28,28,BufferedImage.TYPE_BYTE_GRAY);
                for( int i=0; i<784; i++ ){
                    bi.getRaster().setSample(i % 28, i / 28, 0, (int)(255*arr.getDouble(i)));
                }
                ImageIcon orig = new ImageIcon(bi);
                Image imageScaled = orig.getImage().getScaledInstance((int)(imageScale*28),(int)(imageScale*28),Image.SCALE_REPLICATE);
                ImageIcon scaled = new ImageIcon(imageScaled);
                images.add(new JLabel(scaled));
            }
            return images;
        }
    }
}

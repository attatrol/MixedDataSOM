package com.github.attatrol.som.ui;

import java.awt.image.RenderedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import javax.imageio.ImageIO;

import com.github.attatrol.preprocessing.distance.DistanceRegisters;
import com.github.attatrol.preprocessing.distance.metric.EuclideanMetric;
import com.github.attatrol.preprocessing.distance.nonmetric.similarity.SimilarityIndexFactory;
import com.github.attatrol.preprocessing.ui.TokenDataSourceAndMisc;
import com.github.attatrol.som.CategoricalDistanceProducer;
import com.github.attatrol.som.TestResult;
import com.github.attatrol.som.benchmark.PurityColumnBenchmark;
import com.github.attatrol.som.som.Som;
import com.github.attatrol.som.som.SomClusterResult;
import com.github.attatrol.som.som.functions.learning.LearningFunction;
import com.github.attatrol.som.som.functions.learning.LearningFunctionFactory;
import com.github.attatrol.som.som.functions.learning.LinearLearningFunction;
import com.github.attatrol.som.som.functions.neighbourhood.GaussNeighbourhoodFunction;
import com.github.attatrol.som.som.functions.neighbourhood.NeighborhoodFunction;
import com.github.attatrol.som.som.functions.neighbourhood.NeighborhoodFunctionFactory;
import com.github.attatrol.som.som.initializers.RandomRecordsInitializer;
import com.github.attatrol.som.som.initializers.SomInitializer;
import com.github.attatrol.som.som.neuron.FuzzyNeuron;
import com.github.attatrol.som.som.neuron.FuzzyNeuronFactory;
import com.github.attatrol.som.som.topology.Point;
import com.github.attatrol.som.som.topology.RectangleTopology;
import com.github.attatrol.som.som.topology.RectangleTopologyFactory;
import com.github.attatrol.som.som.topology.SomTopology;
import com.github.attatrol.som.ui.i18n.SomI18nProvider;

import javafx.scene.paint.Color;

public class TestRunner {
    /**
     * Constants
     */
    public static final FuzzyNeuronFactory<?> DEFAULT_NEURON_FACTORY = new FuzzyNeuron.Factory();

    public static final LearningFunctionFactory<?> DEFAULT_LEARNING_FUNCTION_FACTORY = new LinearLearningFunction.Factory();

    public static final NeighborhoodFunctionFactory<?> DEFAULT_NEIGHBORHOOD_FUNCTION_FACTORY = new GaussNeighbourhoodFunction.Factory();

    public static final RectangleTopologyFactory<?> DEFAULT_TOPOLOGY_FACTORY = new RectangleTopology.Factory();

    public static final SomInitializer DEFAULT_SOM_INITIALIZER = new RandomRecordsInitializer();

    public static final int DEFAULT_NUMBER_OF_EPOCHS = 500;

    public static final double DEFAULT_ALPHA = 0.;

    public static final int REF_COLUMN_INDEX = 0;

    /**
     * Variables
     */
    public static final double BETA_START_VALUE = 1.;

    public static final double BETA_END_VALUE = 3.;

    public static final double BETA_STEP_VALUE = .1;

    public static final int MAP_INITIAL_SIZE = 5;

    public static final int MAP_FINAL_SIZE = 10;

    public static final int TEST_REPLAYS = 5;

    private static final Path ROOT_PATH = Paths.get(System.getProperty("user.home"));

    private final Path testPath;

    private TokenDataSourceAndMisc tdsm;

    private CategoricalDistanceProducer distanceFactory;

    public TestRunner(TokenDataSourceAndMisc tdsm, CategoricalDistanceProducer distanceFactory) throws IOException {
        testPath = ROOT_PATH.resolve(String.format("SOM_tests_%s",
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())));
        Files.createDirectories(testPath);
        this.tdsm = tdsm;
        this.distanceFactory = distanceFactory;
    }

    public void run() throws IOException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        System.out.println("Tests begun");
        Map<Object, Color> refColumnColorMap = ColorUtils.getTokenColorsByFrequency(tdsm)[REF_COLUMN_INDEX];
        int uniqueTestIndex = 1;
        for (SimilarityIndexFactory<?> sif : DistanceRegisters.SIMILARITY_INDEX_FACTORY_REGISTER) {
           final String sifName = sif.getClass().getName();
           final Path sifTestPath = testPath.resolve(sifName);
           Files.createDirectory(sifTestPath);
           try (FileWriter writer = new FileWriter(sifTestPath.resolve("results").toFile())) {
               writer.write(TestResult.getHeader());
               for (int width = MAP_INITIAL_SIZE; width <= MAP_FINAL_SIZE; width++) {
                   for (int height = width; height <= width + 1; height++) {
                       for (double beta = BETA_START_VALUE; beta <= BETA_END_VALUE; beta += BETA_STEP_VALUE) {
                           for (int z = 0; z < TEST_REPLAYS; z++) {
                               System.out.println(String.format("Test no. %d", uniqueTestIndex));
                               final SomComponents somData = new SomComponents();
                               somData.setDistanceFunction(distanceFactory.produceDistanceFunction(sif));
                               somData.setFuzzyNeuronFactory(DEFAULT_NEURON_FACTORY);
                               somData.setLearningFunctionFactory(DEFAULT_LEARNING_FUNCTION_FACTORY);
                               somData.setNeighborhoodFunctionFactory(DEFAULT_NEIGHBORHOOD_FUNCTION_FACTORY);
                               somData.setNumberOfEpochs(DEFAULT_NUMBER_OF_EPOCHS);
                               somData.setOverMedianStrongFactor(beta);
                               somData.setOverMedianWeakFactor(DEFAULT_ALPHA);
                               somData.setRectangleHeight(height);
                               somData.setRectangleWidth(width);
                               somData.setTdsm(tdsm);
                               somData.setTopologyFactory(DEFAULT_TOPOLOGY_FACTORY);
                               somData.setSomInitializer(DEFAULT_SOM_INITIALIZER);
                               somData.registerLastCreatedSomParameters(); // som parameters set
                               somData.setSom(produceSom(somData)); // som created
                               System.out.println("SOM ready");
                               TestResult result = new TestResult(uniqueTestIndex, height, width, beta, sifName);
                               learnSom(somData, result); // som learned
                               final SomClusterResult clusterResult = SomClusterResult.produceClusterResult(
                                       somData.getSom(),
                                       tdsm.getTokenDataSource());
                               // dead neurons
                               result.setDeadNeuronsCount(countDeadNeurons(clusterResult));
                               // add purity
                               PurityColumnBenchmark purityBenchmark = new PurityColumnBenchmark(clusterResult,
                                       somData.getDistanceFunction(), somData.getTdsm().getTokenDataSource(),
                                       REF_COLUMN_INDEX);
                               if (purityBenchmark.hasFailed()) {
                                   throw new IllegalStateException("Purity failed");
                               }
                               result.setPurity(purityBenchmark.getValue());
                               // add buffered image
                               Map<Point, Color> colors = ImageProducer.getColorScheme(somData.getSom().getNeurons(),
                                       clusterResult, refColumnColorMap, somData.getTdsm().getTokenDataSource(),
                                       REF_COLUMN_INDEX);
                               RenderedImage image = ImageProducer.produceImage(colors, height, width);
                               Path file = Files.createFile(sifTestPath.resolve(String.format("Test no.%d", uniqueTestIndex))) ;
                               ImageIO.write(image, "png", file.toFile());
                               // add visual quality
                               result.setVisualQuality(ImageProducer.getVisualQualityIndex(getTopology(somData.getSom()), colors, height, width));
                               writer.write(result.toString());
                               writer.flush();
                               uniqueTestIndex++;
                           }
                       }
                   }
               }
           }
        }
        System.out.println("Tests ended");
    }

    private static void checkSomComponents(SomComponents somData) throws IllegalArgumentException {
        if (somData.getRectangleWidth() < 1) {
            throw new IllegalArgumentException(SomI18nProvider.INSTANCE.getValue("ui.exception.bad.width"));
        }
        if (somData.getRectangleHeight() < 1) {
            throw new IllegalArgumentException(SomI18nProvider.INSTANCE.getValue("ui.exception.bad.height"));
        }
        if (somData.getTdsm() == null) {
            throw new IllegalArgumentException(SomI18nProvider.INSTANCE.getValue("ui.exception.tds.missing"));
        }
        if (somData.getDistanceFunction() == null) {
            throw new IllegalArgumentException(SomI18nProvider.INSTANCE.getValue("ui.exception.distance.missing"));
        }
        if (somData.getLearningFunctionFactory() == null) {
            throw new IllegalArgumentException(SomI18nProvider.INSTANCE.getValue("ui.exception.learning.missing"));
        }
        if (somData.getNeighborhoodFunctionFactory() == null) {
            throw new IllegalArgumentException(SomI18nProvider.INSTANCE.getValue("ui.exception.neighborhood.missing"));
        }
        if (somData.getFuzzyNeuronFactory() == null) {
            throw new IllegalArgumentException(
                    SomI18nProvider.INSTANCE.getValue("ui.exception.neuron.factory.missing"));
        }
        if (somData.getSomInitializer() == null) {
            throw new IllegalArgumentException(SomI18nProvider.INSTANCE.getValue("ui.exception.init.missing"));
        }
        if (somData.getTopologyFactory() == null) {
            throw new IllegalArgumentException(SomI18nProvider.INSTANCE.getValue("ui.exception.topology.missing"));
        }
        if (somData.getOverMedianStrongFactor() <= somData.getOverMedianWeakFactor()) {
            throw new IllegalArgumentException(
                    SomI18nProvider.INSTANCE.getValue("ui.exception.median.factors.intersection"));
        }
    }

    private static Som produceSom(SomComponents somData) throws IllegalStateException, IOException {
        checkSomComponents(somData);
        final int width = somData.getRectangleWidth();
        final int height = somData.getRectangleHeight();
        final int epochNumber = somData.getNumberOfEpochs();
        if (epochNumber < 1) {
            throw new IllegalArgumentException(SomI18nProvider.INSTANCE.getValue("ui.exception.bad.epoch"));
        }
        final SomTopology topology = somData.getTopologyFactory().createTopology(width, height, new EuclideanMetric());
        final NeighborhoodFunction neighborhoodFunction = somData.getNeighborhoodFunctionFactory()
                .produceNeighborhoodFunction(epochNumber, width * height);
        final LearningFunction learningFunction = somData.getLearningFunctionFactory()
                .produceLearningFunction(epochNumber);
        final FuzzyNeuronFactory<?> neuronFactory = somData.getFuzzyNeuronFactory();
        final double overMedianWeakFactor = somData.getOverMedianWeakFactor();
        final double overMedianStrongFactor = somData.getOverMedianStrongFactor();
        final SomInitializer somInitializer = somData.getSomInitializer();
        return somInitializer.checkDataSourceAndCreateSom(somData.getTdsm(), somData.getDistanceFunction(), topology,
                neighborhoodFunction, learningFunction, neuronFactory, overMedianWeakFactor, overMedianStrongFactor);
    }

    private static void learnSom(SomComponents somData, TestResult testResult) throws IOException {
        final Som som = somData.getSom();
        final int epochNumber = somData.getLastCreatedNumberOfEpochs();
        int epochCounter = 0;
        while (++epochCounter <= epochNumber) {
            final double avgError = som.learnEpoch(epochCounter);
            if (epochCounter == 1) {
                testResult.setStartAvgError(avgError);
                testResult.setMaxAvgError(avgError);
                testResult.setMinAvgError(avgError);
            }
            if (avgError > testResult.getMaxAvgError()) {
                testResult.setMaxAvgError(avgError);
            }
            if (avgError < testResult.getMinAvgError()) {
                testResult.setMinAvgError(avgError);
            }
            if (epochCounter == epochNumber) {
                testResult.setEndAvgError(avgError);
            }
        }
    }

    private int countDeadNeurons(SomClusterResult clusterResult) {
        int counter = 0;
        for (int i = 0; i < clusterResult.getClusterNumber(); i++) {
            if (clusterResult.getClusterSize(i) == 0L) {
                counter++;
            }
        }
        return counter;
    }

    // some dirty reflection
    private SomTopology getTopology(Som som) throws NoSuchFieldException, SecurityException,
        IllegalArgumentException, IllegalAccessException {
        Field f = som.getClass().getDeclaredField("topology");
        f.setAccessible(true);
        return (SomTopology) f.get(som); 
    }

}

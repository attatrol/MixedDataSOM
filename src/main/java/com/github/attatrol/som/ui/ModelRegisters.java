package com.github.attatrol.som.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.attatrol.som.som.functions.learning.HyperbolicLearningFunction;
import com.github.attatrol.som.som.functions.learning.LearningFunctionFactory;
import com.github.attatrol.som.som.functions.learning.LinearLearningFunction;
import com.github.attatrol.som.som.functions.neighbourhood.AbsentNeighborhoodFunction;
import com.github.attatrol.som.som.functions.neighbourhood.BubbleNeighborhoodFunction;
import com.github.attatrol.som.som.functions.neighbourhood.GaussNeighbourhoodFunction;
import com.github.attatrol.som.som.functions.neighbourhood.LinearNeighborhoodFunction;
import com.github.attatrol.som.som.functions.neighbourhood.NeighborhoodFunctionFactory;
import com.github.attatrol.som.som.initializers.RandomRecordsInitializer;
import com.github.attatrol.som.som.initializers.RandomWeightsInitializer;
import com.github.attatrol.som.som.initializers.SomInitializer;
import com.github.attatrol.som.som.neuron.FrequencyControlledFuzzyNeuron;
import com.github.attatrol.som.som.neuron.FuzzyNeuron;
import com.github.attatrol.som.som.neuron.FuzzyNeuronFactory;
import com.github.attatrol.som.som.neuron.ResettingFuzzyNeuron;
import com.github.attatrol.som.som.topology.RectangleTopology;
import com.github.attatrol.som.som.topology.RectangleTopologyFactory;
import com.github.attatrol.som.som.topology.ToroidalTopology;
import com.github.attatrol.som.ui.utils.benchmarkfactories.BenchmarkUiFactory;
import com.github.attatrol.som.ui.utils.benchmarkfactories.CalinskiHarabaszIndexFactory;
import com.github.attatrol.som.ui.utils.benchmarkfactories.TrivialReferenceColumnBenchmarkFactory;

/**
 * All observable lists of UI are filled with this collections.
 * This class created just to hold everything in one place.
 * @author atta_troll
 *
 */
final class ModelRegisters {

    public static final List<LearningFunctionFactory<?>> LEARNING_FUNCTON_FACTORIES;
    static {
        List<LearningFunctionFactory<?>> set = new ArrayList<>();
        set.add(new LinearLearningFunction.Factory());
        set.add(new HyperbolicLearningFunction.Factory());
        LEARNING_FUNCTON_FACTORIES = Collections.unmodifiableList(set);
    }

    public static final List<NeighborhoodFunctionFactory<?>> NEIGHBORHOOD_FUNCTON_FACTORIES;
    static {
        List<NeighborhoodFunctionFactory<?>> set = new ArrayList<>();
        set.add(new GaussNeighbourhoodFunction.Factory());
        set.add(new LinearNeighborhoodFunction.Factory());
        set.add(new BubbleNeighborhoodFunction.Factory());
        set.add(new AbsentNeighborhoodFunction.Factory());
        NEIGHBORHOOD_FUNCTON_FACTORIES = Collections.unmodifiableList(set);
    }

    public static final List<RectangleTopologyFactory<?>> RECTANGLE_TOPOLOGY_FACTORIES;
    static {
        List<RectangleTopologyFactory<?>> set = new ArrayList<>();
        set.add(new RectangleTopology.Factory());
        set.add(new ToroidalTopology.Factory());
        RECTANGLE_TOPOLOGY_FACTORIES = Collections.unmodifiableList(set);
    }

    public static final List<SomInitializer> SOM_INITIALIZERS;
    static {
        List<SomInitializer> set = new ArrayList<>();
        set.add(new RandomWeightsInitializer());
        set.add(new RandomRecordsInitializer());
        SOM_INITIALIZERS = Collections.unmodifiableList(set);
    }

    public static final List<FuzzyNeuronFactory<?>> FUZZY_NEURON_FACTORIES;
    static {
        List<FuzzyNeuronFactory<?>> set = new ArrayList<>();
        set.add(new FuzzyNeuron.Factory());
        set.add(new FrequencyControlledFuzzyNeuron.Factory());
        set.add(new ResettingFuzzyNeuron.Factory());
        FUZZY_NEURON_FACTORIES = Collections.unmodifiableList(set);
    }

    public static final List<BenchmarkUiFactory> BENCHMARK_FACTORIES;
    static {
        List<BenchmarkUiFactory> set = new ArrayList<>();
        set.add(new CalinskiHarabaszIndexFactory());
        set.add(new TrivialReferenceColumnBenchmarkFactory());
        BENCHMARK_FACTORIES = Collections.unmodifiableList(set);
    }
    
    private ModelRegisters(){}

}

package com.github.attatrol.som;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.TokenDataSourceUtils;
import com.github.attatrol.preprocessing.datasource.parsing.TokenType;
import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.preprocessing.distance.DistanceRegisters;
import com.github.attatrol.preprocessing.distance.MaskedDistanceFunction;
import com.github.attatrol.preprocessing.distance.metric.Metric;
import com.github.attatrol.preprocessing.distance.metric.NormalizedMetric;
import com.github.attatrol.preprocessing.distance.metric.PNorm;
import com.github.attatrol.preprocessing.distance.nonmetric.gower.GowerDistance;
import com.github.attatrol.preprocessing.distance.nonmetric.gower.GowerTokenSimilarityIndexFactory;
import com.github.attatrol.preprocessing.distance.nonmetric.similarity.DissimilarityFunction;
import com.github.attatrol.preprocessing.distance.nonmetric.similarity.SimilarityIndexFactory;
import com.github.attatrol.preprocessing.ui.TokenDataSourceAndMisc;

/**
 * Produces distances of any type.
 * @author atta_troll
 *
 */
public class DistanceProducer {

    private AbstractTokenDataSource<?> maskedDataSource;

    private int[] mask;

    private TokenType[] tokenTypes;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public DistanceProducer(TokenDataSourceAndMisc tdsm) {
        mask = produceMask(tdsm);
        maskedDataSource = new TokenDataSourceUtils
                .MaskedTokenDataSource(tdsm.getTokenDataSource(), mask);
        tokenTypes = tdsm.getTokenTypes();
    }

    public DistanceFunction produceDistanceFunction(SimilarityIndexFactory<?> siFactory) throws IOException {
        return new MaskedDistanceFunction(
                DissimilarityFunction.produceDissimilarityFunction(maskedDataSource,
                        siFactory),
                mask);
    }

    public DistanceFunction produceDistanceFunction(Metric metric) throws IOException {
        return new MaskedDistanceFunction(NormalizedMetric.getNormalizedMetric(maskedDataSource,metric), mask);
    }

    private static int[] produceMask(TokenDataSourceAndMisc tdsm) {
        final TokenType[] types = tdsm.getTokenTypes();
        int maskSize = 0;
        for (TokenType type : types) {
            if (TokenType.isSupportedTokenType(type)) {
                maskSize++;
            }
        }
        int[] mask = new int[maskSize];
        int counter = 0;
        for (int i = 0; i < types.length; i++) {
            if (TokenType.isSupportedTokenType(types[i])) {
                mask[counter] = i;
                counter++;
            }
        }
        return mask;
    }

    public Map<String, DistanceFunction> produceAllCategoricalDistanceFunctions() throws IOException {
        Map<String, DistanceFunction> distances = new HashMap<>();
        for (SimilarityIndexFactory<?> sif : DistanceRegisters.SIMILARITY_INDEX_FACTORY_REGISTER) {
            distances.put(sif.getClass().getName(), produceDistanceFunction(sif));
        }
        return distances;
    }

    public Map<String, DistanceFunction> produceAllNumericalDistanceFunctions() throws IOException {
        Map<String, DistanceFunction> distances = new HashMap<>();
        for (Metric metric : DistanceRegisters.SIMPLE_METRIC_REGISTER) {
            distances.put(metric.getClass().getName(), produceDistanceFunction(metric));
        }
        distances.put("P-Norm 2.5", produceDistanceFunction(PNorm.getPNorm(2.5)));
        distances.put("P-Norm 3", produceDistanceFunction(PNorm.getPNorm(3.)));
        distances.put("P-Norm 3.5", produceDistanceFunction(PNorm.getPNorm(3.5)));
        distances.put("P-Norm 4", produceDistanceFunction(PNorm.getPNorm(4.)));
        distances.put("P-Norm 4.5", produceDistanceFunction(PNorm.getPNorm(4.5)));
        distances.put("P-Norm 5", produceDistanceFunction(PNorm.getPNorm(5.)));
        return distances;
    }

    public Map<String, DistanceFunction> produceGowerDistance() throws IOException {
        Map<String, DistanceFunction> distances = new HashMap<>();
        for (GowerTokenSimilarityIndexFactory<?> gsif :
                DistanceRegisters.GOWER_TOKEN_SIMILARITY_INDEX_FACTORY_REGISTER.get(TokenType.CATEGORICAL_STRING)) {
            GowerTokenSimilarityIndexFactory<?>[] maskedGowerTokenSimilarityIndexesFactories =
                    new GowerTokenSimilarityIndexFactory<?>[mask.length];
            double[] maskedWeights = new double[mask.length];
            for (int i = 0; i < mask.length; i++) {
                maskedGowerTokenSimilarityIndexesFactories[i] = tokenTypes[mask[i]] != TokenType.CATEGORICAL_STRING
                        ? DistanceRegisters.GOWER_TOKEN_SIMILARITY_INDEX_FACTORY_REGISTER.get(tokenTypes[mask[i]])[0]
                                : gsif;
                maskedWeights[i] = 1.;
            }
            final DistanceFunction distanceFunction = new MaskedDistanceFunction(
                    GowerDistance.produceGowerDistance(
                            maskedGowerTokenSimilarityIndexesFactories, maskedWeights,
                            maskedDataSource), mask);
            distances.put(gsif.getClass().getName(), distanceFunction);
        }
        return distances;
    }

    
}

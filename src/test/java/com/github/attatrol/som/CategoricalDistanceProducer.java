package com.github.attatrol.som;

import java.io.IOException;
import java.util.Optional;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.TokenDataSourceUtils;
import com.github.attatrol.preprocessing.datasource.parsing.TokenType;
import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.preprocessing.distance.MaskedDistanceFunction;
import com.github.attatrol.preprocessing.distance.nonmetric.similarity.DissimilarityFunction;
import com.github.attatrol.preprocessing.distance.nonmetric.similarity.SimilarityIndexFactory;
import com.github.attatrol.preprocessing.ui.TokenDataSourceAndMisc;

/**
 * Produces decent cat distance
 * @author atta_troll
 *
 */
public class CategoricalDistanceProducer {

    private TokenDataSourceAndMisc tdsm;

    private AbstractTokenDataSource<?> maskedDataSource;

    private int[] mask;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CategoricalDistanceProducer(TokenDataSourceAndMisc tdsm) {
        this.tdsm = tdsm;
        mask = produceMask(tdsm);
        maskedDataSource = new TokenDataSourceUtils
                .MaskedTokenDataSource(tdsm.getTokenDataSource(), mask);
    }

    public DistanceFunction produceDistanceFunction(SimilarityIndexFactory<?> siFactory) throws IOException {
        return new MaskedDistanceFunction(
                DissimilarityFunction.produceDissimilarityFunction(maskedDataSource,
                        siFactory),
                mask);
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

}

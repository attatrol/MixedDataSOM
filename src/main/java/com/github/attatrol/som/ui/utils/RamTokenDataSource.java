package com.github.attatrol.som.ui.utils;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.DataSource;
import com.github.attatrol.preprocessing.datasource.Record;

/**
 * Token data source intended to be a wrapper over a RAM placed list of already parsed
 * {@link Record}.
 * @author atta_troll
 *
 */
public class RamTokenDataSource extends AbstractTokenDataSource<Object[]> {

    

    private RamTokenDataSource(DataSource<? extends Object[]> internalDataSource, int recordLength) {
        super(internalDataSource, recordLength);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Record<Object[]> parseRecord(Record<? extends Object[]> record)
            throws IllegalArgumentException {
        return  (Record<Object[]>) record;
    }

    public static RamTokenDataSource getClusterRamTokenSource(
            List<Record<Object[]>> recordsInRam, int recordLength) {
        return new RamTokenDataSource(new RamDataSource(recordsInRam), recordLength);
    }

    /**
     * Small internal wrapper over list of records.
     * @author atta_troll
     *
     */
    private static class RamDataSource implements DataSource<Object[]> {

        private List<Record<Object[]>> recordList;

        private int counter = 0;

        private int recordListSize;

        private boolean isClosed;

        public RamDataSource(List<Record<Object[]>> recordsInRam) {
            recordList = recordsInRam;
            recordListSize = recordsInRam.size();
        }

        @Override
        public void close() throws IOException {
            isClosed = true;
            recordList = null;
        }

        @Override
        public Record<Object[]> next()
                throws IOException, IllegalArgumentException, NoSuchElementException {
            stateCheck();
            try {
                final Record<Object[]> record = recordList.get(counter++);
                return record;
            }
            catch (IndexOutOfBoundsException ex) {
                throw new NoSuchElementException("Ram data source is out of elements");
            }
        }

        @Override
        public boolean hasNext() throws IOException {
            stateCheck();
            return counter < recordListSize;
        }

        @Override
        public void reset() throws IOException {
            stateCheck();
            counter = 0;
        }

        /**
         * Checks if IO operation is available.
         * @throws IOException on failure to perform stream opening.
         */
        private void stateCheck() throws IOException {
            if (isClosed) {
                throw new IllegalStateException("Illegal access to closed resource");
            }
        }
    }
}

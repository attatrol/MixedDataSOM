
package com.github.attatrol.som.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.Record;
import com.github.attatrol.preprocessing.ui.TokenDataSourceAndMisc;
import com.github.attatrol.preprocessing.ui.TokenDataSourceTableView;
import com.github.attatrol.preprocessing.ui.misc.UiUtils;
import com.github.attatrol.som.som.Som;
import com.github.attatrol.som.som.SomClusterResult;
import com.github.attatrol.som.som.neuron.AbstractNeuron;
import com.github.attatrol.som.som.neuron.FuzzyNeuron;
import com.github.attatrol.som.som.topology.Point;
import com.github.attatrol.som.ui.i18n.SomI18nProvider;
import com.github.attatrol.som.ui.utils.RamTokenDataSource;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;

/**
 * Pane that shows result of your trained SOM.
 * 
 * @author atta_troll
 *
 */
public class ResultPane extends GridPane {

    /**
     * Max number of neurons to be shown in table view
     */
    private static final int SHOWN_NUMBER_OF_NEURONS = 2500;

    /**
     * Minimal color brightness for a tile.
     */
    private static final double MIN_COLOR_LEVEL = 0.1;

    /**
     * Maximal brightness for a tile.
     */
    private static final double MAX_COLOR_LEVEL = 1.0;

    /**
     * Minimal brightness for a populated (non-empty) tile.
     */
    private static final double MIN_POPULATED_COLOR_LEVEL = 0.3;

    /*
     * Internal state.
     */
    /**
     * Token data source associated with this SOM.
     */
    private final TokenDataSourceAndMisc tdsm;

    /**
     * Result of cluster process of all data with the SOM.
     */
    private final SomClusterResult clusterResult;

    /**
     * Cache for table views.
     */
    private final Map<FuzzyNeuron, TokenDataSourceTableView> tableViews = new WeakHashMap<>();

    /*
     * UI controls.
     */
    /**
     * Current table view.
     */
    private TokenDataSourceTableView currentTableView;

    /**
     * Resets table to very first records.
     */
    private Button reloadTableViewButton = new Button(SomI18nProvider
            .INSTANCE.getValue("result.button.table.reload"));
    {
        reloadTableViewButton.setOnAction(ev -> {
            if (currentTableView != null) {
                currentTableView.reloadView();
                currentTableView.loadNext();
            }
        });
    }

    /**
     * Loads next records.
     */
    private Button loadNextTableViewButton = new Button(
            String.format(SomI18nProvider.INSTANCE.getValue("result.button.table.loadnext"),
                    TokenDataSourceTableView.DEFAULT_SHOWN_RECORD_NUMBER));
    {
        loadNextTableViewButton.setOnAction(ev -> {
            if (currentTableView != null) {
                currentTableView.loadNext();
            }
        });
    }

    /**
     * Currently chosen som tile.
     */
    private SomTile chosenSomTile;

    /**
     * Default ctor
     * @param tdsm token data source with metadata
     * @param clusterResult result of clustering
     * @param som trained SOM
     */
    public ResultPane(TokenDataSourceAndMisc tdsm, SomClusterResult clusterResult, Som som) {
        super();
        this.tdsm = tdsm;
        this.clusterResult = clusterResult;
        final List<AbstractNeuron> neurons = som.getNeurons();
        final List<SomTile> tiles = new ArrayList<>();
        neurons.forEach(neuron -> tiles.add(new SomTile(neuron)));
        paintTiles(tiles);
        final Pane tilePane = new Pane();
        placeTiles(tilePane, tiles);
        add(tilePane, 0, 0, 3, 1);
        GridPane.setHgrow(tilePane, Priority.NEVER);
        GridPane.setVgrow(tilePane, Priority.NEVER);
        //TODO i'm lazy and sure there is a better way of scaling
        tilePane.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth() / 2.);
        tilePane.setPrefHeight(Screen.getPrimary().getVisualBounds().getHeight() / 2.);
        setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth() / 1.5);
        setPrefHeight(Screen.getPrimary().getVisualBounds().getHeight() - 25.);
        add(reloadTableViewButton, 0, 1);
        add(loadNextTableViewButton, 1, 1);
        add(new Label(String.format(SomI18nProvider.INSTANCE.getValue("result.label.table.info"),
                SHOWN_NUMBER_OF_NEURONS)), 0, 2, 3, 1);
    }

    /**
     * Paints tiles in wonderful colors according to their size.
     * @param tiles tile collection
     */
    private void paintTiles(List<SomTile> tiles) {
        long maxSize = 0L;
        long minSize = 0L;
        Iterator<SomTile> iterator = tiles.iterator();
        if (iterator.hasNext()) {
            final long size = iterator.next().getSize();
            maxSize = size;
            minSize = size;
        }
        while (iterator.hasNext()) {
            final long size = iterator.next().getSize();
            if (maxSize < size) {
                maxSize = size;
            }
            else if (minSize > size) {
                minSize = size;
            }
        }
        final long range = maxSize - minSize;
        for (SomTile tile : tiles) {
            final long size = tile.getSize();
            final double colorLevel;
            if (size == 0L) {
                colorLevel = MIN_COLOR_LEVEL;
            }
            else {
                colorLevel = ((double) (size - minSize)) / range * (MAX_COLOR_LEVEL
                        - MIN_POPULATED_COLOR_LEVEL) + MIN_POPULATED_COLOR_LEVEL;
            }
            tile.setColorIndex(colorLevel);
        }
    }

    /**
     * Places tiles onto tile pane.
     * @param tilePane tile pane
     * @param tiles tiles
     */
    private void placeTiles(Pane tilePane, List<SomTile> tiles) {
        double xMin = 0;
        double xMax = 0;
        double yMin = 0;
        double yMax = 0;
        Iterator<SomTile> iterator = tiles.iterator();
        if (iterator.hasNext()) {
            final Point point = iterator.next().getPosition();
            xMin = point.getX();
            xMax = point.getX();
            yMin = point.getY();
            yMax = point.getY();
        }
        while (iterator.hasNext()) {
            final Point point = iterator.next().getPosition();
            final double x = point.getX();
            final double y = point.getY();
            if (x < xMin) {
                xMin = x;
            }
            if (x > xMax) {
                xMax = x;
            }
            if (y < yMin) {
                yMin = y;
            }
            if (y > yMax) {
                yMax = y;
            }
        }
        final double xRange = xMax - xMin + 1.;
        final double yRange = yMax - yMin + 1.;
        for (SomTile tile : tiles) {
            final Point point = tile.getPosition();
            final double x = point.getX();
            final double y = point.getY();
            tile.xProperty().bind(tilePane.widthProperty().multiply(x - xMin).divide(xRange));
            tile.yProperty().bind(tilePane.heightProperty().multiply(y - yMin).divide(yRange));
            tile.heightProperty().bind(tilePane.heightProperty().divide(yRange));
            tile.widthProperty().bind(tilePane.widthProperty().divide(xRange));
            tilePane.getChildren().add(tile);
        }
    }

    /**
     * Sets new visible table view.
     * @param neuron neuron which content is to be shown
     */
    private void setTableView(AbstractNeuron neuron) {
        TokenDataSourceTableView newTableView = tableViews.get(neuron);
        try {
            if (newTableView == null) {
                newTableView = produceNewTableView(neuron);
            }
            if (newTableView != currentTableView) {
                if (currentTableView != null) {
                    getChildren().remove(currentTableView);
                }
                currentTableView = newTableView;
                add(newTableView, 0, 3, 4, 1);
                newTableView.loadNext();
            }
        }
        catch (IOException ex) {
            UiUtils.showExceptionMessage(ex);
        }
    }

    /**
     * Produces new table view for a neuron.
     * @param neuron neuron
     * @return table with records associated with the neuron
     * @throws IOException on data source i/o error
     */
    private TokenDataSourceTableView produceNewTableView(AbstractNeuron neuron) throws IOException {
        final AbstractTokenDataSource<?> originalDataSource = tdsm.getTokenDataSource();
        final List<Record<Object[]>> records = clusterResult.getClusterRecords(
                originalDataSource, neuron, SHOWN_NUMBER_OF_NEURONS);
        final AbstractTokenDataSource<?> ramDatasource = RamTokenDataSource
                .getClusterRamTokenSource(records, originalDataSource.getRecordLength());
        final TokenDataSourceTableView tableView =
                new TokenDataSourceTableView(ramDatasource, tdsm.getTitles());
        return tableView;
    }

    /**
     * A single rectangular SOM tile.
     * @author atta_troll
     *
     */
    private class SomTile extends Rectangle {

        /**
         * Color brightness based on number of records.
         */
        private double colorIndex;

        /**
         * FuzzyNeuron associated with this tile.
         */
        private final AbstractNeuron neuron;

        SomTile(AbstractNeuron neuron) {
            this.neuron = neuron;
            this.setStrokeWidth(2.);
            setStroke(Color.WHITE);
            setOnMouseClicked(ev -> {
                setTableView(neuron);
                if (chosenSomTile != null) {
                    chosenSomTile.setInactiveColor();
                }
                chosenSomTile = this;
                setActiveColor();
            });
        }

        public Point getPosition() {
            return neuron.getPosition();
        }

        public long getSize() {
            return clusterResult.getClusterSize(neuron);
        }

        public void setColorIndex(double colorIndex) {
            this.colorIndex = colorIndex;
            setInactiveColor();
        }

        public void setInactiveColor() {
            setFill(Color.color(0., colorIndex, 0.));
        }

        public void setActiveColor() {
            setFill(Color.color(colorIndex, 0., 0.));
        }
    }
}

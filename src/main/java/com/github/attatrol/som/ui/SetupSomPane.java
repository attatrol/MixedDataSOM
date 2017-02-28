package com.github.attatrol.som.ui;

import com.github.attatrol.preprocessing.ui.TokenDataSourceTableView;
import com.github.attatrol.preprocessing.ui.misc.UiUtils;
import com.github.attatrol.som.som.functions.learning.LearningFunctionFactory;
import com.github.attatrol.som.som.functions.neighbourhood.NeighborhoodFunctionFactory;
import com.github.attatrol.som.som.initializers.SomInitializer;
import com.github.attatrol.som.som.neuron.FuzzyNeuronFactory;
import com.github.attatrol.som.som.topology.RectangleTopologyFactory;
import com.github.attatrol.som.ui.AvgErrorChart.ChartFiller;
import com.github.attatrol.som.ui.i18n.SomI18nComboBox;
import com.github.attatrol.som.ui.i18n.SomI18nProvider;
import com.github.attatrol.som.ui.utils.PositiveDoubleParsingTextField;
import com.github.attatrol.som.ui.utils.PositiveIntegerParsingTextField;
import com.github.attatrol.som.ui.utils.ZeroToOneDoubleParsingTetField;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

public class SetupSomPane extends BorderPane {

    public final static int CHART_NUMBER_OF_POINTS = 200;

    /*
     * Internal state
     */
    /**
     * Current internal state of this window.
     * Think of this as of state of finite automata.
     */
    private SetupFormState currentState;

    /**
     * POJO, holds all and every model variables used to create a SOM.
     */
    private final SomComponents somData = new SomComponents();

    /*
     * GUI controls
     */
    /**
     * Content pane where volatile controls are placed.
     */
    private GridPane contentPane = UiUtils.getGridPane();

    /**
     * Status label says current stage of the process.
     */
    private Label statusLabel = new Label();
    {
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 20;");
        statusLabel.setWrapText(true);
        BorderPane.setAlignment(statusLabel, Pos.CENTER);
        BorderPane.setMargin(statusLabel, new Insets(12,12,12,12));
    }

    /**
     * Setups data source.
     */
    private Button setDataSourceButton = new SetDataSourceButton(this);

    /**
     * Setups distance function.
     */
    private Button setDistanceFunctionButton = new SetDistanceFunctionButton(this);

    /**
     * Creates untrained SOM.
     */
    private Button createSomButton = new CreateSomButton(this);

    /**
     * Trains SOM.
     */
    private Button learnSomButton = new LearnSomButton(this);

    /**
     * Cancels learning process.
     */
    private Button cancelLearnSomButton =
            new Button(SomI18nProvider.INSTANCE.getValue("main.label.button.cancellearning"));
    {
        cancelLearnSomButton.setOnAction(ev -> somData.setLearnSomAbortFlag(true));
    }

    /**
     * Shows SOM as it is.
     */
    private Button showSomButton = new ShowResultFormButton(this);

    /**
     * Table, shows current data source content.
     */
    private TokenDataSourceTableView dataSourceTableView;

    /**
     * Resets table to very first records.
     */
    private Button reloadTableViewButton =
            new Button(SomI18nProvider.INSTANCE.getValue("main.label.button.table.reload"));
    {
        reloadTableViewButton.setOnAction(ev -> {
            dataSourceTableView.reloadView();
            dataSourceTableView.loadNext();
        });
    }

    /**
     * Loads next records.
     */
    private Button loadNextTableViewButton = new Button(
            String.format(SomI18nProvider.INSTANCE.getValue(
                    "main.label.button.table.loadnext"),
                    TokenDataSourceTableView.DEFAULT_SHOWN_RECORD_NUMBER));
    {
        loadNextTableViewButton.setOnAction(ev -> dataSourceTableView.loadNext());
    }

    /**
     * Chart with average error per epoch filled via learning process
     */
    private AvgErrorChart avgErrorChart;

    /**
     * SOM width input tetx field.
     */
    private PositiveIntegerParsingTextField widthTextField = new PositiveIntegerParsingTextField();
    {
        widthTextField.getValueProperty().addListener(
                (observable, oldValue,newValue) -> somData.setRectangleWidth(newValue));
    }

    /**
     * SOM height input text field.
     */
    private PositiveIntegerParsingTextField heightTextField =
            new PositiveIntegerParsingTextField();
    {
        heightTextField.getValueProperty().addListener(
                (observable, oldValue,newValue) -> somData.setRectangleHeight(newValue));
    }

    /**
     * Number of epoch input text field.
     */
    private PositiveIntegerParsingTextField epochNumberTextField =
            new PositiveIntegerParsingTextField();
    {
        epochNumberTextField.getValueProperty().addListener(
                (observable, oldValue,newValue) -> somData.setNumberOfEpochs(newValue));
    }

    /**
     * desired average error level input text field.
     */
    private PositiveDoubleParsingTextField averageErrorTextField =
            new PositiveDoubleParsingTextField();
    {
        averageErrorTextField.getValueProperty().addListener(
                (observable, oldValue,newValue) -> somData.setDesiredAverageError(newValue));
    }

    /**
     * desired average error level input text field.
     */
    private PositiveDoubleParsingTextField winnerLoweringFactorTextField =
            new PositiveDoubleParsingTextField();
    {
        winnerLoweringFactorTextField.getValueProperty().addListener(
                (observable, oldValue,newValue) -> somData.setWinnerLoweringFactor(newValue));
    }

    /**
     * Check box that switches rules that define trained SOM during learning process.
     */
    private CheckBox learningRegimesSwitchCheckBox = new CheckBox(
            SomI18nProvider.INSTANCE.getValue("main.checkbox.switch.learning.modes"));
    {
        learningRegimesSwitchCheckBox.selectedProperty().addListener(
                (observable, oldValue,newValue) -> {
            epochNumberTextField.setDisable(newValue);
            averageErrorTextField.setDisable(!newValue);
            if (newValue) {
                somData.setChosenSomMode(SomMode.AVERAGE_ERROR_SET);
            }
            else {
                somData.setChosenSomMode(SomMode.EPOCH_NUMBER_SET);
            }
        });
    }

    private ComboBox<RectangleTopologyFactory<?>> rectangleTopologyFactoryComboBox =
            new SomI18nComboBox<>();
    {
        rectangleTopologyFactoryComboBox.getItems()
            .addAll(ModelRegisters.RECTANGLE_TOPOLOGY_FACTORIES);
        rectangleTopologyFactoryComboBox.valueProperty().addListener(
                (observable, oldValue,newValue) -> somData.setTopologyFactory(newValue));
    }

    private ComboBox<LearningFunctionFactory<?>> learningFunctionFactoryComboBox =
            new SomI18nComboBox<>();
    {
        learningFunctionFactoryComboBox.getItems()
            .addAll(ModelRegisters.LEARNING_FUNCTON_FACTORIES);
        learningFunctionFactoryComboBox.valueProperty().addListener(
                (observable, oldValue,newValue) -> somData.setLearningFunctionFactory(newValue));
    }

    private ComboBox<NeighborhoodFunctionFactory<?>> neighborhoodFunctionFactoryComboBox =
            new SomI18nComboBox<>();
    {
        neighborhoodFunctionFactoryComboBox.getItems()
            .addAll(ModelRegisters.NEIGHBORHOOD_FUNCTON_FACTORIES);
        neighborhoodFunctionFactoryComboBox.valueProperty().addListener(
                (observable, oldValue,newValue) ->
                    somData.setNeighborhoodFunctionFactory(newValue));
    }

    private ComboBox<FuzzyNeuronFactory<?>> neuronFactoryComboBox = new SomI18nComboBox<>();
    {
        neuronFactoryComboBox.getItems().addAll(ModelRegisters.FUZZY_NEURON_FACTORIES);
        neuronFactoryComboBox.valueProperty().addListener(
                (observable, oldValue,newValue) -> somData.setFuzzyNeuronFactory(newValue));
    }

    private ComboBox<SomInitializer> somInitializerComboBox = new SomI18nComboBox<>();
    {
        somInitializerComboBox.getItems().addAll(ModelRegisters.SOM_INITIALIZERS);
        somInitializerComboBox.valueProperty().addListener(
                (observable, oldValue,newValue) -> somData.setSomInitializer(newValue));
    }

    /**
     * Default ctor.
     */
    public SetupSomPane() {
        super();
        setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth() - 25.);
        setLeft(createButtonPane());
        setCenter(createContentPane());
        setTop(statusLabel);
        setInternalState(SetupFormState.INITIAL_0);
    }

    /**
     * Creates and fills with controls a button pane.
     * @return ready button pane
     */
    private Node createButtonPane() {
        VBox buttonPane = new VBox();
        buttonPane.setPadding(new Insets(15, 15, 15, 15));
        buttonPane.setSpacing(10);
        buttonPane.getChildren().addAll(setDataSourceButton,
                setDistanceFunctionButton,
                new Label(SomI18nProvider.INSTANCE.getValue("main.label.enter.width")),
                widthTextField,
                new Label(SomI18nProvider.INSTANCE.getValue("main.label.enter.height")),
                heightTextField,
                learningRegimesSwitchCheckBox,
                new Label(SomI18nProvider.INSTANCE.getValue("main.label.enter.epoch")),
                epochNumberTextField,
                new Label(SomI18nProvider.INSTANCE.getValue("main.label.enter.avgerror")),
                averageErrorTextField,
                new Label(SomI18nProvider.INSTANCE.getValue("main.label.enter.patronage")),
                winnerLoweringFactorTextField,
                new Label(SomI18nProvider.INSTANCE.getValue("main.label.choose.topology")),
                rectangleTopologyFactoryComboBox,
                new Label(SomI18nProvider.INSTANCE.getValue("main.label.choose.learning")),
                learningFunctionFactoryComboBox,
                new Label(SomI18nProvider.INSTANCE.getValue("main.label.choose.neighborhood")),
                neighborhoodFunctionFactoryComboBox,
                new Label(SomI18nProvider.INSTANCE.getValue("main.label.choose.neuron")),
                neuronFactoryComboBox,
                new Label(SomI18nProvider.INSTANCE.getValue("main.label.choose.initializer")),
                somInitializerComboBox,
                createSomButton,
                learnSomButton,
                cancelLearnSomButton,
                showSomButton);
        showSomButton.setMaxWidth(Double.MAX_VALUE);
        for (Node child : buttonPane.getChildren()) {
            VBox.setVgrow(child, Priority.ALWAYS);
            // make sure the list above are Region instances
            ((Region) child).setMaxWidth(Double.MAX_VALUE);
        }
        return new ScrollPane(buttonPane);
    }

    /**
     * Fills with content a content pane.
     * @return ready content pane
     */
    private Node createContentPane() {
        contentPane.add(reloadTableViewButton, 0, 0);
        contentPane.add(loadNextTableViewButton, 1, 0);
        return contentPane;
    }

    /**
     * Checks current internal state of the main form.
     * Main form is a finite automata.
     * @return current internal state of the main form
     */
    public SetupFormState getInternalState() {
        return currentState;
    }

    /**
     * Sets new internal state to the main form.
     * Main form is a finite automata.
     * @param newState new internal state
     */
    public void setInternalState(SetupFormState newState) {
        currentState = newState;
        statusLabel.setText(newState.getStatusText());
        newState.applyState(this);
    }

    /**
     * @return model associated with this view. Model is a set of variables used
     * to create a single SOM and further SOM action.
     */
    public SomComponents getSomComponents() {
        return somData;
    }

    /**
     * @return {@link AvgErrorChart.ChartFiller} for current chart.
     */
    public ChartFiller getNewChartFiller() {
        return avgErrorChart == null ? null : new AvgErrorChart.ChartFiller(avgErrorChart);
    }



    /**
     * Internal states of this stage. Must be applied by calling {@link #applyState(SetupSomPane)}.
     * @author atta_troll
     *
     */
    public enum SetupFormState {
        INITIAL_0(SomI18nProvider.INSTANCE.getValue("main.state.0")) {
            @Override
            public void applyState(SetupSomPane form) {
                removeTableView(form);
                removeChart(form);
                form.somData.erase();
                form.epochNumberTextField.setTextAndValue(1000);
                form.averageErrorTextField.setTextAndValue(0.1);
                form.learningRegimesSwitchCheckBox.setSelected(false);
                form.heightTextField.setTextAndValue(5);
                form.widthTextField.setTextAndValue(5);
                form.winnerLoweringFactorTextField.setTextAndValue(0.);
                form.rectangleTopologyFactoryComboBox.getSelectionModel().clearSelection();
                form.learningFunctionFactoryComboBox.getSelectionModel().clearSelection();
                form.neighborhoodFunctionFactoryComboBox.getSelectionModel().clearSelection();
                form.neuronFactoryComboBox.getSelectionModel().clearSelection();
                form.somInitializerComboBox.getSelectionModel().clearSelection();
                disableControls(form, false, true, true, true, true, true, true, true);
            }
        },
        DATA_SOURCE_IN_PRORESS_1(SomI18nProvider.INSTANCE.getValue("main.state.1")) {
            @Override
            public void applyState(SetupSomPane form) {
                disableControls(form, true, true, true, true, true, true, true, true);
            }
        },
        DATA_SOURCE_SET_2(SomI18nProvider.INSTANCE.getValue("main.state.2")) {
            @Override
            public void applyState(SetupSomPane form) {
                removeTableView(form);
                form.dataSourceTableView = new TokenDataSourceTableView(form.somData.getTdsm());
                form.contentPane.add(form.dataSourceTableView, 0, 1, 2, 1);
                disableControls(form, false, false, true, true, true, true, false, true);
            }
        },
        DISTANCE_FUNCTION_IN_PRORESS_3(SomI18nProvider.INSTANCE.getValue("main.state.3")) {
            @Override
            public void applyState(SetupSomPane form) {
                disableControls(form, true, true, true, true, true, true, true, true);
            }
        },
        DISTANCE_FUNCTION_SET_4(SomI18nProvider.INSTANCE.getValue("main.state.4")) {
            @Override
            public void applyState(SetupSomPane form) {
                disableControls(form, false, false, false, true, true, true, false, true);
            }
        },
        SOM_CREATION_IN_PROGRESS_5(SomI18nProvider.INSTANCE.getValue("main.state.5")) {
            @Override
            public void applyState(SetupSomPane form) {
                disableControls(form, true, true, true, true, true, true, false, true);
            }
        },
        SOM_CREATED_6(SomI18nProvider.INSTANCE.getValue("main.state.6")) {
            @Override
            public void applyState(SetupSomPane form) {
                if (form.avgErrorChart != null 
                        && form.avgErrorChart.getEpochNumber()
                        < form.somData.getNumberOfEpochs()) {
                    final AvgErrorChart biggerChart = new AvgErrorChart(
                            form.somData.getNumberOfEpochs(), form.avgErrorChart);
                    removeChart(form);
                    form.avgErrorChart = biggerChart;
                    form.contentPane.add(form.avgErrorChart, 0, 2, 4, 1);
                }
                form.somData.registerLastCreatedSomParameters();
                disableControls(form, false, false, false, false, true, true, false, true);
            }
        },
        SOM_LEARNING_IN_PROGRESS_7(SomI18nProvider.INSTANCE.getValue("main.state.7")) {
            @Override
            public void applyState(SetupSomPane form) {
                if (form.avgErrorChart == null) {
                    form.avgErrorChart = new AvgErrorChart(
                            form.somData.getLastCreatedNumberOfEpochs());
                    form.contentPane.add(form.avgErrorChart, 0, 2, 4, 1);
                }
                disableControls(form, true, true, true, true, false, true, false, false);
            }
        },
        SOM_COMPLETED_8(SomI18nProvider.INSTANCE.getValue("main.state.8")) {
            @Override
            public void applyState(SetupSomPane form) {
                disableControls(form, false, false, false, false, true, false, false, false);
            }
        },
        RESULT_FORM_CRREATION_IN_PROGRESS_9(SomI18nProvider.INSTANCE.getValue("main.state.9")) {
            @Override
            public void applyState(SetupSomPane form) {
                disableControls(form, true, true, true, true, true, true, false, false);
            }
        },
        SOM_CREATION_ERROR(SomI18nProvider.INSTANCE.getValue("main.state.error.som.creation")) {
            @Override
            public void applyState(SetupSomPane form) {
                INITIAL_0.applyState(form);
            }
        },
        SOM_LEARNING_ERROR(SomI18nProvider.INSTANCE.getValue("main.state.error.som.learning")) {
            @Override
            public void applyState(SetupSomPane form) {
                INITIAL_0.applyState(form);
            }
        },
        RESULT_FORM_PRODUCTION_ERROR(SomI18nProvider.INSTANCE.getValue("main.state.error.result.form")) {
            @Override
            public void applyState(SetupSomPane form) {
                INITIAL_0.applyState(form);
            }
        },
        UNKNOWN_ERROR(SomI18nProvider.INSTANCE.getValue("main.state.error.unknown")) {
            @Override
            public void applyState(SetupSomPane form) {
                INITIAL_0.applyState(form);
            }
        };

        private String statusText;

        SetupFormState(String statusText) {
            this.statusText = statusText;
        }

        public String getStatusText() {
            return statusText;
        }

        /**
         * Applies some state of automata to the SetupSomPane instance.
         * I know it is ugly, but don't know better way.
         * @param form SetupSomPane instance
         */
        public abstract void applyState(SetupSomPane form);

        private static void disableControls(SetupSomPane form, 
                boolean setDataSourceButtonDisabled,
                boolean setDistanceFunctionButtonDisabled,
                boolean createSomButtonDisabled,
                boolean learnSomButtonDisabled,
                boolean cancelLearnSomButtonDisabled,
                boolean showSomButtonDisabled,
                boolean tableViewDisabled,
                boolean plotDisabled) {
            form.setDataSourceButton.setDisable(setDataSourceButtonDisabled);
            form.setDistanceFunctionButton.setDisable(setDistanceFunctionButtonDisabled);
            form.learningRegimesSwitchCheckBox.setDisable(createSomButtonDisabled);
            form.averageErrorTextField.setDisable(createSomButtonDisabled
                    || !form.learningRegimesSwitchCheckBox.isSelected());
            form.epochNumberTextField.setDisable(createSomButtonDisabled
                    || form.learningRegimesSwitchCheckBox.isSelected());
            form.heightTextField.setDisable(createSomButtonDisabled);
            form.widthTextField.setDisable(createSomButtonDisabled);
            form.winnerLoweringFactorTextField.setDisable(createSomButtonDisabled);
            form.learningFunctionFactoryComboBox.setDisable(createSomButtonDisabled);
            form.neighborhoodFunctionFactoryComboBox.setDisable(createSomButtonDisabled);
            form.rectangleTopologyFactoryComboBox.setDisable(createSomButtonDisabled);
            form.neuronFactoryComboBox.setDisable(createSomButtonDisabled);
            form.somInitializerComboBox.setDisable(createSomButtonDisabled);
            form.createSomButton.setDisable(createSomButtonDisabled);
            form.learnSomButton.setDisable(learnSomButtonDisabled);
            form.cancelLearnSomButton.setDisable(cancelLearnSomButtonDisabled);
            form.showSomButton.setDisable(showSomButtonDisabled);
            if (form.dataSourceTableView != null) {
                form.dataSourceTableView.setDisable(tableViewDisabled);
                if (!tableViewDisabled) {
                    form.dataSourceTableView.reloadView();
                    form.dataSourceTableView.loadNext();
                }
                else {
                    form.dataSourceTableView.getItems().clear();
                }
            }
            form.reloadTableViewButton.setDisable(tableViewDisabled
                    || !cancelLearnSomButtonDisabled);
            form.loadNextTableViewButton.setDisable(tableViewDisabled
                    || !cancelLearnSomButtonDisabled);
        }
    }

    private static void removeTableView(SetupSomPane form) {
        if (form.dataSourceTableView != null) {
            form.contentPane.getChildren().remove(form.dataSourceTableView);
            form.dataSourceTableView = null;
        }
    }

    public static void removeChart(SetupSomPane form) {
        if (form.avgErrorChart != null) {
            form.contentPane.getChildren().remove(form.avgErrorChart);
            form.avgErrorChart = null;
        }
    }
}

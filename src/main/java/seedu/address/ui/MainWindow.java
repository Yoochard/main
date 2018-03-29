package seedu.address.ui;

import static seedu.address.logic.commands.ChangeThemeCommand.BRIGHT_THEME_CSS_FILE_NAME;
import static seedu.address.logic.commands.ChangeThemeCommand.DARK_THEME_CSS_FILE_NAME;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import seedu.address.commons.core.Config;
import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.ui.ChangeThemeEvent;
import seedu.address.commons.events.ui.ExitAppRequestEvent;
import seedu.address.commons.events.ui.ShowHelpRequestEvent;
import seedu.address.commons.events.ui.ShowNotificationEvent;
import seedu.address.commons.events.ui.ShowReviewDialogEvent;
import seedu.address.logic.Logic;
import seedu.address.model.UserPrefs;
import seedu.address.model.theme.Theme;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final int ENTER = -1;
    private static final int EXIT = 1;
    private static final int NOTIFICATION_CARD_WIDTH = 300;
    private static final int NOTIFICATION_CARD_HEIGHT = 100;
    private static final int NOTIFICATION_CARD_X_OFFSET = 15;
    private static final int NOTIFICATION_CARD_Y_OFFSET = 15;
    private static final int NOTIFICATION_PANEL_WIDTH = 400;

    private final Logger logger = LogsCenter.getLogger(this.getClass());

    private Stage primaryStage;
    private Logic logic;

    // Independent Ui parts residing in this Ui container
    private BrowserPanel browserPanel;
    private PersonListPanel personListPanel;
    private Config config;
    private UserPrefs prefs;

    private ArrayList<ShowNotificationEvent> notifications;

    @FXML
    private StackPane browserPlaceholder;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    @FXML
    private StackPane test;

    @FXML
    private VBox test1;

    public MainWindow(Stage primaryStage, Config config, UserPrefs prefs, Logic logic) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.config = config;
        this.prefs = prefs;

        // Configure the UI
        setTitle(config.getAppTitle());
        setWindowDefaultSize(prefs);
        setAccelerators();
        registerAsAnEventHandler(this);

        notifications = new ArrayList<>();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in later version of SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, and
         * ResultDisplay contains a TextArea, thus some accelerators (e.g F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add following event filter to capture such key events and open
         * help window purposely so to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        browserPanel = new BrowserPanel();
        browserPlaceholder.getChildren().add(browserPanel.getRoot());

        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        ResultDisplay resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(prefs.getAddressBookFilePath());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(logic);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    void hide() {
        primaryStage.hide();
    }

    private void setTitle(String appTitle) {
        primaryStage.setTitle(appTitle);
    }

    /**
     * Sets the default size based on user preferences.
     */
    private void setWindowDefaultSize(UserPrefs prefs) {
        primaryStage.setHeight(prefs.getGuiSettings().getWindowHeight());
        primaryStage.setWidth(prefs.getGuiSettings().getWindowWidth());
        if (prefs.getGuiSettings().getWindowCoordinates() != null) {
            primaryStage.setX(prefs.getGuiSettings().getWindowCoordinates().getX());
            primaryStage.setY(prefs.getGuiSettings().getWindowCoordinates().getY());
        }
    }

    /**
     * Returns the current size and the position of the main Window.
     */
    GuiSettings getCurrentGuiSetting() {
        return new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY(), Theme.getTheme());
    }

    /**
     * Opens the help window.
     */
    @FXML
    public void handleHelp() {
        HelpWindow helpWindow = new HelpWindow();
        helpWindow.show();
    }
    @FXML
    private void handleChangeDarkTheme() {
        EventsCenter.getInstance().post(new ChangeThemeEvent("dark"));
    }

    @FXML
    private void handleChangeBrightTheme() {
        EventsCenter.getInstance().post(new ChangeThemeEvent("bright"));
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        raise(new ExitAppRequestEvent());
    }

    public PersonListPanel getPersonListPanel() {
        return this.personListPanel;
    }

    void releaseResources() {
        browserPanel.freeResources();
    }

    @Subscribe
    private void handleShowHelpEvent(ShowHelpRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        handleHelp();
    }

    @Subscribe
    private void handleChangeThemeEvent (ChangeThemeEvent changeThemeEvent) {
        Scene scene = primaryStage.getScene();
        // Clear the original theme
        scene.getStylesheets().clear();

        String newTheme = changeThemeEvent.getTheme();
        String cssFileName = null;

        // Get the associate CSS file path for theme
        switch (newTheme) {
        case "dark":
            cssFileName = DARK_THEME_CSS_FILE_NAME;
            break;
        case "bright":
            cssFileName = BRIGHT_THEME_CSS_FILE_NAME;
            break;
        default:
            cssFileName = DARK_THEME_CSS_FILE_NAME;
            //Theme.changeTheme(primaryStage, changeThemeEvent.getTheme());
        }

        scene.getStylesheets().add(cssFileName);
        primaryStage.setScene(scene);
    }
    /**
     * Show the notification panel with an animation
     */
    public void showNotificationPanel() {
        animate(test1, NOTIFICATION_PANEL_WIDTH, EXIT);

    }

    /**
     * Show in-app notification
     */
    public void showNewNotification(ShowNotificationEvent event) {
        notifications.add(event);
        System.out.println("Preparing in app notification");
        Region notificationCard = (new NotificationCard(event.getTitle(), notifications.size() + "",
                event.getOwnerName(), event.getEndTime())).getRoot();
        notificationCard.setTranslateX(NOTIFICATION_CARD_WIDTH);
        notificationCard.setTranslateY(-1 * ((notifications.size() - 1) * NOTIFICATION_CARD_HEIGHT
                + notifications.size() * NOTIFICATION_CARD_Y_OFFSET));
        notificationCard.setMaxHeight(NOTIFICATION_CARD_HEIGHT);
        notificationCard.setMaxWidth(NOTIFICATION_CARD_WIDTH);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                test.getChildren().add(notificationCard);
                animate(notificationCard, NOTIFICATION_CARD_WIDTH + NOTIFICATION_CARD_X_OFFSET, ENTER);
            }
        });
    }

    /**
     * Animates any Region object according to predefined style.
     */
    private void animate(Region component, double width, int direction) {
        TranslateTransition enterAnimation = new TranslateTransition(Duration.millis(250), component);
        enterAnimation.setByX(direction * 0.25 * width);
        enterAnimation.play();
        TranslateTransition enterAnimation1 = new TranslateTransition(Duration.millis(250), component);
        enterAnimation1.setByX(direction * 0.75 * width);
        enterAnimation1.play();
        TranslateTransition enterAnimation2 = new TranslateTransition(Duration.millis(250), component);
        enterAnimation2.setByX(direction * width);
        enterAnimation2.play();
    }

    @Subscribe
    private void showDialogPane(ShowReviewDialogEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        ReviewDialog reviewDialog = new ReviewDialog();
        reviewDialog.show();
    }
}

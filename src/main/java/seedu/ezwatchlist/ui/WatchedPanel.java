package seedu.ezwatchlist.ui;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.ezwatchlist.commons.core.LogsCenter;
import seedu.ezwatchlist.model.show.Show;

/**
 * Panel containing the list of shows.
 */
public class WatchedPanel extends UiPart<Region> {
    private static final String FXML = "WatchedPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(WatchedPanel.class);

    @FXML
    private ListView<Show> watchedListView;

    public WatchedPanel(ObservableList<Show> showList) {
        super(FXML);
        watchedListView.setItems(showList);
        watchedListView.setCellFactory(listView -> new WatchedListViewCell());
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Show} using a {@code ShowCard}.
     */
    class WatchedListViewCell extends ListCell<Show> {
        @Override
        protected void updateItem(Show show, boolean empty) {
            super.updateItem(show, empty);

            if (empty || show == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new ShowCard(show, getIndex() + 1).getRoot());
            }
        }
    }

}


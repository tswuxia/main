package seedu.ezwatchlist.model;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.ezwatchlist.commons.core.GuiSettings;
import seedu.ezwatchlist.commons.core.LogsCenter;
import seedu.ezwatchlist.model.show.Name;
import seedu.ezwatchlist.model.show.Show;
import seedu.ezwatchlist.commons.util.CollectionUtil;

/**
 * Represents the in-memory model of the watchlist data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final WatchList watchList;
    private final FilteredList<Show> watchedList;
    private final UserPrefs userPrefs;
    private final FilteredList<Show> filteredShows;
    private final WatchList searchResult = new WatchList();

    /**
     * Initializes a ModelManager with the given watchList and userPrefs.
     */
    public ModelManager(ReadOnlyWatchList watchList, ReadOnlyUserPrefs userPrefs) {
        super();
        CollectionUtil.requireAllNonNull(watchList, userPrefs);

        logger.fine("Initializing with watchlist: " + watchList + " and user prefs " + userPrefs);

        this.watchList = new WatchList(watchList);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredShows = new FilteredList<>(this.watchList.getShowList());

        watchedList = new FilteredList<>(this.watchList.getShowList());
        updateWatchedShowList();
    }

    public ModelManager() {
        this(new WatchList(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs);
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public Path getWatchListFilePath() {
        return userPrefs.getWatchListFilePath();
    }

    @Override
    public void setWatchListFilePath(Path watchListFilePath) {
        requireNonNull(watchListFilePath);
        userPrefs.setWatchListFilePath(watchListFilePath);
    }

    //=========== WatchList ================================================================================

    @Override
    public void setWatchList(ReadOnlyWatchList watchList) {
        this.watchList.resetData(watchList);
    }

    @Override
    public ReadOnlyWatchList getWatchList() {
        return watchList;
    }

    @Override
    public boolean hasShow(Show show) {
        requireNonNull(show);
        return watchList.hasShow(show);
    }

    @Override
    public boolean hasShowName(Name showName) {
        requireNonNull(showName);
        return watchList.hasName(showName);
    }

    @Override
    public List<Show> getShowIfSameNameAs(Name showName) {
        requireNonNull(showName);
        return watchList.getShowIfSameNameAs(showName);
    }

    @Override
    public void deleteShow(Show target) {
        watchList.removeShow(target);
    }

    @Override
    public void addShow(Show show) {
        watchList.addShow(show);
    }

    @Override
    public void setShow(Show target, Show editedShow) {
        CollectionUtil.requireAllNonNull(target, editedShow);

        watchList.setShow(target, editedShow);
    }

    //=========== Filtered Show List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Show} backed by the internal list of
     * {@code versionedWatchList}
     */
    @Override
    public ObservableList<Show> getFilteredShowList() {
        return filteredShows;
    }

    @Override
    public void updateFilteredShowList(Predicate<Show> predicate) {
        requireNonNull(predicate);
        filteredShows.setPredicate(predicate);
    }

    /**
     * Returns an unmodifiable view of the watched list of {@code Show} backed by the internal list of
     * {@code versionedWatchList}
     */
    @Override
    public ObservableList<Show> getWatchedShowList() {
        return watchedList;
    }

    @Override
    public void updateWatchedShowList() {
        watchedList.setPredicate(show -> show.isWatched().value);
    }

    @Override
    public void updateSearchResultList(List<Show> shows) {
        searchResult.setShows(shows);
        updateFilteredShowList(PREDICATE_SHOW_ALL_SHOWS);
    }

    public ObservableList<Show> getSearchResultList() {
        return searchResult.getShowList();
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return watchList.equals(other.watchList)
                && userPrefs.equals(other.userPrefs)
                && filteredShows.equals(other.filteredShows);
    }

}

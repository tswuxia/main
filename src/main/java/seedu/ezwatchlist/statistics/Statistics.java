package seedu.ezwatchlist.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import seedu.ezwatchlist.model.Model;
import seedu.ezwatchlist.model.show.Genres;
import seedu.ezwatchlist.model.show.Show;
import seedu.ezwatchlist.model.show.UniqueShowList;

/**
 * Represents a Statistics object that contains relevant information.
 */
public class Statistics {
    private final Model model;

    public Statistics (Model model) {
        this.model = model;
    }

    /**
     * Gets the movies that are likely to be forgotten by the user.
     * @return an observable list of forgotten shows
     */
    public ObservableList<Show> getForgotten() {
        ObservableList<Show> watchlist = model.getWatchList().getShowList().filtered(show -> !show.isWatched().value);
        UniqueShowList forgotten = new UniqueShowList();
        if (watchlist.size() > 5) {
            forgotten.add(watchlist.get(0));
            forgotten.add(watchlist.get(1));
            forgotten.add(watchlist.get(2));
        }
        return forgotten.asUnmodifiableObservableList();
    }

    /**
     * Gets the favourite genre of the user.
     * @return an observable list of genres strings
     */
    public ObservableList<String> getFavouriteGenre() {
        HashMap<String, Integer> genreRecords = new HashMap<>();

        model.getWatchList().getShowList().stream().forEach(show -> {
            show.getGenres().stream().forEach(genre -> {
                if (genreRecords.containsKey(genre)) {
                    genreRecords.put(genre, genreRecords.get(genre) + 1);
                } else {
                    genreRecords.put(genre, new Integer(1));
                }
            });
        });

        List<String> keyList = new ArrayList<>();
        keyList.addAll(genreRecords.keySet());
        Collections.sort(keyList, (key1, key2) -> genreRecords.get(key2) - genreRecords.get(key1));

        ObservableList<String> favouriteGenres = FXCollections.observableArrayList();
        for (int i = 0; i < 3 && i < keyList.size(); i++) {
            favouriteGenres.add(keyList.get(i));
        }
        return favouriteGenres;
    }
}

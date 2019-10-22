package seedu.ezwatchlist.model.show;

import java.util.ArrayList;
import java.util.Set;

import javafx.scene.image.Image;
import seedu.ezwatchlist.model.actor.Actor;

/**
 * Represents a TvShow in the watchlist.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class TvShow extends Show {

    private static final Image imageOfShow = null;
    private int numOfEpisodesWatched;
    private ArrayList<TvSeason> tvSeasons;
    private final int totalNumOfEpisodes;

    public TvShow(Name name, Description description, IsWatched isWatched,
                  Date dateOfRelease, RunningTime runningTime, Set<Actor> actors,
                  int numOfEpisodesWatched, int totalNumOfEpisodes, ArrayList<TvSeason> tvSeasons) {
        super(name, description, isWatched, dateOfRelease, runningTime, actors);
        this.numOfEpisodesWatched = numOfEpisodesWatched;
        this.totalNumOfEpisodes = totalNumOfEpisodes;
        this.tvSeasons = tvSeasons;
        super.type = "Tv Show";
    }

    public int getNumOfEpisodesWatched() {
        return numOfEpisodesWatched;
    }

    public ArrayList<TvSeason> getTvSeasons() {
        return tvSeasons;
    }

    public int getTotalNumOfEpisodes() {
        return totalNumOfEpisodes;
    }

}

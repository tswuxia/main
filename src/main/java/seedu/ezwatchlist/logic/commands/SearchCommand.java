package seedu.ezwatchlist.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.ezwatchlist.api.ApiMain;
import seedu.ezwatchlist.api.exceptions.OnlineConnectionException;
import seedu.ezwatchlist.commons.core.Messages;
import seedu.ezwatchlist.model.Model;
import seedu.ezwatchlist.model.ReadOnlyWatchList;
import seedu.ezwatchlist.model.show.*;
import seedu.ezwatchlist.api.ApiMain;
import seedu.ezwatchlist.ui.SearchPanel;
import seedu.ezwatchlist.model.show.Movie;
import seedu.ezwatchlist.model.show.Show;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



/**
 * Finds and lists all shows in watchlist whose name contains any of the argument keywords.
 * Keyword matching is case insensitive.
 */
public class SearchCommand extends Command {
    public static final String COMMAND_WORD = "search";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Searches for shows online whose names contain any of "
            + "the specified keywords (case-insensitive) and displays them as a list with index numbers.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " Joker";

    private final String EMPTY_STRING = "";
    private Name showName;
    private String type;
    /*private IsWatched isWatched;

    private List<String> actorList;*/

    public SearchCommand(Optional<String> name/*, Optional<String> type, Optional<String> isWatched,
                         List<String> actorList*/) {
        if (name.isPresent()) {
            this.showName = new Name(name.get().trim());
        } else {
            this.showName = new Name(EMPTY_STRING);
        }

        /*if (type.isPresent()) {
            this.type = type.get().trim();
        } else {
            this.type = EMPTY_STRING;
        }

        this.isWatched = new IsWatched(false);
        if (isWatched.isPresent()) {
            this.isWatched = new IsWatched(Boolean.parseBoolean(isWatched.get().trim()));
        }

        this.actorList = actorList;*/
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        try {
            List<Show> searchResult = new ArrayList<>();

            if (!showName.getName().equals(EMPTY_STRING) /*&&  (model.hasShowName(name))*/) {
                List<Show> filteredShowList = model.getShowIfSameNameAs(showName);
                for (Show show : filteredShowList) {
                    searchResult.add(show);
                }
            }

            if (!showName.getName().equals("")) {
                List<Movie> movies = new ApiMain().getMovieByName(showName.getName());
                List<TvShow> tvShows = new ApiMain().getTvShowByName(showName.getName());
                for (Movie movie : movies) {
                    searchResult.add(movie);
                }
                for(TvShow tvShow : tvShows) {
                    searchResult.add(tvShow);
                }
            }

            model.updateSearchResultList(searchResult);

            return new CommandResult(String.format(Messages.MESSAGE_SHOWS_LISTED_OVERVIEW, model.getSearchResultList().size()));
        } catch (OnlineConnectionException e) {
            return null;
            //to be added
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof SearchCommand // instanceof handles nulls
                && showName.equals(((SearchCommand) other).showName)
                /*&& type.equals(((SearchCommand) other).type)
                && isWatched.equals(((SearchCommand) other).isWatched)
                && actorList.equals(((SearchCommand) other).actorList)*/); // state check
    }


}

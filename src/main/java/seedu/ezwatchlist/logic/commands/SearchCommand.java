package seedu.ezwatchlist.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.ezwatchlist.api.exceptions.OnlineConnectionException;
import seedu.ezwatchlist.api.model.ApiInterface;
import seedu.ezwatchlist.api.model.ApiManager;
import seedu.ezwatchlist.commons.core.messages.SearchMessages;
import seedu.ezwatchlist.logic.commands.exceptions.CommandException;
import seedu.ezwatchlist.logic.parser.SearchKey;
import seedu.ezwatchlist.model.Model;
import seedu.ezwatchlist.model.actor.Actor;
import seedu.ezwatchlist.model.show.Genre;
import seedu.ezwatchlist.model.show.Movie;
import seedu.ezwatchlist.model.show.Name;
import seedu.ezwatchlist.model.show.Show;
import seedu.ezwatchlist.model.show.TvShow;
import seedu.ezwatchlist.model.show.Type;

/**
 * Finds and lists all shows in watchlist whose name contains any of the argument keywords.
 * Keyword matching is case insensitive.
 */
public class SearchCommand extends Command {
    public static final String COMMAND_WORD = "search";

    private static final String INPUT_TRUE = "true";
    private static final String INPUT_YES = "yes";
    private static final String INPUT_FALSE = "false";
    private static final String INPUT_NO = "no";

    private ApiInterface onlineSearch;
    private List<String> nameList;
    private List<String> typeList;
    private List<String> actorList;
    private List<String> genreList;
    private List<String> isWatchedList;
    private List<String> fromOnlineList;
    private List<Show> searchResult = new ArrayList<>();

    private boolean isOffline = false;

    public SearchCommand(HashMap<SearchKey, List<String>> searchShowsHashMap) {
        nameList = searchShowsHashMap.get(SearchKey.KEY_NAME);
        typeList = searchShowsHashMap.get(SearchKey.KEY_TYPE);
        actorList = searchShowsHashMap.get(SearchKey.KEY_ACTOR); // unable to search online
        genreList = searchShowsHashMap.get(SearchKey.KEY_GENRE); // unable to search for shows online
        isWatchedList = searchShowsHashMap.get(SearchKey.KEY_IS_WATCHED);
        fromOnlineList = searchShowsHashMap.get(SearchKey.KEY_FROM_ONLINE);
        try {
            onlineSearch = new ApiManager();
        } catch (OnlineConnectionException e) {
            fromOnlineList = new ArrayList<>();
            fromOnlineList.add(INPUT_NO);
            isOffline = true;
        }
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            if (emptyCompulsoryKeyword()) {
                throw new CommandException("Make sure keyword(s) for n/, a/ or g/ is not empty.\n"
                        + SearchMessages.MESSAGE_USAGE);
            }

            if (!nameList.isEmpty()) {
                searchByName(model);
            }
            if (!actorList.isEmpty()) {
                searchByActor(model);
            }
            if (!genreList.isEmpty()) {
                searchByGenre(model);
            }

            List<Show> result = searchResult.stream().distinct().collect(Collectors.toList());
            model.updateSearchResultList(result);

            if (isOffline) {
                return new CommandResult(String.format(
                        SearchMessages.MESSAGE_INTERNAL_SHOW_LISTED_OVERVIEW, model.getSearchResultList().size()));
            } else {
                return new CommandResult(String.format(SearchMessages.MESSAGE_SHOWS_FOUND_OVERVIEW,
                        model.getSearchResultList().size()));
            }
        } catch (OnlineConnectionException e) {
            if (!nameList.isEmpty()) {
                for (String showName : nameList) {
                    addShowFromWatchListIfSameNameAs(showName, model);
                }
            }
            if (!actorList.isEmpty()) {
                Set<Actor> actorSet = new HashSet<Actor>();
                for (String actorName : actorList) {
                    Actor actor = new Actor(actorName);
                    actorSet.add(actor);
                }
                addShowFromWatchListIfHasActor(actorSet, model);
            }
            if (!genreList.isEmpty()) {
                Set<Genre> genreSet = new HashSet<Genre>();
                for (String genreName : genreList) {
                    Genre genre = new Genre(genreName);
                    if (!genreName.isBlank()) {
                        genreSet.add(genre);
                    } else if (genreName.isBlank()) {
                        throw new CommandException(SearchMessages.MESSAGE_INVALID_GENRE_COMMAND);
                    }
                }
                addShowFromWatchListIfIsGenre(genreSet, model);
            }
            return new CommandResult(String.format(SearchMessages.MESSAGE_INTERNAL_SHOW_LISTED_OVERVIEW,
                    model.getSearchResultList().size()));
        }
    }

    /**
     * Search for shows by name.
     * @param model Model used.
     * @throws CommandException If command exception occurred.
     * @throws OnlineConnectionException If online exception occurred.
     */
    private void searchByName(Model model) throws CommandException, OnlineConnectionException {
        if (requestedSearchFromInternal()) {
            for (String showName : nameList) {
                addShowFromWatchListIfSameNameAs(showName, model);
            }
        } else if (requestedSearchFromOnline()) {
            for (String showName : nameList) {
                addShowFromOnlineIfSameNameAs(showName);
            }
        } else if (requestedFromOnline()) {
            throw new CommandException(SearchMessages.MESSAGE_INVALID_FROM_ONLINE_COMMAND);
        } else { // there's no restriction on where to search from
            for (String showName : nameList) {
                addShowFromWatchListIfSameNameAs(showName, model);
                addShowFromOnlineIfSameNameAs(showName);
            }
        }
    }

    /**
     * Search for shows by actor.
     * @param model Model used.
     * @throws CommandException If command exception occurred.
     * @throws OnlineConnectionException If online exception occurred.
     */
    private void searchByActor(Model model) throws CommandException, OnlineConnectionException {
        Set<Actor> actorSet = new HashSet<Actor>();
        for (String actorName : actorList) {
            Actor actor = new Actor(actorName);
            actorSet.add(actor);
        }

        if (requestedSearchFromInternal()) {
            addShowFromWatchListIfHasActor(actorSet, model);
        } else if (requestedSearchFromOnline()) {
            throw new CommandException(SearchMessages.MESSAGE_UNABLE_TO_SEARCH_FROM_ONLINE_WHEN_SEARCHING_BY_ACTOR);
        } else if (requestedFromOnline()) {
            throw new CommandException(SearchMessages.MESSAGE_INVALID_FROM_ONLINE_COMMAND);
        } else { // there's no restriction on where to search from
            addShowFromWatchListIfHasActor(actorSet, model);
            // addShowFromOnlineIfHasActor(actorSet); // unable to search online for now
        }
    }

    /**
     * Search for shows by genre.
     * @param model Model used.
     * @throws CommandException If command exception occurred.
     * @throws OnlineConnectionException If online exception occurred.
     */
    private void searchByGenre(Model model) throws CommandException, OnlineConnectionException {
        Set<Genre> genreSet = new HashSet<Genre>();
        for (String genreName : genreList) {
            Genre genre = new Genre(genreName);
            if (!genreName.isBlank()) {
                genreSet.add(genre);
            } else if (genreName.isBlank()) {
                throw new CommandException(SearchMessages.MESSAGE_INVALID_GENRE_COMMAND);
            }
        }

        if (requestedSearchFromInternal()) {
            addShowFromWatchListIfIsGenre(genreSet, model);
        } else if (requestedSearchFromOnline()) {
            addShowFromOnlineIfIsGenre(genreSet); //unable to search for online tv
        } else if (requestedFromOnline()) {
            throw new CommandException(SearchMessages.MESSAGE_INVALID_FROM_ONLINE_COMMAND);
        } else { // there's no restriction on where to search from
            addShowFromWatchListIfIsGenre(genreSet, model);
            addShowFromOnlineIfIsGenre(genreSet); //unable to search for online tv
        }
    }

    /**
     * Adds show from list if it has the same name as in {@code showName}.
     * @param showName name of the given show.
     * @param model current model of the program.
     */
    private void addShowFromWatchListIfSameNameAs(String showName, Model model) {
        if (!showName.isBlank()) {
            List<Show> filteredShowList = model.getShowIfHasName(new Name(showName));
            addShowToSearchResult(filteredShowList);
        }
    }

    /**
     * Adds show from list if it has any actor in {@code actorSet}.
     * @param actorSet Set of actors to be searched for.
     * @param model Model used.
     */
    private void addShowFromWatchListIfHasActor(Set<Actor> actorSet, Model model) {
        if (!actorSet.isEmpty()) {
            List<Show> filteredShowList = model.getShowIfHasActor(actorSet);
            addShowToSearchResult(filteredShowList);
        }
    }

    /**
     * Adds show from list if it has any genre in {@code genreSet}.
     * @param genreSet Set of actors to be searched for.
     * @param model Model used.
     */
    private void addShowFromWatchListIfIsGenre(Set<Genre> genreSet, Model model) {
        if (!genreSet.isEmpty()) {
            List<Show> filteredShowList = model.getShowIfIsGenre(genreSet);
            addShowToSearchResult(filteredShowList);
        }
    }

    /**
     * Add show to search result.
     * @param showList List of shows to be added.
     */
    private void addShowToSearchResult(List<Show> showList) {
        for (Show show : showList) {
            if (requestedSearchFromWatched() && !show.isWatched().getIsWatchedBoolean()) {
                continue; // skip if request to be watched but show is not watched
            } else if (requestedSearchFromWatchList() && show.isWatched().getIsWatchedBoolean()) {
                continue; // skip if requested to be in watchlist but show is watched
            } else if (requestedSearchForMovie() && !show.getType().equals(Type.MOVIE)) {
                continue; // skip if requested search for movie but show is tv
            } else if (requestedSearchForTv() && !show.getType().equals(Type.TV_SHOW)) {
                continue; // skip if requested search for tv but show is movie
            }
            searchResult.add(show);
        }
    }

    /**
     * Add shows, both movies and tv series, searched by name from online to the search result list.
     * @param showName Name of the show to be searched.
     * @throws OnlineConnectionException If online exception occurred.
     * @throws CommandException If command exception occurred.
     */
    private void addShowFromOnlineIfSameNameAs(String showName) throws OnlineConnectionException, CommandException {
        if (!requestedIsWatched() && !showName.isBlank()) {
            if (requestedSearchForMovie()) {
                addOnlineMovieSearchedByNameToResult(showName);
            } else if (requestedSearchForTv()) {
                addOnlineTvSearchedByNameToResult(showName);
            } else if (requestedType()) {
                throw new CommandException(SearchMessages.MESSAGE_INVALID_TYPE_COMMAND);
            } else {
                addOnlineMovieSearchedByNameToResult(showName);
                addOnlineTvSearchedByNameToResult(showName);
            }
        }
    }

    /**
     * Add movies, searched by name from online to the search result list.
     * @param showName Name of the show to be searched.
     * @throws OnlineConnectionException If online exception occurred.
     */
    private void addOnlineMovieSearchedByNameToResult(String showName) throws OnlineConnectionException {

        List<Movie> movies = onlineSearch.getMovieByName(showName);
        for (Movie movie : movies) {
            searchResult.add(movie);
        }
    }

    /**
     * Add tv series, searched by name from online to the search result list.
     * @param showName Name of the show to be searched.
     * @throws OnlineConnectionException If online exception occurred.
     */
    private void addOnlineTvSearchedByNameToResult(String showName) throws OnlineConnectionException {
        List<TvShow> tvShows = onlineSearch.getTvShowByName(showName);
        for (TvShow tvShow : tvShows) {
            searchResult.add(tvShow);
        }
    }

    /**
     * Returns a list of movies from the API search method.
     *
     * @param genreSet the set of genres that the user wants to search.
     * @throws OnlineConnectionException when not connected to the internet.
     */
    private void addShowFromOnlineIfIsGenre(Set<Genre> genreSet) throws OnlineConnectionException {
        if (!genreSet.isEmpty()) {
            List<Movie> movies = onlineSearch.getMovieByGenre(genreSet);
            for (Movie movie : movies) {
                searchResult.add(movie);
            }
        }
    }

    /**
     * Returns whether there is any compulsory keyword present.
     * One of the following keyword needs to be present: show name, actor or genre.
     * @return True if all compulsory keyword is empty.
     */
    private boolean emptyCompulsoryKeyword() {
        return nameList.isEmpty() && actorList.isEmpty() && genreList.isEmpty();
    }

    /**
     * Returns true if user requests to search for tv series or movies only.
     * @return True if user requests to search for tv series or movies only.
     */
    private boolean requestedType() {
        return !typeList.isEmpty();
    }

    /**
     * Returns true if user requests to search for movies only.
     * @return True if user requests to search for movies only.
     */
    private boolean requestedSearchForMovie() {
        return requestedType() && (typeList.get(0).equals(Type.MOVIE.getType()));
    }

    /**
     * Returns true if user requests to search for tv series only.
     * @return True if user requests to search for tv series only.
     */
    private boolean requestedSearchForTv() {
        return requestedType() && (typeList.get(0).equals(Type.TV_SHOW.getType()));
    }

    /**
     * Returns true if user requests to search from watch or watched list.
     * @return True if user requests to search from watch or watched list.
     */
    private boolean requestedIsWatched() {
        return !isWatchedList.isEmpty();
    }

    /**
     * Returns true if user requests to search from watched list.
     * @return True if user requests to search from watched list.
     */
    private boolean requestedSearchFromWatched() {
        return requestedIsWatched()
                && (isWatchedList.get(0).equals(INPUT_TRUE) || isWatchedList.get(0).equals(INPUT_YES));
    }

    /**
     * Returns true if user requests to search from watch list.
     * @return True if user requests to search from watch list.
     */
    private boolean requestedSearchFromWatchList() {
        return requestedIsWatched()
                && (isWatchedList.get(0).equals(INPUT_FALSE) || isWatchedList.get(0).equals(INPUT_NO));
    }

    /**
     * Returns true if user requests to search from internal or online.
     * @return True if user requests to search from internal or online.
     */
    private boolean requestedFromOnline() throws CommandException {
        for (String input : fromOnlineList) {
            if (!(input.equals(INPUT_FALSE) || input.equals(INPUT_NO) || input.equals(INPUT_TRUE)
                    || input.equals(INPUT_YES))) {
                throw new CommandException(SearchMessages.MESSAGE_INVALID_FROM_ONLINE_COMMAND);
            }
        }
        return !fromOnlineList.isEmpty();
    }

    /**
     * Returns true if user requests to search from internal.
     * @return True if user requests to search from internal.
     */
    private boolean requestedSearchFromInternal() throws CommandException {
        boolean requestedFromOnline = requestedFromOnline();
        for (String input : fromOnlineList) {
            if (requestedFromOnline && (input.equals(INPUT_FALSE) || input.equals(INPUT_NO))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if user requests to search from online.
     * @return True if user requests to search from online.
     */
    private boolean requestedSearchFromOnline() throws CommandException {
        boolean requestedFromOnline = requestedFromOnline();
        for (String input : fromOnlineList) {
            if (requestedFromOnline && (input.equals(INPUT_TRUE) || input.equals(INPUT_YES))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the list of search results.
     * @return List of search results.
     */
    public List<Show> getSearchResult() {
        return searchResult;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof SearchCommand // instanceof handles nulls
                && nameList.equals(((SearchCommand) other).nameList)
                && typeList.equals(((SearchCommand) other).typeList)
                && actorList.equals(((SearchCommand) other).actorList)
                && isWatchedList.equals(((SearchCommand) other).isWatchedList)
                && fromOnlineList.equals(((SearchCommand) other).fromOnlineList));
    }
}

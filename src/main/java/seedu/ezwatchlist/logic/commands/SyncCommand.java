package seedu.ezwatchlist.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.ezwatchlist.commons.core.index.Index;
import seedu.ezwatchlist.commons.core.messages.Messages;
import seedu.ezwatchlist.logic.commands.exceptions.CommandException;
import seedu.ezwatchlist.model.Model;
import seedu.ezwatchlist.model.show.Name;
import seedu.ezwatchlist.model.show.Show;

/**
 * Syncs a result from the search list into the watch list.
 */
public class SyncCommand extends Command {

    public static final String COMMAND_WORD = "sync";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sync a show from IMDB to the watchlist. "
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Sync movie: %1$s";
    public static final String MESSAGE_DUPLICATE_SHOW = "?"; //"This show already exists in the watchlist";

    public static final String MESSAGE_UNSUCCESSFUL = "No matching name found in local internal storage.";
    public static final String MESSAGE_UNSUCCESFUL2 = "You may use the add INDEX command to add searched-online shows"
            + " into Watchlist.";
    private Index toSync;

    public SyncCommand(Index toSync) {
        requireNonNull(toSync);
        this.toSync = toSync;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Show> searchResultList = model.getSearchResultList();
        List<Show> unWatchedList = model.getUnWatchedShowList();
        if (toSync.getZeroBased() >= searchResultList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_SHOW_DISPLAYED_INDEX);
        }
        Show fromImdb = searchResultList.get(toSync.getZeroBased());
        Name name = fromImdb.getName();
        boolean matchingShowName = false;
        int matchingIndex = -1;
        for (int i = 0; i < unWatchedList.size(); i++) {
            Name nameFromUnWatched = unWatchedList.get(i).getName();
            if (name.equals(nameFromUnWatched)) {
                matchingShowName = true;
                matchingIndex = i;
                break;
            }
        }
        if (matchingShowName) {
            Show fromUnWatchedList = unWatchedList.get(matchingIndex);
            model.setShow(fromUnWatchedList, fromImdb);
            return new CommandResult(String.format(MESSAGE_SUCCESS, fromImdb));
        } else {
            throw new CommandException(MESSAGE_UNSUCCESSFUL + " " + MESSAGE_UNSUCCESFUL2);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof SyncCommand // instanceof handles nulls
                && toSync.equals(((SyncCommand) other).toSync));
    }

}

package nl.juraji.imagemanager.tasks;

import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.Process;
import nl.juraji.imagemanager.util.io.pinterest.PinterestWebSession;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import javax.security.auth.login.CredentialException;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 28-8-2018.
 * Image Manager
 */
public class FindPinterestBoardsProcess extends Process<List<PinterestBoard>> {

    private final File targetDirectory;
    private final String[] pinterestLogin;

    public FindPinterestBoardsProcess() throws CredentialException {
        this.pinterestLogin = Preferences.Pinterest.getLogin();
        if (TextUtils.isEmpty(this.pinterestLogin)) {
            throw new CredentialException("Pinterest login is not set up");
        }

        this.targetDirectory = Preferences.Pinterest.getTargetDirectory();

        this.setTitle(resources.getString("tasks.findPinterestBoardsTask.title"));
    }

    @Override
    public List<PinterestBoard> call() throws Exception {
        try (PinterestWebSession webSession = new PinterestWebSession(pinterestLogin[0], pinterestLogin[1])) {
            final List<PinterestBoard> existingBoards = new Dao().getAllPinterestBoards();

            webSession.goToProfile();
            webSession.startAutoScroll(500);

            final WebElement boardsFeed = webSession.getElement(webSession.by("class.profileBoards.feed"));
            List<WebElement> boardWrappers;
            List<WebElement> boardWrappersTmp = new ArrayList<>();

            do {
                boardWrappers = boardWrappersTmp;
                Thread.sleep(1000);
                boardWrappersTmp = boardsFeed.findElements(webSession.by("xpath.profileBoards.feed.Items"));
            } while (boardWrappersTmp.size() > boardWrappers.size());

            webSession.stopAutoScroll();

            final int totalBoards = boardWrappers.size();
            setMaxProgress(totalBoards);

            return boardWrappers.stream()
                    .peek(e -> updateProgress())
                    .map(e -> this.mapElementToBoard(e, webSession))
                    .filter(Objects::nonNull)
                    .filter(board -> existingBoards.stream()
                            .noneMatch(exBoard -> exBoard.getBoardUrl().equals(board.getBoardUrl())))
                    .sorted(Comparator.comparing(PinterestBoard::getName))
                    .collect(Collectors.toList());
        }
    }

    private PinterestBoard mapElementToBoard(WebElement webElement, PinterestWebSession webSession) {
        try {
            String boardUri = webElement
                    .findElement(webSession.by("xpath.profileBoards.feed.items.boardLink"))
                    .getAttribute("href");
            String boardName = webElement
                    .findElement(webSession.by("xpath.profileBoards.feed.items.boardName"))
                    .getText();


            PinterestBoard board = new PinterestBoard();
            board.setName(boardName);
            board.setBoardUrl(URI.create(boardUri));
            board.setTargetLocation(new File(targetDirectory, TextUtils.getFileSystemSafeName(boardName)));

            return board;
        } catch (NoSuchElementException ignored) {
            // If mapping fails it's most probably not an actual board
        }

        return null;
    }
}

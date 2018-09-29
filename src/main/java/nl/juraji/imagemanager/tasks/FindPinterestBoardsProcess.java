package nl.juraji.imagemanager.tasks;

import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.Process;
import nl.juraji.imagemanager.util.io.pinterest.PinterestWebSession;

import javax.security.auth.login.CredentialException;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
            final ArrayList<Map<String, Object>> pinterestBoardsResource = webSession.getPinterestBoardsResource();

            return pinterestBoardsResource.stream()
                    .map(this::mapResourceItemToPinterestBoard)
                    .filter(board -> existingBoards.stream()
                            .noneMatch(exBoard -> exBoard.getBoardUrl().equals(board.getBoardUrl())))
                    .sorted(Comparator.comparing(PinterestBoard::getName))
                    .collect(Collectors.toList());
        }
    }

    private PinterestBoard mapResourceItemToPinterestBoard(Map<String, Object> boardResourceItem) {
        final PinterestBoard board = new PinterestBoard();

        final String name = (String) boardResourceItem.get("name");
        final String boardId = (String) boardResourceItem.get("id");
        final String url = "https://pinterest.com" + boardResourceItem.get("url");

        board.setName(name);
        board.setBoardId(boardId);
        board.setBoardUrl(URI.create(url));
        board.setTargetLocation(new File(targetDirectory, TextUtils.getFileSystemSafeName(name)));

        return board;
    }
}

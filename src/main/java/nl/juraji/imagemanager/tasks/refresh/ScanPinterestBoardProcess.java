package nl.juraji.imagemanager.tasks.refresh;

import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.pinterest.PinMetaData;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.Process;
import nl.juraji.imagemanager.util.io.pinterest.PinterestWebSession;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public class ScanPinterestBoardProcess extends Process<Void> {
    private static final int PIN_FETCH_COUNT_OFFSET = 100;
    private final PinterestBoard board;
    private final String[] pinterestLogin;
    private final Dao dao;

    public ScanPinterestBoardProcess(PinterestBoard board) {
        this.pinterestLogin = Preferences.Pinterest.getLogin();
        if (TextUtils.isEmpty(this.pinterestLogin)) {
            throw new IllegalArgumentException("Pinterest login is not set up");
        }

        this.board = board;
        this.dao = new Dao();

        this.setTitle(TextUtils.format(resources, "tasks.scanPinterestDirectoryTask.title", board.getName()));
    }

    @Override
    public Void call() throws Exception {
        try (PinterestWebSession webSession = new PinterestWebSession(pinterestLogin[0], pinterestLogin[1])) {
            webSession.navigate(board.getBoardUrl().toString());
            webSession.executeScript("/nl/juraji/imagemanager/util/io/pinterest/js/disable-rendering-grid-items.js");

            final List<PinMetaData> existingPins = board.getImageMetaData().stream()
                    .map(p -> (PinMetaData) p)
                    .collect(Collectors.toList());

            final int reportedPinCount = getReportedPinCount(webSession);
            final int pinsToFetchCount = reportedPinCount - existingPins.size() + PIN_FETCH_COUNT_OFFSET;

            if (reportedPinCount == existingPins.size()) {
                return null;
            }

            setMaxProgress(pinsToFetchCount);
            final List<PinMetaData> fetchedPins = new ArrayList<>();
            String bookmarkTemp = null;

            do {
                final Map<String, Object> boardItemsResource = webSession.getPinterestBoardItemsResource(board.getBoardId(), bookmarkTemp);

                bookmarkTemp = (String) boardItemsResource.get("bookmark");

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) boardItemsResource.get("pins");

                items.stream()
                        .map(this::mapPinResourceToPinMetaData)
                        .filter(Objects::nonNull)
                        .forEach(fetchedPins::add);

                updateProgress(fetchedPins.size(), pinsToFetchCount);
            } while (fetchedPins.size() < pinsToFetchCount && !bookmarkTemp.equals("-end-"));


            resetProgress();
            final List<PinMetaData> pinsToPersist = fetchedPins.stream()
                    .filter(pin -> existingPins.stream().noneMatch(pin1 -> pin.getPinId().equals(pin1.getPinId())))
                    .collect(Collectors.toList());

            dao.save(pinsToPersist);
            board.getImageMetaData().addAll(pinsToPersist);
        }

        return null;
    }

    private PinMetaData mapPinResourceToPinMetaData(Map<String, Object> pinResource) {
        try {
            if (pinResource.get("type").equals("pin")) {
                PinMetaData pin = new PinMetaData();
                pin.setDirectory(board);

                String pinId = (String) pinResource.get("id");
                pin.setPinId(pinId);
                pin.setPinterestUri(new URL(board.getBoardUrl().toURL(), "/pin/" + pinId).toURI());

                //noinspection unchecked
                final Map<String, Map<String, String>> images = (Map<String, Map<String, String>>) pinResource.get("images");
                pin.setDownloadUrl(URI.create(images.get("orig").get("url")));

                pin.setDescription(((String) pinResource.get("description")).trim());

                // File is not nullable in model, so we set the file to be the
                // pin id in the target location, this should not exist, but satisfy model
                pin.setFile(new File(board.getTargetLocation(), pinId));
                pin.setDateAdded(LocalDateTime.now());
                pin.getTags().add("Pinterest");
                pin.getTags().add(board.getName());

                return pin;
            }

        } catch (MalformedURLException | URISyntaxException ignored) {
            // If mapping fails it's most probably not a valid pin
        }

        return null;
    }

    private int getReportedPinCount(PinterestWebSession webSession) {
        WebElement pinCountElement = webSession.getElement(webSession.by("xpath.boardPins.pinCount"));
        if (pinCountElement != null) {
            String count = pinCountElement.getText()
                    .replace(" pins", "")
                    .replace(".", "");

            return Integer.parseInt(count);
        }

        return 0;
    }
}

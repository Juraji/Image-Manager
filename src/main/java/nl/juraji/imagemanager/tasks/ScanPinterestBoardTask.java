package nl.juraji.imagemanager.tasks;

import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.pinterest.PinMetaData;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.QueueTask;
import nl.juraji.imagemanager.util.io.pinterest.PinterestWebSession;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public class ScanPinterestBoardTask extends QueueTask<Void> {
    private static final int MAX_FETCH_RETRY = 10;
    private static final int SCROLL_WAIT = 500;

    private final PinterestBoard board;
    private final String[] pinterestLogin;
    private final Dao dao;
    private final Logger logger;

    public ScanPinterestBoardTask(PinterestBoard board) {
        this.pinterestLogin = Preferences.getPinterestLogin();
        if (TextUtils.isEmpty(this.pinterestLogin)) {
            throw new IllegalArgumentException("Pinterest login is not set up");
        }

        this.board = board;
        this.dao = new Dao();
        this.logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public String getTaskTitle(ResourceBundle resources) {
        return TextUtils.format(resources, "tasks.scanPinterestDirectoryTask.title", board.getName());
    }

    @Override
    public Void call() throws Exception {
        try (PinterestWebSession webSession = new PinterestWebSession(pinterestLogin[0], pinterestLogin[1])) {
            webSession.login();

            webSession.navigate(board.getBoardUrl().toString());
            webSession.executeScript("/nl/juraji/imagemanager/util/io/pinterest/js/disable-rendering-grid-items.js");

            final List<PinMetaData> existingPins = board.getImageMetaData().stream()
                    .map(p -> (PinMetaData) p)
                    .collect(Collectors.toList());

            final int reportedPinCount = getReportedPinCount(webSession);
            final int pinsToFetchCount = reportedPinCount - existingPins.size();

            if (reportedPinCount == existingPins.size()) {
                return null;
            }

            AtomicInteger previousElCount = new AtomicInteger(0);
            AtomicInteger currentCount = new AtomicInteger(0);
            AtomicInteger retryCounter = new AtomicInteger(1);

            do {
                // Scroll to end and wait for a bit
                webSession.scrollDown(retryCounter.get() * SCROLL_WAIT);

                // Fetch all pin wrapper elements
                final int count = webSession.countElements(webSession.getData("xpath.boardPins.pins.feed"));
                currentCount.set(count);
                updateProgress(count, pinsToFetchCount);

                if (previousElCount.get() == count) {
                    // If the previous element count equals the current,
                    // increment the retry counter and try again.
                    // on MAX_FETCH_RETRY try break the loop and continue
                    if (retryCounter.addAndGet(1) == MAX_FETCH_RETRY) {
                        logger.log(Level.WARNING, "Too many retries for fetching pins, board: " + board.getName()
                                + ", reported count: " + reportedPinCount + ", found: " + count);
                        break;
                    }
                } else {
                    retryCounter.set(1);
                    previousElCount.set(count);
                }
            } while (currentCount.get() < pinsToFetchCount);

            final List<WebElement> elements = webSession.getElements(webSession.by("xpath.boardPins.pins.feed"));

            restartProgress();
            final int elementCount = elements.size();

            final List<PinMetaData> result = elements.stream()
                    .peek(e -> incrementProgress(elementCount))
                    .map(e -> this.mapElementToPin(e, webSession))
                    .filter(Objects::nonNull)
                    .filter(pin -> existingPins.stream().noneMatch(pin1 -> pin.getPinId().equals(pin1.getPinId())))
                    .collect(Collectors.toList());


            dao.save(result);
            board.getImageMetaData().addAll(result);
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

    private PinMetaData mapElementToPin(WebElement webElement, PinterestWebSession webSession) {
        try {
            final PinMetaData pin = new PinMetaData();
            pin.setDirectory(board);

            final String pinUrl = webElement
                    .findElement(webSession.by("xpath.boardPins.pins.feed.pinLink"))
                    .getAttribute("href");

            pin.setPinId(pinUrl.replaceAll("^.*/pin/(.+)/$", "$1"));
            pin.setPinterestUri(URI.create(pinUrl));

            final String[] pinImgSrcSet = webElement
                    .findElement(webSession.by("xpath.boardPins.pins.feed.pinImgLink"))
                    .getAttribute("srcset")
                    .split(", ");

            final Map<Integer, String> imgUrls = Arrays.stream(pinImgSrcSet)
                    .collect(Collectors.toMap(
                            s -> Integer.parseInt(s.substring(s.indexOf(" ") + 1, s.length() - 1)),
                            s -> s.substring(0, s.indexOf(" ")),
                            (s1, s2) -> s1,
                            HashMap::new));

            pin.setDownloadUrls(imgUrls);

            try {
                final String description = webElement
                        .findElement(webSession.by("xpath.boardPins.pins.feed.pinDescription"))
                        .getText();
                pin.setDescription(description.trim());
            } catch (org.openqa.selenium.NoSuchElementException ignored) {
            }

            // File is not nullable in model, so we set the file to be the
            // pin id in the target location, this should not exist, but satisfy model
            pin.setFile(new File(board.getTargetLocation(), pin.getPinId()));
            pin.setDateAdded(LocalDateTime.now());

            return pin;
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            // If mapping fails it's most probably not a valid pin
        }
        return null;
    }
}
package ru.darkkeks.pixel;

import ru.darkkeks.pixel.graphics.Template;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class Start {

    public static void main(String[] args) throws IOException, URISyntaxException {
        BufferedImage image = loadImage("template.png");

        List<String> urls = Files.lines(new File("urls.txt").toPath()).collect(Collectors.toList());
        ListIterator<String> iterator = urls.listIterator();
        Controller controller = new Controller(new LoginCredentials(iterator.next()), new Template(image));
        iterator.remove();
        iterator.forEachRemaining(url -> {
            if (url.startsWith("?")) controller.addAccount(new LoginCredentials(url));
        });
        
        // Watch dir for changes
        Path dir = Paths.get(".");
        
        DirectoryWatcher watcher = new DirectoryWatcher(dir) {
            @Override
            protected void onEvent(WatchEvent event) {
                try {
                    handleWatcherEvent(event, controller);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        
        new Thread(watcher).start();
    }
    
    static BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(new File(path));
    }
    
    static void handleWatcherEvent(WatchEvent event, Controller controller) throws IOException {
        if ( !event.kind().name().equals("ENTRY_DELETE") ) {
            if ( event.context().toString().equals("template.png") ) {
                System.out.println("New template is loading");
                controller.updateTemplate(new Template(loadImage("template.png")));
            }
        }
    }
}

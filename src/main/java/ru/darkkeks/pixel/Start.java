package ru.darkkeks.pixel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class Start {

    public static void main(String[] args) throws IOException {
        List<String> urls = Files.lines(new File("urls.txt").toPath()).collect(Collectors.toList());
        ListIterator<String> iterator = urls.listIterator();
        Controller controller = new Controller(new LoginCredentials(iterator.next()), new File("template.png"));
        iterator.remove();
        iterator.forEachRemaining(url -> {
            controller.addAccount(new LoginCredentials(url));
        });
    }
}

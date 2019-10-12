package ru.darkkeks.pixel;

import java.io.IOException;
import java.nio.file.*;


public abstract class DirectoryWatcher implements Runnable {
    
    private final Path dir;
    
    public DirectoryWatcher(Path dir) {
        this.dir = dir;
    }
    
    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
    
            Path path = Paths.get(".");
            path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            
            WatchKey key;
            
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    onEvent(event);
                }
                key.reset();
    
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    protected abstract void onEvent(WatchEvent event);
    
}

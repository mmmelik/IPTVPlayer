package com.appbroker.livetvplayer.model;

import android.webkit.MimeTypeMap;

import java.io.File;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomFile {
    private File file;
    private String name;
    private boolean isDirectory;
    private String extension;
    private String mime;
    private String size;
    private String lastModified;
    private boolean isParentPlaceHolder=false;
    public CustomFile(File file, boolean isParentPlaceHolder) {
        this.file = file;
        this.name = file.getName();
        this.isDirectory = file.isDirectory();
        this.mime = getMimeType(file.getAbsolutePath());
        this.size = humanReadableByteCountBin(file.length());
        this.lastModified = DateFormat.getDateInstance().format(new Date(file.lastModified()));
        this.isParentPlaceHolder=isParentPlaceHolder;
    }

    public static List<CustomFile> customFiles(List<File> files){
        if (files==null){
            return null;
        }
        ArrayList<CustomFile> customFiles=new ArrayList<>();
        for (File file:files){
            customFiles.add(new CustomFile(file,false));
        }
        return customFiles;
    }

    private String getMimeType(String url) {
        String type = null;
        String e = MimeTypeMap.getFileExtensionFromUrl(url);
        extension=e;
        if (e != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(e);
        }
        return type;
    }

    public boolean isParentPlaceHolder() {
        return isParentPlaceHolder;
    }

    public void setParentPlaceHolder(boolean parentPlaceHolder) {
        isParentPlaceHolder = parentPlaceHolder;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }
}

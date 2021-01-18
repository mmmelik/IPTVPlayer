package com.appbroker.livetvplayer;

import android.net.Uri;

import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.M3UParser;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void m3uParserTest() throws IOException {
        BufferedReader bufferedReader=new BufferedReader(new FileReader("C:\\Users\\melik\\AndroidStudioProjects\\IPTVPlayer\\app\\src\\test\\java\\com\\appbroker\\livetvplayer\\playlist\\1.m3u"));
        String line;
        StringBuilder stringBuilder=new StringBuilder();
        while ((line=bufferedReader.readLine())!=null){
            stringBuilder.append(line).append("\n");
        }

        M3UParser m3UParser=new M3UParser();
        assertEquals(5017,m3UParser.parseString(stringBuilder.toString()).size());
    }
}
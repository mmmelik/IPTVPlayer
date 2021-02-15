package com.appbroker.livetvplayer.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.model.Channel;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import com.appbroker.livetvplayer.listener.ParserListener;

public class M3UParser {
    private ParserListener parserListener;
    private Context context;

    public M3UParser(Context context,ParserListener parserListener) {
        this.context=context;
        this.parserListener=parserListener;
    }

    public M3UParser(Context context) {
        this.context=context;
    }

    public boolean isPlayList(Uri uri){
        try {
            if (Constants.MIME_TYPE_M3U.equals(context.getContentResolver().getType(uri))){
                return true;
            }
            InputStream inputStream=context.getContentResolver().openInputStream(uri);
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line=bufferedReader.readLine())!=null){
                if (line.contains("#EXTM3U"))return true;
            }
            return false;
        } catch (FileNotFoundException e){
            e.printStackTrace();
            if (parserListener!=null)parserListener.onError(e);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            if (parserListener!=null)parserListener.onError(e);
            return false;
        }
    }
    public void parseLocal(String s){
        Uri uri=Uri.parse(s);
        if (isPlayList(uri)){
            parserListener.onFinish(Enums.ParseResult.SUCCESS,parseFromLocalPlaylist(uri),"Parsing finished successfully");
        }else {
            ArrayList<Channel> channels=new ArrayList<>();
            channels.add(new Channel(Constants.CATEGORY_ID_TEMP,parseNameFromUri(uri),uri));
            parserListener.onFinish(Enums.ParseResult.SUCCESS,channels,"Parsing finished successfully");
        }
    }
    private String parseNameFromUri(Uri uri){
        try {
            File file=new File(uri.getPath());
            return file.getName();
        }catch (Exception e){
            return context.getResources().getString(R.string.unknown);
        }
    }
    private List<Channel> parseFromLocalPlaylist(Uri uri){
        try {
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(context.getContentResolver().openInputStream(uri)));
            StringBuilder lineBuilder=new StringBuilder();
            String line;
            while ((line=bufferedReader.readLine())!=null){
                lineBuilder.append(line).append("\n");
            }
            return parseString(lineBuilder.toString());
        } catch (FileNotFoundException e){
            e.printStackTrace();
            if (parserListener!=null)parserListener.onError(e);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            if (parserListener!=null)parserListener.onError(e);
            return null;
        }
    }
    public void parseFromURL(String url){
        new Thread() {
            @Override
            public void run() {
                try {
                    Connection connection=Jsoup.connect(url).timeout(10000).ignoreContentType(true);
                    Connection.Response response=connection.execute();
                    switch (response.statusCode()){
                        case 400:
                            parserListener.onFinish(Enums.ParseResult.SC_400,null,"400");
                            break;
                        case 401:
                            parserListener.onFinish(Enums.ParseResult.SC_401,null,"401");
                            break;
                        case 402:
                            parserListener.onFinish(Enums.ParseResult.SC_402,null,"402");
                            break;
                        case 403:
                            parserListener.onFinish(Enums.ParseResult.SC_403,null,"403");
                            break;
                        case 404:
                            parserListener.onFinish(Enums.ParseResult.SC_404,null,"404");
                            break;
                        case 500:
                            parserListener.onFinish(Enums.ParseResult.SC_500,null,"500");
                            break;
                        default:
                            Log.d("content",response.contentType());
                            if (response.contentType().startsWith("video/")||response.contentType().startsWith("audio/")||response.contentType().contains("vnd.apple.mpegURL")){
                                List<Channel> channels=new ArrayList<>();
                                channels.add(new Channel(Constants.CATEGORY_ID_TEMP,"",Uri.parse(url)));
                                parserListener.onFinish(Enums.ParseResult.REQUIRE_NAME,channels,response.statusMessage());
                                break;
                            }else if (response.contentType().startsWith("text/")||response.contentType().contains("mpegurl")||response.contentType().startsWith("application/octet-stream")){
                                String s=response.body();
                                if(!s.contains("#EXTM3U")){
                                    List<Channel> channels=new ArrayList<>();
                                    channels.add(new Channel(Constants.CATEGORY_ID_TEMP,"",Uri.parse(url)));
                                    parserListener.onFinish(Enums.ParseResult.SUCCESS,channels,response.statusMessage());
                                }else {
                                    try {
                                        List<Channel> channels=parseString(s);
                                        parserListener.onFinish(Enums.ParseResult.SUCCESS,channels,response.statusMessage());
                                    }catch (Exception e){
                                        List<Channel> channels=new ArrayList<>();
                                        channels.add(new Channel(Constants.CATEGORY_ID_TEMP,"",Uri.parse(url)));
                                        parserListener.onFinish(Enums.ParseResult.REQUIRE_NAME,channels,response.statusMessage());
                                    }
                                }
                                break;
                            }else {
                                String s=response.body();
                                if(!s.startsWith("#EXTM3U")){
                                    List<Channel> channels=new ArrayList<>();
                                    channels.add(new Channel(Constants.CATEGORY_ID_TEMP,"",Uri.parse(url)));
                                    parserListener.onFinish(Enums.ParseResult.SUCCESS,channels,response.statusMessage());
                                }else {
                                    try {
                                        List<Channel> channels=parseString(s);
                                        parserListener.onFinish(Enums.ParseResult.SUCCESS,channels,response.statusMessage());
                                    }catch (Exception e){
                                        List<Channel> channels=new ArrayList<>();
                                        channels.add(new Channel(Constants.CATEGORY_ID_TEMP,"",Uri.parse(url)));
                                        parserListener.onFinish(Enums.ParseResult.REQUIRE_NAME,channels,response.statusMessage());
                                    }
                                }
                                break;
                            }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    parserListener.onError(e);
                }
            }

        }.start();
    }

    public List<Channel> parseString(String s) throws IOException {
        List<Channel> channels=new ArrayList<>();
        BufferedReader bufferedReader=new BufferedReader(new StringReader(s));
        String line;
        boolean m3uFlag=false;
        boolean inChannelFlag=false;
        String channelName = null;
        String channelUri;
        while ((line=bufferedReader.readLine())!=null){
            if ("#EXTM3U".equals(line)){m3uFlag=true;}
            if (m3uFlag){
                if (line.startsWith("#EXTINF")){
                    channelName=line.split(",")[1];
                    inChannelFlag=true;
                }else if (inChannelFlag){
                    channelUri=line;
                    inChannelFlag=false;
                    channels.add(new Channel(Constants.CATEGORY_ID_TEMP,channelName, StringUtils.makeUri(channelUri)));
                }
            }
        }
        return channels;
    }

    public void generateM3UPlaylist(String name,List<Channel> channels,File targetDir,ParserListener parserListener){
        new Thread(){
            @Override
            public void run() {
                try {
                    File file=new File(targetDir,name+".m3u");
                    if (file.createNewFile()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("#EXTM3U\n");
                        for (Channel channel : channels) {
                            stringBuilder
                                    .append("#EXTINF:-1,")
                                    .append(channel.getName())
                                    .append("\n")
                                    .append(channel.getUri().getPath())
                                    .append("\n");
                        }
                        PrintStream printStream = new PrintStream(file);
                        printStream.print(stringBuilder.toString());
                        printStream.close();
                        parserListener.onCreateFile(file);
                    }else {
                        parserListener.onError(new Exception("Target file already exist."));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    parserListener.onError(e);
                }
            }
        }.start();
    }
}

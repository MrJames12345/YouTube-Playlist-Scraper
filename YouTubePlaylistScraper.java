import java.util.*;
import java.io.*;
 // Webscraping
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;


class YouTubePlaylistScraper
{

// - Variables - //

    // Data
    String playlistUrl;
    String playlistHtml;
    LinkedList<String> songsList;
    // Scraping
    private String textAtHrefLeft      = "\"commandMetadata\":{\"webCommandMetadata\":{\"url\":\"/watch?v=";
    private String textAtHrefRight     = "\\u0026list";
    private int    textAtHrefLeftLen   = 48;


// - Main Methods - //

    // Set the current playlist, retrieving it's html and loading it's list of href's
    public void setPlaylist( String inPlaylist, int inNumTries )
    {

        try
        {

            // Get page url and html
            playlistUrl = inPlaylist;
            playlistHtml = Jsoup.connect( playlistUrl ).get().html();
            // Cut down to video parts so less processing
            playlistHtml = cutOutMainData();
            // Load list of href's into songsList
            loadPlaylistSongs();

        }
        catch( IOException e )
        {

            // IF num of tries is > 0, try again, otherwise recurse back up
            if ( inNumTries > 0 )
            {
                setPlaylist( inPlaylist, inNumTries - 1 );
            }

        }

    }


    // Get data of the next song (title, artist, album)
    public HashMap<String, String> getNextSongData() throws IOException, NotYouTubeSongException
    {
        String videoUrl = null;
        HashMap<String, String> songData = new HashMap<String, String>();


        // Get and remove next song's href and use to create video's url
        videoUrl = "https://www.youtube.com" + songsList.removeFirst();
        // Use video scraper to get title, artist and album
        songData = YouTubeVideoScraper.getSongTags( videoUrl );

        return songData;

    }


    // Check if any more songs
    public boolean endOfPlaylist()
    {
        boolean check = false;

        if ( songsList.size() <= 0 )
        {
            check = true;
        }

        return check;
    }



// - Accessors - //


    // Get the page's html
    public String getHtml()
    {
        return playlistHtml;
    }


// - Private Methods - //


    // Load list of href's from playlistHtml into songsList
    private void loadPlaylistSongs()
    {

        songsList = new LinkedList<String>();
        int hrefLeft;
        int hrefRight;
        String href = null;
        int currentIndex = 0;

        // WHILE there is a next video, add it's href
        while ( playlistHtml.indexOf( textAtHrefLeft, currentIndex ) >= 0 )
        {

            hrefLeft = playlistHtml.indexOf( textAtHrefLeft, currentIndex ) + textAtHrefLeftLen;
            hrefRight = playlistHtml.indexOf ( textAtHrefRight, hrefLeft );
            href = playlistHtml.substring( hrefLeft, hrefRight );
            songsList.add( href );
            currentIndex = hrefRight;

        }

        // Remove last 2 href's, as for some reason the first video and one of the middle video's get added
        songsList.removeLast();
        songsList.removeLast();

    }


    // Get video href's from html
    private String cutOutMainData() throws IOException
    {
        String outMainData = null;

        // IF html does not include required text, error in retrieving html originally, so throw exception
        if ( playlistHtml.indexOf( textAtHrefLeft ) < 0 )
        {
            throw new IOException();
        }

        // ELSE get main data and return
        outMainData = playlistHtml.substring( playlistHtml.indexOf( textAtHrefLeft ), playlistHtml.lastIndexOf( textAtHrefLeft ) + 50 );

        return outMainData;
    }



}
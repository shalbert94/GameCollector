package com.example.android.gamecollector;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by shalom on 2017-10-04.
 * This class will parse .csv files into JSON files using JSONBuilder.java that are exported from the spreadsheats which contain data about
 * collectible items.
 * Takes into account the .csv file will have values with " and , inside
 */

public class ParseCSV {

    public static final String LOG_TAG = ParseCSV.class.getSimpleName();
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
    private static Context context;

    /**
     * @return A list of arrays containing an array for every line in each csv file
     */
    protected static ArrayList<List<String>> parseFiles() {
        ArrayList<List<String>> everyParsedLine = new ArrayList<>();
        ArrayList<InputStream> csvArray = new ArrayList<>();

        InputStream nesCsv = context.getResources().openRawResource(R.raw.nintendo_entertainment_system); //NES
        InputStream snesCsv = context.getResources().openRawResource(R.raw.super_nintendo_entertainment_system); //SNES
        InputStream n64Csv = context.getResources().openRawResource(R.raw.nintendo64); //N64
        InputStream gbCsv = context.getResources().openRawResource(R.raw.original_gameboy); //Original Gameboy
        InputStream gbcCsv = context.getResources().openRawResource(R.raw.gameboy_color); //Gameboy Color

        csvArray.add(nesCsv);
        csvArray.add(snesCsv);
        csvArray.add(n64Csv);
        csvArray.add(gbCsv);
        csvArray.add(gbcCsv);

        Scanner scanner = null;

        for(InputStream csv : csvArray) {
            scanner = new Scanner(csv);

            while (scanner.hasNext()) {
                List<String> line = parseLine(scanner.nextLine());
                Log.e(LOG_TAG, "console: " + line.get(0) + ", title: " + line.get(1)
                        + ", licensee: " + line.get(2) + ", release date: " + line.get(3));
                everyParsedLine.add(line);
            }
            scanner.close();
        }
        return everyParsedLine;
    }

    private static List<String> parseLine(String csvLine) {
        return parseLine(csvLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    private static List<String> parseLine(String csvLine, char separators) {
        return parseLine(csvLine, separators, DEFAULT_QUOTE);
    }

    private static List<String> parseLine(String csvLine, char separators, char customQuote) {

        List<String> result = new ArrayList<>();

        //if empty, return!
        if (csvLine == null && csvLine.isEmpty()) {
            return result;
        }

        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = csvLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }

}
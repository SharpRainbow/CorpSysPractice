package ru.mirea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MyScanner {

    private final BufferedReader br;

    public MyScanner() {
        br = new BufferedReader(new InputStreamReader(System.in));
    }

    public String nextLine() {
        try {
            return br.readLine().trim();
        } catch (IOException e) {
            return null;
        }
    }

    public boolean hasNextLine() {
        try {
            return br.ready();
        } catch (IOException e) {
            return false;
        }
    }

}

package ru.sooslick.seabattle.ai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiMain {
    public static void main(String[] args) {
        boolean create = true;
        int sessionId = 0;
        //parse args
        if (args.length > 0) {
            Matcher m = Pattern.compile("[-]?sessionId=(\\d+)").matcher(args[0]);
            if (m.matches()) {
                sessionId = Integer.parseInt(m.group(1));
                create = false;
            }
        }
        if (create)
            System.out.println("sessionId not specified, ai will create new game");
        else
            System.out.println("Selected session: " + sessionId);
    }
}

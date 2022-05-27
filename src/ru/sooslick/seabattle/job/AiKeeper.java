package ru.sooslick.seabattle.job;

import org.jetbrains.annotations.Nullable;
import ru.sooslick.seabattle.Log;
import ru.sooslick.seabattle.SeaBattleProperties;
import ru.sooslick.seabattle.ai.AiMain;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI threads keeper
 */
public class AiKeeper {
    private static int analyzeSkips = 0;

    private static final List<Thread> aInstances = new LinkedList<>();

    /**
     * Launch bot session via hidden api call
     * @param sessionId player's game session
     * @param pw password
     * @param skill preferred AI mode
     */
    public static void launchAi(int sessionId, @Nullable String pw, boolean skill) {
        List<String> argList = new LinkedList<>();
        argList.add("host=http://localhost:" + SeaBattleProperties.APP_SERVER_PORT);
        argList.add("sessionid=" + sessionId);
        if (pw != null)
            argList.add("sessionpw=" + pw);
        if (skill)
            argList.add("useheatmap");
        argList.add("heatdir=" + SeaBattleProperties.AI_DATA_DIR);
        String[] args = argList.toArray(new String[0]);
        aiThread(args);
    }

    /**
     * Launch AI for analyze collected data
     */
    public static void analyze() {
        if (analyzeSkips-- > 0)
            return;
        String[] args = new String[] {"-analyze", "heatdir=" + SeaBattleProperties.AI_DATA_DIR};
        aiThread(args);
        analyzeSkips = 86400 / SeaBattleProperties.APP_CLEANUP_INTERVAL;    // one day at least
    }

    /**
     * Kill and remove inactive threads
     */
    public static void cleanup() {
        List<Thread> finishedThreads = aInstances.stream()
                .filter(t -> !t.isAlive())
                .collect(Collectors.toList());
        aInstances.removeAll(finishedThreads);
        if (finishedThreads.size() > 0)
            Log.info(finishedThreads.size() + " AI instances removed");
    }

    /**
     * Kill all AIs
     */
    public static void kill() {
        aInstances.stream().filter(Thread::isAlive).forEach(Thread::interrupt);
    }

    private static void aiThread(String[] args) {
        Thread aiThread = new Thread(() -> new AiMain().run(args));
        aiThread.start();
        aInstances.add(aiThread);
    }

    private AiKeeper() {}
}

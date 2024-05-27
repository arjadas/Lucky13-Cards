public class LogData {

    private static StringBuilder logResult = new StringBuilder();









    public static void addRoundInfoToLog(int roundNumber) {
        logResult.append("Round" + roundNumber + ":");
    }

}

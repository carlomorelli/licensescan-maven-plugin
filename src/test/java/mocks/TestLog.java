package mocks;

import org.apache.maven.plugin.logging.SystemStreamLog;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

public class TestLog extends SystemStreamLog {
    private List<LogRow> logs = new ArrayList();

    @Override
    public void info(CharSequence content) {
        logs.add(new LogRow("info", String.valueOf(content)));
        super.info(content);
    }

    @Override
    public void warn(CharSequence content) {
        logs.add(new LogRow("warn", String.valueOf(content)));
        super.warn(content);
    }

    public void assertInfo(String info) {
        assertMessage("info", info);
    }

    public void assertWarning(String warn) {
        assertMessage("warn", warn);
    }

    public void assertNoWarning(String warn) {
        assertNoMessage("warn", warn);
    }

    private void assertNoMessage(String level, String mssg) {
        for(LogRow l : logs){
            if(l.level.equals(level) && l.mssg.equals(mssg)){
                fail("Unwanted log line level: " + level + " message: " + mssg);
            }
        }
    }

    private void assertMessage(String level, String mssg) {
        for(LogRow l : logs){
            if(l.level.equals(level) && l.mssg.contains(mssg)){
                return;
            }
        }
        fail("No log line level: " + level + " message: " + mssg);
    }

    private class LogRow {
        private final String level;
        private final String mssg;

        public LogRow(String level, String mssg){
            this.level = level;
            this.mssg = mssg;
        }
    }
}

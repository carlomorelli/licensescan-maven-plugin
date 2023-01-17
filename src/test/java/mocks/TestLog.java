package mocks;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.SystemStreamLog;

public class TestLog extends SystemStreamLog {
    private List<LogRow> logs = new ArrayList<>();

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
        for (LogRow l : logs) {
            if (l.level.equals(level) && l.mssg.equals(mssg)) {
                fail("Unwanted log line level: " + level + " message: " + mssg);
            }
        }
    }

    private void assertMessage(String level, String mssg) {
        for (LogRow l : logs) {
            if (l.level.equals(level) && l.mssg.contains(mssg)) {
                return;
            }
        }
        fail("No log line level: " + level + " message: " + mssg + "\n but found:" + getLevelMessages(level));
    }

    private String getLevelMessages(String level) {
        StringBuffer buffer = new StringBuffer();
        for (LogRow l : logs) {
            if (l.level.equals(level)) {
                buffer.append("\n").append("[").append(level).append("] ").append(l.mssg);
            }
        }
        return buffer.toString();
    }

    private class LogRow {
        private final String level;
        private final String mssg;

        public LogRow(String level, String mssg) {
            this.level = level;
            this.mssg = mssg;
        }
    }
}

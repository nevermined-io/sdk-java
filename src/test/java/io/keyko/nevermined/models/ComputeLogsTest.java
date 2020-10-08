package io.keyko.nevermined.models;

import com.fasterxml.jackson.core.type.TypeReference;
import io.keyko.nevermined.models.gateway.ComputeLogs;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ComputeLogsTest {

    @Test
    public void fromJsonTest() throws IOException {
        String logsJson = "[{\"content\":\"line1\",\"podName\":\"configurator\"},{\"content\":\"line2\",\"podName\":\"configurator\"}]";
        List<ComputeLogs> computeLogs = ComputeLogs.fromJSON(new TypeReference<>() {
        }, logsJson);

        assertEquals("configurator", computeLogs.get(0).podName);
        assertEquals("line1", computeLogs.get(0).content);
        assertEquals("configurator", computeLogs.get(1).podName);
        assertEquals("line2", computeLogs.get(1).content);
    }
}
